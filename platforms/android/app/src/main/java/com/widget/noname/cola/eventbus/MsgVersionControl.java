package com.widget.noname.cola.eventbus;

import android.net.Uri;

public class MsgVersionControl {

    public static final int MSG_TYPE_EXTRA_INTERNAL = 1;
    public static final int MSG_TYPE_EXTRA_EXTERNAL_DOCUMENT = 2;
    public static final int MSG_TYPE_EXTRA_EXTERNAL = 3;
    public static final int MSG_TYPE_CHANGE_ASSET_FINISH = 4;

    private int msgType = 0;
    private Uri uri;

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
