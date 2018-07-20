package com.xybean.taskmanager.download

import com.xybean.taskmanager.download.task.IDownloadTask

/**
 * Author @xybean on 2018/7/16.
 */
interface DownloadListener {

    fun onStart(task: IDownloadTask)

    fun onUpdate(task: IDownloadTask)

    fun onSucceed(task: IDownloadTask)

    fun onFailed(task: IDownloadTask, e: Exception)

}
