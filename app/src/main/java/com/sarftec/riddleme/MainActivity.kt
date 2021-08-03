package com.sarftec.riddleme

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.BitmapFactory
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.view.animation.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.appodeal.ads.Appodeal
import com.sarftec.riddleme.databinding.ActivityMainBinding
import com.sarftec.riddleme.dialogs.SettingsDialog
import com.sarftec.riddleme.fragment.CongratsFragment
import com.sarftec.riddleme.fragment.GameFragment
import com.sarftec.riddleme.fragment.MenuFragment
import com.sarftec.riddleme.fragment.WinFragment
import com.sarftec.riddleme.repository.SettingsRepository
import com.sarftec.riddleme.tools.SoundManager
import com.sarftec.riddleme.tools.getScreenDimension
import com.sarftec.riddleme.tools.rateApp
import com.sarftec.riddleme.viewmodel.AppViewModel
import com.sarftec.riddleme.viewmodel.BoardNotification
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), GameListener {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var soundManager: SoundManager

    private val mainBinding by lazy {
        ActivityMainBinding.inflate(
            LayoutInflater.from(this)
        )
    }

    private lateinit var settingsDialog: SettingsDialog

    private val viewModel by viewModels<AppViewModel>()

    override fun onDestroy() {
        soundManager.destroy()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemBars()
        setContentView(mainBinding.root)
        /**************Set up appodeal configuration*****************/
       // Appodeal.setTesting(true)
        Appodeal.setBannerViewId(R.id.main_banner)
        Appodeal.initialize(
            this,
            getString(R.string.appodeal_id),
            Appodeal.BANNER_VIEW or Appodeal.REWARDED_VIDEO
        )
        Appodeal.cache(this, Appodeal.REWARDED_VIDEO)
        /*************************************************************/
        settingsDialog = SettingsDialog(this) { isSoundOn ->
            lifecycleScope.launch {
                settingsRepository.setSoundOn(isSoundOn)
            }
        }
        savedInstanceState ?: supportFragmentManager.commit {
            add(R.id.fragment_container, MenuFragment())
        }
        configureToolbar()
        viewModel.coins.observe(this) {
            mainBinding.toolbar.coinSection.coins.text = it.amount.toString()
        }
        viewModel.level.observe(this) {
            mainBinding.toolbar.level.text = "Level ${it.level}"
        }
        viewModel.boardNotification.observe(this) { winNotification ->
            when (winNotification) {
                is BoardNotification.ShowWinMessage -> {
                    if (!winNotification.handled) {
                        showWinMessage(winNotification.message)
                        winNotification.handled = true
                    }
                }
                is BoardNotification.DismissWinMessage -> {
                    dismissWinMessage()
                }
                else -> {
                    //Has been set to neutral
                }
            }
        }
    }

    private fun dismissWinMessage() {
        mainBinding.toolbar.apply {
            back.setImageDrawable(
                ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_back)
            )
            coinSection.parent.visibility = View.VISIBLE
            winMessage.visibility = View.GONE
        }
    }

    private fun showWinMessage(message: String) {
        mainBinding.toolbar.apply {
            winMessage.text = message
            back.setImageDrawable(null)
            coinSection.parent.visibility = View.GONE
            winMessage.visibility = View.VISIBLE //Perform some animation here!
            val dimension = Point()
            this@MainActivity.getScreenDimension(dimension)
            val scaleX = ObjectAnimator.ofFloat(
                winMessage,
                "scaleX",
                0.5f,
                1f
            )

            val scaleY = ObjectAnimator.ofFloat(
                winMessage,
                "scaleX",
                0.5f,
                1f
            )

            ObjectAnimator.ofFloat(
                winMessage,
                "alpha",
                0f,
                1f
            ).apply {
                interpolator = LinearInterpolator()
                duration = 500
                start()
            }
            AnimatorSet().apply {
                playTogether(
                    scaleX,
                    scaleY
                )
                startDelay = 100
                duration = 500
                interpolator = OvershootInterpolator()
                start()
            }

            lifecycleScope.launch {
                delay(400)
                supportFragmentManager.commit {
                    setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit)
                    replace(R.id.fragment_container, WinFragment())
                }
            }
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

    override fun onBackPressed() {
        switchToolbar(true)
        supportFragmentManager.fragments.firstOrNull()?.let {
            when (it) {
                is MenuFragment -> finish()
                is CongratsFragment -> finish()
                else -> supportFragmentManager.commit {
                    replace(R.id.fragment_container, MenuFragment())
                }
            }
        }
    }

    private fun configureToolbar() {
        mainBinding.toolbar.apply {
            back.setOnClickListener {
                playSound(SoundManager.Sound.CANCEL)
                onBackPressed()
            }
            menu.setOnClickListener {
                lifecycleScope.launch {
                    playSound(SoundManager.Sound.DIALOG_REVEAL)
                    settingsDialog.showDialog(settingsRepository.isSoundOn())
                }
            }
            coinSection.addCoins.setOnClickListener {
                if(supportFragmentManager.fragments.first() !is MenuFragment)
                {
                    playSound(SoundManager.Sound.DIALOG_REVEAL)
                    viewModel.showCoinDialog()
                }
            }
            coinSection.coins.setOnClickListener {
                if(supportFragmentManager.fragments.first() !is MenuFragment)
                viewModel.showCoinDialog()
            }
           // coinSection.image = "coin_image.png"
            assets.open("images/coin_image.png").use {
                coinSection.image = BitmapFactory.decodeStream(it)
            }
            coinSection.executePendingBindings()
        }
    }

    private fun switchToolbar(toMenu: Boolean) {
        mainBinding.toolbar.apply {
            if (toMenu) {
                levelLayout.visibility = View.GONE
                menu.visibility = View.VISIBLE
            } else {
                menu.visibility = View.GONE
                levelLayout.visibility = View.VISIBLE
            }
        }
    }

    override fun play() {
        switchToolbar(false)
        supportFragmentManager.commit {
            replace(R.id.fragment_container, GameFragment())
        }
    }

    override fun rate() {
        rateApp()
    }

    override fun nextRiddle() {
        dismissWinMessage()
        supportFragmentManager.commit {
            replace(R.id.fragment_container, GameFragment())
            viewModel.showIncreaseCoins()
        }
    }

    override fun playSound(sound: SoundManager.Sound) {
        lifecycleScope.launch {
            if(settingsRepository.isSoundOn()) soundManager.playSound(sound)
        }
    }

    override fun gameCompleted() {
        switchToolbar(true)
        supportFragmentManager.commit {
            replace(R.id.fragment_container, CongratsFragment())
        }
    }
}