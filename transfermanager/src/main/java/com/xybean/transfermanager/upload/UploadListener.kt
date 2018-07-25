package com.xybean.transfermanager.upload

import com.xybean.transfermanager.upload.task.IUploadTask

/**
 * Author @xybean on 2018/7/24.
 */
interface UploadListener {
    fun onStart(task: IUploadTask)

    fun onUpdate(task: IUploadTask)

    fun onSucceed(task: IUploadTask, response: String)

    fun onFailed(task: IUploadTask, e: Exception)
}