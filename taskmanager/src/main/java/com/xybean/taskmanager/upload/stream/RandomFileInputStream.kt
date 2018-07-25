package com.xybean.taskmanager.upload.stream

import java.io.*

/**
 * Author @xybean on 2018/7/24.
 */
class RandomFileInputStream(file: File) : InputStream() {

    private var randomFile: RandomAccessFile

    init {
        if (!file.exists()) {
            throw FileNotFoundException("if you want to access file by offset, please make sure there is a file at ${file.absolutePath}")
        }
        randomFile = RandomAccessFile(file, "r")
    }

    override fun read(): Int {
        return randomFile.read()
    }

    @Throws(IOException::class)
    fun seek(pos: Long) {
        randomFile.seek(pos)
    }

}