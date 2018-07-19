package com.xybean.taskmanagertest.test;

import com.xybean.taskmanager.IExecutorFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Author @xybean on 2018/3/28.
 */

public class CustomExecutorFactory implements IExecutorFactory {

    private Executor executor;

    public CustomExecutorFactory(int size) {
        executor = Executors.newFixedThreadPool(size);
    }

    @Override
    public Executor getExecutorService() {
        return executor;
    }

}
