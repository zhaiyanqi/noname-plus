package com.widget.noname.cola;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.util.Log;
import android.webkit.WebView;

import com.lxj.xpopup.XPopup;
import com.norman.webviewup.lib.UpgradeCallback;
import com.norman.webviewup.lib.WebViewUpgrade;
import com.norman.webviewup.lib.source.UpgradePackageSource;
import com.norman.webviewup.lib.util.ProcessUtils;
import com.norman.webviewup.lib.util.VersionUtils;
import com.tencent.mmkv.MMKV;
import com.widget.noname.plus.common.manager.FontManager;
import com.widget.noname.plus.common.manager.ThreadManager;
import com.widget.noname.plus.common.manager.WebViewManager;
import com.widget.noname.plus.common.util.FileConstant;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyApplication extends Application {
    public static boolean webViewInited = false;
    private static Typeface typeface = null;
    private static ExecutorService threadPool = null;
    @SuppressLint("StaticFieldLeak")
    private static Context context = null;
    private static final String TAG = "MyApplication";
    private final CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        // WebViewManager.prepare(this);
        MMKV.initialize(this);

        typeface = Typeface.createFromAsset(getAssets(), "font/xinwei.ttf");
        FontManager.getInstance().setTypeFace("xinwei", typeface);
        threadPool = Executors.newFixedThreadPool(3);
        ThreadManager.getInstance().init(threadPool);

        String GameRootPath = MMKV.defaultMMKV().getString(FileConstant.GAME_PATH_KEY, null);
        if (GameRootPath != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                File gamePath =  new File(GameRootPath);
                WebView.setDataDirectorySuffix(gamePath.getName());
            }
            else {
                boolean needTip = getSharedPreferences("nonameyuri", MODE_PRIVATE)
                        .getBoolean("AndroidVersionPTip", false);

                if (needTip) {
                    XPopup.Builder builder = new XPopup.Builder(context);
                    context.getSharedPreferences("nonameyuri", MODE_PRIVATE)
                            .edit()
                            .putBoolean("AndroidVersionPTip", true)
                            .apply();

                    builder.asConfirm("提示", "安卓9以下的设备多个游戏版本之间数据不能隔离！", () -> {}).show();
                }
            }
        }

        boolean is64Bit = ProcessUtils.is64Bit();
        String[] supportBitAbis = is64Bit ? Build.SUPPORTED_64_BIT_ABIS : Build.SUPPORTED_32_BIT_ABIS;

        // 内置的apk只有这2种，如果都不包含，就不触发升级内核操作（例如: 虚拟机需要x86）
        int indexOfArm64 = Arrays.binarySearch(supportBitAbis, "arm64-v8a");
        int indexOfArmeabi = Arrays.binarySearch(supportBitAbis, "armeabi-v7a");
        // int indexOfX86 = Arrays.binarySearch(supportBitAbis, "x86");

        Log.e(TAG, Arrays.toString(supportBitAbis));

        if (webViewInited || (indexOfArm64 < 0 && indexOfArmeabi < 0 /*&& indexOfX86 < 0*/)) {
            WebViewManager.prepare(context);
            latch.countDown();
        }
        else {
            webViewInited = true;

            WebViewUpgrade.addUpgradeCallback(new UpgradeCallback() {
                @Override
                public void onUpgradeProcess(float percent) {
                }

                @Override
                public void onUpgradeComplete() {
                    Log.e(TAG, "onUpgradeComplete");
                    WebViewManager.prepare(context);
                    latch.countDown();
                }

                @Override
                public void onUpgradeError(Throwable throwable) {
                    Log.e(TAG, "onUpgradeError: " + throwable.getMessage());
                    WebViewManager.prepare(context);
                    latch.countDown();
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
                    WebViewManager.prepare(context);
                    latch.countDown();
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
                        WebViewManager.prepare(context);
                        latch.countDown();
                        return;
                    }
                    WebViewUpgrade.upgrade(upgradeSource);
                } else {
                    WebViewManager.prepare(context);
                    latch.countDown();
                }
            }
            catch (Exception e) {
                Log.e(TAG, String.valueOf(e));
                WebViewManager.prepare(context);
                latch.countDown();
            }
        }
    }

    // 提供一个方法让其他地方可以等待
    public void waitForInit() throws InterruptedException {
        latch.await();
    }

    public static Typeface getTypeface() {
        return typeface;
    }

    public static ExecutorService getThreadPool() {
        return threadPool;
    }

    public static Context getContext() {
        return context;
    }
}
