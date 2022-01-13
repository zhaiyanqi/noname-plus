package com.widget.noname.server.nonameserver.util;

public class ServerUtil {

    public static boolean isEmpty(String str) {
        return (null == str) || "".equals(str);
    }

    public static String checkNikeName(String nickname) {
        if (null == nickname || "".equals(nickname)) {
            nickname = "无名玩家";
        }

        if (nickname.length() > 12) {
            nickname = nickname.substring(0, 12);
        }

        return nickname;
    }
}
