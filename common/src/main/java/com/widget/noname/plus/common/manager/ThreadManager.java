package com.widget.noname.plus.common.manager;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;

public enum ThreadManager {
    INSTANCE;

    private ExecutorService executorService = null;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static ThreadManager getInstance() {
        return INSTANCE;
    }

    public void init(ExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * 在后台线程执行耗时任务
     */
    public void execute(Runnable runnable) {
        if (executorService != null) {
            executorService.execute(runnable);
        } else {
            // 可选：降级策略，但不推荐直接在主线程跑
            mainHandler.post(runnable);
        }
    }

    /**
     * 在主线程执行 UI 相关操作
     */
    public void postToMain(Runnable runnable) {
        mainHandler.post(runnable);
    }

    /**
     * 延迟在主线程执行
     */
    public void postToMainDelayed(Runnable runnable, long delayMillis) {
        mainHandler.postDelayed(runnable, delayMillis);
    }
}
