package com.xybean.transfermanager

import android.annotation.SuppressLint
import android.content.Context
import com.xybean.transfermanager.monitor.NetworkMonitor

/**
 * Author @xybean on 2018/7/18.
 */
@SuppressLint("StaticFieldLeak")
object TransferManager {

    private var APP_CONTEXT: Context? = null

    fun init(context: Context) {
        APP_CONTEXT = context
        NetworkMonitor.init(context)
    }

    fun getAppContext(): Context {
        if (APP_CONTEXT == null) {
            throw IllegalStateException("You should call TransferHelper.holdContext() first!")
        }
        return APP_CONTEXT!!
    }

}