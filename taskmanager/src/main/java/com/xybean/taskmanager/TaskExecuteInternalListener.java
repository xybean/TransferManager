package com.xybean.taskmanager;

/**
 * Author @xybean on 2018/3/22.
 */

interface TaskExecuteInternalListener<K, R> {
    void onStart(Task<K, R> task);

    void onSuccess(Task<K, R> task, R result);

    void onFailed(Task<K, R> task, Throwable throwable);

    void onCanceled(Task<K, R> task);
}
