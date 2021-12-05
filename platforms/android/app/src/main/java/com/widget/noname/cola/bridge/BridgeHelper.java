package com.widget.noname.cola.bridge;

import android.annotation.SuppressLint;
import android.content.Context;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class BridgeHelper {

    private final WebView webView;
    private JavaBridgeInterface javaBridge;
    private JsBridgeInterface jsBridge;

    public BridgeHelper(WebView webView) {
        this.webView = webView;

        init();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        javaBridge = new JavaBridgeInterface(webView);

        webView.setInitialScale(0);
        webView.setVerticalScrollBarEnabled(false);
        final WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);

        settings.setAllowFileAccess(true);

        //We don't save any form data in the application
        settings.setSaveFormData(false);
        settings.setSavePassword(false);

        // Jellybean rightfully tried to lock this down. Too bad they didn't give us a whitelist
        // while we do this
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setMediaPlaybackRequiresUserGesture(false);

        // Enable database
        // We keep this disabled because we use or shim to get around DOM_EXCEPTION_ERROR_16
        String databasePath = webView.getContext().getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        settings.setDatabaseEnabled(true);
        settings.setDatabasePath(databasePath);
        settings.setGeolocationDatabasePath(databasePath);
        settings.setDomStorageEnabled(true);

        settings.setGeolocationEnabled(true);
        settings.setAppCachePath(databasePath);
        settings.setAppCacheEnabled(true);
        jsBridge = new JsBridgeInterface(webView.getContext());
        webView.addJavascriptInterface(jsBridge, jsBridge.getCallTag());

        webView.loadUrl(JsBridgeInterface.ROOT_URI);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

//                javaBridge.callJs("fromAndroidCall('cola')");
            }
        });
    }

    public void callJs(String fun) {
        javaBridge.callJs(fun);
    }
}
