package com.xybean.transfermanager.monitor

/**
 * Author @xybean on 2018/8/13.
 */
interface MonitorListener {

    fun onFailed(e: Exception, netProfile: String)

}