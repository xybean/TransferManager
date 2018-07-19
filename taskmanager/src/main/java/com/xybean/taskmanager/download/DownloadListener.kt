package com.xybean.taskmanager.download

/**
 * Author @xybean on 2018/7/16.
 */
interface DownloadListener {

    fun onStart(task: IDownloadTask)

    fun onUpdate(task: IDownloadTask, current: Long, total: Long)

    fun onSucceed(task: IDownloadTask)

    fun onFailed(task: IDownloadTask, e: Exception)

}
