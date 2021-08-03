package com.sarftec.riddleme.advertisement

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import android.util.Range
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            registerNetworkCallbacks(context)
        }
    }

    private var connectivityStatus: Boolean = false

    private fun lowerVersionStatus(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val activeNetworkInfo = connectivityManager!!.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getNetworkCallback(): ConnectivityManager.NetworkCallback {
        return object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                connectivityStatus = true
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                connectivityStatus = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun registerNetworkCallbacks(context: Context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCallback = getNetworkCallback()
        if (Build.VERSION.SDK_INT in Range(21, 24)) {
            cm.registerNetworkCallback(
                NetworkRequest.Builder().build(),
                networkCallback
            )
        } else if (Build.VERSION.SDK_INT >= 24) {
            cm.registerDefaultNetworkCallback(networkCallback)
        }
    }

    fun isNetworkAvailable(): Boolean {
        if (Build.VERSION.SDK_INT < 21) return lowerVersionStatus(context)
        return connectivityStatus
    }
}