package com.xybean.taskmanager;

/**
 * Author @xybean on 2018/4/2.<br/>
 * Interface definition for a callback to be invoked when task queues are updated.
 */
public interface TaskQueueListener {

    void onWaitingQueueUpdate(int preSize, int currSize);

    void onExecutingQueueUpdate(int preSize, int currSize);

    void onFailedQueueUpdate(int preSize, int currSize);
}
