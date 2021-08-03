package com.sarftec.riddleme.dialogs

import android.app.Activity
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.Window
import androidx.appcompat.app.AlertDialog
import com.sarftec.riddleme.GameListener
import com.sarftec.riddleme.databinding.CoinsDialogBinding
import com.sarftec.riddleme.tools.SoundManager

class CoinsDialog(
    private val activity: Activity,
    private val onOk: () -> Unit
) : AlertDialog(activity){
    private val binding = CoinsDialogBinding.inflate(
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
            cancel.setOnClickListener {
                if(activity is GameListener) activity.playSound(SoundManager.Sound.CANCEL)
                cancel()
            }
            ok.setOnClickListener {
                if(activity is GameListener) activity.playSound(SoundManager.Sound.TAP)
                cancel()
                onOk()
            }
            rotatingCoin.playAnimation()
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setView(binding.root)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCancelable(false)
    }

    override fun show() {
        super.show()
        binding.coinsCard.showRevealAnimation(activity)
    }
}