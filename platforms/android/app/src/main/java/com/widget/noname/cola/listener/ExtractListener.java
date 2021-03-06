package com.widget.noname.cola.listener;

public interface ExtractListener {
    void onExtractProgress(int progress);

    void onExtractDone();

    void onExtractError();

    void onExtractCancel();

    void onExtractSaved(String path);
}
