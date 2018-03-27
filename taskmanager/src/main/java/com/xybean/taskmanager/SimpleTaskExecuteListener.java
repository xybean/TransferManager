package com.xybean.taskmanager;

/**
 * Author @xybean on 2018/3/22.
 */

public class SimpleTaskExecuteListener<K, R> implements TaskExecuteListener<K, R> {

    @Override
    public void onWait(K key) {

    }

    @Override
    public void onStart(K key) {

    }

    @Override
    public void onSuccess(K key, R result) {

    }

    @Override
    public void onFailed(K key, Throwable throwable) {

    }

    @Override
    public void onCanceled(K key) {

    }
}
