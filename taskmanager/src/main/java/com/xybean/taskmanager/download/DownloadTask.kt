package com.xybean.taskmanager.download

import com.xybean.taskmanager.download.connection.IDownloadConnection
import com.xybean.taskmanager.download.exception.NoEnoughSpaceException
import com.xybean.taskmanager.download.stream.IOutputStream
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Author @xybean on 2018/7/16.
 */
class DownloadTask private constructor() : IDownloadTask, Runnable {

    companion object {
        private const val BUFFER_SIZE = 1024
    }

    private var internalListener: DownloadListener? = null
    private lateinit var connection: IDownloadConnection
    private lateinit var outputStream: IOutputStream
    private val headers: MutableMap<String, String> = HashMap()
    private var offset: Long = -1

    override var targetPath: String = ""
    override var targetName: String = ""
    override var url: String = ""
    override var listener: DownloadListener? = null
    override var status: Int
        get() {
            return internalStatus.get()
        }
        set(value) {
            internalStatus.set(value)
        }
    private var internalStatus: AtomicInteger = AtomicInteger(DownloadStatus.WAIT)
    override var id = -1
        get() {
            if (field < 0) {
                field = IdGenerator.generateId(url, targetPath, targetName)
            }
            return field
        }

    @Volatile
    private var canceled = false
    @Volatile
    private var paused = false

    override fun run() {

        if (canceled || paused) {
            return
        }
        status = DownloadStatus.START
        internalListener!!.onStart(this)

        // 连接网络、读流、写流、存库
        try {

            // 添加请求头
            if (!headers.isEmpty()) {
                for (key in headers.keys) {
                    connection.addHeader(key, headers[key]!!)
                }
            }

            connection.request(url)

            // 检查磁盘空间是否够用
            val total = connection.getContentLength()
            val freeSpace = Utils.sdCardFreeSpace
            if (freeSpace < total) {
                throw NoEnoughSpaceException(total, freeSpace)
            }

            // 读流
            val out = outputStream.getOutputStream(generateTempFile())
            val bis = connection.getInputStream()
            val buffer = ByteArray(BUFFER_SIZE)
            var count: Int
            var current = if (offset > 0) offset else 0

            // 有内容,正常读写
            status = DownloadStatus.UPDATE
            count = bis.read(buffer)
            while (count != -1 && !canceled && !paused) {
                out.write(buffer, 0, count)
                current += count.toLong()
                // 更新数据库
                internalListener?.onUpdate(this, current, total)
                count = bis.read(buffer)
            }
            when {
                canceled -> return
                paused -> {
                    out.flush()
                    status = DownloadStatus.PAUSED
                    return
                }
                else -> out.flush()
            }

            // 完成后重命名文件
            val file = File(saveAsFile())
            if (file.exists()) {
                file.delete()
            }
            Utils.renameFile(generateTempFile(), saveAsFile())
            status = DownloadStatus.SUCCEED
            internalListener!!.onSucceed(this)

        } catch (e: Exception) {
            status = DownloadStatus.FAILED
            internalListener!!.onFailed(this, e)
        } finally {
            connection.close()
        }

    }

    internal fun bindInternalListener(listener: DownloadListener) {
        internalListener = listener
    }

    fun cancel() {
        canceled = true
    }

    fun pause() {
        paused = true
    }

    override fun equals(other: Any?): Boolean {
        return other is DownloadTask && other.id == id
    }

    override fun hashCode(): Int {
        return id
    }

    private fun generateTempFile(): String {
        return saveAsFile() + "_temp"
    }

    private fun saveAsFile(): String {
        return String.format("%s/%s", targetPath, targetName)
    }

    class Builder {

        private val task: DownloadTask = DownloadTask()

        fun connection(connection: IDownloadConnection) = apply {
            task.connection = connection
        }

        fun outputStream(os: IOutputStream) = apply {
            task.outputStream = os
        }

        fun url(url: String) = apply {
            task.url = url
        }

        fun targetPath(path: String) = apply {
            task.targetPath = path
        }

        fun targetName(name: String) = apply {
            task.targetName = name
        }

        fun addHeader(key: String, value: String) = apply {
            task.headers[key] = value
        }

        fun addHeaders(headers: Map<String, String>) = apply {
            task.headers.putAll(headers)
        }

        fun offset(start: Long) = apply {
            task.offset = start
        }

        fun build() = task

    }
}
