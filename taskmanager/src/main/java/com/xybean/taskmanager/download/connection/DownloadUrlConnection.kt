package com.xybean.taskmanager.download.connection

import android.os.Build
import com.xybean.taskmanager.download.task.IDownloadTask
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.net.URLConnection

/**
 * Author @xybean on 2018/7/18.
 */
class DownloadUrlConnection @Throws(IOException::class)
@JvmOverloads constructor(task: IDownloadTask, downloadConfig: IDownloadConnection.Configuration? = null)
    : IDownloadConnection(task, downloadConfig) {

    private var mConnection: URLConnection

    init {
        val url = URL(task.getUrl())
        mConnection = url.openConnection()
        if (config != null) {
            if (config.getReadTimeOut() > 0) {
                mConnection.readTimeout = config.getReadTimeOut()
            }

            if (config.getConnectTimeout() > 0) {
                mConnection.connectTimeout = config.getConnectTimeout()
            }
        }
    }

    @Throws(IOException::class)
    override fun getInputStream(): InputStream {
        return mConnection.getInputStream()
    }

    override fun getContentLength(): Long {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mConnection.contentLengthLong
        } else {
            mConnection.contentLength.toLong()
        }
    }

    override fun getRequestHeaderFields(): Map<String, String> {
        val result = mConnection.requestProperties
        val fields = HashMap<String, String>()
        result.forEach { name, values ->
            fields[name] = values.toString()
        }
        return fields
    }

    override fun getResponseHeaderFields(): Map<String, String> {
        val result = mConnection.headerFields
        val fields = HashMap<String, String>()
        result.forEach { key, values ->
            fields[key] = values.toString()
        }
        return fields
    }

    override fun addHeader(name: String, value: String) {
        mConnection.addRequestProperty(name, value)
    }

    override fun request(url: String) {
        mConnection.connect()
    }

    override fun close() {
        mConnection.getInputStream()?.close()
    }
}