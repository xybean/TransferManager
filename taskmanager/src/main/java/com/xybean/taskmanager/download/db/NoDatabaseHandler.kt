package com.xybean.taskmanager.download.db

/**
 * Author @xybean on 2018/7/16.<br></br>
 * 空实现，表示并不进行持久化处理
 */
class NoDatabaseHandler : DownloadDatabaseHandler {
    override fun find(id: Int): DownloadTaskModel? {
        return null
    }

    override fun insert(model: DownloadTaskModel) {}

    override fun updateProgress(id: Int, current: Long, total: Long) {}

    override fun updatePaused(id: Int, current: Long, total: Long) {}

    override fun updateFailed(id: Int, e: Exception) {}

    override fun remove(id: Int): Boolean = true

    override fun clear() {}
}
