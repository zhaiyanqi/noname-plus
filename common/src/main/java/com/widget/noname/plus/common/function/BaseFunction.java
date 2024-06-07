package com.widget.noname.plus.common.function;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Optional;

public abstract class BaseFunction {

    protected boolean isAlive = false;
    protected boolean isPause = true;

    private Context context = null;
    private ViewGroup container = null;
    private View view = null;

    public BaseFunction(@NonNull Context context) {
        this.context = context;
    }

    public final View obtainView() {
        if (null == view) {
            view = onCreateView(context, container);
            onViewCreated(view);
        }

        return view;
    }

    public void onCreate() {
    }

    public void onPause() {
        isPause = true;
    }

    public void onResume() {
        isPause = false;
    }

    @CallSuper
    public void onInit() {
        isAlive = true;
    }

    public void onDeInit() {
        isAlive = false;

        if (null != view) {
            Optional.ofNullable(view.getParent()).ifPresent(p -> ((ViewGroup) p).removeView(view));
        }
    }

    public void onDestroy() {

    }

    public boolean hasView() {
        return true;
    }

    public void onClick() {

    }

    protected Context getContext() {
        return context;
    }

    public void setContainer(ViewGroup container) {
        this.container = container;
    }

    protected ViewGroup getContainer() {
        return container;
    }

    public View onCreateView(Context context, @Nullable ViewGroup container) {
        return null;
    }

    protected void onViewCreated(View view) {

    }
}
