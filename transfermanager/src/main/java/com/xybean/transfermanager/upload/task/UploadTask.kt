package com.xybean.transfermanager.upload.task

import android.text.TextUtils
import com.xybean.transfermanager.IdGenerator
import com.xybean.transfermanager.Logger
import com.xybean.transfermanager.upload.UploadConfig
import com.xybean.transfermanager.upload.UploadListener
import com.xybean.transfermanager.upload.connection.IUploadConnection
import com.xybean.transfermanager.upload.stream.IUploadStream
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Author @xybean on 2018/7/24.
 */
class UploadTask private constructor() : IUploadTask, Runnable {

    private companion object {
        private const val TAG = "UploadTask"
        private const val BUFFER_SIZE = 1024
        private const val DEFAULT_MIME_TYPE = "multipart/form-data; charset=utf-8"
        private const val DEFAULT_FILE_BODY = "file"
    }

    private lateinit var idGenerator: IdGenerator
    private var internalListener: UploadInternalListener? = null
    private lateinit var connection: IUploadConnection
    private lateinit var uploadStream: IUploadStream
    private val headers: MutableMap<String, String> = HashMap()

    private var current: Long = 0
    private var total: Long = 0
    private var sourcePath: String = ""
    private var fileName = ""
    private var url: String = ""
    private var listener: UploadListener? = null
    private var status: AtomicInteger = AtomicInteger(UploadStatus.WAIT)
    private var id = -1
    private var mimeType = DEFAULT_MIME_TYPE
    private var fileBody = DEFAULT_FILE_BODY

    @Volatile
    private var canceled = false
    @Volatile
    private var paused = false

    override fun run() {
        if (canceled || paused) {
            Logger.d(TAG, "UploadTask(id = $id) has been canceled or paused before start.")
            return
        }
        status.set(UploadStatus.START)
        Logger.i(TAG, "UploadTask(id = $id) start to upload file at $sourcePath")
        internalListener?.onStart(this)

        try {
            // 添加请求头
            if (!headers.isEmpty()) {
                for (key in headers.keys) {
                    connection.addHeader(key, headers[key]!!)
                }
            }

            connection.request(url)

            // 写入文件流
            val inputStream = uploadStream.getInputStream()
            total = uploadStream.length()
            val buffer = ByteArray(BUFFER_SIZE)
            status.set(UploadStatus.UPDATE)
            var count = inputStream.read(buffer, 0, buffer.size)
            while (count != -1) {
                connection.write(buffer, 0, count)
                current += count
                internalListener?.onUpdate(this)
                count = inputStream.read(buffer, 0, buffer.size)
            }
            when {
                canceled -> {
                    Logger.i(TAG, "UploadTask(id = $id) is canceled.")
                    return
                }
                paused -> {
                    connection.flush()
                    status.set(UploadStatus.PAUSED)
                    Logger.i(TAG, "UploadTask(id = $id) is paused.")
                    return
                }
                else -> connection.flush()
            }

            val response = connection.getResponse()
            if (!canceled) {
                status.set(UploadStatus.SUCCEED)
                internalListener?.onSucceed(this, response)
            }
        } catch (e: Exception) {
            status.set(UploadStatus.FAILED)
            Logger.e(TAG, "Task(id = $id) is failed.", e)
            internalListener?.onFailed(this, e)
        } finally {
            connection.close()
            uploadStream.close()
        }

    }

    fun pause() {
        paused = true
    }

    fun cancel() {
        canceled = true
    }

    internal fun bindInternalListener(listener: UploadInternalListener) {
        internalListener = listener
    }

    override fun getUrl(): String {
        return url
    }

    override fun getId(): Int {
        if (id < 0) {
            id = idGenerator.getId()
        }
        return id
    }

    override fun getStatus(): Int {
        return status.get()
    }

    override fun getListener(): UploadListener? {
        return listener
    }

    override fun setListener(listener: UploadListener) {
        this@UploadTask.listener = listener
    }

    override fun getSourcePath(): String {
        return sourcePath
    }

    override fun getFileName(): String {
        if (fileName.isEmpty()) {
            fileName = sourcePath.substring(sourcePath.lastIndexOf(File.separator) + 1)
        }
        return fileName
    }

    override fun getMimeType(): String {
        return mimeType
    }

    override fun getFileBody(): String {
        return fileBody
    }

    override fun getCurrent(): Long {
        return current
    }

    override fun getTotal(): Long {
        return total
    }

    class Builder {

        private val task: UploadTask = UploadTask()
        private lateinit var connectionFactory: IUploadConnection.Factory
        private lateinit var streamFactory: IUploadStream.Factory

        fun url(url: String) = apply {
            task.url = url
        }

        fun sourcePath(path: String) = apply {
            task.sourcePath = path
        }

        fun listener(listener: UploadListener?) = apply {
            task.listener = listener
        }

        fun config(config: UploadConfig) = apply {
            if (config.connectionFactory != null) {
                this@Builder.connectionFactory = config.connectionFactory!!
            }
            if (config.streamFactory != null) {
                this@Builder.streamFactory = config.streamFactory!!
            }
            if (!TextUtils.isEmpty(config.fileName)) {
                task.fileName = config.fileName
            }
            if (!TextUtils.isEmpty(config.fileBody)) {
                task.fileBody = config.fileBody
            }
            if (!TextUtils.isEmpty(config.mimeType)) {
                task.mimeType = config.mimeType
            }
            if (!config.headers.isEmpty()) {
                task.headers.putAll(config.headers)
            }
            if (config.offset > 0) {
                task.current = config.offset
            }
            if (config.idGenerator != null) {
                task.idGenerator = config.idGenerator!!
            }
        }

        fun build(): UploadTask {
            task.connection = connectionFactory.createUploadConnection(task)
            task.uploadStream = streamFactory.createUploadStream(task)
            return task
        }

    }

}