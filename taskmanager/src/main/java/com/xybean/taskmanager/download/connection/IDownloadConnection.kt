package com.xybean.taskmanager.download.connection

import java.io.IOException
import java.io.InputStream

/**
 * Author @xybean on 2018/7/11.
 */
interface IDownloadConnection {

    @Throws(IOException::class)
    fun getInputStream(): InputStream

    fun getContentLength(): Long

    fun getRequestHeaderFields(): Map<String, String>

    fun getResponseHeaderFields(): Map<String, String>

    fun addHeader(name: String, value: String)

    @Throws(Exception::class)
    fun request(url: String)

    fun close()

    interface Factory {
        @Throws(IOException::class)
        fun createConnection(): IDownloadConnection
    }

    interface Configuration {

        fun getReadTimeOut(): Int = -1

        fun getConnectTimeout(): Int = -1
    }

}
