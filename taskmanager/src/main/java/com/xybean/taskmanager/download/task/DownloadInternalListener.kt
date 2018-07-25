package com.xybean.taskmanager.download.task

/**
 * Author @xybean on 2018/7/24.
 */
internal interface DownloadInternalListener {
    fun onStart(task: IDownloadTask)

    fun onUpdate(task: IDownloadTask, sync: Boolean)

    fun onSucceed(task: IDownloadTask)

    fun onFailed(task: IDownloadTask, e: Exception)
}