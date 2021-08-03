package com.sarftec.riddleme.fragment

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.sarftec.riddleme.GameListener
import com.sarftec.riddleme.R
import com.sarftec.riddleme.databinding.FragmentWinBinding
import com.sarftec.riddleme.tools.SoundManager
import com.sarftec.riddleme.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WinFragment : Fragment() {

    private lateinit var binding: FragmentWinBinding

    private val appViewModel by activityViewModels<AppViewModel>()

    private var gameListener: GameListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is GameListener) gameListener = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWinBinding.inflate(
            inflater,
            container,
            false
        )
        binding.gameContinue.setOnClickListener {
            gameListener?.nextRiddle()
        }

        appViewModel.riddleAnswer.observe(viewLifecycleOwner) {
           lifecycleScope.launch {
               delay(resources.getInteger(R.integer.fragment_anim_duration).toLong())
               playCongratsAnimation()
               binding.riddleAnswer.text = it.answer
               binding.apply {
                   val animDuration = 500L
                   val alphaDuration = 500L
                   listOf(riddleAnswer, cleared, title).forEach { view ->
                       scaleIn(view, animDuration)
                       alphaIn(view, alphaDuration)
                   }
                   val delay = 500L
                   scaleIn(gameContinue, animDuration, delay)
                   alphaIn(gameContinue, alphaDuration, delay)
               }
               delay(200L)
               gameListener?.playSound(SoundManager.Sound.CONGRATULATIONS)
           }
        }
        return binding.root
    }

    private suspend fun playCongratsAnimation() {
        delay(500)
        binding.congratsAnimation.apply {
            visibility = View.VISIBLE
            playAnimation()
        }
    }

    private fun alphaIn(view: View, animDuration: Long, delay: Long = 0) {
        ObjectAnimator.ofFloat(
            view,
            "alpha",
            0f,
            1f
        ).apply {
            duration = animDuration
            startDelay = delay
            interpolator = LinearInterpolator()
            start()
        }
    }

    private fun scaleIn(view: View, animDuration: Long, delay: Long = 0) {
        binding.apply {
            val scaleX = ObjectAnimator.ofFloat(
                view,
                "scaleX",
                0f,
                1f
            )
            val scaleY = ObjectAnimator.ofFloat(
                view,
                "scaleY",
                0f,
                1f
            )

            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = animDuration
                startDelay = delay
                interpolator = OvershootInterpolator()
                start()
            }
        }
    }
}