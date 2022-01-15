package com.widget.noname.plus.common.server.util;

import com.alibaba.fastjson.JSONArray;
import com.widget.noname.plus.common.server.WebSocketClient;
import com.widget.noname.plus.common.server.pojo.Room;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Util {
    private static final String NICK_NAME_NONAME = "五名玩家";
    private static final String EMPTY = "";

    public static String getId() {
        return String.valueOf(1000000000L + (long) (9000000000L * Math.random()));
    }

    public static ArrayList<JSONArray> getRoomlist(ConcurrentHashMap<String, Room> rooms) {
        final ArrayList<JSONArray> roomlist = new ArrayList<>();

        rooms.forEach((s, room) -> {
            if (null != room) {
                JSONArray array = new JSONArray();

                if (room.isServerMode()) {
                    array.add("com/widget/noname/plus/common/server");
                } else if (room.getOwner() != null && room.getConfig() != null) {
                    WebSocketClient owner = room.getOwner();

                    if (room.getPlayerNum() == 0) {
                        owner.sendl("reloadroom");
                    }

                    array.add(owner.getNickName());
                    array.add(owner.getAvatar());
                    array.add(room.getConfig());
                    array.add(room.getPlayerNum());
                    array.add(owner.getOnlineKey());
                }

                roomlist.add(array);
            }
        });

        return roomlist;
    }

    public static ArrayList<JSONArray> getClientlist(ConcurrentHashMap<String, WebSocketClient> clients) {
        final ArrayList<JSONArray> clientlist = new ArrayList<>();

        clients.forEach((s, client) -> {
            JSONArray array = new JSONArray();
            array.add(client.getNickName());
            array.add(client.getAvatar());
            array.add(!client.isInRoom());
            array.add(client.getStatus());
            array.add(client.getWsid());
            array.add(client.getOnlineKey());
            clientlist.add(array);
        });

        return clientlist;
    }

    public static String getNickName(String str) {
        if (isEmpty(str)) {
            return NICK_NAME_NONAME;
        }

        if (str.length() > 12) {
            return str.substring(0, 12);
        }

        return str;
    }

    public static boolean isEmpty(String str) {
        return null == str || EMPTY.equals(str);
    }

}
