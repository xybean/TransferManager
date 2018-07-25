package com.xybean.transfermanager.upload.task

import com.xybean.transfermanager.upload.UploadListener

/**
 * Author @xybean on 2018/7/24.
 */
interface IUploadTask {

    fun getUrl(): String

    fun getId(): Int

    fun getStatus(): Int

    fun getListener(): UploadListener?

    fun setListener(listener: UploadListener)

    fun getSourcePath(): String

    fun getFileName(): String

    fun getMimeType(): String

    fun getCurrent(): Long

    fun getTotal(): Long
}