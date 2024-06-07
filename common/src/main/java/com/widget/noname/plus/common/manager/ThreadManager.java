package com.widget.noname.plus.common.manager;

import java.util.concurrent.ExecutorService;

public enum ThreadManager {
    INSTANCE;

    private ExecutorService executorService = null;

    public static ThreadManager getInstance() {
        return INSTANCE;
    }

    public void init(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void execute(Runnable runnable) {
        if (null != executorService) {
            executorService.execute(runnable);
        }
    }
}
