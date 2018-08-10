package com.xybean.transfermanager.upload.processor

import android.os.Build
import com.xybean.transfermanager.upload.provider.IFileProvider
import com.xybean.transfermanager.upload.task.IUploadTask
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection

/**
 * Author @xybean on 2018/8/9.
 */
class HttpUrlUploadProcessor(task: IUploadTask, fileProvider: IFileProvider) : IUploadProcessor(task, fileProvider) {

    companion object {
        private const val END = "\r\n"
        private const val TWO_HYPHENS = "--"
        private const val BOUNDARY = "*****"
        private const val BUFFER_SIZE = 1024
    }

    private var mConnection: URLConnection
    private var output: OutputStream? = null
    private var input: InputStream? = null

    private val prefix: ByteArray
    private val suffix: ByteArray

    @Volatile
    private var canceled = false
    @Volatile
    private var paused = false

    init {
        val url = URL(task.getUrl())
        mConnection = url.openConnection()
        // 设置允许输入
        mConnection.doInput = true
        // 设置允许输出
        mConnection.doOutput = true
        mConnection.useCaches = false
        mConnection.setRequestProperty("Connection", "Keep-Alive")
        // 设置字符编码
        mConnection.setRequestProperty("Charset", "UTF-8")
        // 设置请求内容类型
        mConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=$BOUNDARY")

        val sb = StringBuffer()
        sb.append(TWO_HYPHENS)
        sb.append(BOUNDARY)
        sb.append(END)
        sb.append("Content-Disposition: form-data; name=\"${task.getFileBody()}\";filename=\"${task.getFileName()}\"$END")
        sb.append("Content-Type: ${task.getMimeType()}$END")
        sb.append(END)
        prefix = sb.toString().toByteArray()
        suffix = (END + TWO_HYPHENS + BOUNDARY + TWO_HYPHENS + END).toByteArray()

        if (mConnection is HttpURLConnection) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                (mConnection as HttpURLConnection).setFixedLengthStreamingMode(prefix.size + fileProvider.getLength() + suffix.size)
            } else {
                (mConnection as HttpURLConnection).setFixedLengthStreamingMode((prefix.size + fileProvider.getLength() + suffix.size).toInt())
            }
        }
    }

    override fun addHeader(name: String, value: String) {
        mConnection.addRequestProperty(name, value)
    }

    @Throws(Exception::class)
    override fun upload(url: String) {
        mConnection.connect()
        output = mConnection.getOutputStream()
        // 写入boundary头
        output!!.write(prefix, 0, prefix.size)

        input = fileProvider.getInputStream()
        var current = task.getCurrent()
        val buffer = ByteArray(BUFFER_SIZE)
        var count = input!!.read(buffer, 0, buffer.size)
        while (count != -1 && !canceled && !paused) {
            output!!.write(buffer, 0, count)
            current += count
            task.onUpdate(current)
            count = input!!.read(buffer, 0, buffer.size)
        }
        when {
            paused -> {
                output!!.flush()
            }
            canceled -> {
                output!!.flush()
            }
            else -> {
                // 写入boundary尾
                output!!.write(suffix, 0, suffix.size)
                output!!.flush()
            }
        }
    }

    override fun getResponseCode(): Int {
        return if (mConnection is HttpURLConnection) {
            (mConnection as HttpURLConnection).responseCode
        } else -1
    }

    @Throws(Exception::class)
    override fun getResponse(): String {
        val code = getResponseCode()
        if (code >= 300) {
            throw Exception("HTTP Request is failed, Response code is $code")
        }
        val resultBuffer = StringBuffer()
        if (code == HttpURLConnection.HTTP_OK) {
            var inputStream: InputStream? = null
            var inputStreamReader: InputStreamReader? = null
            var reader: BufferedReader? = null
            try {
                inputStream = mConnection.getInputStream()
                inputStreamReader = InputStreamReader(inputStream)
                reader = BufferedReader(inputStreamReader)
                var tempLine = reader.readLine()
                while (tempLine != null) {
                    resultBuffer.append(tempLine)
                    resultBuffer.append("\n")
                    tempLine = reader.readLine()
                }
            } finally {
                inputStream?.close()
                inputStreamReader?.close()
                reader?.close()
            }
        }
        return resultBuffer.toString()
    }

    override fun cancel() {
        canceled = true
    }

    override fun pause() {
        paused = true
    }

    override fun close() {
        input?.close()
        (mConnection as HttpURLConnection?)?.disconnect()
    }

}