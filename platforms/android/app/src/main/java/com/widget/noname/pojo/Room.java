package com.widget.noname.pojo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.widget.noname.cola.net.WebSocketClient;

import java.util.concurrent.ConcurrentHashMap;

public class Room {

    private final ConcurrentHashMap<String, WebSocketClient> players = new ConcurrentHashMap<>();

    private WebSocketClient owner = null;
    private String key = null;
    private Object config = null;
    private Config config2 = null;
    private boolean serverMode = false;

    public boolean isServerMode() {
        return serverMode;
    }

    public void setServerMode(boolean serverMode) {
        this.serverMode = serverMode;
    }

    public Object getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = JSONObject.parse(config);
        this.config2 = JSON.parseObject(config, Config.class);
    }

    public Config getConfig2() {
        return config2;
    }

    public int getPlayerNum() {
        return players.size();
    }

    public ConcurrentHashMap<String, WebSocketClient> getPlayers() {
        return players;
    }

    public WebSocketClient getOwner() {
        return owner;
    }

    public void enter(WebSocketClient ws) {
        players.put(ws.getWsid(), ws);
    }

    public void leave(WebSocketClient ws) {
        players.remove(ws.getWsid());
    }

    public void destroy() {
        players.clear();
    }

    public void setOwner(WebSocketClient ws) {
        this.owner = ws;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
