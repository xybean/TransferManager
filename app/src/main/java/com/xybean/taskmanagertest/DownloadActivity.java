package com.xybean.taskmanagertest;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.xybean.taskmanager.download.DownloadListener;
import com.xybean.taskmanager.download.DownloadManager;
import com.xybean.taskmanager.download.IDownloadTask;
import com.xybean.taskmanager.download.TransferHelper;
import com.xybean.taskmanager.download.connection.DownloadUrlConnection;
import com.xybean.taskmanager.download.connection.IDownloadConnection;
import com.xybean.taskmanager.download.db.SqliteDatabaseHandler;

import java.io.IOException;
import java.util.concurrent.Executors;

public class DownloadActivity extends AppCompatActivity {

    private String LIULISHUO_APK_URL = "http://cdn.llsapp.com/android/LLS-v4.0-595-20160908-143200.apk";

    private DownloadManager manager;
    private String targetPath;

    private int id1;
    private TextView tvProgress1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        TransferHelper.INSTANCE.holdContext(getApplicationContext());

        manager = new DownloadManager.Builder()
                .dbHandler(new SqliteDatabaseHandler())
                .executor(Executors.newSingleThreadExecutor())
                .connection(new IDownloadConnection.Factory() {
                    @Override
                    @NonNull
                    public IDownloadConnection createConnection() throws IOException {
                        return new DownloadUrlConnection(LIULISHUO_APK_URL);
                    }
                })
                .build();

        targetPath = getCacheDir().getAbsolutePath();

        tvProgress1 = findViewById(R.id.tv_progress1);

        findViewById(R.id.btn_start1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                id1 = manager.offline(LIULISHUO_APK_URL, targetPath, "xxx.apk", new DownloadListener() {
                    @Override
                    public void onStart(@NonNull IDownloadTask task) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvProgress1.setText("");
                            }
                        });
                    }

                    @Override
                    public void onUpdate(@NonNull IDownloadTask task, final long current, final long total) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvProgress1.setText(current + "/" + total);
                            }
                        });
                    }

                    @Override
                    public void onSucceed(@NonNull IDownloadTask task) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvProgress1.setText("succeed");
                            }
                        });
                    }

                    @Override
                    public void onFailed(@NonNull IDownloadTask task, Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvProgress1.setText("failed");
                            }
                        });
                    }
                }, null);
            }
        });

        findViewById(R.id.btn_pause1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (id1 > 0) {
                    manager.pause(id1);
                }
            }
        });

        findViewById(R.id.btn_cancel1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (id1 > 0) {
                    manager.cancel(id1);
                }
            }
        });

        findViewById(R.id.btn_start2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        findViewById(R.id.btn_pause2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        findViewById(R.id.btn_cancel2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


    }
}
