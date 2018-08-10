package com.xybean.transfermanager.upload.provider

import com.xybean.transfermanager.upload.task.IUploadTask
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream

/**
 * Author @xybean on 2018/8/9.
 */
class FileProvider(task: IUploadTask) : IFileProvider(task) {

    private var inputStream: InputStream? = null

    override fun getInputStream(): InputStream {
        val sourcePath = task.getSourcePath()
        val sourceFile = File(sourcePath)
        if (!sourceFile.exists()) {
            throw FileNotFoundException("ensure there is a file at ${sourceFile.absolutePath} before you upload it.")
        }
        inputStream = sourceFile.inputStream()
        return inputStream!!
    }

    override fun getLength(): Long {
        val sourceFile = File(task.getSourcePath())
        if (!sourceFile.exists()) {
            throw FileNotFoundException("ensure there is a file at ${sourceFile.absolutePath} before you upload it.")
        }
        return sourceFile.length()
    }

    override fun close() {
        inputStream?.close()
    }

}