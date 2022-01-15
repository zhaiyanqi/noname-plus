package com.widget.noname.plus.common.server;

import com.alibaba.fastjson.JSONArray;
import com.widget.noname.plus.common.server.pojo.Room;

import org.java_websocket.WebSocket;

public class WebSocketClient {

    private final WebSocket session;

    private Room room;
    private WebSocketClient owner;
    private WebSocketClient onConfig = null;

    private String onlineKey = null;
    private String wsid = null;
    private String nickName = null;
    private String avatar = null;
    private String status = null;
    private String version = null;

    public WebSocketClient(WebSocket session) {
        this.session = session;
        setWsid(generateId());
    }

    public WebSocketClient getOwner() {
        return owner;
    }

    public void setOwner(WebSocketClient owner) {
        this.owner = owner;
    }

    public boolean isInRoom() {
        return (null != room);
    }


    public void setVersion(String version) {
        this.version = version;
    }

    private String generateId() {
        return String.valueOf(1000000000L + (long) (9000000000L * Math.random()));
    }

    public WebSocket getSession() {
        return session;
    }

    public String getOnlineKey() {
        return onlineKey;
    }

    public void setOnlineKey(String onlineKey) {
        this.onlineKey = onlineKey;
    }

    public String getWsid() {
        return wsid;
    }

    public void setWsid(String wsid) {
        this.wsid = wsid;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void sendl(Object... arguments) {
        try {
            session.send(JSONArray.toJSONString(arguments));
        } catch (Exception e) {
            e.printStackTrace();
            session.close();
        }
    }

    public void close() {
        session.close();
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public Room getRoom() {
        return room;
    }

    public WebSocketClient getOnConfig() {
        return onConfig;
    }

    public void setOnConfig(WebSocketClient onConfig) {
        this.onConfig = onConfig;
    }
}
