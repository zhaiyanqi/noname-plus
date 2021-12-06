package com.widget.noname.cola;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import com.widget.noname.cola.bridge.BridgeHelper;
import com.widget.noname.cola.listener.ExtractAdapter;
import com.widget.noname.cola.util.FileUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LaunchActivity extends AppCompatActivity {

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
            unZipUri(data);
        }

        initWebView();

    }

    private void initWaveView() {
        Resources resources = getResources();
        waveLoadingView = findViewById(R.id.waveLoadingView);
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
        if (null == waveLoadingView) {
            initWaveView();
        }

        waveLoadingView.setVisibility(View.VISIBLE);
        waveLoadingView.startAnimation();
        waveLoadingView.setProgressValue(0);

        mThreadPool.execute(() -> {
            FileUtil.extractAll(this, uri, "default", new ExtractAdapter() {

                @Override
                public void onExtractProgress(int progress) {
//                    dialogState.setProgress(progress);
                    runOnUiThread(() -> {
                        waveLoadingView.setProgressValue(progress);
                        waveLoadingView.setCenterTitle(String.valueOf(progress));
                    });
                }

                @Override
                public void onExtractDone() {
//                    dialog.dismiss();
                }

                @Override
                public void onExtractSaved(String path) {
                    Log.e("zyq", "saved: " + path);
                    runOnUiThread(() -> {
                        waveLoadingView.setProgressValue(100);
                        waveLoadingView.setCenterTitle(String.valueOf(100));
                    });
                }
            });
        });
    }

    private void initWebView() {
        WebView webView = findViewById(R.id.web_view);
        bridgeHelper = new BridgeHelper(webView);
    }

    public void testJavaBridge(View view) {
        bridgeHelper.callJs("window.app.test();");
    }

    public void startGame(View view) {
        startActivity(new Intent(this, MainActivity.class));
    }
}