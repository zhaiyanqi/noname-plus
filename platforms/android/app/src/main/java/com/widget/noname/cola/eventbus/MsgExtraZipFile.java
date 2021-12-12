package com.widget.noname.cola.eventbus;

import android.net.Uri;

public class MsgExtraZipFile {

    public static final int EXTRA_TYPE_INTERNAL = 1;
    public static final int EXTRA_TYPE_EXTERNAL_DOCUMENT = 2;
    public static final int EXTRA_TYPE_EXTERNAL = 3;


    private int extraType = 0;

    private Uri uri;


    public int getExtraType() {
        return extraType;
    }

    public void setExtraType(int extraType) {
        this.extraType = extraType;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
