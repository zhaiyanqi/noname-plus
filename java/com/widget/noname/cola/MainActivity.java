/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.widget.noname.cola;

import static com.widget.noname.cola.MyApplication.webViewInited;

import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.noname.api.NonameJavaScriptInterface;
import com.norman.webviewup.lib.UpgradeCallback;
import com.norman.webviewup.lib.WebViewUpgrade;
import com.norman.webviewup.lib.source.UpgradePackageSource;
import com.norman.webviewup.lib.util.ProcessUtils;
import com.norman.webviewup.lib.util.VersionUtils;
import com.widget.noname.cola.bridge.JsBridgeInterface;

import org.apache.cordova.CordovaActivity;
import org.apache.cordova.engine.SystemWebView;

import java.util.Arrays;

public class MainActivity extends CordovaActivity {
    private void ActivityOnCreate(Bundle extras) {
        if (appView == null) {
            init();
        }
        View view = appView.getView();
        SystemWebView webview = (SystemWebView) view;
        WebSettings settings = webview.getSettings();
        Log.e(TAG, settings.getUserAgentString());
        initWebviewSettings(webview, settings);

        // Set by <content src="index.js" /> in config.xml
        loadUrl(launchUrl);
    }

    private void initWebviewSettings(SystemWebView webview, WebSettings settings) {
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        JsBridgeInterface jsBridgeInterface = new JsBridgeInterface(this, null);
        webview.addJavascriptInterface(jsBridgeInterface, jsBridgeInterface.getCallTag());
        webview.addJavascriptInterface(new NonameJavaScriptInterface(this, webview, preferences), "NonameAndroidBridge");
        WebView.setWebContentsDebuggingEnabled(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // enable Cordova apps to be started in the background
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getBoolean("cdvStartInBackground", false)) {
            moveTaskToBack(true);
        }

        boolean is64Bit = ProcessUtils.is64Bit();
        String[] supportBitAbis = is64Bit ? Build.SUPPORTED_64_BIT_ABIS : Build.SUPPORTED_32_BIT_ABIS;

        // 内置的apk只有这两种，如果都不包含，就不触发升级内核操作（例如: 虚拟机需要x86）
        int indexOfArm64 = Arrays.binarySearch(supportBitAbis, "arm64-v8a");
        int indexOfArmeabi = Arrays.binarySearch(supportBitAbis, "armeabi-v7a");
        int indexOfX86 = Arrays.binarySearch(supportBitAbis, "x86");

        Log.e(TAG, Arrays.toString(supportBitAbis));

        if (webViewInited || (indexOfArm64 < 0 && indexOfArmeabi < 0 && indexOfX86 < 0)) {
            ActivityOnCreate(extras);
        } else {
            webViewInited = true;

            WebViewUpgrade.addUpgradeCallback(new UpgradeCallback() {
                @Override
                public void onUpgradeProcess(float percent) {
                }

                @Override
                public void onUpgradeComplete() {
                    Log.e(TAG, "onUpgradeComplete");
                    ActivityOnCreate(extras);
                }

                @Override
                public void onUpgradeError(Throwable throwable) {
                    Log.e(TAG, "onUpgradeError: " + throwable.getMessage());
                    ActivityOnCreate(extras);
                }
            });

            try {
                // 添加webview
                UpgradePackageSource upgradeSource = new UpgradePackageSource(
                        getApplicationContext(),
                        "com.android.chrome");
                String SystemWebViewPackageName = WebViewUpgrade.getSystemWebViewPackageName();
                // 如果webview就是chrome
                if ("com.android.chrome".equals(SystemWebViewPackageName)) {
                    ActivityOnCreate(extras);
                    return;
                }
                PackageInfo upgradePackageInfo = getPackageManager().getPackageInfo(upgradeSource.getPackageName(), 0);
                if (upgradePackageInfo != null) {
                    // googleWebview应当等同于chrome
                    if ("com.google.android.webview".equals(SystemWebViewPackageName)) {
                        SystemWebViewPackageName = "com.android.chrome";
                    }
                    if (SystemWebViewPackageName.equals(upgradeSource.getPackageName())
                            && VersionUtils.compareVersion(WebViewUpgrade.getSystemWebViewPackageVersion(),
                            upgradePackageInfo.versionName) >= 0) {
                        ActivityOnCreate(extras);
                        return;
                    }
                    WebViewUpgrade.upgrade(upgradeSource);
                } else {
                    ActivityOnCreate(extras);
                }
            } catch (Exception e) {
                Log.e(TAG, String.valueOf(e));
                ActivityOnCreate(extras);
            }
        }
    }
}
