package com.xybean.taskmanager;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Author @xybean on 2018/3/28.<br/>
 * If you want to use your own thread pool,<br/>
 * just implement this interface.
 */

public interface IExecutorFactory {

    Executor getExecutorService();

    /**
     * Singleton, usually used for CPU-bound tasks.<br/>
     */
    class DefaultComputationExecutor implements IExecutorFactory {

        public static final DefaultComputationExecutor INSTANCE = new DefaultComputationExecutor();

        private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
        private static final int CORE_POOL_SIZE = CPU_COUNT;

        private static final ExecutorService FIXED_EXECUTOR = Executors.newFixedThreadPool(CORE_POOL_SIZE);

        private DefaultComputationExecutor() {
        }

        @Override
        public Executor getExecutorService() {
            return FIXED_EXECUTOR;
        }
    }

    /**
     * Used for IO-bound tasks. <br/>
     * All of tasks will run in this pool by default, <br/>
     * even if tasks were put by different TaskManagers.
     */
    class DefaultIOExecutor implements IExecutorFactory {

        public static final DefaultComputationExecutor INSTANCE = new DefaultComputationExecutor();

        private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
        private static final int CORE_POOL_SIZE = CPU_COUNT * 2;

        private static final ExecutorService CACHED_EXECUTOR = Executors.newFixedThreadPool(CORE_POOL_SIZE);

        @Override
        public Executor getExecutorService() {
            return CACHED_EXECUTOR;
        }
    }

    /**
     * Tasks will be executed one by one.<br/>
     * Tasks is scheduled by FIFO.
     */
    class DefaultSingleExecutor implements IExecutorFactory {

        public static final DefaultSingleExecutor INSTANCE = new DefaultSingleExecutor();

        private static final ExecutorService SINGLE_EXECUTOR = Executors.newSingleThreadExecutor();

        private DefaultSingleExecutor() {
        }

        @Override
        public Executor getExecutorService() {
            return SINGLE_EXECUTOR;
        }
    }

}
