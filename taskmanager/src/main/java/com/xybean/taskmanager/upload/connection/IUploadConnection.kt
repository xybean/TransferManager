package com.xybean.taskmanager.upload.connection

import com.xybean.taskmanager.upload.task.IUploadTask
import java.io.InputStream

/**
 * Author @xybean on 2018/7/24.
 */
abstract class IUploadConnection(internal val task: IUploadTask, protected val config: Configuration? = null) {

    abstract fun getInputStream(): InputStream

    abstract fun write(byteArray: ByteArray, off: Int, len: Int)

    abstract fun flush()

    abstract fun addHeader(name: String, value: String)

    @Throws(Exception::class)
    abstract fun request(url: String)

    abstract fun getResponseCode(): Int

    @Throws(Exception::class)
    abstract fun getResponse(): String

    abstract fun close()

    interface Factory {
        fun createUploadConnection(task: IUploadTask): IUploadConnection
    }

    interface Configuration {

        fun getReadTimeOut(): Int = -1

        fun getConnectTimeout(): Int = -1
    }

}