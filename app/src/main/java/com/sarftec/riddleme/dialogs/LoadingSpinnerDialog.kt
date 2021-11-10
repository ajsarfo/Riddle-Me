package com.sarftec.riddleme.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.Window
import com.sarftec.riddleme.databinding.LoadingSpinnerDialogBinding

class LoadingSpinnerDialog(
    private val activity: Activity
) : AlertDialog(activity) {

    init {
        val binding = LoadingSpinnerDialogBinding.inflate(
            activity.layoutInflater,
            activity.findViewById(android.R.id.content),
            false
        )

        val displayRect = Rect()
        activity.window.decorView.getWindowVisibleDisplayFrame(displayRect)
        binding.root.apply {
            minimumWidth = (displayRect.width() * 1f).toInt()
            minimumHeight = (displayRect.height() * 1f).toInt()
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setView(binding.root)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding.loadingSpinner.playAnimation()
        setCancelable(false)
    }
}