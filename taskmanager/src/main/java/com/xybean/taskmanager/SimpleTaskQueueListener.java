package com.xybean.taskmanager;

/**
 * Author @xybean on 2018/4/2.
 */
public class SimpleTaskQueueListener implements TaskQueueListener {
    @Override
    public void onWaitingQueueUpdate(int preSize, int currSize) {

    }

    @Override
    public void onExecutingQueueUpdate(int preSize, int currSize) {

    }

    @Override
    public void onFailedQueueUpdate(int preSize, int currSize) {

    }
}
