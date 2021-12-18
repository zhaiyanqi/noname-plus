package com.widget.noname.cola.listener;

import com.widget.noname.cola.data.VersionData;

public interface VersionControlItemListener {

    void onSetPathItemClick(VersionData data);

    void onItemDelete(VersionData data);
}
