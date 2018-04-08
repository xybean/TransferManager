package com.xybean.taskmanager;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Author @xybean on 2018/3/27.<br/>
 * wrapper for FutureTask
 */

public abstract class FutureCallTask<K, R> extends Task<K, R> implements Callable<R> {

    private FutureTask<R> internalTask;

    public FutureCallTask(K key) {
        super(key);
        internalTask = new FutureTask<R>(this) {
            @Override
            protected void done() {
                FutureCallTask.this.done();
            }
        };
    }

    @Override
    protected final R execute() throws Exception {

        internalTask.run();

        return internalTask.get();
    }

    @Override
    public void cancel() {
        super.cancel();
        internalTask.cancel(false);
    }

    public void cancel(boolean mayInterruptIfRunning) {
        super.cancel();
        internalTask.cancel(mayInterruptIfRunning);
    }

    public boolean isCanceled() {
        return super.isCanceled() || internalTask.isCancelled();
    }

    public boolean isDone() {
        return internalTask.isDone();
    }

    public R get() throws InterruptedException, ExecutionException {
        return internalTask.get();
    }

    public R get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return internalTask.get(timeout, unit);
    }

    protected void done() {
    }

}
