package com.xybean.taskmanager.download

import android.annotation.SuppressLint
import android.content.Context
import java.lang.IllegalArgumentException

/**
 * Author @xybean on 2018/7/18.
 */
@SuppressLint("StaticFieldLeak")
object TransferHelper {

    private var APP_CONTEXT: Context? = null

    fun holdContext(context: Context) {
        APP_CONTEXT = context
    }

    fun getAppContext(): Context {
        if (APP_CONTEXT == null) {
            throw IllegalArgumentException("You should call TransferHelper.holdContext() first!")
        }
        return APP_CONTEXT!!
    }

}