package com.xybean.taskmanager.download.stream

import java.io.*

/**
 * Author @xybean on 2018/7/12.
 */
class DefaultOutputStream @JvmOverloads constructor(private var offset: Long = -1) : IOutputStream {

    @Throws(IOException::class)
    override fun getOutputStream(path: String): OutputStream {
        if (offset > 0) {
            val file = File(path)
            if (!file.exists()) {
                throw FileNotFoundException("if you want to access file by offset("
                        + offset
                        + "), please make sure there is a file at " + path)
            }
            val randomFileOutputStream = RandomFileOutputStream(file)
            randomFileOutputStream.seek(offset)
            return BufferedOutputStream(randomFileOutputStream)
        } else {
            return BufferedOutputStream(FileOutputStream(path))
        }
    }
}
