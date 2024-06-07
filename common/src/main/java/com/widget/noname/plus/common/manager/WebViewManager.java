package com.widget.noname.plus.common.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.MutableContextWrapper;
import android.os.Looper;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.ArrayList;

public class WebViewManager {

    private static final ArrayList<WebView> webViewCache = new ArrayList<>();

    public static WebView create(Context context) {
        return new WebView(context);
    }

    public static void addMinWebViewToContainer(ViewGroup viewGroup, WebView webView) {
        if ((null != viewGroup) && (null != webView)) {
            viewGroup.addView(webView, new ViewGroup.LayoutParams(1, 1));
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    public static void initWebview(WebView webView, String url, Object jsBridge, String tag) {
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
        // settings.setAppCachePath(databasePath);
        // settings.setAppCacheEnabled(true);
        webView.addJavascriptInterface(jsBridge, tag);

        webView.loadUrl(url);
    }

    public static WebView obtain(Context context) {
        if (webViewCache.isEmpty()) {
            webViewCache.add(create(new MutableContextWrapper(context)));
        }

        WebView webView = webViewCache.remove(0);
        MutableContextWrapper contextWrapper = (MutableContextWrapper) webView.getContext();
        contextWrapper.setBaseContext(context);
        webView.clearHistory();
        webView.resumeTimers();
        return webView;
    }

    public static void prepare(Context context) {
        if (webViewCache.isEmpty()) {
            Looper.myQueue().addIdleHandler(() -> {
                webViewCache.add(create(new MutableContextWrapper(context)));
                return false;
            });
        }
    }

    public static void destroy(WebView webView) {
        try {
            webView.stopLoading();
            webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            webView.clearHistory();
            webView.pauseTimers();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            ViewParent parent = webView.getParent();

            if (parent != null) {
                ((ViewGroup) parent).removeView(webView);
            }
            webView.removeAllViews();
            webView.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void destroy() {
        try {
            webViewCache.forEach(it -> {
                it.removeAllViews();
                it.destroy();
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            webViewCache.clear();
        }
    }
}
