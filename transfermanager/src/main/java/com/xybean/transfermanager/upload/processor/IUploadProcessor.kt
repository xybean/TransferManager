package com.xybean.transfermanager.upload.processor

import com.xybean.transfermanager.upload.provider.IFileProvider
import com.xybean.transfermanager.upload.task.IUploadTask

/**
 * Author @xybean on 2018/8/9.
 */
abstract class IUploadProcessor(protected val task: IUploadTask, protected val fileProvider: IFileProvider) {

    abstract fun addHeader(name: String, value: String)

    @Throws(Exception::class)
    abstract fun upload(url: String)

    @Throws(Exception::class)
    abstract fun getResponseCode(): Int

    @Throws(Exception::class)
    abstract fun getResponse(): String

    abstract fun cancel()

    abstract fun pause()

    abstract fun close()

    interface Factory {
        fun createUploadProcessor(task: IUploadTask, fileProvider: IFileProvider): IUploadProcessor
    }

}