package com.xybean.taskmanager;

/**
 * Author @xybean on 2018/3/21.
 */

public interface TaskExecuteListener<K, V> {

    void onWait(K key);

    void onStart(K key);

    void onSuccess(K key, V result);

    void onFailed(K key, Throwable throwable);

    void onCanceled(K key);

}
