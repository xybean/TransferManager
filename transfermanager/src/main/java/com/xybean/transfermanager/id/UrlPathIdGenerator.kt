package com.xybean.transfermanager.id

/**
 * Author @xybean on 2018/7/24.
 */
class UrlPathIdGenerator(private val url: String, private val absolutePath: String) : IdGenerator {

    companion object {
        fun getId(url: String, absolutePath: String): Int {
            return IdGenerator.md5("$url#$absolutePath").hashCode()
        }
    }

    override fun getId(): Int {
        return IdGenerator.md5("$url#$absolutePath").hashCode()
    }
}