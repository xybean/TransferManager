package com.xybean.transfermanager.download.connection

import com.xybean.transfermanager.download.task.IDownloadTask
import java.io.IOException
import java.io.InputStream

/**
 * Author @xybean on 2018/7/11.
 */
abstract class IDownloadConnection(internal val task: IDownloadTask,
                                   protected val config: IDownloadConnection.Configuration? = null) {

    @Throws(IOException::class)
    abstract fun getInputStream(): InputStream

    abstract fun getContentLength(): Long

    abstract fun getRequestHeaderFields(): Map<String, String>

    abstract fun getResponseHeaderFields(): Map<String, String>

    abstract fun addHeader(name: String, value: String)

    @Throws(Exception::class)
    abstract fun request(url: String)

    abstract fun close()

    interface Factory {
        @Throws(IOException::class)
        fun createConnection(task: IDownloadTask): IDownloadConnection
    }

    interface Configuration {

        fun getReadTimeOut(): Int = -1

        fun getConnectTimeout(): Int = -1
    }

}
