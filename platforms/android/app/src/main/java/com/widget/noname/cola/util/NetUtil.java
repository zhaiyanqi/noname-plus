package com.widget.noname.cola.util;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

public class NetUtil {
    private static final String FIX_QUOT = "\"";
    private static final String EMPTY = "";

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

    public static String getIpaddr() {
        String hostIp = null;

        List<String> list = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();

            InetAddress ia = null;

            while (nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();

                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();

                    hostIp = ia.getHostAddress();
                    list.add(hostIp);
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return Arrays.toString(list.toArray());
    }
}
