package com.xybean.taskmanager;

/**
 * Author @xybean on 2018/3/26.
 */

public abstract class SimpleTask<K> extends Task<K, Void> {

    public SimpleTask(K key) {
        super(key);
    }

}
