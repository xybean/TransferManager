package com.xybean.transfermanager.upload

import android.util.SparseArray
import com.xybean.transfermanager.LogUtils
import com.xybean.transfermanager.download.id.IdGenerator
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
            LogUtils.i(TAG, "upload succeed and remove a Task(id = ${task.getId()})," +
                    " TaskList's size is ${taskList.size()} now.")
            val listener = task.getListener()
            listener?.onSucceed(task, response)
        }

        override fun onFailed(task: IUploadTask, e: Exception) {
            synchronized(taskList) {
                taskList.remove(task.getId())
            }
            LogUtils.i(TAG, "upload failed and remove a Task(id = ${task.getId()})," +
                    " TaskList's size is ${taskList.size()} now.")
            val listener = task.getListener()
            listener?.onFailed(task, e)
        }
    }

    fun upload(url: String, source: String, listener: UploadListener? = null, idGenerator: IdGenerator): Int {
        val id = idGenerator.getId()

        synchronized(taskList) {
            val cachedTask: UploadTask? = taskList.get(id)
            if (cachedTask != null) {
                // 如果已经在执行或者正在等待执行，则重新绑定监听后直接返回
                LogUtils.d(TAG, "task(id = ${cachedTask.getId()}) is executing now, so we won't start it twice.")
                if (listener != null) {
                    cachedTask.setListener(listener)
                }
                return id
            }
            val task = UploadTask.Builder()
                    .url(url)
                    .sourcePath(source)
                    .listener(listener)
                    .connection(connectionFactory)
                    .stream(streamFactory)
                    .idGenerator(idGenerator)
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

        fun debug(debug: Boolean) = apply {
            LogUtils.DEBUG = debug
        }

        fun build() = manager

    }

}