package com.xybean.transfermanager.upload.provider

import com.xybean.transfermanager.upload.task.IUploadTask
import java.io.*

/**
 * Author @xybean on 2018/8/9.
 */
class RandomFileProvider(task: IUploadTask) : IFileProvider(task) {

    private var inputStream: InputStream? = null

    override fun getInputStream(): InputStream {
        val sourcePath = task.getSourcePath()
        val offset = task.getCurrent()
        val sourceFile = File(sourcePath)
        if (!sourceFile.exists()) {
            throw FileNotFoundException("ensure there is a file at ${sourceFile.absolutePath} before you upload it.")
        }
        inputStream = if (offset > 0) {
            val randomFileInputStream = RandomFileInputStream(sourceFile)
            randomFileInputStream.seek(offset)
            BufferedInputStream(randomFileInputStream)
        } else {
            BufferedInputStream(FileInputStream(sourceFile))
        }
        return inputStream!!
    }

    override fun getLength(): Long {
        val sourceFile = File(task.getSourcePath())
        return if (task.getCurrent() > 0) {
            sourceFile.length() - task.getCurrent()
        } else {
            sourceFile.length()
        }
    }

    override fun close() {
        inputStream?.close()
    }
}