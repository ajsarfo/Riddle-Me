package com.sarftec.riddleme.advertisement

import android.app.Activity
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.widget.LinearLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

class BannerManager(
    private val activity: Activity,
    private val adRequest: AdRequest = AdRequest.Builder().build()
) {

    /*
     fun loadBanner(admobView: AdView) {
         admobView.loadAd(adRequest)
         admobView.adSize = computeAdaptiveAdSize(admobView)
     }
     */

    fun attachBannerAd(bannerId: String, parent: LinearLayout) {
        val adView = AdView(activity)
        adView.adSize = computeAdaptiveAdSize(adView)
        adView.adUnitId = bannerId
        adView.loadAd(adRequest)
        parent.removeAllViews()
        parent.addView(adView, 0)
    }

    private fun computeAdaptiveAdSize(admobView: AdView) : AdSize {
        val dimension = Point()
        val density: Float

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.windowManager.currentWindowMetrics.bounds.let {
                dimension.x = it.width()
                dimension.y = it.height()
            }
            density = activity.resources.configuration.densityDpi.toFloat()
        }
        else {
            val display = activity.windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)
            dimension.x = outMetrics.widthPixels
            dimension.y = outMetrics.heightPixels
            density = outMetrics.density
        }

        var adWidthPixels = admobView.width.toFloat()
        if (adWidthPixels == 0f) {
            adWidthPixels = dimension.x.toFloat()
        }

        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }
}