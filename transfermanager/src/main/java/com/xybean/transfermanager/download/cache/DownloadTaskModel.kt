package com.xybean.transfermanager.download.cache

import android.content.ContentValues

/**
 * Author @xybean on 2018/7/16.
 */
class DownloadTaskModel {

    var id: Int = 0

    var url: String = ""

    var targetPath: String = ""

    var targetName: String = ""

    var status: Int = 0

    var current: Long = 0

    var total: Long = 0

    companion object {
        const val TABLE_NAME = "DownloadTaskTable"
        const val ID = "_id"
        const val URL = "url"
        const val PATH = "path"
        const val NAME = "name"
        const val STATUS = "status"
        const val CURRENT = "current"
        const val TOTAL = "total"
    }

    fun toContentValues(): ContentValues {
        val cv = ContentValues()
        cv.put(ID, id)
        cv.put(URL, url)
        cv.put(PATH, targetPath)
        cv.put(NAME, targetName)
        cv.put(STATUS, status)
        cv.put(CURRENT, current)
        cv.put(TOTAL, total)
        return cv
    }
}
