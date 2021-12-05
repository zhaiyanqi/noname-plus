package com.widget.noname.cola.bridge;

import android.support.annotation.NonNull;
import android.webkit.WebView;

public class JavaBridgeInterface {
    private static final String JS_PREFIX = "javascript:";

    private final WebView webView;

    public JavaBridgeInterface(@NonNull WebView webView) {
        this.webView = webView;
    }

    public void callJs(String fun) {
        webView.loadUrl(JS_PREFIX + fun);
    }
}
