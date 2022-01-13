package com.widget.noname.server.nonameserver.pojo;

import java.util.ArrayList;

public class Event {
    private long utc = 0;
    private int day = 0;
    private int hour = 0;
    private String nickname = null;
    private String avatar = null;
    private String content = null;
    private String creator = null;
    private String id = null;
    private final ArrayList<String> members = new ArrayList<>();

    public ArrayList<String> getMembers() {
        return members;
    }

    public void addMembers(String id) {
        members.add(id);
    }

    public void leaveMembers(String id) {
        members.remove(id);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getUtc() {
        return utc;
    }

    public void setUtc(long utc) {
        this.utc = utc;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreator(String id) {
        creator = id;
    }

    public String getCreator() {
        return creator;
    }

    public boolean containMembers(String mem) {
        return members.contains(mem);
    }
}
