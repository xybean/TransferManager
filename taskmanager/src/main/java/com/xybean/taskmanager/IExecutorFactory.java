package com.xybean.taskmanager;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Author @xybean on 2018/3/28.
 */

public interface IExecutorFactory {

    Executor getExecutorService();

    class DefaultFixedExecutor implements IExecutorFactory {

        private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
        private static final int CORE_POOL_SIZE = CPU_COUNT;

        static final ExecutorService FIXED_EXECUTOR = Executors.newFixedThreadPool(CORE_POOL_SIZE);

        @Override
        public Executor getExecutorService() {
            return FIXED_EXECUTOR;
        }
    }

    class DefaultSingleExecutor implements IExecutorFactory {

        static final ExecutorService SINGLE_EXECUTOR = Executors.newSingleThreadExecutor();

        @Override
        public Executor getExecutorService() {
            return SINGLE_EXECUTOR;
        }
    }

}
