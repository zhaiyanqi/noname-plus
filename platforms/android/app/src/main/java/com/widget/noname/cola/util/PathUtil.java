package com.widget.noname.cola.util;

import android.content.Context;
import android.net.Uri;

import java.io.File;

public class PathUtil {

    private static final String PATH_EMPTY = "";
    private static final String PATH_TEMP_FOLDER_NAME = "/temp/";

    public static String getExternalAssetPath(Context context, String folder) {
        if (null != context) {
            File filesDis = context.getExternalFilesDir(null);
            return Uri.fromFile(filesDis).toString() + "/" + folder + "/";
        }

        return PATH_EMPTY;
    }

    public static String getImportAssetTempPath(Context context, String folder) {
        if (null != context) {
            return context.getExternalCacheDir().getPath() + ((null == folder)
                    ? PATH_TEMP_FOLDER_NAME : folder);
        }

        return PATH_EMPTY;
    }

}
