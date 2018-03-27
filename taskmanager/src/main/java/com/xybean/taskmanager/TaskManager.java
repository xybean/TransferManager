package com.xybean.taskmanager;

import android.os.HandlerThread;
import android.os.Message;

/**
 * Author @xybean on 2018/3/22.
 */

public class TaskManager<K, R> {

    private TaskHandler<K, R> handler;
    private HandlerThread thread;
    private int poolSize = 0;

    public TaskManager(String name) {
        this(name, 0);
    }

    public TaskManager(String name, int poolSize) {
        thread = new HandlerThread(name);
        this.poolSize = poolSize;
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
        if (poolSize > 0) {
            handler = new TaskHandler<>(thread.getLooper(), poolSize);
        } else {
            handler = new TaskHandler<>(thread.getLooper());
        }
        handler.start();
    }

    public void shutdown() {
        handler.shutdown();
    }

}
