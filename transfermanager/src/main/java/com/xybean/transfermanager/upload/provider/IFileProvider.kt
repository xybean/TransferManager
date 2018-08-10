package com.xybean.transfermanager.upload.provider

import com.xybean.transfermanager.upload.task.IUploadTask
import java.io.InputStream

/**
 * Author @xybean on 2018/8/9.
 */
abstract class IFileProvider(protected val task: IUploadTask) {

    abstract fun getInputStream(): InputStream

    abstract fun getLength(): Long

    abstract fun close()

    interface Factory {
        fun createFileProvider(task: IUploadTask): IFileProvider
    }

}