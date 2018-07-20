package com.xybean.taskmanager.download.task

import com.xybean.taskmanager.download.DownloadListener

/**
 * Author @xybean on 2018/7/16.
 */
interface IDownloadTask {

    fun getUrl(): String

    fun getId(): Int

    fun getStatus(): Int

    fun getListener(): DownloadListener?

    fun setListener(listener: DownloadListener)

    fun getTargetPath(): String

    fun getTargetName(): String

    fun getCurrent(): Long

    fun getTotal(): Long
}
