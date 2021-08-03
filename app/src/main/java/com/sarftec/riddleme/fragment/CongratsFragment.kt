package com.sarftec.riddleme.fragment

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.sarftec.riddleme.GameListener
import com.sarftec.riddleme.databinding.FragmentCongratsBinding
import com.sarftec.riddleme.tools.SoundManager
import com.sarftec.riddleme.tools.rateApp
import kotlinx.coroutines.delay

class CongratsFragment : Fragment() {

    private lateinit var gameListener: GameListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is GameListener) gameListener = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentCongratsBinding.inflate(
            inflater,
            container,
            false
        )
        binding.rateButton.setOnClickListener {
            requireContext().rateApp()
        }
        lifecycleScope.launchWhenCreated {
            delay(1000)
            gameListener.playSound(SoundManager.Sound.GAME_FINISHED)
            playAnimation(binding)
        }
        ObjectAnimator.ofFloat(binding.parent, "alpha", 0f, 1f).apply {
            interpolator = AccelerateInterpolator()
            duration = 1500
            startDelay = 500
            start()
        }
        return binding.root
    }

    private fun playAnimation(binding: FragmentCongratsBinding) {
        binding.gameFinished.apply {
            visibility = View.VISIBLE
            playAnimation()
        }
    }
}