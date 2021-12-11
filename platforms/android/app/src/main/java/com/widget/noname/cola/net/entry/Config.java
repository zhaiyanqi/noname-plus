package com.widget.noname.cola.net.entry;

public class Config {

    private String mode;
    private String doudizhu_mode;
    private boolean double_character;
    private boolean change_card;
    private String choose_timeout;
    private boolean observe;
    private boolean observeReady;
    private boolean observe_handcard;
    private boolean gameStarted;
    private String zhinang_tricks;
    private String characterPack;
    private String cardPack;
    private String banned;
    private String bannedcards;

    private int version;
    private int number;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getDoudizhu_mode() {
        return doudizhu_mode;
    }

    public void setDoudizhu_mode(String doudizhu_mode) {
        this.doudizhu_mode = doudizhu_mode;
    }

    public boolean isDouble_character() {
        return double_character;
    }

    public void setDouble_character(boolean double_character) {
        this.double_character = double_character;
    }

    public boolean isChange_card() {
        return change_card;
    }

    public void setChange_card(boolean change_card) {
        this.change_card = change_card;
    }

    public String getChoose_timeout() {
        return choose_timeout;
    }

    public void setChoose_timeout(String choose_timeout) {
        this.choose_timeout = choose_timeout;
    }

    public boolean isObserve() {
        return observe;
    }

    public void setObserve(boolean observe) {
        this.observe = observe;
    }

    public boolean isObserve_handcard() {
        return observe_handcard;
    }

    public void setObserve_handcard(boolean observe_handcard) {
        this.observe_handcard = observe_handcard;
    }

    public String getZhinang_tricks() {
        return zhinang_tricks;
    }

    public void setZhinang_tricks(String zhinang_tricks) {
        this.zhinang_tricks = zhinang_tricks;
    }

    public String getCharacterPack() {
        return characterPack;
    }

    public void setCharacterPack(String characterPack) {
        this.characterPack = characterPack;
    }

    public String getCardPack() {
        return cardPack;
    }

    public void setCardPack(String cardPack) {
        this.cardPack = cardPack;
    }

    public String getBanned() {
        return banned;
    }

    public void setBanned(String banned) {
        this.banned = banned;
    }

    public String getBannedcards() {
        return bannedcards;
    }

    public void setBannedcards(String bannedcards) {
        this.bannedcards = bannedcards;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public boolean isObserveReady() {
        return observeReady;
    }

    public void setObserveReady(boolean observeReady) {
        this.observeReady = observeReady;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    @Override
    public String toString() {
        return "Config{" +
                "mode='" + mode + '\'' +
                ", doudizhu_mode='" + doudizhu_mode + '\'' +
                ", double_character=" + double_character +
                ", change_card=" + change_card +
                ", choose_timeout='" + choose_timeout + '\'' +
                ", observe=" + observe +
                ", observe_handcard=" + observe_handcard +
                ", zhinang_tricks='" + zhinang_tricks + '\'' +
                ", characterPack='" + characterPack + '\'' +
                ", cardPack='" + cardPack + '\'' +
                ", banned='" + banned + '\'' +
                ", bannedcards='" + bannedcards + '\'' +
                ", version=" + version +
                ", number=" + number +
                '}';
    }
}
