package com.xybean.transfermanager.download.cache

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.xybean.transfermanager.TransferManager
import com.xybean.transfermanager.download.task.DownloadStatus

/**
 * Author @xybean on 2018/7/16.
 */
class SqliteCacheHandler : DownloadCacheHandler {

    private val db: SQLiteDatabase

    private val lock = Any()

    init {
        val openHelper = SqliteDatabaseOpenHelper(
                TransferManager.getAppContext())
        db = openHelper.writableDatabase
    }

    override fun find(id: Int): DownloadTaskModel? {
        synchronized(lock) {
            var c: Cursor? = null
            try {
                c = db.rawQuery("SELECT * FROM ${DownloadTaskModel.TABLE_NAME} WHERE ${DownloadTaskModel.ID} = ?",
                        arrayOf(Integer.toString(id)))
                if (c!!.moveToNext()) {
                    return createFromCursor(c)
                }
            } finally {
                c?.close()
            }
            return null
        }
    }

    override fun replace(model: DownloadTaskModel) {
        synchronized(lock) {
            db.replace(DownloadTaskModel.TABLE_NAME, null, model.toContentValues())
        }
    }

    override fun updateProgress(id: Int, current: Long, total: Long) {
        synchronized(lock) {
            val cv = ContentValues()
            cv.put(DownloadTaskModel.STATUS, DownloadStatus.UPDATE)
            cv.put(DownloadTaskModel.CURRENT, current)
            cv.put(DownloadTaskModel.TOTAL, total)
            update(id, cv)
        }
    }

    override fun updatePaused(id: Int, current: Long, total: Long) {
        synchronized(lock) {
            val cv = ContentValues()
            cv.put(DownloadTaskModel.STATUS, DownloadStatus.PAUSED)
            cv.put(DownloadTaskModel.CURRENT, current)
            cv.put(DownloadTaskModel.TOTAL, total)
            update(id, cv)
        }
    }

    override fun updateFailed(id: Int, e: Exception) {
        synchronized(lock) {
            val cv = ContentValues()
            cv.put(DownloadTaskModel.STATUS, DownloadStatus.FAILED)
            update(id, cv)
        }
    }

    override fun remove(id: Int): Boolean {
        synchronized(lock) {
            return db.delete(DownloadTaskModel.TABLE_NAME, DownloadTaskModel.ID + " = ?", arrayOf(id.toString())) != 0
        }
    }

    override fun clear() {
        synchronized(lock) {
            db.delete(DownloadTaskModel.TABLE_NAME, null, null)
        }
    }

    private fun update(id: Int, cv: ContentValues) {
        db.update(DownloadTaskModel.TABLE_NAME, cv, DownloadTaskModel.ID + " = ? ", arrayOf(id.toString()))
    }

    private fun createFromCursor(c: Cursor): DownloadTaskModel {
        val model = DownloadTaskModel()
        model.id = c.getInt(c.getColumnIndex(DownloadTaskModel.ID))
        model.url = c.getString(c.getColumnIndex(DownloadTaskModel.URL))
        model.targetPath = c.getString(c.getColumnIndex(DownloadTaskModel.PATH))
        model.targetName = c.getString(c.getColumnIndex(DownloadTaskModel.NAME))
        model.status = c.getInt(c.getColumnIndex(DownloadTaskModel.STATUS))
        model.current = c.getLong(c.getColumnIndex(DownloadTaskModel.CURRENT))
        model.total = c.getLong(c.getColumnIndex(DownloadTaskModel.TOTAL))
        return model
    }
}
