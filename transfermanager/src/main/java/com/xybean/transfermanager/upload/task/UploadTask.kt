package com.xybean.transfermanager.upload.task

import android.text.TextUtils
import com.xybean.transfermanager.Logger
import com.xybean.transfermanager.id.IdGenerator
import com.xybean.transfermanager.monitor.MonitorListener
import com.xybean.transfermanager.monitor.NetworkMonitor
import com.xybean.transfermanager.upload.UploadConfig
import com.xybean.transfermanager.upload.UploadListener
import com.xybean.transfermanager.upload.processor.IUploadProcessor
import com.xybean.transfermanager.upload.provider.IFileProvider
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Author @xybean on 2018/7/24.
 */
class UploadTask private constructor() : IUploadTask(), Runnable, Comparable<UploadTask> {

    private companion object {
        private const val TAG = "UploadTask"
        private const val DEFAULT_MIME_TYPE = "multipart/form-data; charset=utf-8"
        private const val DEFAULT_FILE_BODY = "file"
    }

    private var processor: IUploadProcessor? = null
    private lateinit var idGenerator: IdGenerator
    private lateinit var processorFactory: IUploadProcessor.Factory
    private lateinit var fileFactory: IFileProvider.Factory
    private var internalListener: UploadInternalListener? = null
    private var monitorListener: MonitorListener? = null
    private val headers: MutableMap<String, String> = HashMap()

    private var current: Long = 0
    private var total: Long = 0
    private var sourcePath: String = ""
    private var fileName = ""
    private var url: String = ""
    private var listener: UploadListener? = null
    private val status: AtomicInteger = AtomicInteger(UploadStatus.WAIT)
    private var id = -1
    private val priority = AtomicInteger(0)
    private var mimeType = DEFAULT_MIME_TYPE
    private var fileBody = DEFAULT_FILE_BODY

    private lateinit var networkMonitor: NetworkMonitor

    @Volatile
    private var canceled = false
    @Volatile
    private var paused = false

    override fun onUpdate(current: Long) {
        this.current = current
        networkMonitor.update(current)
        internalListener?.onUpdate(this)
    }

    override fun run() {
        if (canceled || paused) {
            Logger.d(TAG, "UploadTask(id = $id) has been canceled or paused before start.")
            return
        }
        var fileProvider: IFileProvider? = null
        try {
            fileProvider = fileFactory.createFileProvider(this)
            processor = processorFactory.createUploadProcessor(this, fileProvider)

            total = fileProvider.getLength() + current
            status.set(UploadStatus.START)
            Logger.i(TAG, "UploadTask(id = $id) start to upload file at $sourcePath")
            internalListener?.onStart(this)

            for (name in headers.keys) {
                processor!!.addHeader(name, headers[name]!!)
            }

            status.set(UploadStatus.UPDATE)
            processor!!.upload(url)
            if (!canceled && !paused) {
                val response = processor!!.getResponse()
                status.set(UploadStatus.SUCCEED)
                internalListener?.onSucceed(this, response)
            }
        } catch (e: Exception) {
            status.set(UploadStatus.FAILED)
            Logger.e(TAG, "Task(id = $id) is failed.", e)
            internalListener?.onFailed(this, e)
            monitorListener?.onFailed(e, networkMonitor.toString())
        } finally {
            fileProvider?.close()
            processor?.close()
        }
    }

    override fun compareTo(other: UploadTask): Int {
        return other.getPriority() - priority.get()
    }

    fun pause() {
        paused = true
        processor?.pause()
    }

    fun cancel() {
        canceled = true
        processor?.cancel()
    }

    internal fun bindInternalListener(listener: UploadInternalListener) {
        internalListener = listener
    }

    override fun getUrl(): String {
        return url
    }

    override fun getId(): Int {
        if (id == -1) {
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

    override fun getPriority(): Int {
        return priority.get()
    }

    override fun setPriority(priority: Int) {
        this.priority.set(priority)
    }

    override fun equals(other: Any?): Boolean {
        return other is UploadTask && other.id == id
    }

    override fun hashCode(): Int {
        return id
    }

    class Builder {

        private val task: UploadTask = UploadTask()
        private lateinit var processorFactory: IUploadProcessor.Factory
        private lateinit var fileFactory: IFileProvider.Factory

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
            if (config.fileFactory != null) {
                this@Builder.fileFactory = config.fileFactory!!
            }
            if (config.processorFactory != null) {
                this@Builder.processorFactory = config.processorFactory!!
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
            if (config.monitor != null) {
                task.monitorListener = config.monitor
            }
        }

        fun build(): UploadTask {
            task.fileFactory = fileFactory
            task.processorFactory = processorFactory
            task.networkMonitor = NetworkMonitor(task.getId(), NetworkMonitor.TYPE_UPSTREAM)
            return task
        }

    }

}