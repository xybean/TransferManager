package com.xybean.taskmanager.download.task

import com.xybean.taskmanager.download.DownloadListener
import com.xybean.taskmanager.download.IdGenerator
import com.xybean.taskmanager.download.LogUtils
import com.xybean.taskmanager.download.Utils
import com.xybean.taskmanager.download.connection.IDownloadConnection
import com.xybean.taskmanager.download.exception.NoEnoughSpaceException
import com.xybean.taskmanager.download.stream.IDownloadStream
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Author @xybean on 2018/7/16.
 */
internal class DownloadTask private constructor() : IDownloadTask, Runnable {

    companion object {
        private const val TAG = "DownloadTask"
        private const val BUFFER_SIZE = 1024
    }

    private var internalListener: DownloadListener? = null
    private lateinit var connection: IDownloadConnection
    private lateinit var outputStream: IDownloadStream
    private val headers: MutableMap<String, String> = HashMap()

    private var offset: Long = -1
    private var targetPath: String = ""
    private var targetName: String = ""
    private var url: String = ""
    private var listener: DownloadListener? = null
    private var status: AtomicInteger = AtomicInteger(DownloadStatus.WAIT)
    private var id = -1

    @Volatile
    private var canceled = false
    @Volatile
    private var paused = false

    override fun run() {

        if (canceled || paused) {
            LogUtils.d(TAG, Utils.formatString("Task(id = %d) has been canceled or paused before start.", id))
            return
        }
        status.set(DownloadStatus.START)
        LogUtils.i(TAG, Utils.formatString("Task(id = %d) start to be executed and save at %s", id, saveAsFile()))
        internalListener?.onStart(this)

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
            val contentLength = connection.getContentLength()
            val freeSpace = Utils.sdCardFreeSpace
            if (freeSpace < contentLength) {
                throw NoEnoughSpaceException(contentLength, freeSpace)
            }

            // 读流
            val tempFile = File(generateTempFile())
            if (offset <= 0) {
                if (tempFile.exists()) {
                    LogUtils.d(TAG, Utils.formatString("task(id = %d): in order to redownload file," +
                            "we delete file at %s firstly.", id, generateTempFile()))
                    tempFile.delete()
                }
            }
            tempFile.createNewFile()
            val out = outputStream.getOutputStream()
            val bis = connection.getInputStream()
            val buffer = ByteArray(BUFFER_SIZE)
            var count: Int
            var current = if (offset > 0) offset else 0
            val total = if (offset > 0) {
                offset + contentLength
            } else {
                contentLength
            }
            // 有内容,正常读写
            status.set(DownloadStatus.UPDATE)
            count = bis.read(buffer)
            while (count != -1 && !canceled && !paused) {
                out.write(buffer, 0, count)
                current += count.toLong()
                // 更新数据库
                internalListener?.onUpdate(this, current, total)
                count = bis.read(buffer)
            }
            when {
                canceled -> {
                    LogUtils.i(TAG, Utils.formatString("Task(id = %d) is canceled.", id))
                    return
                }
                paused -> {
                    out.flush()
                    status.set(DownloadStatus.PAUSED)
                    LogUtils.i(TAG, Utils.formatString("Task(id = %d) is paused.", id))
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
            status.set(DownloadStatus.SUCCEED)
            internalListener?.onSucceed(this)

        } catch (e: Exception) {
            status.set(DownloadStatus.FAILED)
            LogUtils.e(TAG, Utils.formatString("Task(id = %d) is failed.", id), e)
            internalListener?.onFailed(this, e)
        } finally {
            connection.close()
        }

    }

    override fun getUrl(): String {
        return url
    }

    override fun getId(): Int {
        if (id < 0) {
            id = IdGenerator.generateId(url, targetPath, targetName)
        }
        return id
    }

    override fun getStatus(): Int {
        return status.get()
    }

    override fun getListener(): DownloadListener? {
        return listener
    }

    override fun setListener(listener: DownloadListener) {
        this@DownloadTask.listener = listener
    }

    override fun getTargetPath(): String {
        return targetPath
    }

    override fun getTargetName(): String {
        return targetName
    }

    override fun getOffset(): Long {
        return offset
    }

    fun cancel() {
        canceled = true
    }

    fun pause() {
        paused = true
    }

    internal fun bindInternalListener(listener: DownloadListener) {
        internalListener = listener
    }

    private fun generateTempFile(): String {
        return saveAsFile() + "_temp"
    }

    private fun saveAsFile(): String {
        return String.format("%s/%s", targetPath, targetName)
    }

    override fun equals(other: Any?): Boolean {
        return other is DownloadTask && other.id == id
    }

    override fun hashCode(): Int {
        return id
    }

    class Builder {

        private val task: DownloadTask = DownloadTask()
        private lateinit var connectionFactory: IDownloadConnection.Factory
        private lateinit var streamFactory: IDownloadStream.Factory

        fun connection(connection: IDownloadConnection.Factory) = apply {
            this@Builder.connectionFactory = connection
        }

        fun outputStream(streamFactory: IDownloadStream.Factory) = apply {
            this@Builder.streamFactory = streamFactory
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

        fun build(): DownloadTask {
            task.connection = connectionFactory.createConnection(task)
            task.outputStream = streamFactory.createDownloadStream(task)
            return task
        }

    }
}
