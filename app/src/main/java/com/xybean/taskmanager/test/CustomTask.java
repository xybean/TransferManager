package com.xybean.taskmanager.test;


import com.xybean.taskmanager.Task;
import com.xybean.taskmanager.TaskExecuteListener;

/**
 * Author @xybean on 2018/3/22.
 */

public class CustomTask extends Task<Long, String> {

    private String param1;
    private String param2;

    private UpdateListener updateListener;

    private String name;

    public CustomTask(long key, String name) {
        super(key);
        this.name = name;
    }

    @Override
    protected String execute() throws Exception {
        int count = 0;
        for (int i = 0; i < 10; i++) {
            if (isCanceled()) {
                return "";
            }
            Thread.sleep(1000);
            updateListener.update(getKey(), name + "_count : " + count);
            count++;
        }
        return name + "_" + param1 + "_" + param2;
    }

    @Override
    public void cancel() {
        super.cancel();
    }

    public static CustomTask builder(long key, String name) {
        return new CustomTask(key, name);
    }

    public CustomTask param1(String param1) {
        this.param1 = param1;
        return this;
    }

    public CustomTask param2(String param2) {
        this.param2 = param2;
        return this;
    }

    public CustomTask updateListener(UpdateListener listener) {
        this.updateListener = listener;
        return this;
    }

    public CustomTask executeListener(TaskExecuteListener<Long, String> listener) {
        setExecuteListener(listener);
        return this;
    }
}
