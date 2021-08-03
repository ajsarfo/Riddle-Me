package com.sarftec.riddleme

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sarftec.riddleme.database.RiddleDatabaseSetup
import com.sarftec.riddleme.databinding.ActivityLoadBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis

@AndroidEntryPoint
class LoadActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemBars()
        val binding = ActivityLoadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        assets.open("images/riddle_icon.jpg").use {
            binding.heading.riddleView.setImageBitmap(
                BitmapFactory.decodeStream(it)
            )
        }
        animateHeading(binding.heading.parent)
    }

    private fun animateHeading(view: View) {
        val animDuration = 1500
        val customInterpolator = Interpolator {
           sin(Math.PI * (it / sqrt(it))).toFloat()
        }
        ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
            duration = animDuration.toLong()
            interpolator = customInterpolator
            addListener(
                object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {

                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        val intent = Intent(this@LoadActivity , MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        overridePendingTransition(R.anim.no_anim, R.anim.no_anim)
                    }

                    override fun onAnimationCancel(animation: Animator?) {

                    }

                    override fun onAnimationRepeat(animation: Animator?) {

                    }
                }
            )
            start()
        }
    }

    private fun hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.systemBars())
        } else {
            var uiVisibility = window.decorView.systemUiVisibility
            uiVisibility = uiVisibility or View.SYSTEM_UI_FLAG_LOW_PROFILE
            uiVisibility = uiVisibility or View.SYSTEM_UI_FLAG_FULLSCREEN
            uiVisibility = uiVisibility or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                uiVisibility = uiVisibility or View.SYSTEM_UI_FLAG_IMMERSIVE
                uiVisibility = uiVisibility or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            }
            window.decorView.systemUiVisibility = uiVisibility
        }
    }
}