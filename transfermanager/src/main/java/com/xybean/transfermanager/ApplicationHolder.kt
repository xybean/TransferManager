package com.xybean.transfermanager

import android.annotation.SuppressLint
import android.content.Context

/**
 * Author @xybean on 2018/7/18.
 */
@SuppressLint("StaticFieldLeak")
object ApplicationHolder {

    private var APP_CONTEXT: Context? = null

    fun holdContext(context: Context) {
        APP_CONTEXT = context
    }

    fun getAppContext(): Context {
        if (APP_CONTEXT == null) {
            throw IllegalStateException("You should call TransferHelper.holdContext() first!")
        }
        return APP_CONTEXT!!
    }

}