package com.sarftec.riddleme.fragment

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.sarftec.riddleme.GameListener
import com.sarftec.riddleme.databinding.FragmentMenuBinding
import com.sarftec.riddleme.dialogs.AboutDialog
import com.sarftec.riddleme.tools.SoundManager
import com.sarftec.riddleme.tools.rateApp
import com.sarftec.riddleme.viewmodel.MenuNotification
import com.sarftec.riddleme.viewmodel.MenuViwModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MenuFragment : Fragment() {

    private var gameListener: GameListener? = null

    private val viewModel by viewModels<MenuViwModel>()

    private val aboutDialog by lazy {
        AboutDialog(requireActivity())
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is GameListener) gameListener = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentMenuBinding.inflate(
            inflater,
            container,
            false
        )
        requireContext().assets.open("images/riddle_icon.jpg").use {
            binding.titleInclude.riddleView.setImageBitmap(
                BitmapFactory.decodeStream(it)
            )
        }

        binding.apply {
            play.setOnClickListener {
                gameListener?.playSound(SoundManager.Sound.TAP)
                gameListener?.play()
            }
            rate.setOnClickListener {
                gameListener?.playSound(SoundManager.Sound.TAP)
                gameListener?.rate()
            }
            about.setOnClickListener {
                viewModel.showAboutDialog()
            }
        }
        playRevealAnimation(binding)

        viewModel.menuNotification.observe(viewLifecycleOwner) { notification ->
            when(notification) {
                is MenuNotification.ShowAboutDialog -> {
                    gameListener?.playSound(SoundManager.Sound.DIALOG_REVEAL)
                    aboutDialog.show()
                    viewModel.neutralMenuNotification()
                }
                else -> {
                    //Neutral notification
                }
            }
        }
        return binding.root
    }

    private fun playRevealAnimation(binding: FragmentMenuBinding) {
        fun buttonAnimate(button: CardView, offset: Long, animDuration: Long = 300, startScale: Float = 0f) {
            ObjectAnimator.ofFloat(button, "alpha", 0f, 1f).apply {
                duration = animDuration
                startDelay = offset
                interpolator = LinearInterpolator()
                start()
            }
            AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(button, "scaleX", startScale, 1f),
                    ObjectAnimator.ofFloat(button, "scaleY", startScale, 1f)
                )
                startDelay = offset
                interpolator = OvershootInterpolator()
                duration = animDuration + offset
                start()
            }
        }
        fun titleAnimate(title: View, offset: Long, animDuration: Long, startScale: Float = 0f ) {
            ObjectAnimator.ofFloat(title, "alpha", 0f, 1f).apply {
                duration = animDuration
                interpolator = LinearInterpolator()
               start()
            }
            AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(title, "scaleX", startScale, 1f),
                    ObjectAnimator.ofFloat(title, "scaleY", startScale, 1f)
                )
                startDelay = offset
                interpolator = OvershootInterpolator()
                duration = animDuration
                start()
            }
        }
        val offset = 200L
        val interval = 150L
        binding.apply {
            titleAnimate(titleInclude.parent, 0, 500)
            buttonAnimate(play, offset)
            buttonAnimate(rate, offset + interval)
            buttonAnimate(about, offset + 2 * interval)
        }
    }
}