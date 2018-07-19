package com.xybean.taskmanager.download

import android.annotation.SuppressLint
import android.content.Context

/**
 * Author @xybean on 2018/7/18.
 */
@SuppressLint("StaticFieldLeak")
object TransferHelper {

    private lateinit var APP_CONTEXT: Context

    fun holdContext(context: Context) {
        APP_CONTEXT = context
    }

    fun getAppContext(): Context {
        return APP_CONTEXT
    }

}