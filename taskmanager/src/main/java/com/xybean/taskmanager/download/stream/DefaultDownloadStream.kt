package com.xybean.taskmanager.download.stream

import com.xybean.taskmanager.download.task.IDownloadTask
import java.io.*

/**
 * Author @xybean on 2018/7/12.
 */
class DefaultDownloadStream(task: IDownloadTask) : IDownloadStream(task) {

    @Throws(IOException::class)
    override fun getOutputStream(): OutputStream {
        val path = task.getTargetPath() + File.separator + task.getTargetName()
        return if (task.getCurrent() > 0) {
            val file = File(path)
            if (!file.exists()) {
                throw FileNotFoundException("if you want to access file by offset("
                        + task.getCurrent()
                        + "), please make sure there is a file at " + path)
            }
            val randomFileOutputStream = RandomFileOutputStream(file)
            randomFileOutputStream.seek(task.getCurrent())
            BufferedOutputStream(randomFileOutputStream)
        } else {
            BufferedOutputStream(FileOutputStream(path))
        }
    }

}
