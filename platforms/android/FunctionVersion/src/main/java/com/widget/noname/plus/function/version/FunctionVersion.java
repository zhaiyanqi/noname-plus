package com.widget.noname.plus.function.version;

import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.widget.noname.plus.common.function.BaseFunction;

public class FunctionVersion extends BaseFunction {
    public FunctionVersion(@NonNull ViewGroup container) {
        super(container);
    }

    @NonNull
    @Override
    public String getFunctionName() {
        return "版本";
    }

    @Override
    public String getFunctionKey() {
        return "function_key_version";
    }
}
