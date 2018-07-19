package com.xybean.taskmanager.download.stream

import java.io.OutputStream

/**
 * Author @xybean on 2018/4/17.<br></br>
 */
interface IOutputStream {

    @Throws(Exception::class)
    fun getOutputStream(path: String): OutputStream

}
