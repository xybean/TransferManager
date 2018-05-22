package com.xybean.taskmanager;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.xybean.taskmanager.test.CustomExecutorFactory;
import com.xybean.taskmanager.test.CustomTask;
import com.xybean.taskmanager.test.UpdateListener;

import java.util.ArrayList;
import java.util.List;

public class TestTaskActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvWaiting;
    private TextView tvExecuting;
    private TextView tvFailed;

    private TaskManager<Long, String> taskManager;

    private List<Bean> items = new ArrayList<>();

    private CustomAdapter adapter;

    private int index = 0;

    private TaskExecuteListener<Long, String> listener = new TaskExecuteListener<Long, String>() {
        @Override
        public void onWait(final Long key) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < items.size(); i++) {
                        if (items.get(i).key == key) {
                            adapter.notifyItemChanged(i);
                            return;
                        }
                    }
                }
            });
        }

        @Override
        public void onStart(final Long key) {

        }

        @Override
        public void onSuccess(final Long key, final String result) {
            System.out.println("============>>>>>>xyb helllllo");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < items.size(); i++) {
                        if (items.get(i).key == key) {
                            items.get(i).name = result;
                            adapter.notifyItemChanged(i);
                            return;
                        }
                    }
                }
            });
        }

        @Override
        public void onFailed(final Long key, Throwable throwable) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < items.size(); i++) {
                        if (items.get(i).key == key) {
                            items.get(i).name = "failed";
                            adapter.notifyItemChanged(i);
                            return;
                        }
                    }
                }
            });
        }

        @Override
        public void onCanceled(final Long key) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int index = -1;
                    for (int i = 0; i < items.size(); i++) {
                        if (items.get(i).key == key) {
                            index = i;
                            break;
                        }
                    }
                    if (index >= 0) {
                        items.remove(index);
                        adapter.notifyItemRemoved(index);
                    }
                }
            });
        }
    };

    private TaskQueueListener queueListener = new TaskQueueListener() {
        @Override
        public void onWaitingQueueUpdate(final int preSize, final int currSize) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvWaiting.setText("pre: " + preSize + "  curr : " + currSize);
                }
            });
        }

        @Override
        public void onExecutingQueueUpdate(final int preSize, final int currSize) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvExecuting.setText("pre: " + preSize + "  curr : " + currSize);
                }
            });
        }

        @Override
        public void onFailedQueueUpdate(final int preSize, final int currSize) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvFailed.setText("pre: " + preSize + "  curr : " + currSize);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_task);

        taskManager = new TaskManager<>("TaskTest", new CustomExecutorFactory(3));

        tvWaiting = findViewById(R.id.tv_waiting);
        tvExecuting = findViewById(R.id.tv_executing);
        tvFailed = findViewById(R.id.tv_failed);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CustomAdapter(this, items, taskManager);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bean bean = new Bean(index, (index) + "_name");
                items.add(bean);
                adapter.notifyItemInserted(items.size());
                taskManager.execute(CustomTask.builder(bean.key, bean.name)
                        .param1("hello")
                        .param2("world")
                        .updateListener(new UpdateListener() {
                            @Override
                            public void update(final long key, final String process) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (int i = 0; i < items.size(); i++) {
                                            if (items.get(i).key == key) {
                                                items.get(i).name = process;
                                                adapter.notifyItemChanged(i);
                                                return;
                                            }
                                        }
                                    }
                                });
                            }
                        })
                        .executeListener(listener));
                System.out.println("==============>>>>>submit a task ：" + (index) + "_name");
                index++;
            }
        });

        findViewById(R.id.btn_cancel_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskManager.cancelAll();
            }
        });

        taskManager.setOnTaskQueueListener(queueListener);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        taskManager.shutdown();
    }

    public static class CustomAdapter extends RecyclerView.Adapter<CustomViewHolder> {

        private Context context;
        private List<Bean> items;
        private TaskManager<Long, String> taskManager;

        public CustomAdapter(Context context, List<Bean> items, TaskManager<Long, String> taskManager) {
            this.context = context;
            this.items = items;
            this.taskManager = taskManager;
        }

        @Override
        public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new CustomViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_item,
                    parent, false), taskManager);
        }

        @Override
        public void onBindViewHolder(CustomViewHolder holder, int position) {
            holder.update(items.get(position), position);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    public static class CustomViewHolder extends RecyclerView.ViewHolder {

        private TextView tv;
        private Button btn;

        private TaskManager<Long, String> taskManager;

        public CustomViewHolder(View itemView, TaskManager<Long, String> taskManager) {
            super(itemView);
            tv = itemView.findViewById(R.id.tv_msg);
            btn = itemView.findViewById(R.id.btn_cancel);
            this.taskManager = taskManager;
        }

        public void update(final Bean item, final int position) {
            tv.setText(item.name);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    taskManager.cancel(item.key);
                }
            });
        }
    }

    public static class Bean {
        public long key;
        public String name;

        public Bean(long key, String name) {
            this.key = key;
            this.name = name;
        }
    }
}
