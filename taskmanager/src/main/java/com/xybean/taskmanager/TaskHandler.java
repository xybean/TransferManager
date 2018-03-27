package com.xybean.taskmanager;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Author @xybean on 2018/3/21.
 */

final class TaskHandler<K, R> extends Handler {

    static final int MSG_SUBMIT = 0;
    static final int MSG_CANCEL = 1;
    private static final int MSG_UPDATE_START = 2;
    private static final int MSG_UPDATE_SUCCESS = 3;
    private static final int MSG_UPDATE_FAILED = 4;
    private static final int MSG_UPDATE_CANCELED = 5;

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT;

    private volatile boolean running;

    private final LinkedBlockingQueue<Task<K, R>> waiting = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Task<K, R>> executing = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Task<K, R>> finished = new LinkedBlockingQueue<>();

    private ExecutorService executorService;

    private TaskExecuteInternalListener<K, R> listener = new TaskExecuteInternalListener<K, R>() {

        @Override
        public void onStart(Task<K, R> task) {
            Message msg = TaskHandler.this.obtainMessage(TaskHandler.MSG_UPDATE_START, task);
            TaskHandler.this.sendMessage(msg);
        }

        @Override
        public void onSuccess(Task<K, R> task, R result) {
            Object[] objects = new Object[2];
            objects[0] = task;
            objects[1] = result;
            Message msg = TaskHandler.this.obtainMessage(TaskHandler.MSG_UPDATE_SUCCESS, objects);

            TaskHandler.this.sendMessage(msg);
        }

        @Override
        public void onFailed(Task<K, R> task, Throwable throwable) {
            Object[] objects = new Object[2];
            objects[0] = task;
            objects[1] = throwable;
            Message msg = TaskHandler.this.obtainMessage(TaskHandler.MSG_UPDATE_FAILED, objects);

            TaskHandler.this.sendMessage(msg);
        }

        @Override
        public void onCanceled(Task<K, R> task) {
            Message msg = TaskHandler.this.obtainMessage(TaskHandler.MSG_UPDATE_CANCELED, task);
            TaskHandler.this.sendMessage(msg);
        }
    };

    TaskHandler(Looper looper) {
        this(looper, CORE_POOL_SIZE);
    }

    TaskHandler(Looper looper, int poolSize) {
        super(looper);
        if (poolSize == 1) {
            executorService = Executors.newSingleThreadExecutor();
        } else {
            executorService = Executors.newFixedThreadPool(poolSize);
        }
    }

    Task<K, R> find(K key) {
        synchronized (waiting) {
            for (Task<K, R> task : waiting) {
                if (task.getKey().equals(key)) {
                    return task;
                }
            }
        }
        synchronized (executing) {
            for (Task<K, R> task : executing) {
                if (task.getKey().equals(key)) {
                    return task;
                }
            }
        }
        return null;
    }

    boolean isInWaiting(K key) {
        synchronized (waiting) {
            for (Task<K, R> task : waiting) {
                if (task.getKey().equals(key)) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean isInExecuting(K key) {
        synchronized (executing) {
            for (Task<K, R> task : waiting) {
                if (task.getKey().equals(key)) {
                    return true;
                }
            }
        }
        return false;
    }

    void start() {
        running = true;
    }

    void shutdown() {
        running = false;
        executorService.shutdown();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleMessage(Message msg) {
        if (!running) {
            return;
        }
        switch (msg.what) {
            case MSG_SUBMIT: {
                Task<K, R> task = (Task<K, R>) msg.obj;
                waiting.add(task);
                task.bindInternalListener(listener);
                task.onWait();
                executorService.execute(task);
                break;
            }
            case MSG_CANCEL: {
                K key = (K) msg.obj;
                // if task is in the waiting queue, remove it and call callback
                boolean removed = false;
                synchronized (waiting) {
                    for (Task<K, R> task : waiting) {
                        if (task.getKey().equals(key)) {
                            task.onCanceled();
                            removed = true;
                        }
                    }
                }
                // if task is in the executing queue, call task.cancel()
                if (!removed) {
                    synchronized (executing) {
                        for (Task<K, R> task : executing) {
                            if (task.getKey().equals(key)) {
                                task.cancel();
                            }
                        }
                    }
                }
                break;
            }
            case MSG_UPDATE_START: {
                Task<K, R> task = (Task<K, R>) msg.obj;
                waiting.remove(task);
                executing.add(task);
                if (task.listener != null) {
                    task.listener.onStart(task.getKey());
                }
                break;

            }
            case MSG_UPDATE_SUCCESS: {
                Task<K, R> task = (Task<K, R>) ((Object[]) msg.obj)[0];
                R result = (R) ((Object[]) msg.obj)[1];
                executing.remove(task);
                finished.add(task);
                if (task.listener != null) {
                    task.listener.onSuccess(task.getKey(), result);
                }
                break;
            }
            case MSG_UPDATE_FAILED: {
                Task<K, R> task = (Task<K, R>) ((Object[]) msg.obj)[0];
                Throwable throwable = (Throwable) ((Object[]) msg.obj)[1];
                executing.remove(task);
                finished.add(task);
                if (task.listener != null) {
                    task.listener.onFailed(task.getKey(), throwable);
                }
                break;
            }
            case MSG_UPDATE_CANCELED: {
                Task<K, R> task = (Task<K, R>) msg.obj;
                if (!waiting.remove(task)) {
                    executing.remove(task);
                }
                finished.add(task);
                if (task.listener != null) {
                    task.listener.onCanceled(task.getKey());
                }
                break;
            }
        }
    }

}
