package com.sarftec.riddleme.dialogs

import android.animation.ObjectAnimator
import android.app.Activity
import android.graphics.Point
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import androidx.cardview.widget.CardView
import com.sarftec.riddleme.tools.getScreenDimension

fun CardView.showRevealAnimation(activity: Activity) {

    val dimension = Point()
    activity.getScreenDimension(dimension)
    ObjectAnimator.ofFloat(
        this,
        "translationY",
        -dimension.y.toFloat(),
        0f
    ).apply {
        startDelay = 100
        interpolator = OvershootInterpolator()
        duration = 500
        start()
    }

    ObjectAnimator.ofFloat(
        this,
        "alpha",
        0f,
        1f
    ).apply {
        startDelay = 50
        interpolator = LinearInterpolator()
        duration = 150
        start()
    }
}