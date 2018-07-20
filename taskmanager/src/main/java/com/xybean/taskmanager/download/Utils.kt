package com.xybean.taskmanager.download

import android.os.Environment
import android.os.StatFs
import java.io.File

/**
 * Author @xybean on 2018/7/16.
 */
internal object Utils {
    /**
     * 获取磁盘的剩余空间
     *
     * @return
     */
    // 获得一个磁盘状态对象
    // 获得一个扇区的大小
    // 获得扇区的总数
    val sdCardFreeSpace: Long
        get() {
            val sdcardFileDir = Environment.getExternalStorageDirectory()
            val stat = StatFs(sdcardFileDir.path)
            val blockSize = stat.blockSizeLong
            stat.blockCountLong
            return stat.availableBlocksLong * blockSize
        }

    /**
     * 删除文件
     *
     * @param file
     */
    fun deleteFile(file: File) {
        if (file.exists()) {
            val to = File(file.absolutePath + System.currentTimeMillis())
            if (file.renameTo(to)) {
                recursionDeleteFile(to)
            }
        }
    }

    /**
     * 删除目录
     *
     * @param file
     */
    private fun recursionDeleteFile(file: File) {
        if (file.isFile) {
            file.delete()
        } else if (file.isDirectory) {
            val childFile = file.listFiles()
            if (childFile == null || childFile.isEmpty()) {
                file.delete()
            } else {
                for (f in childFile) {
                    recursionDeleteFile(f)
                }
                file.delete()
            }
        }
    }

    /**
     * 重命名文件夹
     *
     * @param origin
     * @param dest
     */
    fun renameFile(origin: String, dest: String): Boolean {
        try {
            val originFile = File(origin)
            val destFile = File(dest)
            if (originFile.exists() && !destFile.exists()) {
                return originFile.renameTo(destFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    fun getRangeHeader(offset: Long, total: Long = -1): Pair<String, String> {
        val range: String
        return if (total > 0 && total > offset) {
            range = "bytes=$offset-$total"
            Pair("Range", range)
        } else {
            range = "bytes=$offset-"
            Pair("Range", range)
        }
    }
}
