package com.widget.noname.plus.function;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.widget.noname.plus.common.function.BaseFunction;
import com.widget.noname.plus.server.R;

public class FunctionServer extends BaseFunction {
    public FunctionServer(@NonNull Context context) {
        super(context);
    }

    @Override
    public View onCreateView(Context context, @Nullable ViewGroup container) {
        return LayoutInflater.from(context).inflate(R.layout.function_server, container, false);
    }
}
