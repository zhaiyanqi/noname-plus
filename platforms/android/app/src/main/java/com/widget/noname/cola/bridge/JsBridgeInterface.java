package com.widget.noname.cola.bridge;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.widget.noname.cola.util.JsPathUtil;

public class JsBridgeInterface {
    public static final String ROOT_URI = "file:///android_asset/html/start.html";
    private static final String CALL_TAG = "jsBridge";

    private final Context context;

    public JsBridgeInterface(Context context) {
        this.context = context;
    }

    @JavascriptInterface
    public String getAssetPath() {
        return JsPathUtil.getGameRootPath(context);
    }

    public String getCallTag() {
        return CALL_TAG;
    }
}
