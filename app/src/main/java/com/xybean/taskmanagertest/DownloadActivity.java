package com.xybean.taskmanagertest;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.xybean.taskmanager.ApplicationHolder;
import com.xybean.taskmanager.download.DownloadListener;
import com.xybean.taskmanager.download.DownloadManager;
import com.xybean.taskmanager.download.connection.DownloadUrlConnection;
import com.xybean.taskmanager.download.connection.IDownloadConnection;
import com.xybean.taskmanager.download.db.SqliteDatabaseHandler;
import com.xybean.taskmanager.download.id.DefaultIdGenerator;
import com.xybean.taskmanager.download.stream.DefaultDownloadStream;
import com.xybean.taskmanager.download.stream.IDownloadStream;
import com.xybean.taskmanager.download.task.IDownloadTask;

import java.io.IOException;
import java.util.concurrent.Executors;

public class DownloadActivity extends AppCompatActivity {

    private String DOWNLOAD_URL = "http://fjdx.sc.chinaz.com/Files/DownLoad/sound1/201807/10363.mp3";
    private String APK_URL = "https://app.fangcloud.com/android/fangcloud-v2/fangcloud-v2-android-latest.apk?t=1532076481";

    private DownloadManager manager;
    private String targetPath;

    private int id1;
    private TextView tvProgress1;

    private int id2;
    private TextView tvProgress2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        ApplicationHolder.INSTANCE.holdContext(getApplicationContext());

        manager = new DownloadManager.Builder()
                .dbHandler(new SqliteDatabaseHandler())
                .executor(Executors.newFixedThreadPool(2))
                .connection(new IDownloadConnection.Factory() {
                    @NonNull
                    @Override
                    public IDownloadConnection createConnection(@NonNull IDownloadTask task) throws IOException {
                        return new DownloadUrlConnection(task);
                    }
                })
                .stream(new IDownloadStream.Factory() {
                    @NonNull
                    @Override
                    public IDownloadStream createDownloadStream(@NonNull IDownloadTask task) {
                        return new DefaultDownloadStream(task);
                    }
                })
                .debug(true)
                .build();

        targetPath = getCacheDir().getAbsolutePath();

        tvProgress1 = findViewById(R.id.tv_progress1);
        tvProgress2 = findViewById(R.id.tv_progress2);

        findViewById(R.id.btn_start1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                id1 = manager.download(DOWNLOAD_URL,
                        targetPath,
                        "10363.mp3",
                        false,
                        new DownloadListener() {
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
                            public void onUpdate(@NonNull final IDownloadTask task) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvProgress1.setText(task.getCurrent() + "/" + task.getTotal());
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
                        },
                        new DefaultIdGenerator(DOWNLOAD_URL, targetPath, "10363.mp3"));
            }
        });

        findViewById(R.id.btn_pause1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.pause(id1);
            }
        });

        findViewById(R.id.btn_cancel1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.cancel(id1);
            }
        });

        findViewById(R.id.btn_start2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                id2 = manager.download(APK_URL,
                        targetPath,
                        "fangcloud.apk",
                        false,
                        new DownloadListener() {
                            @Override
                            public void onStart(IDownloadTask task) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvProgress2.setText("");
                                    }
                                });
                            }

                            @Override
                            public void onUpdate(final IDownloadTask task) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvProgress2.setText(task.getCurrent() + "/" + task.getTotal());
                                    }
                                });
                            }

                            @Override
                            public void onSucceed(IDownloadTask task) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvProgress2.setText("succeed");
                                    }
                                });
                            }

                            @Override
                            public void onFailed(IDownloadTask task, Exception e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvProgress2.setText("failed");
                                    }
                                });
                            }
                        },
                        new DefaultIdGenerator(APK_URL, targetPath, "fangcloud.apk"));
            }
        });

        findViewById(R.id.btn_pause2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.pause(id2);
            }
        });

        findViewById(R.id.btn_cancel2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.cancel(id2);
            }
        });


    }
}
