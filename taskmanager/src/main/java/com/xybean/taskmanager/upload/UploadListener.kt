package com.xybean.taskmanager.upload

import com.xybean.taskmanager.upload.task.IUploadTask

/**
 * Author @xybean on 2018/7/24.
 */
interface UploadListener {
    fun onStart(task: IUploadTask)

    fun onUpdate(task: IUploadTask)

    fun onSucceed(task: IUploadTask, response: String)

    fun onFailed(task: IUploadTask, e: Exception)
}