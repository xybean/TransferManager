package com.xybean.transfermanager.download.stream

import com.xybean.transfermanager.download.task.IDownloadTask
import java.io.*

/**
 * Author @xybean on 2018/7/12.
 */
class DefaultDownloadStream(task: IDownloadTask) : IDownloadStream(task) {

    private var outputStream: OutputStream? = null

    @Throws(IOException::class)
    override fun getOutputStream(): OutputStream {
        val path = task.getTargetPath() + File.separator + task.getTargetName()
        outputStream = if (task.getCurrent() > 0) {
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
        return outputStream!!
    }

    override fun close() {
        outputStream?.close()
    }

}
