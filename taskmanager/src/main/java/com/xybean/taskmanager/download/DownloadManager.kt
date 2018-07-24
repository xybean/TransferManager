package com.xybean.taskmanager.download

import android.util.SparseArray
import com.xybean.taskmanager.download.connection.IDownloadConnection
import com.xybean.taskmanager.download.db.DownloadDatabaseHandler
import com.xybean.taskmanager.download.db.DownloadTaskModel
import com.xybean.taskmanager.download.stream.IDownloadStream
import com.xybean.taskmanager.download.task.DownloadInternalListener
import com.xybean.taskmanager.download.task.DownloadStatus
import com.xybean.taskmanager.download.task.DownloadTask
import com.xybean.taskmanager.download.task.IDownloadTask
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

    private val dbExecutor = TaskExecutor("DownloadManagerDbExecutor", 1)

    private lateinit var executor: Executor
    private lateinit var dbHandler: DownloadDatabaseHandler
    private lateinit var connectionFactory: IDownloadConnection.Factory
    private lateinit var streamFactory: IDownloadStream.Factory

    private val taskList: SparseArray<DownloadTask> = SparseArray()

    private val internalListener = object : DownloadInternalListener {
        override fun onStart(task: IDownloadTask) {
            dbExecutor.execute(object : BaseTask<Void?>() {
                override fun execute(): Void? {
                    dbHandler.replace(DownloadTaskModel().apply {
                        id = task.getId()
                        url = task.getUrl()
                        targetPath = task.getTargetPath()
                        targetName = task.getTargetName()
                        status = DownloadStatus.START
                        current = 0
                        total = 0
                    })
                    return null
                }
            })
            val listener = task.getListener()
            listener?.onStart(task)
        }

        override fun onUpdate(task: IDownloadTask, sync: Boolean) {
            val listener = task.getListener()
            if (sync) {
                dbExecutor.execute(object : BaseTask<Void?>() {
                    override fun execute(): Void? {
                        dbHandler.updateProgress(task.getId(), task.getCurrent(), task.getTotal())
                        return null
                    }
                })
            }
            listener?.onUpdate(task)
        }

        override fun onSucceed(task: IDownloadTask) {
            synchronized(taskList) {
                taskList.remove(task.getId())
                LogUtils.i(TAG, "download succeed and remove a Task(id = ${task.getId()})," +
                        " TaskList's size is ${taskList.size()} now.")
            }
            dbExecutor.execute(object : BaseTask<Void?>() {
                override fun execute(): Void? {
                    dbHandler.remove(task.getId())
                    return null
                }
            })
            val listener = task.getListener()
            listener?.onSucceed(task)
        }

        override fun onFailed(task: IDownloadTask, e: Exception) {
            synchronized(taskList) {
                taskList.remove(task.getId())
                LogUtils.i(TAG, "download failed and remove a Task(id = ${task.getId()})," +
                        " TaskList's size is ${taskList.size()} now.")
            }
            dbExecutor.execute(object : BaseTask<Void?>() {
                override fun execute(): Void? {
                    dbHandler.updateFailed(task.getId(), e)
                    return null
                }
            })
            val listener = task.getListener()
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
     */
    fun download(url: String, targetPath: String, targetName: String, forceReload: Boolean, listener: DownloadListener? = null): Int {
        val id = IdGenerator.generateId(url, targetPath, targetName)
        synchronized(taskList) {
            val cachedTask: DownloadTask? = taskList.get(id)
            if (cachedTask != null) {
                // 如果已经在执行或者正在等待执行，则重新绑定监听后直接返回
                LogUtils.d(TAG, "task(id = ${cachedTask.getId()}) is executing now, so we won't start it twice.")
                if (listener != null) {
                    cachedTask.setListener(listener)
                }
                return id
            }
        }

        // 如果内存中不存在正在执行的任务，则尝试从磁盘中读取
        dbExecutor.execute(object : BaseTask<Void?>() {
            override fun execute(): Void? {
                synchronized(taskList) {
                    if (forceReload) {
                        dbHandler.remove(id)
                    }
                    var task: DownloadTask? = null
                    val model = dbHandler.find(id)
                    if (model != null) {
                        // 如果之前有下载记录，则恢复记录
                        val header = Utils.getRangeHeader(model.current)
                        task = DownloadTask.Builder()
                                .url(model.url)
                                .targetPath(model.targetPath)
                                .targetName(model.targetName)
                                .offset(model.current)
                                .addHeader(header.first, header.second)
                                .connection(connectionFactory)
                                .outputStream(streamFactory)
                                .build()
                    }

                    if (task == null) {
                        // 如果内存与磁盘中均无记录，则新建一个任务
                        task = DownloadTask.Builder()
                                .url(url)
                                .targetPath(targetPath)
                                .targetName(targetName)
                                .connection(connectionFactory)
                                .outputStream(streamFactory)
                                .build()
                    }

                    if (listener != null) {
                        task.setListener(listener)
                    }
                    task.bindInternalListener(internalListener)
                    taskList.put(id, task)
                    LogUtils.i(TAG, "insert a new Task(id = ${task.getId()}), TaskList's size is ${taskList.size()} now.")

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
                LogUtils.i(TAG, "cancel and remove a Task(id = ${task.getId()}), TaskList's size is ${taskList.size()} now.")
            }
            dbExecutor.execute(object : BaseTask<Void?>() {
                override fun execute(): Void? {
                    dbHandler.remove(task.getId())
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
                LogUtils.i(TAG, "pause and remove a Task(id = ${task.getId()}), " +
                        "TaskList's size is ${taskList.size()} now.")
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

        fun stream(streamFactory: IDownloadStream.Factory) = apply {
            manager.streamFactory = streamFactory
        }

        fun debug(debug: Boolean) = apply {
            LogUtils.DEBUG = debug
        }

        fun build() = manager

    }

}
