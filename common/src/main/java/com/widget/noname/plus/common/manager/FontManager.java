package com.widget.noname.plus.common.manager;

import android.graphics.Typeface;

import java.util.HashMap;

public enum FontManager {
    INSTANCE;

    private static final String FONT_XINWEI = "xinwei";

    private HashMap<String, Typeface> typefaceHashMap = new HashMap<>();

    public static FontManager getInstance() {
        return INSTANCE;
    }

    public void setTypeFace(String key, Typeface typeface) {
        typefaceHashMap.put(key, typeface);
    }

    public Typeface getTypeface() {
        return getTypeface(FONT_XINWEI);
    }

    public Typeface getTypeface(String key) {
        return typefaceHashMap.get(key);
    }
}
