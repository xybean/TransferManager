package com.xybean.transfermanager.upload.stream

import com.xybean.transfermanager.upload.task.IUploadTask
import java.io.IOException
import java.io.InputStream

/**
 * Author @xybean on 2018/7/24.
 */
abstract class IUploadStream(protected val task: IUploadTask) {

    @Throws(IOException::class)
    abstract fun getInputStream(): InputStream

    abstract fun length(): Long

    abstract fun close()

    interface Factory {
        fun createUploadStream(task: IUploadTask): IUploadStream
    }

}