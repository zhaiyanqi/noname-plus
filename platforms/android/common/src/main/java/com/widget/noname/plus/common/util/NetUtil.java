package com.widget.noname.plus.common.util;

import android.text.TextUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class NetUtil {
    private static final String FIX_QUOT = "\"";
    private static final String EMPTY = "";
    private static final String IP_REGEX = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
            + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
            + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
            + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";

    public static String toJsonStr(String str) {
        if ((null != str) && !EMPTY.equals(str)) {
            return FIX_QUOT + str + FIX_QUOT;
        }

        return null;
    }

    public static String getId() {
        return String.valueOf(1000000000L + (long) (9000000000L * Math.random()));
    }

    public static String checkNikeName(String nickname) {
        if (TextUtils.isEmpty(nickname)) {
            nickname = "无名玩家";
        }

        if (nickname.length() > 12) {
            nickname = nickname.substring(0, 12);
        }

        return nickname;
    }

    public static String[] getIpaddr() {
        List<String> list = new ArrayList<>();

        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();

            InetAddress ia = null;

            while (nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();

                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    String hostAddress = ia.getHostAddress();

                    if (hostAddress.startsWith("fe80") || hostAddress.startsWith("::")) {
                        continue;
                    }

                    if (hostAddress.contains(":")) {
                        list.add("[" + hostAddress + "]");
                    } else {
                        list.add(hostAddress);
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        String[] result = new String[list.size()];

        return list.toArray(result);
    }

    public static boolean ipCheck(String text) {
        if ((text != null) && !text.isEmpty()) {
            return text.matches(IP_REGEX);
        }

        return false;
    }
}
