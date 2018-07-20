package com.xybean.taskmanager.download

import android.util.Log

/**
 * Author @xybean on 2018/7/19.
 */
internal object LogUtils {
    private const val DEBUG = true

    fun d(tag: String, msg: String) {
        if (DEBUG) {
            Log.d(tag, msg)
        }
    }

    fun e(tag: String, msg: String) {
        if (DEBUG) {
            Log.e(tag, msg)
        }
    }

    fun i(tag: String, msg: String) {
        if (DEBUG) {
            Log.i(tag, msg)
        }
    }

}