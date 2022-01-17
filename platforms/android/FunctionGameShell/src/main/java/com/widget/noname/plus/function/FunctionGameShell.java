package com.widget.noname.plus.function;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.tencent.mmkv.MMKV;
import com.widget.noname.plus.common.function.BaseFunction;
import com.widget.noname.plus.common.util.FileConstant;

import java.io.File;

public class FunctionGameShell extends BaseFunction {
    public FunctionGameShell(@NonNull Context context) {
        super(context);
    }

    @Override
    public boolean hasView() {
        return false;
    }

    @Override
    public void onClick() {
        String path = MMKV.defaultMMKV().getString(FileConstant.GAME_PATH_KEY, null);

        if (path == null) {
            Toast.makeText(getContext(), "游戏目录不正确，请检查版本设置。", Toast.LENGTH_SHORT).show();
            return;
        }

        File file = new File(path);

        if (!file.exists() || !file.isDirectory()) {
            Toast.makeText(getContext(), "游戏目录不存在，请检查版本设置。", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.widget.noname.cola",
                    "com.widget.noname.cola.MainActivity"));

            getContext().startActivity(intent);
            ((Activity) getContext()).overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
        } catch (Exception e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
