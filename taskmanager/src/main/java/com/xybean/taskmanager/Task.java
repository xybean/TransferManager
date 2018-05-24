package com.xybean.taskmanager;

import android.support.annotation.CallSuper;

/**
 * Author @xybean on 2018/3/21.<br/>
 *
 * @param <K> key for Task, same as identify
 * @param <R> return value for task
 */

public abstract class Task<K, R> implements Runnable {

    TaskExecuteListener<K, R> listener;

    private TaskExecuteInternalListener<K, R> internalListener;

    private static final int WAITING = 0;
    private static final int EXECUTING = 1;
    private static final int COMPLETED = 2;
    private static final int FAILED = 3;

    private volatile int state = WAITING;

    private volatile boolean canceled;

    private K key;

    public Task(K key) {
        this.key = key;
    }

    public void setExecuteListener(TaskExecuteListener<K, R> listener) {
        this.listener = listener;
    }

    /**
     * implement execute() instead of run()
     */
    @Override
    public final void run() {
        if (canceled) {
            return;
        }
        state = EXECUTING;
        internalListener.onStart(this);
        try {
            R r = execute();
            if (isCanceled()) {
                return;
            }
            state = COMPLETED;
            // can't be canceled anymore, so reset the value
            canceled = false;
            internalListener.onSuccess(this, r);
        } catch (InterruptedException e) {
            state = FAILED;
            // reset
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            state = FAILED;
            internalListener.onFailed(this, e);
        }
    }

    /**
     * implementation for executing logic
     *
     * @return result of task
     * @throws Exception the exception will be caught by {@link Task#run()}
     */
    protected abstract R execute() throws Exception;

    public K getKey() {
        return key;
    }

    public boolean isWaiting() {
        return state == WAITING;
    }

    public boolean isExecuting() {
        return state == EXECUTING;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public boolean isFailed() {
        return state == FAILED;
    }

    public boolean isCompleted() {
        return state == COMPLETED;
    }

    /**
     * override this method to implement logic for canceling task if necessary,<br/>
     * always using with isCanceled()
     */
    @CallSuper
    public void cancel() {
        canceled = true;
    }

    void bindInternalListener(TaskExecuteInternalListener<K, R> internalListener) {
        this.internalListener = internalListener;
    }

}
