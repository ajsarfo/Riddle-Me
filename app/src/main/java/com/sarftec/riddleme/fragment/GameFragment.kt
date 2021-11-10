package com.sarftec.riddleme.fragment

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.AdRequest
import com.sarftec.riddleme.GameListener
import com.sarftec.riddleme.R
import com.sarftec.riddleme.advertisement.BannerManager
import com.sarftec.riddleme.advertisement.NetworkManager
import com.sarftec.riddleme.advertisement.RewardVideoManager
import com.sarftec.riddleme.databinding.CharacterHolderBinding
import com.sarftec.riddleme.databinding.CharacterSelectionBinding
import com.sarftec.riddleme.databinding.FragmentGameBinding
import com.sarftec.riddleme.databinding.SpecialSelectionBinding
import com.sarftec.riddleme.dialogs.*
import com.sarftec.riddleme.tools.SoundManager
import com.sarftec.riddleme.tools.toast
import com.sarftec.riddleme.viewmodel.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class GameFragment : Fragment() {

    private val viewModel by viewModels<GameViewModel>()

    private val appViewModel by activityViewModels<AppViewModel>()

    private val regularLetterHolders: MutableList<RegularLetterHolder> = mutableListOf()

    private var specialLetterHolder: SpecialLetterHolder? = null

    private val characterHolder: MutableList<CharacterHolder> = mutableListOf()

    private lateinit var gameBinding: FragmentGameBinding

    private var gameListener: GameListener? = null

    private var isCreated = false

    private lateinit var hintDialog: HintDialog

    private val skipLevelDialog by lazy {
        SkipLevelDialog(requireActivity()) {
            viewModel.showSkipRewardVideo()
        }
    }

    private val skipLevelErrorDialog by lazy {
        SkipLevelErrorDialog(requireActivity())
    }

    private val coinsDialog by lazy {
        CoinsDialog(requireActivity()) {
            loadingSpinnerDialog.show()
            coinsRewardVideoManager.showRewardVideo()
        }
    }

    private val loadingSpinnerDialog by lazy {
        LoadingSpinnerDialog(requireActivity())
    }

    private val networkManager by lazy {
        NetworkManager(requireContext())
    }

    private val skipRewardVideoManager by lazy {
        RewardVideoManager(
            requireActivity(),
            adRequest,
            networkManager,
            onSuccess = {
                loadingSpinnerDialog.dismiss()
                viewModel.onSkipLevelVideoShown()
            },
            onCancelled = {
                loadingSpinnerDialog.dismiss()
                requireContext().toast("Skip Reward Video Cancelled!")
            },
            onNetworkError = {
                loadingSpinnerDialog.dismiss()
                requireContext().toast("Network Error! Check Internet Connection")
            }
        )
    }

    private val coinsRewardVideoManager by lazy {
        RewardVideoManager(
            requireActivity(),
            adRequest,
            networkManager,
            onSuccess = {
                Log.v("TAG", "coin video success!")
                loadingSpinnerDialog.dismiss()
                gameListener?.playSound(SoundManager.Sound.RECEIVE_COINS)
                playCoinsAnimation()
                viewModel.onReceiveCoinsReward(60)
            },
            onCancelled = {
                loadingSpinnerDialog.dismiss()
                Log.v("TAG", "coin video  Cancelled!")
            },
            onNetworkError = {
                loadingSpinnerDialog.dismiss()
                requireContext().toast("Network Error! Check Internet Connection")
            }
        )
    }

    private val adRequest by lazy {
        AdRequest.Builder().build()
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is GameListener) gameListener = context
    }

    override fun onResume() {
        super.onResume()

        specialLetterHolder?.pulsate()
    }

    override fun onPause() {
        super.onPause()
        specialLetterHolder?.cancel()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        isCreated = true
        gameBinding = FragmentGameBinding.inflate(
            inflater,
            container,
            false
        )
        /*************** Admob Configuration ********************/
        BannerManager(requireActivity(), adRequest).attachBannerAd(
            getString(R.string.admob_banner_main),
            gameBinding.topAnchor.mainBanner
        )
        /**********************************************************/
        hintDialog = HintDialog(requireActivity()) {
            when (it) {
                0 -> viewModel.onRevealLetter()
                1 -> viewModel.onRemoveThreeLetters()
                2 -> viewModel.onRevealAnswer()
            }
        }
        configureRevealLayout(gameBinding)
        createCharacterGrid(gameBinding)
        configureOptionPalette()

        appViewModel.boardNotification.observe(viewLifecycleOwner) { notification ->
            when (notification) {
                is BoardNotification.ShowCoinDialog -> {
                    coinsDialog.show()
                    appViewModel.neutralBoardNotification()
                }
                is BoardNotification.IncreaseCoins -> {
                    gameListener?.playSound(SoundManager.Sound.RECEIVE_COINS)
                    playCoinsAnimation()
                   lifecycleScope.launch {
                       delay(1000)
                       viewModel.onReceiveCoinsReward(10)
                       appViewModel.neutralBoardNotification()
                   }
                }
                else -> {
                    //Do not handle other notifications from main activity
                }
            }
        }

        viewModel.panel.observe(viewLifecycleOwner) { containers ->
            containers.zip(regularLetterHolders).forEach { (container, holder) ->
                holder.bind(container)
            }
        }

        viewModel.screen.observe(viewLifecycleOwner) { screen ->
            if (isCreated) {
                createSolutionLayout(gameBinding, screen.boxes)
                isCreated = false
            }
            gameBinding.riddle.text = screen.question
            screen.boxes.forEach { box ->
                characterHolder.firstOrNull { holder -> holder.position == box.position }
                    ?.bind(box.content)
            }
        }

        viewModel.coins.observe(viewLifecycleOwner) { coins ->
            appViewModel.saveCoins(coins.amount)
        }

        viewModel.showWinMessage.observe(viewLifecycleOwner) { winMessage ->
            when (winMessage) {
                is WinMessage.ShowWinMessage -> {
                    appViewModel.showWinMessage(
                        winMessage.message,
                        winMessage.riddleAnswer
                    )
                    gameListener?.playSound(SoundManager.Sound.ANSWER_FOUND)
                }
                else -> appViewModel.dismissWinMessage()
            }
        }

        viewModel.advertisement.observe(viewLifecycleOwner) { advertisement ->
            when (advertisement) {
                is Advertisement.SkipLevelVideo -> {
                    loadingSpinnerDialog.show()
                    skipRewardVideoManager.showRewardVideo()
                    viewModel.onNeutralAdvertisement()
                }
                else -> {
                    //Neutral advertisement
                }
            }
        }

        viewModel.notification.observe(viewLifecycleOwner) {
            when (val notification = it) {
                is Notification.UpdateLevel -> {
                    appViewModel.updateLevel()
                    viewModel.onNeutralNotification()
                }

                is Notification.RewindAlpha -> {
                    gameBinding.options.rewind.alpha = if (notification.dim) 0.5f else 1f
                    viewModel.onNeutralNotification()
                }
                is Notification.RevealMode -> {
                    gameBinding.apply {
                        revealLayout.parent.visibility = View.VISIBLE
                        gridRowsHolder.visibility = View.GONE
                    }

                }
                is Notification.GameMode -> {
                    gameBinding.apply {
                        revealLayout.parent.visibility = View.GONE
                        gridRowsHolder.visibility = View.VISIBLE
                    }
                }
                is Notification.ShowHintDialog -> {
                    hintDialog.show()
                    viewModel.onNeutralNotification()
                }
                is Notification.ShowSkipLevelDialog -> {
                    skipLevelDialog.showDialog(notification.skipCount)
                    viewModel.onNeutralNotification()
                }
                is Notification.ShowSkipLevelErrorDialog -> {
                    skipLevelErrorDialog.show()
                    viewModel.onNeutralNotification()
                }
                is Notification.ShowInsufficientCoins -> {
                    requireContext().toast("Coins Insufficient!")
                    viewModel.onNeutralNotification()
                }
                is Notification.GameCompleted -> {
                    gameListener?.gameCompleted()
                    viewModel.onNeutralNotification()
                }
                else -> {
                    //Notification is neutral
                }
            }
        }
        return gameBinding.root
    }

    private fun playCoinsAnimation() {
        gameBinding.coinsAnimation.apply {
            this.addAnimatorListener(
                object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {

                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        visibility = View.GONE
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                    }

                    override fun onAnimationRepeat(animation: Animator?) {
                    }

                }
            )
            visibility = View.VISIBLE
            playAnimation()
        }
    }

    private fun configureOptionPalette() {
        gameBinding.options.apply {
            rewind.setOnClickListener {
                gameListener?.playSound(SoundManager.Sound.CANCEL)
                viewModel.rewind()
            }
            shuffle.setOnClickListener {
                gameListener?.playSound(SoundManager.Sound.TAP)
                viewModel.shuffle()
            }
            skipLevel.setOnClickListener {
                gameListener?.playSound(SoundManager.Sound.DIALOG_REVEAL)
                viewModel.onShowSkipLevelDialog()
            }
        }
    }

    private fun configureRevealLayout(binding: FragmentGameBinding) {
        binding.apply {
            revealLayout.revealCancel.setOnClickListener {
                gameListener?.playSound(SoundManager.Sound.CANCEL)
                viewModel.revealCancelled()
            }
            revealLayout.revealOk.setOnClickListener {
                gameListener?.playSound(SoundManager.Sound.TAP)
                viewModel.revealLetter()
            }
        }
    }

    private fun createSolutionLayout(binding: FragmentGameBinding, boxes: List<Box>) {
        characterHolder.clear()
        val layout = LinearLayout(requireContext())
        layout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layout.gravity = Gravity.CENTER
        layout.orientation = LinearLayout.HORIZONTAL
        for (i in boxes.indices) layout.addView(
            CharacterHolderBinding.inflate(
                layoutInflater,
                layout,
                false
            ).let {
                characterHolder.add(CharacterHolder(it, i))
                it.root
            }
        )
        binding.layoutContainer.let {
            it.removeAllViews()
            it.addView(layout)
        }
    }

    private fun createCharacterGrid(binding: FragmentGameBinding) {
        for (i in 0 until 7) {
            val child = if (i == 6) {
                SpecialSelectionBinding.inflate(
                    layoutInflater,
                    binding.firstRow,
                    false
                ).let {
                    specialLetterHolder = SpecialLetterHolder(it)
                    it.root
                }
            } else {
                CharacterSelectionBinding.inflate(
                    layoutInflater,
                    binding.firstRow,
                    false
                ).let {
                    regularLetterHolders.add(RegularLetterHolder(it))
                    it.root
                }
            }
            binding.firstRow.addView(child, i)
        }
        for (i in 0 until 7) {
            val child = CharacterSelectionBinding.inflate(
                LayoutInflater.from(requireContext()),
                binding.secondRow,
                false
            ).let {
                regularLetterHolders.add(RegularLetterHolder(it))
                it.root
            }
            binding.secondRow.addView(child, i)
        }
    }

    private inner class SpecialLetterHolder(private val binding: SpecialSelectionBinding) {

        private var pulsatingJob: Job? = null

        init {
            binding.inner.setOnClickListener {
                gameListener?.playSound(SoundManager.Sound.DIALOG_REVEAL)
                viewModel.onShowHintDialog()
            }
        }

        fun pulsate() {
            pulsatingJob = lifecycleScope.launch {
                while (true) {
                    delay(TimeUnit.SECONDS.toMillis(8))
                    AnimatorSet().apply {
                        playTogether(
                            ObjectAnimator.ofFloat(binding.inner, "scaleX", 0.8f, 1f),
                            ObjectAnimator.ofFloat(binding.inner, "scaleY", 0.8f, 1f)
                        )
                        duration = 180
                        interpolator = OvershootInterpolator()
                        start()
                    }
                }
            }
        }

        fun cancel() {
            pulsatingJob?.cancel()
        }
    }

    private inner class RegularLetterHolder(private val binding: CharacterSelectionBinding) {
        fun bind(letter: Letter) {
            binding.text.text = letter.character.toString()
            binding.inner.visibility = if (letter.visible) View.VISIBLE else View.GONE
            binding.inner.setOnClickListener {
                if(!viewModel.isCharacterBoxesFilled()) {
                    gameListener?.playSound(SoundManager.Sound.TAP)
                }
                viewModel.selected(letter)
            }
        }
    }

    private inner class CharacterHolder(
        private val binding: CharacterHolderBinding,
        val position: Int
    ) {

        init {
            binding.holder.setOnClickListener {
                viewModel.onBoxPositionClicked(position)
            }
        }

        fun showMarked(show: Boolean) {
            if (show) binding.apply {
                binding.apply {
                    arrowDown.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(), R.drawable.ic_top_arrow
                        )
                    )
                    arrowUp.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(), R.drawable.ic_bottom_arrow
                        )
                    )
                    underline.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.arrow_color)
                    )
                }
            }
            else binding.apply {
                arrowDown.setImageDrawable(null)
                arrowUp.setImageDrawable(null)
                underline.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.white)
                )
            }
        }

        fun bind(content: BoxContent) {
            when (content) {
                is BoxContent.Empty -> {
                    showMarked(false)
                    binding.apply {
                        text.visibility = View.GONE
                        holder.background = ContextCompat.getDrawable(
                            requireContext(), R.drawable.character_holder_shape
                        )
                        underline.visibility = View.VISIBLE
                    }
                }
                is BoxContent.Text -> {
                    showMarked(false)
                    binding.apply {
                        holder.background = ContextCompat.getDrawable(
                            requireContext(), R.drawable.character_holder_shape
                        )
                        underline.visibility = View.GONE
                        text.visibility = View.VISIBLE
                        text.text = content.letter.character.toString()
                        val color = when (content.color) {
                            LetterColor.DANGER -> R.color.wrong_solution_color
                            LetterColor.FOUND -> R.color.solution_found_color
                            else -> R.color.white
                        }
                        text.setTextColor(ContextCompat.getColor(requireContext(), color))
                    }
                }
                is BoxContent.Marked -> {
                    showMarked(true)
                    //Show something when holder is marked!!!!!
                }
                is BoxContent.Revealed -> {
                    showMarked(false)
                    binding.apply {
                        holder.background = null
                        underline.visibility = View.GONE
                        text.visibility = View.VISIBLE
                        text.text = content.letter.character.toString()
                        val color =
                            if (content.color == LetterColor.FOUND) R.color.solution_found_color
                            else R.color.white
                        text.setTextColor(ContextCompat.getColor(requireContext(), color))
                    }
                }
            }
        }
    }
}