package com.sarftec.riddleme.advertisement

import android.app.Activity
import com.appodeal.ads.Appodeal
import com.appodeal.ads.RewardedVideoCallbacks

class RewardVideoManager(
    private val activity: Activity,
    private val networkManager: NetworkManager,
    private val onSuccess: () -> Unit,
    private val onCancelled: () -> Unit,
    private val onNetworkError: () -> Unit
) {

    private var isFinished = false

    init {
        configure()
    }

    fun showRewardVideo() {
        Appodeal.cache(activity, Appodeal.REWARDED_VIDEO)
        if (!networkManager.isNetworkAvailable()) {
            onNetworkError()
        } else {
            if (Appodeal.isLoaded(Appodeal.REWARDED_VIDEO)) Appodeal.show(
                activity,
                Appodeal.REWARDED_VIDEO
            )
            else onNetworkError()
        }
    }

    private fun configure() {
        Appodeal.setRewardedVideoCallbacks(
            object : RewardedVideoCallbacks {
                override fun onRewardedVideoLoaded(p0: Boolean) {
                }

                override fun onRewardedVideoFailedToLoad() {
                    onNetworkError()
                }

                override fun onRewardedVideoShown() {

                }

                override fun onRewardedVideoShowFailed() {
                    onNetworkError()
                }

                override fun onRewardedVideoFinished(p0: Double, p1: String?) {
                    isFinished = true
                }

                override fun onRewardedVideoClosed(p0: Boolean) {
                    if (!isFinished) return
                    isFinished = false
                    onSuccess()
                }

                override fun onRewardedVideoExpired() {

                }

                override fun onRewardedVideoClicked() {

                }
            }
        )
    }

    companion object {
        fun runAppodealConfiguration() {
            Appodeal.disableWriteExternalStoragePermissionCheck()
            Appodeal.disableLocationPermissionCheck()
        }
    }
}