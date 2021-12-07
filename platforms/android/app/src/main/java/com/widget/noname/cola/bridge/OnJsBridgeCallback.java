package com.widget.noname.cola.bridge;

public interface OnJsBridgeCallback {

    void onExtensionGet(String[] extensions);

    void onExtensionStateGet(String ext, boolean state);

    void onPageStarted();
}
