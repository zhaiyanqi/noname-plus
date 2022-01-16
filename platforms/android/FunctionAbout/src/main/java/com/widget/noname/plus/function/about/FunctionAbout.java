package com.widget.noname.plus.function.about;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.widget.noname.plus.common.function.BaseFunction;

public class FunctionAbout extends BaseFunction {

    public FunctionAbout(@NonNull Context context) {
        super(context);
    }

    @Override
    public View onCreateView(Context context, @Nullable ViewGroup container) {
        return LayoutInflater.from(context).inflate(R.layout.function_about, container, false);
    }
}
