package com.xybean.transfermanager.id

/**
 * Author @xybean on 2018/7/27.
 */
class UrlIdGenerator(private val url: String) : IdGenerator {

    companion object {
        fun getId(url: String): Int {
            return IdGenerator.md5(url).hashCode()
        }
    }

    override fun getId(): Int {
        return IdGenerator.md5(url).hashCode()
    }
}