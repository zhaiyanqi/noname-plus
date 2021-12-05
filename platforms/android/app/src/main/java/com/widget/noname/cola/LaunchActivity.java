package com.widget.noname.cola;

import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        mThreadPool = Executors.newFixedThreadPool(3);

        Intent intent = getIntent();

        if ((null != intent) && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            unZipUri(data);
        }

        initWebView();
    }

    private void unZipUri(Uri uri) {
        mThreadPool.execute(() -> {
            FileUtil.extractAll(this, uri, "default", new ExtractAdapter() {

                @Override
                public void onExtractProgress(float progress) {

                }

                @Override
                public void onExtractDone() {

                }

                @Override
                public void onExtractSaved(String path) {
                    Log.e("zyq", "saved: " + path);
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