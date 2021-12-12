package com.widget.noname.cola.net.entry;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.widget.noname.cola.net.WebSocketClient;

import java.util.ArrayList;

public class ClientList extends ArrayList<WebSocketClient> {

    @NonNull
    @Override
    public String toString() {
        int iMax = this.size() - 1;

        if (iMax == -1) {
            return "[]";
        }

        StringBuilder b = new StringBuilder();
        b.append('[');

        for (int i = 0; ; i++) {
            b.append(get(i).toString());

            if (i == iMax) {
                return b.append(']').toString();
            }

            b.append(", ");
        }
    }

    public String toUpdateRoomString() {
        int iMax = this.size() - 1;

        if (iMax == -1) {
            return "[]";
        }

        StringBuilder b = new StringBuilder();
        b.append('[');

        for (int i = 0; ; i++) {
            b.append(get(i).toUpdateRoomString());

            if (i == iMax) {
                return b.append(']').toString();
            }

            b.append(", ");
        }
    }

    public WebSocketClient findById(String id) {
        if (TextUtils.isEmpty(id)) {
            return null;
        }

        for (WebSocketClient ws : this) {
            if (id.equals(ws.getWsid())) {
                return ws;
            }
        }

        return null;
    }
}
