package com.xybean.transfermanager.id

import com.xybean.transfermanager.Utils

/**
 * Author @xybean on 2018/7/24.
 */
class UrlPathIdGenerator(private val url: String, private val absolutePath: String) : IdGenerator {

    companion object {
        fun getId(url: String, absolutePath: String): Int {
            return Utils.md5("$url#$absolutePath").hashCode()
        }
    }

    override fun getId(): Int {
        return Utils.md5("$url#$absolutePath").hashCode()
    }
}