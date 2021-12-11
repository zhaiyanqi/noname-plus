package com.widget.noname.cola.net.entry;

import com.widget.noname.cola.net.WebSocketProxy;

import java.util.Arrays;

public class Room {
    private static final String FIX_QUOT = "\"";

    public int num = 0;
    public boolean servermode = false;

    public Config configObj = null;
    public String config = null;
    public String key;
    public WebSocketProxy owner = null;

    @Override
    public String toString() {

        return "[" +
                FIX_QUOT + owner.getNickName() + FIX_QUOT + "," +
                FIX_QUOT + owner.getAvatar() + FIX_QUOT + "," +
                config + "," +
                num + "," +
                FIX_QUOT + owner.getOnlineKey() + FIX_QUOT +
                "]";
    }
}
