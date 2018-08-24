package com.xybean.transfermanager.monitor

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.text.TextUtils
import com.xybean.transfermanager.Logger
import java.util.concurrent.atomic.AtomicReference

/**
 * Author @xybean on 2018/8/10.
 */
internal class NetworkMonitor(private val taskId: Int, private val type: Int) {

    companion object {
        // unit: ms
        private const val MIN_TIME_INTERVAL = 1000
        private const val TAG = "SpeedMonitor"
        const val TYPE_UPSTREAM = -1
        const val TYPE_DOWNSTREAM = 1
        private val current = AtomicReference<NetworkInfo>()

        fun init(context: Context) {
            NetworkReceiver.init(context)
            current.set(getActiveNetworkInfo(context))
        }

        private fun getActiveNetworkInfo(context: Context): NetworkInfo? {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return cm.activeNetworkInfo
        }

        fun setNetworkType(context: Context) {
            val activeNetworkInfo = getActiveNetworkInfo(context)
            current.set(activeNetworkInfo)
        }

        fun getNetworkType(): NetworkInfo? {
            return current.get()
        }
    }

    // unit: kb/s
    private var speeds = CirclerQueue(10, -1)
    private var lastRefreshByteCount = 0L
    private var lastRefreshTime = 0L

    fun getNetInfo(): NetworkInfo? {
        return getNetworkType()
    }

    fun update(currentByte: Long) {
        // 第一次调用只做初始化工作
        if (lastRefreshByteCount == 0L) {
            lastRefreshByteCount = currentByte
            lastRefreshTime = System.currentTimeMillis()
            return
        }

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastRefreshTime > MIN_TIME_INTERVAL) {
            when (type) {
                TYPE_UPSTREAM -> {
                    val speed = Math.max(0, (currentByte - lastRefreshByteCount) / (currentTime - lastRefreshTime)).toInt()
                    Logger.d(TAG, "(task: id = $taskId): current speed of upload stream is ${speed}kb/s")
                    speeds.put(speed)
                }
                TYPE_DOWNSTREAM -> {
                    val speed = Math.max(0, (currentByte - lastRefreshByteCount) / (currentTime - lastRefreshTime)).toInt()
                    Logger.d(TAG, "(task: id = $taskId): current speed of download stream is ${speed}kb/s")
                    speeds.put(speed)
                }
                else -> {
                    throw IllegalArgumentException("SpeedMonitor only support TYPE_UPSTREAM and TYPE_DOWNSTREAM!")
                }
            }
            lastRefreshByteCount = currentByte
            lastRefreshTime = currentTime
        }
    }

    override fun toString(): String {
        val list = speeds.toList()
        val speedsString = StringBuilder("[")
        list.forEachIndexed { index, speed ->
            if (index != list.lastIndex) {
                speedsString.append("${speed}kb/s, ")
            } else {
                speedsString.append("${speed}kb/s")
            }
        }
        speedsString.append("]")

        val netInfo = getNetInfo()?.toString()

        return "{" +
                toJsonMap("TaskId", taskId.toString()) + ", " +
                toJsonMap("Speed", "type: $type, speed: $speedsString") + ", " +
                toJsonMap("NetInfo", if (TextUtils.isEmpty(netInfo)) {
                    ""
                } else {
                    netInfo!!
                }) +
                "}"
    }

    private fun toJsonMap(key: String, value: String): String {
        return "\"${key.replace("\"", "")}\" : \"${value.replace("\"", "")}\""
    }

}