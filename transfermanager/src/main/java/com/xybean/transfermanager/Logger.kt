package com.xybean.transfermanager

import android.util.Log

/**
 * Author @xybean on 2018/7/19.
 */
internal object Logger {
    var DEBUG = false

    fun d(tag: String, msg: String) {
        if (DEBUG) {
            Log.d(tag, msg)
        }
    }

    fun w(tag: String, msg: String, tr: Throwable) {
        if (DEBUG) {
            Log.w(tag, msg, tr)
        }
    }

    fun e(tag: String, msg: String, tr: Throwable) {
        if (DEBUG) {
            Log.e(tag, msg, tr)
        }
    }

    fun i(tag: String, msg: String) {
        if (DEBUG) {
            Log.i(tag, msg)
        }
    }

}