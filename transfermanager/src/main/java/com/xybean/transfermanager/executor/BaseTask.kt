package com.xybean.transfermanager.executor

/**
 * Author @xybean on 2018/7/19.
 */
abstract class BaseTask<R> : Runnable {

    final override fun run() {
        try {
            val result = execute()
            onSucceed(result)
        } catch (e: Exception) {
            onFailed(e)
        }
    }

    abstract fun execute(): R

    open fun onSucceed(r: R) {

    }

    open fun onFailed(e: Exception) {

    }

}