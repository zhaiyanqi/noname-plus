package com.widget.noname.cola.net;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.widget.noname.cola.eventbus.MsgServerStatus;
import com.widget.noname.cola.net.entry.ClientList;
import com.widget.noname.cola.net.entry.Config;
import com.widget.noname.cola.net.entry.EventList;
import com.widget.noname.cola.net.entry.Room;
import com.widget.noname.cola.net.entry.RoomList;
import com.widget.noname.cola.util.NetUtil;

import org.greenrobot.eventbus.EventBus;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class NonameWebSocketServer extends WebSocketServer {

    public static final int SERVER_TYPE_START = 1;
    public static final int SERVER_TYPE_RUNNING = 2;
    public static final int SERVER_TYPE_STOP = 3;
    public static final int SERVER_TYPE_ERROR = 4;
    public static final int SERVER_TYPE_CLOSE = 5;

    private static final String MSG_PREFIX_UPDATE_CLIENTS = "\"updateclients\"";
    private static final String MSG_PREFIX_UPDATE_ROOMS = "\"updaterooms\"";
    private static final String MSG_PREFIX_ROOM_LIST = "\"roomlist\"";
    private static final String MSG_PREFIX_CREATE_ROOM = "\"createroom\"";
    private static final String MSG_PREFIX_CONNECTION = "\"onconnection\"";
    private static final String MSG_PREFIX_ONMESSAGE = "\"onmessage\"";
    private static final String MSG_PREFIX_ENTER_FAILED = "\"enterroomfailed\"";
    private static final String MSG_PREFIX_SELF_CLOSE = "\"selfclose\"";
    private static final String MSG_PREFIX_ON_CLOSE = "\"onclose\"";

    private static final String HEART_BEAT = "heartbeat";
    private static final String SERVER = "server";

    private static final String FUN_ENTER = "enter";
    private static final String FUN_CHANGE_AVATAR = "changeAvatar";
    private static final String FUN_CREATE = "create";
    private static final String FUN_KEY = "key";
    private static final String FUN_EVENT = "events";
    private static final String FUN_CONFIG = "config";
    private static final String FUN_STATUS = "status";
    private static final String FUN_SEND = "send";
    private static final String FUN_CLOSE = "close";

    private final Set<String> bannedIps = new HashSet<>();
    private final Timer timer = new Timer();

    private final ClientList clients = new ClientList();
    private final RoomList rooms = new RoomList();
    private final EventList events = new EventList();

    public NonameWebSocketServer(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
        setWebSocketFactory(new NonameWebSocketServerFactory());
    }

    public NonameWebSocketServer(InetSocketAddress address) {
        super(address);
        setWebSocketFactory(new NonameWebSocketServerFactory());
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {

        if (conn instanceof WebSocketClient) {
            WebSocketClient ws = (WebSocketClient) conn;
//            InetSocketAddress remoteSocketAddress = ws.getRemoteSocketAddress();
//
//            if (null != remoteSocketAddress) {
//                String hostAddress = remoteSocketAddress.getAddress().getHostAddress();
//                if (bannedIps.contains(hostAddress)) {
//                    ws.sendl("denied", "banned");
//                    setTimeout(ws::close, 500);
//
//                    return;
//                }
//            }

            ws.keyCheck(timer);
            clients.add(ws);
            ws.sendl(MSG_PREFIX_ROOM_LIST,
                    rooms.toString(),
                    events.toString(),
                    clients.toString(),
                    NetUtil.toJsonStr(ws.getWsid()));
            ws.sendHeartbeat();
        } else {
            conn.close();
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        if (conn instanceof WebSocketClient && (null != message)) {
            WebSocketClient ws = (WebSocketClient) conn;

            if (!clients.contains(ws)) {
                return;
            }

            if (HEART_BEAT.equals(message)) {
                ws.setHeartBeat(false);
            } else if (null != ws.getOwner()) {
                ws.getOwner().sendl(MSG_PREFIX_ONMESSAGE, JSON.toJSONString(ws.getWsid()), JSON.toJSONString(message));
            } else {
                String[] arr;

                try {
                    JSONArray jsonArray = new JSONArray(message);
                    int length = jsonArray.length();
                    arr = new String[length];

                    for (int i = 0; i < length; i++) {
                        Object o = jsonArray.get(i);
                        arr[i] = String.valueOf(o);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    arr = null;

                    ws.sendl("denied", "banned");
                }

                message(ws, arr);
            }
        }
    }

    public void message(WebSocketClient ws, String[] msg) {
        if ((null != msg) && (msg.length > 1) && SERVER.equals(msg[0])) {
            String type = msg[1];

            switch (type) {
                case FUN_CHANGE_AVATAR: {
                    ws.setNickName(msg[2]);
                    ws.setAvatar(msg[3]);
                    updateClients();
                    break;
                }

                case FUN_CREATE: {
                    createRoom(ws, msg[2], msg[3], msg[4]);
                    break;
                }

                case FUN_ENTER: {
                    enterRoom(ws, msg[2], msg[3], msg[4]);
                    break;
                }

                case FUN_KEY: {
                    keyCheck(ws, msg[2]);
                    break;
                }

                case FUN_EVENT: {

                    break;
                }
                case FUN_CONFIG: {
                    createConfig(ws, msg[2]);
                    break;
                }
                case FUN_STATUS: {
                    if (!TextUtils.isEmpty(msg[2])) {
                        ws.setStatus(msg[2]);
                    } else {
                        ws.setStatus(null);
                    }

                    updateClients();
                    break;
                }
                case FUN_SEND: {
                    sendMsg(msg[2], msg[3]);
                    break;
                }
                case FUN_CLOSE: {
                    closeSocketSafely(ws);
                    break;
                }
            }

        }
    }

    public void setTimeout(Runnable runnable, int delay) {
        if (null != runnable) {
            timer.schedule(new TimerTask() {
                public void run() {
                    runnable.run();
                }
            }, delay);
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        if (conn instanceof WebSocketClient) {
            closeSocketSafely((WebSocketClient) conn);
        }

        updateServerStatus(SERVER_TYPE_CLOSE);
    }


    @Override
    public void stop(int timeout) throws InterruptedException {
        if (null != timer) {
            timer.cancel();
        }

        super.stop(timeout);
        updateServerStatus(SERVER_TYPE_STOP);
    }

    private void closeSocketSafely(WebSocketClient ws) {
        Iterator<Room> it = rooms.iterator();

        while (it.hasNext()) {
            Room room = it.next();

            if (room.owner == ws) {
                for (WebSocketClient client : clients) {
                    if (client.getRoom() == room && client != ws) {
                        client.sendl(MSG_PREFIX_SELF_CLOSE);
                    }
                }

                it.remove();
            }
        }

        if (clients.contains(ws)) {
            if (null != ws.getOwner()) {
                ws.getOwner().sendl(MSG_PREFIX_ON_CLOSE, JSON.toJSONString(ws.getWsid()));
                clients.remove(ws);
            }
        }
        if (null != ws.getRoom()) {
            updateRooms();
        } else {
            updateClients();
        }
    }

    private void sendMsg(String id, String msg) {
        WebSocketClient ws = clients.findById(id);

        if ((null != ws)) {
            try {
                ws.send(msg);
            } catch (Exception e) {
                ws.close();
            }
        }
    }

    private void enterRoom(WebSocketClient ws, String key, String nickname, String avatar) {
        ws.setNickName(NetUtil.checkNikeName(nickname));
        ws.setAvatar(avatar);
        Room room = rooms.findRoom(key);

        if (null == room) {
            ws.sendl(MSG_PREFIX_ENTER_FAILED);
            return;
        }

        ws.setRoom(room);
        ws.setStatus(null);
        ws.setOwner(null);

        if (null != room.owner) {
            if ((null == room.configObj)
                    || (room.configObj.isGameStarted() && (!room.configObj.isObserve() || !room.configObj.isObserveReady()))) {
                ws.sendl(MSG_PREFIX_ENTER_FAILED);
            } else {
                ws.setOwner(room.owner);
                ws.getOwner().sendl(MSG_PREFIX_CONNECTION, NetUtil.toJsonStr(ws.getWsid()));
            }

            updateRooms();
        }
    }

    private void createConfig(WebSocketClient ws, String config) {
        Room room = ws.getRoom();

        if (null != room && room.owner == ws) {
            room.config = config;
            room.configObj = JSON.parseObject(config, Config.class);
        }

        updateRooms();
    }

    private void createRoom(WebSocketClient ws, String key, String nickname, String avatar) {
        if (null == key || !key.equals(ws.getOnlineKey())) {
            return;
        }

        if (TextUtils.isEmpty(nickname)) {
            nickname = "无名玩家";
        }

        ws.setNickName(NetUtil.checkNikeName(nickname));
        ws.setAvatar(avatar);
        Room room = new Room();
        rooms.add(room);
        ws.setStatus(null);
        ws.setRoom(room);
        room.owner = ws;
        room.key = key;

        ws.sendl(MSG_PREFIX_CREATE_ROOM, NetUtil.toJsonStr(key));
    }

    private void keyCheck(WebSocketClient ws, String message) {
        String[] arr = null;

        try {
            JSONArray jsonArray = new JSONArray(message);
            int length = jsonArray.length();
            arr = new String[length];

            for (int i = 0; i < length; i++) {
                Object o = jsonArray.get(i);
                arr[i] = String.valueOf(o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (null != arr && arr.length > 0) {
            ws.setOnlineKey(arr[0]);
        }
    }

    private void updateClients() {
        Iterator<WebSocketClient> iterator = clients.iterator();
        while (iterator.hasNext()) {
            WebSocketClient next = iterator.next();

            if (next.isClosing() || next.isClosed()) {
                iterator.remove();
            }
        }

        String clientList = clients.toString();

        for (WebSocketClient ws : clients) {
            if (!ws.isInRoom()) {
                ws.sendl(MSG_PREFIX_UPDATE_CLIENTS, clientList, NetUtil.toJsonStr(ws.getOnlineKey()));
            }
        }
    }

    private void updateRooms() {
        Iterator<Room> iterator = rooms.iterator();

        while (iterator.hasNext()) {
            Room next = iterator.next();

            if (next.owner == null || next.owner.isClosed() || next.owner.isClosing()) {
                iterator.remove();
            } else {
                next.num = 0;
            }
        }

        for (WebSocketClient client : clients) {
            if (client.getRoom() != null) {
                client.getRoom().num++;
            }
        }

        String roomList = rooms.toString();
        String clientList = clients.toUpdateRoomString();

        for (WebSocketClient client : clients) {
            if (null == client.getRoom()) {
                client.sendl(MSG_PREFIX_UPDATE_ROOMS, roomList, clientList);
            }
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();

        if (conn instanceof WebSocketClient) {
            closeSocketSafely((WebSocketClient) conn);
        }

        updateServerStatus(SERVER_TYPE_ERROR);
    }

    @Override
    public void onStart() {
        updateServerStatus(SERVER_TYPE_RUNNING);
    }

    public void updateServerStatus(int status) {
        EventBus.getDefault().post(new MsgServerStatus(status));
    }
}
