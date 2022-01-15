package org.apache.cordova.engine;

import android.content.Context;
import android.content.MutableContextWrapper;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.util.ArrayList;

public class SystemWebViewManager {
    private static final String TAG = "SystemWebViewManager";
    private static final ArrayList<SystemWebView> webViewCache = new ArrayList<>();

    public static SystemWebView create(Context context) {
        SystemWebView webView = new SystemWebView(context);
        // todo
        return webView;
    }

    public static SystemWebView obtain(Context context) {
        if (webViewCache.isEmpty()) {
            webViewCache.add(create(new MutableContextWrapper(context)));
        }

        SystemWebView webView = webViewCache.remove(0);
        MutableContextWrapper contextWrapper = (MutableContextWrapper) webView.getContext();
        contextWrapper.setBaseContext(context);
        webView.clearHistory();
        webView.resumeTimers();
        webView.setVisibility(View.VISIBLE);
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

    public static void recycle(SystemWebView webView) {
        Log.d(TAG, "recycle: webView: " + webView);

        try {
            ViewParent parent = webView.getParent();

            if (parent != null) {
                ((ViewGroup) parent).removeView(webView);
            }

            webView.stopLoading();
            webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            webView.clearHistory();
            webView.pauseTimers();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
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
                Log.e(TAG, "destroy, it: " + it);
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
