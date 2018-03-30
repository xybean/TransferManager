package com.xybean.taskmanager;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Author @xybean on 2018/3/28.<br/>
 * If you want to using your own thread pool,<br/>
 * just implement this interface.
 */

public interface IExecutorFactory {

    Executor getExecutorService();

    /**
     * Singleton.<br/>
     * All of tasks will run in this pool by default, <br/>
     * even if tasks were put by different TaskManagers.
     */
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
