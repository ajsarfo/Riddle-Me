package com.sarftec.riddleme.dialogs

import android.app.Activity
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.sarftec.riddleme.GameListener
import com.sarftec.riddleme.R
import com.sarftec.riddleme.databinding.SettingsDialogBinding
import com.sarftec.riddleme.tools.SoundManager

class SettingsDialog(
    private val activity: Activity,
    private val onSound: (Boolean) -> Unit
) : AlertDialog(activity){

    private val binding: SettingsDialogBinding = SettingsDialogBinding.inflate(
        activity.layoutInflater,
        activity.findViewById(android.R.id.content),
        false
    )

    private var soundOn = false

    init {
        val displayRect = Rect()
        activity.window.decorView.getWindowVisibleDisplayFrame(displayRect)
        binding.root.apply {
            minimumWidth = (displayRect.width() * 1f).toInt()
            minimumHeight = (displayRect.height() * 1f).toInt()
        }
        binding.cancel.setOnClickListener {
            cancel()
        }
        binding.soundToggle.setOnClickListener {
            if(activity is GameListener) activity.playSound(SoundManager.Sound.TAP)
            soundOn = !soundOn
            setSoundColor(soundOn)
            onSound(soundOn)
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setView(binding.root)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCancelable(false)
    }

    private fun setSoundColor(isSoundOn: Boolean) {
        val color = if(isSoundOn) R.color.button_color else R.color.wrong_solution_color
        binding.soundToggle.text = if(isSoundOn) "On" else "Off"
        binding.soundToggle.setTextColor(
            ContextCompat.getColor(context, color)
        )
    }

    fun showDialog(isSoundOn: Boolean) {
        this.soundOn = isSoundOn
        setSoundColor(isSoundOn)
        show()
        binding.settingsCard.showRevealAnimation(activity)
    }
}