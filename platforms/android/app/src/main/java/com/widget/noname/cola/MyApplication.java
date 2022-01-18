package com.widget.noname.cola;

import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;

import com.tencent.mmkv.MMKV;
import com.widget.noname.plus.common.manager.FontManager;
import com.widget.noname.plus.common.manager.ThreadManager;
import com.widget.noname.plus.common.manager.WebViewManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyApplication extends Application {

    private static Typeface typeface = null;
    private static Typeface typefaceXingKai = null;
    private static ExecutorService threadPool = null;
    private static Context context = null;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        WebViewManager.prepare(this);
        MMKV.initialize(this);

        typeface = Typeface.createFromAsset(getAssets(), "font/xinwei.ttf");
        FontManager.getInstance().setTypeFace("xinwei", typeface);
        threadPool = Executors.newFixedThreadPool(3);
        ThreadManager.getInstance().init(threadPool);
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
