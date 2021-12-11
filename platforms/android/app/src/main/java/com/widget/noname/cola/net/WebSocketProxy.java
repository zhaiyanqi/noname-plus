package com.widget.noname.cola.net;

import android.util.Log;

import com.widget.noname.cola.net.entry.Room;
import com.widget.noname.cola.util.NetUtil;

import org.java_websocket.WebSocketImpl;
import org.java_websocket.WebSocketListener;
import org.java_websocket.drafts.Draft;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WebSocketProxy extends WebSocketImpl {
    private static final String TAG = "WebSocketProxy";


    private final Timer heartBeatTimer = new Timer();

    private String onlineKey = null;
    private String wsid = null;
    private String nickName = null;
    private String avatar = null;
    private String status = null;

    private boolean inRoom = false;
    private boolean heartBeat = false;
    private boolean onConfig = false;

    private Room room = null;
    private WebSocketProxy owner = null;

    public WebSocketProxy(WebSocketListener listener, List<Draft> drafts) {
        super(listener, drafts);
        setWsid(NetUtil.getId());
    }

    public WebSocketProxy(WebSocketListener listener, Draft draft) {
        super(listener, draft);
        setWsid(NetUtil.getId());
    }

    public void keyCheck(Timer timer) {
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                sendl("denied", "key");
//                timer.schedule(new TimerTask() {
//                    @Override
//                    public void run() {
//                        close();
//                    }
//                }, 500);
//            }
//        }, 2000);
    }

    public void sendl(String... args) {
        try {
            if (args == null) {
                send("[]");
            } else {
                String msg = Arrays.toString(args);
                send(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            close();
        }
    }

    public void setWsid(String id) {
        this.wsid = id;
    }

    public String getWsid() {
        return wsid;
    }

    public void setInRoom(boolean inRoom) {
        this.inRoom = inRoom;
    }

    public boolean isInRoom() {
        return inRoom;
    }

    public String getNickName() {
        return nickName;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getStatus() {
        return status;
    }

    private String roomId = null;

    public String getRoomId() {
        return roomId;
    }

    public void sendHeartbeat() {
        heartBeatTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (heartBeat) {
                    close();
                    heartBeatTimer.cancel();
                } else {
                    heartBeat = true;
                    try {
                        send("heartbeat");
                    } catch (Exception e) {
                        e.printStackTrace();
                        tryClose();
                    }
                }
            }
        }, 0, 60000);
    }

    private void tryClose() {
        if (null != heartBeatTimer) {
            heartBeatTimer.cancel();
        }

        if (!isClosed() && !isClosing()) {
            close();
        }
    }

    @Override
    public void close() {
        super.close();

        if (null != heartBeatTimer) {
            heartBeatTimer.cancel();
        }
    }

    @Override
    public void close(int code, String message, boolean remote) {
        if (null != heartBeatTimer) {
            heartBeatTimer.cancel();
        }

        super.close(code, message, remote);
    }

    public void setHeartBeat(boolean beat) {
        heartBeat = beat;
    }

    public WebSocketProxy getOwner() {
        return owner;
    }

    public void setOwner(WebSocketProxy owner) {
        this.owner = owner;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setNickName(String nickname) {
        this.nickName = nickname;
    }

    public void setOnlineKey(String id) {
        this.onlineKey = id;
    }

    public String getOnlineKey() {
        return onlineKey;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public void setRoom(Room room) {
        this.room = room;
        setInRoom(room != null);
    }

    public Room getRoom() {
        return room;
    }

    @Override
    public String toString() {
        return "[" +
                NetUtil.toJsonStr(getNickName()) + "," +
                NetUtil.toJsonStr(getAvatar()) + "," +
                !isInRoom() + "," +
                NetUtil.toJsonStr(getStatus()) + "," +
                NetUtil.toJsonStr(getWsid()) + "]";
    }

    public String toUpdateRoomString() {
        return "[" +
                NetUtil.toJsonStr(getNickName()) + "," +
                NetUtil.toJsonStr(getAvatar()) + "," +
                !isInRoom() + "," +
                NetUtil.toJsonStr(getStatus()) + "," +
                NetUtil.toJsonStr(getWsid()) + "," +
                NetUtil.toJsonStr(getOnlineKey()) + "]";
    }

    public boolean onConfig() {
        return onConfig;
    }

    public void setOnConfig(boolean onConfig) {
        this.onConfig = onConfig;
    }
}
