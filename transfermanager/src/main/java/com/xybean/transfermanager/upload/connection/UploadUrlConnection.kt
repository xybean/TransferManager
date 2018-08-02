package com.xybean.transfermanager.upload.connection

import android.os.Build
import com.xybean.transfermanager.upload.task.IUploadTask
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection

/**
 * Author @xybean on 2018/7/24.
 */
class UploadUrlConnection(task: IUploadTask, config: IUploadConnection.Configuration? = null)
    : IUploadConnection(task, config) {

    companion object {
        private const val END = "\r\n"
        private const val TWO_HYPHENS = "--"
        private const val BOUNDARY = "*****"
    }

    private var mConnection: URLConnection
    private var outputStream: OutputStream? = null

    private val prefix: ByteArray
    private val suffix: ByteArray
    private var prefixWritten = false
    private var suffixWritten = false

    init {
        val url = URL(task.getUrl())
        mConnection = url.openConnection()
        if (config != null) {
            if (config.getReadTimeOut() > 0) {
                mConnection.readTimeout = config.getReadTimeOut()
            }

            if (config.getConnectTimeout() > 0) {
                mConnection.connectTimeout = config.getConnectTimeout()
            }
        }
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
    }

    override fun write(byteArray: ByteArray, off: Int, len: Int) {
        if (outputStream == null) {
            outputStream = mConnection.getOutputStream()
        }
        if (!prefixWritten) {
            outputStream!!.write(prefix, 0, prefix.size)
            prefixWritten = true
        }
        outputStream!!.write(byteArray, off, len)
    }

    override fun flush() {
        if (!suffixWritten && task.getTotal() == task.getCurrent()) {
            outputStream!!.write(suffix, 0, suffix.size)
            suffixWritten = true
        }
        outputStream?.flush()
    }

    override fun addHeader(name: String, value: String) {
        mConnection.addRequestProperty(name, value)
    }

    override fun request(url: String) {
        if (mConnection is HttpURLConnection) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                (mConnection as HttpURLConnection).setFixedLengthStreamingMode(prefix.size + task.getTotal() + suffix.size)
            } else {
                (mConnection as HttpURLConnection).setFixedLengthStreamingMode((prefix.size + task.getTotal() + suffix.size).toInt())
            }
        }
        mConnection.connect()
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
            throw Exception("HTTP Request is not success, Response code is $code")
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
            } catch (e: Exception) {
                throw e
            } finally {
                inputStream?.close()
                inputStreamReader?.close()
                reader?.close()
            }
        }
        return resultBuffer.toString()
    }

    override fun close() {
        if (mConnection is HttpURLConnection) {
            (mConnection as HttpURLConnection).disconnect()
        }
    }

}