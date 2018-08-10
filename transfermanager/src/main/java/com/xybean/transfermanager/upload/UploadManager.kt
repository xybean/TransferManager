package com.xybean.transfermanager.upload

import android.util.SparseArray
import com.xybean.transfermanager.Logger
import com.xybean.transfermanager.id.IdGenerator
import com.xybean.transfermanager.upload.processor.IUploadProcessor
import com.xybean.transfermanager.upload.provider.IFileProvider
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
    private lateinit var processorFactory: IUploadProcessor.Factory
    private lateinit var fileFactory: IFileProvider.Factory
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
            taskList.put(id, task)
            executor.execute(task)

            return task.getId()
        }
    }

    fun cancel(id: Int) {
        synchronized(taskList) {
            val task = taskList.get(id)
            if (task != null) {
                task.cancel()
                taskList.remove(id)
                Logger.i(TAG, "cancel and remove a Task(id = ${task.getId()}), TaskList's size is ${taskList.size()} now.")
            }
        }
    }

    fun pause(id: Int) {
        synchronized(taskList) {
            val task = taskList.get(id)
            if (task != null) {
                task.pause()
                taskList.remove(id)
                Logger.i(TAG, "pause and remove a Task(id = ${task.getId()}), " +
                        "TaskList's size is ${taskList.size()} now.")
            }
        }
    }

    fun contains(id: Int): Boolean {
        synchronized(taskList) {
            return taskList.get(id) != null
        }
    }

    fun updatePriority(id: Int, priority: Int) {
        // todo 目前这个方法是无效的
        synchronized(taskList) {
            taskList.get(id)?.setPriority(priority)
        }
    }

    private fun ensureConfigValid(config: UploadConfig) {
        if (config.idGenerator == null) {
            config.idGenerator = idGenerator
            if (config.idGenerator == null) {
                throw IllegalArgumentException("you must set idGenerator by UploadManager.Builder.idGenerator() or UploadConfig.Builder.idGenerator()!")
            }
        }
        if (config.processorFactory == null) {
            config.processorFactory = processorFactory
            if (config.processorFactory == null) {
                throw IllegalArgumentException("you must set processorFactory by UploadManager.Builder.processor() or UploadConfig.Builder.processor()!")
            }
        }
        if (config.fileFactory == null) {
            config.fileFactory = fileFactory
            if (config.fileFactory == null) {
                throw IllegalArgumentException("you must set fileFactory by UploadManager.Builder.file() or UploadConfig.Builder.file()!")
            }
        }
    }

    class Builder {

        private val manager: UploadManager = UploadManager()

        fun executor(executor: Executor) = apply {
            manager.executor = executor
        }

        fun processor(processorFactory: IUploadProcessor.Factory) = apply {
            manager.processorFactory = processorFactory
        }

        fun file(fileFactory: IFileProvider.Factory) = apply {
            manager.fileFactory = fileFactory
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