package com.widget.noname.cola.util;

import android.content.Context;
import android.net.Uri;

import java.io.File;

public class JsPathUtil {

    private static final String SEPARATOR = File.separator;

    public static String getGameRootPath(Context context) {
        File rootFiles = JavaPathUtil.getAppRootFiles(context);
        return Uri.fromFile(rootFiles).toString() + SEPARATOR;
    }
}
