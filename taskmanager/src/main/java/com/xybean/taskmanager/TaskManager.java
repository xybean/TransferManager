package com.xybean.taskmanager;

import android.os.HandlerThread;
import android.os.Message;

/**
 * Author @xybean on 2018/3/22.
 */

public class TaskManager<K, R> {

    private TaskHandler<K, R> handler;
    private HandlerThread thread;

    private IExecutorFactory executorFactory;

    public TaskManager(String name) {
        this(name, new IExecutorFactory.DefaultFixedExecutor());
    }

    public TaskManager(String name, IExecutorFactory factory) {
        thread = new HandlerThread(name);
        executorFactory = factory;
    }

    public void execute(Task<K, R> task) {
        if (handler == null) {
            throw new IllegalStateException("you should call TaskManager.start() first");
        }
        Message msg = handler.obtainMessage(TaskHandler.MSG_SUBMIT, task);
        handler.sendMessage(msg);
    }

    public void cancel(K key) {
        if (handler == null) {
            throw new IllegalStateException("you should call TaskManager.start() first");
        }
        Message msg = handler.obtainMessage(TaskHandler.MSG_CANCEL, key);
        handler.sendMessage(msg);
    }

    public Task<K, R> find(K key) {
        return handler.find(key);
    }

    public boolean isInWaiting(K key) {
        return handler.isInWaiting(key);
    }

    public boolean isInExecuting(K key) {
        return handler.isInExecuting(key);
    }

    public void start() {
        thread.start();
        handler = new TaskHandler<>(thread.getLooper(), executorFactory.getExecutorService());
        handler.start();
    }

    public void shutdown() {
        handler.shutdown();
    }

}
