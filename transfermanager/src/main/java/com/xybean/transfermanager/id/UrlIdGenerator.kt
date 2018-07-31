package com.xybean.transfermanager.id

import com.xybean.transfermanager.Utils

/**
 * Author @xybean on 2018/7/27.
 */
class UrlIdGenerator(private val url: String) : IdGenerator {

    companion object {
        fun getId(url: String): Int {
            return Utils.md5(url).hashCode()
        }
    }

    override fun getId(): Int {
        return Utils.md5(url).hashCode()
    }
}