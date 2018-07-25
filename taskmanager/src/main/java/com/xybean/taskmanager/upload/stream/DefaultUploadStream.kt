package com.xybean.taskmanager.upload.stream

import com.xybean.taskmanager.upload.task.IUploadTask
import java.io.*

/**
 * Author @xybean on 2018/7/24.
 */
class DefaultUploadStream(task: IUploadTask) : IUploadStream(task) {

    private var inputStream: InputStream? = null
    private var sourceFile: File? = null

    @Throws(IOException::class)
    override fun getInputStream(): InputStream {

        val sourcePath = task.getSourcePath()
        val offset = task.getCurrent()
        sourceFile = File(sourcePath)
        if (!sourceFile!!.exists()) {
            throw FileNotFoundException("ensure there is a file at ${sourceFile!!.absolutePath} before you upload it.")
        }
        inputStream = if (offset > 0) {
            val randomFileInputStream = RandomFileInputStream(sourceFile!!)
            randomFileInputStream.seek(offset)
            BufferedInputStream(randomFileInputStream)
        } else {
            BufferedInputStream(FileInputStream(sourceFile))
        }
        return inputStream!!
    }

    override fun length(): Long {
        if (sourceFile == null) {
            sourceFile = File(task.getSourcePath())
            if (!sourceFile!!.exists()) {
                throw FileNotFoundException("ensure there is a file at ${sourceFile!!.absolutePath} before you upload it.")
            }
        }
        return sourceFile!!.length()
    }

    override fun close() {
        inputStream?.close()
    }

}