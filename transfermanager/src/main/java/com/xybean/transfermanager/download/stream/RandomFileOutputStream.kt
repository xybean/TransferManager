package com.xybean.transfermanager.download.stream

import java.io.*

/**
 * Author @xybean on 2018/6/27.
 */
class RandomFileOutputStream @Throws(IOException::class)
@JvmOverloads constructor(file: File, private var sync: Boolean = false) : OutputStream() {

    private var randomFile: RandomAccessFile

    val filePointer: Long
        @Throws(IOException::class)
        get() = randomFile.filePointer

    var fileSize: Long
        @Throws(IOException::class)
        get() = randomFile.length()
        @Throws(IOException::class)
        set(len) = randomFile.setLength(len)

    val fd: FileDescriptor
        @Throws(IOException::class)
        get() = randomFile.fd

    @Throws(IOException::class)
    @JvmOverloads constructor(fnm: String, sync: Boolean = false) : this(File(fnm), sync) {
    }

    init {
        if (!file.exists()) {
            throw FileNotFoundException("if you want to access file by offset, please make sure there is a file at ${file.absolutePath}")
        }
        randomFile = RandomAccessFile(file, "rw")
    }

    @Throws(IOException::class)
    override fun write(b: Int) {
        randomFile.write(b)
        if (sync) {
            randomFile.fd.sync()
        }
    }

    @Throws(IOException::class)
    override fun write(ba: ByteArray) {
        randomFile.write(ba)
        if (sync) {
            randomFile.fd.sync()
        }
    }

    @Throws(IOException::class)
    override fun write(ba: ByteArray, off: Int, len: Int) {
        randomFile.write(ba, off, len)
        if (sync) {
            flush()
        }
    }

    @Throws(IOException::class)
    override fun flush() {
        randomFile.fd.sync()
    }

    @Throws(IOException::class)
    override fun close() {
        randomFile.close()
    }

    @Throws(IOException::class)
    fun seek(pos: Long) {
        randomFile.seek(pos)
    }

}
