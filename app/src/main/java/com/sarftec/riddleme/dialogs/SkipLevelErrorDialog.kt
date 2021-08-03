package com.sarftec.riddleme.dialogs

import android.app.Activity
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.Window
import androidx.appcompat.app.AlertDialog
import com.sarftec.riddleme.GameListener
import com.sarftec.riddleme.databinding.SkipLevelErrorDialogBinding
import com.sarftec.riddleme.tools.SoundManager

class SkipLevelErrorDialog(
    private val activity: Activity,
) : AlertDialog(activity.layoutInflater.context) {

    private val binding  = SkipLevelErrorDialogBinding.inflate(activity.layoutInflater)

    init {
        val displayRect = Rect()
        activity.window.decorView.getWindowVisibleDisplayFrame(displayRect)
        binding.apply {
            ok.setOnClickListener {
                if(activity is GameListener) activity.playSound(SoundManager.Sound.TAP)
                cancel()
            }
        }
        binding.root.apply {
            minimumWidth = (displayRect.width() * 1f).toInt()
            minimumHeight = (displayRect.height() * 1f).toInt()
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setView(binding.root)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCancelable(false)
    }

    override fun show() {
        super.show()
        binding.skipLevelErrorCard.showRevealAnimation(activity)
    }
}