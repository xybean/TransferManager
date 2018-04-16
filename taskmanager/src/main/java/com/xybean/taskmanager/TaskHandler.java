package com.xybean.taskmanager;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Author @xybean on 2018/3/21.<br/>
 * handle tasks for executor.<br/>
 * To avoid client thread (UI thread) contending with executor.
 */

final class TaskHandler<K, R> extends Handler {

    static final int MSG_SUBMIT = 0;
    static final int MSG_CANCEL = 1;
    private static final int MSG_UPDATE_START = 2;
    private static final int MSG_UPDATE_SUCCESS = 3;
    private static final int MSG_UPDATE_FAILED = 4;
    private static final int MSG_UPDATE_CANCELED = 5;

    private volatile boolean running;

    // backup for tasks running in executor
    private final LinkedBlockingQueue<Task<K, R>> waiting = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Task<K, R>> executing = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Task<K, R>> failed = new LinkedBlockingQueue<>();

    private Executor executor;

    private TaskQueueListener queueListener;

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

    TaskHandler(Looper looper, Executor executor) {
        super(looper);
        this.executor = executor;
    }

    void setOnTaskQueueListener(TaskQueueListener queueListener) {
        this.queueListener = queueListener;
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

    int getWaitingQueueSize() {
        return waiting.size();
    }

    int getExecutingSize() {
        return executing.size();
    }

    int getFailedSize() {
        return failed.size();
    }

    void start() {
        running = true;
    }

    void shutdown() {
        running = false;
        if (executor instanceof ExecutorService) {
            ((ExecutorService) executor).shutdown();
        }
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
                int preSize = waiting.size();
                waiting.add(task);
                if (queueListener != null) {
                    queueListener.onWaitingQueueUpdate(preSize, preSize + 1);
                }
                task.bindInternalListener(listener);
                if (task.listener != null) {
                    task.listener.onWait(task.getKey());
                }
                executor.execute(task);
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
                int waitingPre = waiting.size();
                int executingPre = executing.size();
                waiting.remove(task);
                executing.add(task);
                if (queueListener != null) {
                    queueListener.onWaitingQueueUpdate(waitingPre, waitingPre - 1);
                    queueListener.onExecutingQueueUpdate(executingPre, executingPre + 1);
                }
                if (task.listener != null) {
                    task.listener.onStart(task.getKey());
                }
                break;

            }
            case MSG_UPDATE_SUCCESS: {
                Task<K, R> task = (Task<K, R>) ((Object[]) msg.obj)[0];
                R result = (R) ((Object[]) msg.obj)[1];
                int executingPre = executing.size();
                executing.remove(task);
                if (queueListener != null) {
                    queueListener.onExecutingQueueUpdate(executingPre, executingPre - 1);
                }
                if (task.listener != null) {
                    task.listener.onSuccess(task.getKey(), result);
                }
                break;
            }
            case MSG_UPDATE_FAILED: {
                Task<K, R> task = (Task<K, R>) ((Object[]) msg.obj)[0];
                Throwable throwable = (Throwable) ((Object[]) msg.obj)[1];
                int executingPre = executing.size();
                int failedPre = failed.size();
                executing.remove(task);
                failed.add(task);
                if (queueListener != null) {
                    queueListener.onExecutingQueueUpdate(executingPre, executingPre - 1);
                    queueListener.onFailedQueueUpdate(failedPre, failedPre + 1);
                }
                if (task.listener != null) {
                    task.listener.onFailed(task.getKey(), throwable);
                }
                break;
            }
            case MSG_UPDATE_CANCELED: {
                Task<K, R> task = (Task<K, R>) msg.obj;
                int waitingPre = waiting.size();
                int executingPre = executing.size();
                boolean removedFromWaiting = waiting.remove(task);
                boolean removedFromExecuting = false;
                if (!removedFromWaiting) {
                    removedFromExecuting = executing.remove(task);
                }

                if (removedFromWaiting) {
                    if (queueListener != null) {
                        queueListener.onWaitingQueueUpdate(waitingPre, waitingPre - 1);
                    }
                }
                if (removedFromExecuting) {
                    if (queueListener != null) {
                        queueListener.onExecutingQueueUpdate(executingPre, executingPre - 1);
                    }
                }
                if (task.listener != null) {
                    task.listener.onCanceled(task.getKey());
                }
                break;
            }
        }
    }

}
