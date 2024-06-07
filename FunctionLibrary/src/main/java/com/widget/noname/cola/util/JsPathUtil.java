package com.widget.noname.cola.util;

import android.content.Context;
import android.net.Uri;

import com.tencent.mmkv.MMKV;
import com.widget.noname.plus.common.util.FileConstant;

import java.io.File;

public class JsPathUtil {

    private static final String SEPARATOR = File.separator;

    public static String getGameRootPath(Context context) {
        File rootFiles = null;
        String path = MMKV.defaultMMKV().getString(FileConstant.GAME_PATH_KEY, null);

        if ((path == null) || (path.length() == 0)) {
            rootFiles = JavaPathUtil.getAppRootFiles(context);
        } else {
            rootFiles = new File(path);
        }

        return Uri.fromFile(rootFiles).toString() + SEPARATOR;
    }
}
