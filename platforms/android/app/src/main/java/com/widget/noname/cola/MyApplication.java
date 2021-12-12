package com.widget.noname.cola;

import android.app.Application;
import android.graphics.Typeface;

import com.tencent.mmkv.MMKV;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyApplication extends Application {

    private static Typeface typeface = null;
    private static ExecutorService threadPool = null;

    @Override
    public void onCreate() {
        super.onCreate();

        MMKV.initialize(this);

        typeface = Typeface.createFromAsset(getAssets(), "font/xinwei.ttf");
        threadPool = Executors.newFixedThreadPool(3);
    }

    public static Typeface getTypeface() {
        return typeface;
    }

    public static ExecutorService getThreadPool() {
        return threadPool;
    }
}
