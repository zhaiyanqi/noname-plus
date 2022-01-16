package com.widget.noname.plus.common.webview;

import android.content.Context;
import android.content.MutableContextWrapper;
import android.os.Looper;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebView;

import java.util.ArrayList;

public class WebViewManager {

    private static final ArrayList<WebView> webViewCache = new ArrayList<>();

    public static WebView create(Context context) {
        WebView webView = new WebView(context);
        return webView;
    }

    public static WebView obtain(Context context) {
        if (webViewCache.isEmpty()) {
            webViewCache.add(create(new MutableContextWrapper(context)));
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

    public static void recycle(WebView webView) {
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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!webViewCache.contains(webView)) {
                webViewCache.add(webView);
            }
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
