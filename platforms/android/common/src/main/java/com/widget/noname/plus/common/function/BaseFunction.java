package com.widget.noname.plus.common.function;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

public abstract class BaseFunction {

    protected boolean isAlive = false;
    protected boolean isPause = true;

    private ViewGroup container = null;

    public BaseFunction(@NonNull ViewGroup container) {
        this.container = container;
    }

    protected void replaceContainer(View view) {
        container.removeAllViews();
        container.addView(view);
    }

    public abstract String getFunctionName();

    public abstract String getFunctionKey();

    protected void onCreate() {
        isAlive = true;
    }

    public void onPause() {
        isPause = true;
    }

    public void onResume() {
        isPause = false;
    }

    public void onDestroy() {
        isAlive = false;
    }
}
