package com.xybean.transfermanager.monitor

import java.util.*
import kotlin.collections.ArrayList

/**
 * Author @xybean on 2018/8/13.
 */
internal class CirclerQueue<E>(private val size: Int, val default: E) {

    private var head: Int = 0

    private val elements = Array(size) {
        default as Any
    }

    @Synchronized
    fun put(value: E) {
        elements[head] = value as Any
        calHead()
    }

    @Synchronized
    fun toList(): List<E> {
        val array = Arrays.copyOf(elements, size)
        val list = ArrayList<E>()

        var cursor = head
        do {
            list.add(array[cursor] as E)
            if (cursor >= size - 1) {
                cursor = 0
            } else {
                cursor++
            }
        } while (cursor != head)

        return list.reversed()
    }

    private fun calHead() {
        if (head >= size - 1) {
            head = 0
        } else {
            head++
        }
    }

}