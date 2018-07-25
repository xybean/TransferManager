/*
 * Copyright (c) 2015 LingoChamp Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xybean.transfermanager.download.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build


class SqliteDatabaseOpenHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setWriteAheadLoggingEnabled(true)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            db.enableWriteAheadLogging()
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS "
                + DownloadTaskModel.TABLE_NAME + "( "
                + DownloadTaskModel.ID + " INTEGER PRIMARY KEY, "  // id
                + DownloadTaskModel.URL + " VARCHAR, "  // url
                + DownloadTaskModel.PATH + " VARCHAR, "  // 下载目标目录
                + DownloadTaskModel.NAME + " VARCHAR, " // 下载后存储的文件名
                + DownloadTaskModel.STATUS + " TINYINT(7), "  // status
                + DownloadTaskModel.CURRENT + " INTEGER, " // 当前进度
                + DownloadTaskModel.TOTAL + " INTEGER " // 总进度
                + ")")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.delete(DownloadTaskModel.TABLE_NAME, null, null)
    }

    companion object {
        private const val DATABASE_NAME = "downloadmanager.db"
        private const val DATABASE_VERSION = 1
    }
}
