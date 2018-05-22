package com.xybean.taskmanager;

import android.os.HandlerThread;
import android.os.Message;

/**
 * Author @xybean on 2018/3/22.<br/>
 * Interface for client.<br/>
 * you could also extends this class and add logic for managing task
 */

public class TaskManager<K, R> {

    private TaskHandler<K, R> handler;
    private HandlerThread thread;

    private IExecutorFactory executorFactory;

    public TaskManager(String name) {
        this(name, IExecutorFactory.DefaultIOExecutor.INSTANCE);
    }

    public TaskManager(String name, IExecutorFactory factory) {
        thread = new HandlerThread(name);
        executorFactory = factory;
        thread.start();
        handler = new TaskHandler<>(thread.getLooper(), executorFactory.getExecutorService());
        handler.start();
    }

    public void execute(Task<K, R> task) {
        Message msg = handler.obtainMessage(TaskHandler.MSG_SUBMIT, task);
        handler.sendMessage(msg);
    }

    public void cancel(K key) {
        Message msg = handler.obtainMessage(TaskHandler.MSG_CANCEL, key);
        handler.sendMessage(msg);
    }

    public void cancelAll() {
        Message msg = handler.obtainMessage(TaskHandler.MSG_CANCEL_ALL);
        handler.sendMessage(msg);
    }

    public void setOnTaskQueueListener(TaskQueueListener listener) {
        handler.setOnTaskQueueListener(listener);
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

    public int getWaitingSize() {
        return handler.getWaitingQueueSize();
    }

    public int getExecutingSize() {
        return handler.getExecutingSize();
    }

    public int getFailedSize() {
        return handler.getFailedSize();
    }

    public void shutdown() {
        handler.shutdown();
    }

}
