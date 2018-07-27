package com.xybean.transfermanager.download.id

import com.xybean.transfermanager.IdGenerator
import com.xybean.transfermanager.Utils

/**
 * Author @xybean on 2018/7/24.
 */
class DefaultIdGenerator(private val url: String, private val targetPath: String,
                         private val targetName: String) : IdGenerator {

    companion object {
        fun getId(url: String, targetPath: String, targetName: String): Int {
            return Utils.md5("$url}#$targetPath/$targetName").hashCode()
        }
    }

    override fun getId(): Int {
        return Utils.md5("$url}#$targetPath/$targetName").hashCode()
    }
}