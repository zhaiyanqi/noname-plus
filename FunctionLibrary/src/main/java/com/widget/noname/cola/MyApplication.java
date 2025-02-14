package com.widget.noname.cola;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.webkit.WebView;

import com.lxj.xpopup.XPopup;
import com.noname.core.application.NonameCoreApplication;
import com.tencent.mmkv.MMKV;
import com.widget.noname.plus.common.manager.FontManager;
import com.widget.noname.plus.common.manager.ThreadManager;
import com.widget.noname.plus.common.util.FileConstant;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyApplication extends NonameCoreApplication {
    private static Typeface typeface = null;
    private static ExecutorService threadPool = null;
    @SuppressLint("StaticFieldLeak")
    private static Context context = null;
    private static final String TAG = "MyApplication";

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
