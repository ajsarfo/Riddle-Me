package com.sarftec.riddleme.dialogs

import android.app.Activity
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.Window
import androidx.appcompat.app.AlertDialog
import com.sarftec.riddleme.GameListener
import com.sarftec.riddleme.databinding.SkipLevelDialogBinding
import com.sarftec.riddleme.tools.SoundManager

class SkipLevelDialog(
    private val activity: Activity,
    private val onWatch: () -> Unit
) : AlertDialog(activity) {

    private val binding: SkipLevelDialogBinding = SkipLevelDialogBinding.inflate(
        activity.layoutInflater,
        activity.findViewById(android.R.id.content),
        false
    )

    init {
        val displayRect = Rect()
        activity.window.decorView.getWindowVisibleDisplayFrame(displayRect)
        binding.root.apply {
            minimumWidth = (displayRect.width() * 1f).toInt()
            minimumHeight = (displayRect.height() * 1f).toInt()
        }
        binding.apply {
            dismiss.setOnClickListener {
                if(activity is GameListener) activity.playSound(SoundManager.Sound.CANCEL)
                cancel()
            }
            watch.setOnClickListener {
                if(activity is GameListener) activity.playSound(SoundManager.Sound.TAP)
                cancel()
                onWatch()
            }
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setView(binding.root)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCancelable(false)
    }

    fun showDialog(skipCount: Int) {
        binding.skipCount.text = skipCount.toString()
        show()
        binding.skipLevelCard.showRevealAnimation(activity)
    }
}