package com.xybean.taskmanager.upload.task

/**
 * Author @xybean on 2018/7/24.
 */
internal interface UploadInternalListener {
    fun onStart(task: IUploadTask)

    fun onUpdate(task: IUploadTask)

    fun onSucceed(task: IUploadTask, response: String)

    fun onFailed(task: IUploadTask, e: Exception)
}