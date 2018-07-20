package com.xybean.taskmanager.download

import android.util.SparseArray
import com.xybean.taskmanager.download.connection.IDownloadConnection
import com.xybean.taskmanager.download.db.DownloadDatabaseHandler
import com.xybean.taskmanager.download.db.DownloadTaskModel
import com.xybean.taskmanager.download.stream.DefaultOutputStream
import com.xybean.taskmanager.download.stream.IOutputStream
import com.xybean.taskmanager.executor.BaseTask
import com.xybean.taskmanager.executor.TaskExecutor
import java.util.concurrent.Executor

/**
 * Author @xybean on 2018/7/16.
 */
class DownloadManager private constructor() {

    companion object {
        const val TAG = "DownloadManager"
    }

    private val internalExecutor = TaskExecutor("DownloadManagerInternal", 1)

    private lateinit var executor: Executor
    private lateinit var dbHandler: DownloadDatabaseHandler
    private lateinit var connectionFactory: IDownloadConnection.Factory

    private val taskList: SparseArray<DownloadTask> = SparseArray()

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

    /**
     * 开始离线任务。
     * @param url 目标url
     * @param targetPath 下载文件后的存储目录
     * @param targetName 下载文件的文件名
     * @param forceReload 是否强制重新下载，如果为true，则不会进行断点续传，而是强制重新下载
     * @param listener
     * @param outputStream 目标文件的写入流
     */
    fun download(url: String, targetPath: String, targetName: String, forceReload: Boolean,
                 listener: DownloadListener? = null, outputStream: IOutputStream? = null): Int {
        val id = IdGenerator.generateId(url, targetPath, targetName)
        synchronized(taskList) {
            val cachedTask: DownloadTask? = taskList.get(id)
            if (cachedTask != null) {
                // 如果已经在执行或者正在等待执行，则重新绑定监听后直接返回
                LogUtils.d(TAG, Utils.formatString("task(id = %d) is executing now, so we won't start it twice.", cachedTask.id))
                cachedTask.listener = listener
                return id
            }
        }

        // 如果内存中不存在正在执行的任务，则尝试从磁盘中读取
        internalExecutor.execute(object : BaseTask<Void?>() {
            override fun execute(): Void? {
                synchronized(taskList) {
                    if (forceReload) {
                        dbHandler.remove(id)
                    }
                    var task: DownloadTask? = null
                    val model = dbHandler.find(id)
                    if (model != null) {
                        // 如果之前有下载记录，则恢复记录
                        val header = Utils.getRangeHeader(model.current);
                        task = DownloadTask.Builder()
                                .url(model.url)
                                .targetPath(model.targetPath)
                                .targetName(model.targetName)
                                .offset(model.current)
                                .addHeader(header.first, header.second)
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
                    return null
                }
            }
        })

        return id
    }


    /**
     * 取消任务，同时将任务从内存中与缓存中删除
     * @param id 任务的id值
     */
    fun cancel(id: Int) {
        synchronized(taskList) {
            val task = taskList.get(id)
            if (task != null) {
                task.cancel()
                taskList.remove(id)
                LogUtils.i(TAG, Utils.formatString("remove a Task(id = %d), TaskList's size is %d now.", task.id, taskList.size()))
            }
            internalExecutor.execute(object : BaseTask<Void?>() {
                override fun execute(): Void? {
                    dbHandler.remove(task.id)
                    return null
                }
            })
        }
    }

    /**
     * 暂停任务，同时将任务从内存中删除
     * @param id 任务的id值
     */
    fun pause(id: Int) {
        synchronized(taskList) {
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
