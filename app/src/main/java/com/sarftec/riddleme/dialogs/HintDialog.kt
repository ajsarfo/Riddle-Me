package com.sarftec.riddleme.dialogs

import android.app.Activity
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.Window
import androidx.appcompat.app.AlertDialog
import com.sarftec.riddleme.GameListener
import com.sarftec.riddleme.databinding.HintDialogBinding
import com.sarftec.riddleme.tools.SoundManager

class HintDialog(
   private val activity: Activity,
    onSelect: (Int) -> Unit
) : AlertDialog(activity.layoutInflater.context) {

    private val binding: HintDialogBinding = HintDialogBinding.inflate(
        activity.layoutInflater,
        activity.findViewById(android.R.id.content),
        false
    )

    private val gameListener: GameListener? = if(activity is GameListener) activity else null

    init {
        val displayRect = Rect()
        activity.window.decorView.getWindowVisibleDisplayFrame(displayRect)
        binding.root.apply {
            minimumWidth = (displayRect.width() * 1f).toInt()
            minimumHeight = (displayRect.height() * 1f).toInt()
        }
        binding.apply {
            cancel.setOnClickListener {
                cancel()
            }
            revealLetter.setOnClickListener {
                gameListener?.playSound(SoundManager.Sound.TAP)
                cancel()
                onSelect(0)
            }
            removeLetters.setOnClickListener {
                gameListener?.playSound(SoundManager.Sound.TAP)
                cancel()
                onSelect(1)
            }
            revealAnswer.setOnClickListener {
                gameListener?.playSound(SoundManager.Sound.TAP)
                cancel()
                onSelect(2)
            }
            activity.assets.open("images/coin_image.png").use {
                image = BitmapFactory.decodeStream(it)
            }
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setView(binding.root)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCancelable(false)
    }

    override fun show() {
        super.show()
        binding.hintCard.showRevealAnimation(activity)
    }
}