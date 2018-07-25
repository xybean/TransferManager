package com.xybean.transfermanager.executor

import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * Author @xybean on 2018/7/19.
 */
class TaskExecutor(name: String, poolSize: Int) {

    private val factory = Factory(name)

    private val executor: Executor = if (poolSize <= 1) {
        Executors.newSingleThreadExecutor(factory)
    } else {
        Executors.newFixedThreadPool(poolSize, factory)
    }

    fun execute(task: BaseTask<*>) {
        executor.execute(task)
    }

    private class Factory(val prefix: String) : ThreadFactory {

        private val threadNumber = AtomicInteger(1)

        override fun newThread(r: Runnable?): Thread {
            val group = Thread.currentThread().threadGroup
            return Thread(group, r, prefix + "#" + threadNumber.getAndIncrement(), 0)
        }
    }

}