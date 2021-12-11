package com.widget.noname.cola.net.entry;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class RoomList extends ArrayList<Room> {

    public Room findRoom(String id) {
        for (Room r : this) {
            if ((null != r.key) && r.key.equals(id)) {
                return r;
            }
        }

        return null;
    }

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
            Room room = get(i);

            if (room.servermode) {
                b.append("server");
            } else if ((null != room.owner) && (null != room.config)) {
                b.append(get(i).toString());
            }

            if (i == iMax) {
                return b.append(']').toString();
            }

            b.append(", ");
        }
    }
}