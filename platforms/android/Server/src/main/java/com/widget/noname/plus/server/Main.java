package com.widget.noname.plus.server;

import java.net.UnknownHostException;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello world.");

        try {
            int port = 8080;
            int timeout = 60000;

            try {
                if (null != args && args.length > 0) {
                    port = Integer.parseInt(args[0]);
                    System.out.println(Arrays.toString(args));
                }
            } catch (Exception ignored) {
            }

            NonameWebSocketServer server = new NonameWebSocketServer(port, System.out::println);
            server.setReuseAddr(true);
            server.setConnectionLostTimeout(timeout);
            server.start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
}
