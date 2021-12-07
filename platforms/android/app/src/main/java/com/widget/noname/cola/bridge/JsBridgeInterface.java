package com.widget.noname.cola.bridge;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.widget.noname.cola.util.JsPathUtil;

import org.json.JSONObject;

import java.util.Arrays;

public class JsBridgeInterface {
    public static final String ROOT_URI = "file:///android_asset/html/start.html";
    private static final String CALL_TAG = "jsBridge";

    private static final String CONFIG_PREFIX = "noname_0.9_";

    private final Context context;
    private final OnJsBridgeCallback jsBridgeCallback;

    public JsBridgeInterface(Context context, OnJsBridgeCallback callback) {
        this.context = context;
        jsBridgeCallback = callback;
    }

    @JavascriptInterface
    public String getAssetPath() {
        return JsPathUtil.getGameRootPath(context);
    }

    @JavascriptInterface
    public void onGetExtensions(String result) {
        if (null != result) {
            String[] extensions = result.split(",");

            if (null != jsBridgeCallback) {
                jsBridgeCallback.onExtensionGet(extensions);
            }
        } else {
            jsBridgeCallback.onExtensionGet(null);
        }
    }

    @JavascriptInterface
    public void onExtensionStateGet(String ext, boolean state) {
        if (null != jsBridgeCallback) {
            jsBridgeCallback.onExtensionStateGet(ext, state);
        }
    }

    @JavascriptInterface
    public void onPageStarted() {
        if (null != jsBridgeCallback) {
            jsBridgeCallback.onPageStarted();
        }
    }

    public String getCallTag() {
        return CALL_TAG;
    }
}
