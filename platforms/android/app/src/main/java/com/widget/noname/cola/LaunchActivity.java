package com.widget.noname.cola;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.widget.noname.cola.bridge.BridgeHelper;
import com.widget.noname.cola.bridge.OnJsBridgeCallback;
import com.widget.noname.cola.listener.ExtractAdapter;
import com.widget.noname.cola.util.FileUtil;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LaunchActivity extends AppCompatActivity implements OnJsBridgeCallback {

    private BridgeHelper bridgeHelper = null;
    private ExecutorService mThreadPool = null;
    private WaveLoadingView waveLoadingView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        mThreadPool = Executors.newFixedThreadPool(3);

        initWaveView();

        Intent intent = getIntent();

        if ((null != intent) && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            showSingleChoiceDialog(data);
        }

        initWebView();

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    private void initWaveView() {
        Resources resources = getResources();
        waveLoadingView = findViewById(R.id.wave_loading_view);
        waveLoadingView.setShapeType(WaveLoadingView.ShapeType.CIRCLE);
        waveLoadingView.setWaterLevelRatio(0);
        waveLoadingView.setBorderWidth(resources.getDimensionPixelSize(R.dimen.wave_border_width));
        waveLoadingView.setAmplitudeRatio(10);
        waveLoadingView.setWaveColor(resources.getColor(R.color.wave_view_wave_color));
        waveLoadingView.setBorderColor(resources.getColor(R.color.wave_view_border_color));
        waveLoadingView.setAnimDuration(3000);
        waveLoadingView.setCenterTitleColor(Color.WHITE);
    }

    private void unZipUri(Uri uri) {
        mThreadPool.execute(() -> {
            FileUtil.extractAll(this, uri, "default", new ExtractAdapter() {

                @Override
                public void onExtractProgress(int progress) {
                    runOnUiThread(() -> {
                        waveLoadingView.setProgressValue(progress);
                        waveLoadingView.setCenterTitle(String.valueOf(progress));
                    });
                }

                @Override
                public void onExtractDone() {

                }

                @Override
                public void onExtractError() {
                }

                @Override
                public void onExtractSaved(String path) {
                    runOnUiThread(() -> {
                        waveLoadingView.setProgressValue(100);
                        waveLoadingView.setCenterTitle(String.valueOf(100));
                        Toast.makeText(LaunchActivity.this, "导入完成：" + path, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
    }

    private int yourChoice = 0;

    private void showSingleChoiceDialog(Uri data) {
        final String[] items = {"我是1", "我是2", "我是3", "我是4"};
        yourChoice = -1;
        AlertDialog.Builder singleChoiceDialog =
                new AlertDialog.Builder(this);
        singleChoiceDialog.setTitle("我是一个单选Dialog");
        // 第二个参数是默认选项，此处设置为0
        singleChoiceDialog.setSingleChoiceItems(items, 0,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        yourChoice = which;
                    }
                });
        singleChoiceDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        runOnUiThread(() -> {
                            if (yourChoice != -1) {
                                Toast.makeText(LaunchActivity.this, "任务执行中...", Toast.LENGTH_SHORT).show();
                                waveLoadingView.setVisibility(View.VISIBLE);
                                waveLoadingView.startAnimation();
                                waveLoadingView.setProgressValue(0);

                                unZipUri(data);
                            }
                        });
                    }
                });
        singleChoiceDialog.show();
    }

    private void initWebView() {
        WebView webView = findViewById(R.id.web_view);
        bridgeHelper = new BridgeHelper(webView, this);
    }

    public void testJavaBridge(View view) {
        if (null != bridgeHelper) {
            bridgeHelper.getExtensions();
        }
    }

    public void onVersionControlClick(View view) {

    }

    public void onExtControlClick(View view) {

    }

    public void onAboutClick(View view) {
        File externalStoragePublicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

        boolean b = externalStoragePublicDirectory.canWrite();
        Log.e("zyq", "b: " + b);

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

        // Provide read access to files and sub-directories in the user-selected
        // directory.
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when it loads.
//        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uriToLoad);

        startActivityForResult(intent, 111);
    }

    public void startGame(View view) {
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    public void onExtensionGet(String[] extensions) {
        if (null != extensions) {
            if (null != bridgeHelper) {
                for (String ext : extensions) {
                    bridgeHelper.getExtensionState(ext);
                }
            }
        } else {
            Log.e("zzz", "未获取到扩展信息");
        }
    }

    @Override
    public void onExtensionStateGet(String ext, boolean state) {
        Log.e("zzz: ", "ext: " + ext + ", state: " + state);
    }

    @Override
    public void onPageStarted() {
        if (null != bridgeHelper) {
            bridgeHelper.getExtensions();
        }
    }
}