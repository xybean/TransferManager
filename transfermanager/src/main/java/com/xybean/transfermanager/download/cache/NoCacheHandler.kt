package com.xybean.transfermanager.download.cache

/**
 * Author @xybean on 2018/7/16.<br></br>
 * 空实现，表示并不进行持久化处理
 */
class NoCacheHandler : DownloadCacheHandler {
    override fun find(id: Int): DownloadTaskModel? {
        return null
    }

    override fun replace(model: DownloadTaskModel) {}

    override fun updateProgress(id: Int, current: Long, total: Long) {}

    override fun updatePaused(id: Int, current: Long, total: Long) {}

    override fun updateFailed(id: Int, e: Exception) {}

    override fun remove(id: Int): Boolean = true

    override fun clear() {}
}
