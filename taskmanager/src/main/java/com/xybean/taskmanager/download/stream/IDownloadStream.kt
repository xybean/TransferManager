package com.xybean.taskmanager.download.stream

import com.xybean.taskmanager.download.task.IDownloadTask
import java.io.OutputStream

/**
 * Author @xybean on 2018/4/17.<br></br>
 */
abstract class IDownloadStream(internal val task: IDownloadTask) {

    @Throws(Exception::class)
    abstract fun getOutputStream(): OutputStream

    abstract fun close()

    interface Factory {
        fun createDownloadStream(task: IDownloadTask): IDownloadStream
    }
}
