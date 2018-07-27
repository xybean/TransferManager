package com.xybean.transfermanager

import android.os.Environment
import android.os.StatFs
import java.io.File
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.experimental.and

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

    @Throws(IOException::class)
    fun checkAndCreateFile(file: File) {
        val parentFile = file.parentFile
        if (!parentFile.exists()) {
            parentFile.mkdirs()
        }
        if (!file.exists()) {
            file.createNewFile()
        }
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

    fun md5(string: String): String {
        val hash: ByteArray
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.toByteArray(charset("UTF-8")))
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Huh, MD5 should be supported?", e)
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException("Huh, UTF-8 should be supported?", e)
        }

        val hex = StringBuilder(hash.size * 2)
        for (b in hash) {
            if (b and 0xFF.toByte() < 0x10) hex.append("0")
            hex.append(Integer.toHexString((b and 0xFF.toByte()).toInt()))
        }
        return hex.toString()
    }
}
