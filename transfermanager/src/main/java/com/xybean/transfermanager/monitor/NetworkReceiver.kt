package com.xybean.transfermanager.monitor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager

/**
 * Author @xybean on 2018/8/13.
 */
internal class NetworkReceiver : BroadcastReceiver() {
    companion object {

        @Volatile
        private var init = false

        private const val TAG = "NetworkReceiver"

        @Synchronized
        internal fun init(context: Context) {
            if (init) {
                return
            }
            init = true

            val filter = IntentFilter()
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
            context.registerReceiver(NetworkReceiver(), filter)
        }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) {
            return
        }
        val action = intent.action
        if (ConnectivityManager.CONNECTIVITY_ACTION == action) {
            NetworkMonitor.setNetworkType(context)
        }
    }
}