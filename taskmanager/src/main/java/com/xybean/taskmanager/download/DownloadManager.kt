package com.xybean.taskmanager.download

import android.util.SparseArray
import com.xybean.taskmanager.download.connection.IDownloadConnection
import com.xybean.taskmanager.download.db.DownloadDatabaseHandler
import com.xybean.taskmanager.download.db.DownloadTaskModel
import com.xybean.taskmanager.download.stream.DefaultOutputStream
import com.xybean.taskmanager.download.stream.IOutputStream
import java.util.concurrent.Executor

/**
 * Author @xybean on 2018/7/16.
 */
class DownloadManager private constructor() {

    companion object {
        const val TAG = "DownloadManager"
    }

    private lateinit var executor: Executor
    private lateinit var dbHandler: DownloadDatabaseHandler
    private lateinit var connectionFactory: IDownloadConnection.Factory

    private val taskList: SparseArray<DownloadTask> = SparseArray()

    private val lock = Any()

    private val internalListener = object : DownloadListener {
        override fun onStart(task: IDownloadTask) {
            synchronized(taskList) {
                dbHandler.replace(DownloadTaskModel().apply {
                    id = task.id
                    url = task.url
                    targetPath = task.targetPath
                    targetName = task.targetName
                    status = DownloadStatus.START
                    current = 0
                    total = 0
                })
            }
            val listener = task.listener
            listener?.onStart(task)
        }

        override fun onUpdate(task: IDownloadTask, current: Long, total: Long) {
            val listener = task.listener
            dbHandler.updateProgress(task.id, current, total)
            listener?.onUpdate(task, current, total)
        }

        override fun onSucceed(task: IDownloadTask) {
            synchronized(taskList) {
                taskList.remove(task.id)
                LogUtils.i(TAG, Utils.formatString("remove a Task(id = %d), TaskList's size is %d now.", task.id, taskList.size()))
            }
            dbHandler.remove(task.id)
            val listener = task.listener
            listener?.onSucceed(task)
        }

        override fun onFailed(task: IDownloadTask, e: Exception) {
            synchronized(taskList) {
                taskList.remove(task.id)
                LogUtils.i(TAG, Utils.formatString("remove a Task(id = %d), TaskList's size is %d now.", task.id, taskList.size()))
            }
            dbHandler.updateFailed(task.id, e)
            val listener = task.listener
            listener?.onFailed(task, e)
        }
    }

    fun offline(url: String, targetPath: String, targetName: String, listener: DownloadListener? = null, outputStream: IOutputStream? = null): Int {
        // todo 添加强制重新下载的支持
        synchronized(taskList) {
            val id = IdGenerator.generateId(url, targetPath, targetName)
            var task: DownloadTask?
            task = taskList.get(id)
            if (task != null) {
                // 如果已经在执行或者正在等待执行，则重新绑定监听后直接返回
                LogUtils.d(TAG, Utils.formatString("task(id = %d) is executing now, so we won't inset it twice.", task.id))
                task.listener = listener
                return id
            }

            val model = dbHandler.find(id)
            if (model != null) {
                // 如果之前有下载记录，则恢复记录
                task = DownloadTask.Builder()
                        .url(model.url)
                        .targetPath(model.targetPath)
                        .targetName(model.targetName)
                        .offset(model.current)
                        .connection(connectionFactory.createConnection())
                        .outputStream(outputStream ?: DefaultOutputStream(model.current))
                        .build()
            }

            if (task == null) {
                // 如果内存与磁盘中均无记录，则新建一个任务
                task = DownloadTask.Builder()
                        .url(url)
                        .targetPath(targetPath)
                        .targetName(targetName)
                        .connection(connectionFactory.createConnection())
                        .outputStream(outputStream ?: DefaultOutputStream())
                        .build()
            }

            task.listener = listener
            task.bindInternalListener(internalListener)
            taskList.put(id, task)
            LogUtils.i(TAG, Utils.formatString("insert a new Task(id = %d), TaskList's size is %d now.", task.id, taskList.size()))

            executor.execute(task)
            return id
        }
    }

    fun cancel(id: Int) {
        synchronized(lock) {
            val task = taskList.get(id)
            if (task != null) {
                task.cancel()
                taskList.remove(id)
                dbHandler.remove(task.id)
                LogUtils.i(TAG, Utils.formatString("remove a Task(id = %d), TaskList's size is %d now.", task.id, taskList.size()))
            }
        }
    }

    fun pause(id: Int) {
        synchronized(lock) {
            val task = taskList.get(id)
            if (task != null) {
                task.pause()
                taskList.remove(id)
                LogUtils.i(TAG, Utils.formatString("remove a Task(id = %d), TaskList's size is %d now.", task.id, taskList.size()))
            }
        }
    }

    class Builder {

        private val manager: DownloadManager = DownloadManager()

        fun executor(executor: Executor) = apply {
            manager.executor = executor
        }

        fun dbHandler(dbHandler: DownloadDatabaseHandler) = apply {
            manager.dbHandler = dbHandler
        }

        fun connection(connectionFactory: IDownloadConnection.Factory) = apply {
            manager.connectionFactory = connectionFactory
        }

        fun build() = manager

    }

}
