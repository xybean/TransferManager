package com.xybean.transfermanager.upload.task

import com.xybean.transfermanager.upload.UploadListener

/**
 * Author @xybean on 2018/7/24.
 */
abstract class IUploadTask {

    abstract fun getUrl(): String

    abstract fun getId(): Int

    abstract fun getStatus(): Int

    abstract fun getListener(): UploadListener?

    abstract fun setListener(listener: UploadListener)

    abstract fun getSourcePath(): String

    abstract fun getFileName(): String

    abstract fun getMimeType(): String

    abstract fun getFileBody(): String

    abstract fun getCurrent(): Long

    abstract fun getTotal(): Long

    abstract fun getPriority(): Int

    abstract fun setPriority(priority: Int)

    abstract fun onUpdate(current: Long)
}