package com.xybean.transfermanager.exception

import java.util.*

/**
 * Author @xybean on 2018/7/16.
 */
class NoEnoughSpaceException(expected: Long, actual: Long)
    : Exception(String.format(Locale.getDefault(), "excepted %d byte, but actually has only %d byte.", expected, actual))
