package com.xybean.transfermanager.upload

import android.util.SparseArray
import com.xybean.transfermanager.id.IdGenerator
import com.xybean.transfermanager.Logger
import com.xybean.transfermanager.upload.connection.IUploadConnection
import com.xybean.transfermanager.upload.stream.IUploadStream
import com.xybean.transfermanager.upload.task.IUploadTask
import com.xybean.transfermanager.upload.task.UploadInternalListener
import com.xybean.transfermanager.upload.task.UploadTask
import java.util.concurrent.Executor

/**
 * Author @xybean on 2018/7/24.
 */
class UploadManager private constructor() {

    private companion object {
        const val TAG = "UploadManager"
    }

    private lateinit var executor: Executor
    private lateinit var connectionFactory: IUploadConnection.Factory
    private lateinit var streamFactory: IUploadStream.Factory
    private var idGenerator: IdGenerator? = null

    private val taskList: SparseArray<UploadTask> = SparseArray()

    private val internalListener = object : UploadInternalListener {
        override fun onStart(task: IUploadTask) {
            val listener = task.getListener()
            listener?.onStart(task)
        }

        override fun onUpdate(task: IUploadTask) {
            val listener = task.getListener()
            listener?.onUpdate(task)
        }

        override fun onSucceed(task: IUploadTask, response: String) {
            synchronized(taskList) {
                taskList.remove(task.getId())
            }
            Logger.i(TAG, "upload succeed and remove a Task(id = ${task.getId()})," +
                    " TaskList's size is ${taskList.size()} now.")
            val listener = task.getListener()
            listener?.onSucceed(task, response)
        }

        override fun onFailed(task: IUploadTask, e: Exception) {
            synchronized(taskList) {
                taskList.remove(task.getId())
            }
            Logger.i(TAG, "upload failed and remove a Task(id = ${task.getId()})," +
                    " TaskList's size is ${taskList.size()} now.")
            val listener = task.getListener()
            listener?.onFailed(task, e)
        }
    }

    fun upload(url: String, source: String, listener: UploadListener? = null, config: UploadConfig): Int {

        ensureConfigValid(config)

        val id = config.idGenerator!!.getId()

        synchronized(taskList) {
            val cachedTask: UploadTask? = taskList.get(id)
            if (cachedTask != null) {
                // 如果已经在执行或者正在等待执行，则重新绑定监听后直接返回
                Logger.d(TAG, "task(id = ${cachedTask.getId()}) is executing now, so we won't start it twice.")
                if (listener != null) {
                    cachedTask.setListener(listener)
                }
                return id
            }
            val task = UploadTask.Builder()
                    .url(url)
                    .sourcePath(source)
                    .listener(listener)
                    .config(config)
                    .build()

            task.bindInternalListener(internalListener)
            executor.execute(task)

            return task.getId()
        }
    }

    fun cancel(id: Int) {
        synchronized(taskList) {
            val task = taskList.get(id)
            task?.cancel()
        }
    }

    fun pause(id: Int) {
        synchronized(taskList) {
            val task = taskList.get(id)
            task?.pause()
        }
    }

    fun contains(id: Int): Boolean {
        synchronized(taskList) {
            return taskList.get(id) != null
        }
    }

    private fun ensureConfigValid(config: UploadConfig) {
        if (config.idGenerator == null) {
            config.idGenerator = idGenerator
            if (config.idGenerator == null) {
                throw IllegalArgumentException("you must set idGenerator by UploadManager.Builder.idGenerator() or UploadConfig.Builder.idGenerator()!")
            }
        }
        if (config.connectionFactory == null) {
            config.connectionFactory = connectionFactory
            if (config.connectionFactory == null) {
                throw IllegalArgumentException("you must set connectionFactory by UploadManager.Builder.connection() or UploadConfig.Builder.connection()!")
            }
        }
        if (config.streamFactory == null) {
            config.streamFactory = streamFactory
            if (config.streamFactory == null) {
                throw IllegalArgumentException("you must set streamFactory by UploadManager.Builder.stream() or UploadConfig.Builder.stream()!")
            }
        }
    }

    class Builder {

        private val manager: UploadManager = UploadManager()

        fun executor(executor: Executor) = apply {
            manager.executor = executor
        }

        fun connection(connectionFactory: IUploadConnection.Factory) = apply {
            manager.connectionFactory = connectionFactory
        }

        fun stream(streamFactory: IUploadStream.Factory) = apply {
            manager.streamFactory = streamFactory
        }

        fun idGenerator(idGenerator: IdGenerator) = apply {
            manager.idGenerator = idGenerator
        }

        fun debug(debug: Boolean) = apply {
            Logger.DEBUG = debug
        }

        fun build() = manager

    }

}