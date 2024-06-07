package com.widget.noname.cola.databinding;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.widget.noname.cola.library.BR;

public class AssetFragmentData extends BaseObservable {
    public static final int STATUS_CHECK_UPDATE = 1; // 刷新最新版
    public static final int STATUS_CHECKING = 2; // 刷新中
    public static final int STATUS_CLICK_UPDATE = 3; // 点击更新
    public static final int STATUS_UPDATING = 4; // 更新中
    public static final int STATUS_NEWEST = 5; // 已是最新
    public static final int STATUS_DOWNLOAD_FAIL = 6; // 已是最新

    private String assetPath = null;
    private String version = null;
    private String assetSize = null;
    private String updateVersion = null;
    private String updateChangeLog = null;
    private String updateUri = null;
    private String downloadProgress = null;
    private String updateBtnStr = null;
    private int updateStatus = -1;
    private boolean canUpdate = false;

    @Bindable
    public String getUpdateBtnStr() {
        return updateBtnStr;
    }

    public void setUpdateBtnStr(String updateBtnStr) {
        this.updateBtnStr = updateBtnStr;
        notifyPropertyChanged(BR.updateBtnStr);
    }

    @Bindable
    public int getUpdateStatus() {
        return updateStatus;
    }

    public void setUpdateStatus(int updateStatus) {
        this.updateStatus = updateStatus;

        setCanUpdate((updateStatus == AssetFragmentData.STATUS_CHECK_UPDATE)
                || (updateStatus == AssetFragmentData.STATUS_CLICK_UPDATE));

        switch (updateStatus) {
            case STATUS_CHECK_UPDATE: {
                setUpdateBtnStr("检查更新");
                break;
            }
            case STATUS_CLICK_UPDATE: {
                setUpdateBtnStr("点击更新");
                break;
            }
            case STATUS_NEWEST: {
                setUpdateBtnStr("已是最新版本");
                break;
            }
            case STATUS_UPDATING: {
                setUpdateBtnStr("更新中...");
                break;
            }
            case STATUS_CHECKING: {
                setUpdateBtnStr("刷新中...");
                break;
            }
            case STATUS_DOWNLOAD_FAIL: {
                setUpdateBtnStr("下载失败");
                break;
            }
        }

        notifyPropertyChanged(BR.updateStatus);
    }

    @Bindable
    public String getDownloadProgress() {
        return downloadProgress;
    }

    public void setDownloadProgress(String downloadProgress) {
        this.downloadProgress = downloadProgress;
        notifyPropertyChanged(BR.downloadProgress);
    }

    @Bindable
    public boolean isCanUpdate() {
        return canUpdate;
    }

    public void setCanUpdate(boolean canUpdate) {
        this.canUpdate = canUpdate;
        notifyPropertyChanged(BR.canUpdate);
    }

    @Bindable
    public String getUpdateUri() {
        return updateUri;
    }

    public void setUpdateUri(String updateUri) {
        this.updateUri = updateUri;
        notifyPropertyChanged(BR.updateUri);
    }

    @Bindable
    public String getUpdateVersion() {
        return updateVersion;
    }

    @Bindable
    public String getUpdateChangeLog() {
        return updateChangeLog;
    }

    @Bindable
    public String getAssetPath() {
        return assetPath;
    }

    public void setUpdateVersion(String updateVersion) {
        this.updateVersion = updateVersion;
        notifyPropertyChanged(BR.updateVersion);
    }

    public void setUpdateChangeLog(String updateChangeLog) {
        this.updateChangeLog = updateChangeLog;
        notifyPropertyChanged(BR.updateChangeLog);
    }

    public void setAssetPath(String assetPath) {
        this.assetPath = assetPath;
        notifyPropertyChanged(BR.assetPath);
    }

    @Bindable
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
        notifyPropertyChanged(BR.version);
    }

    @Bindable
    public String getAssetSize() {
        return assetSize;
    }

    public void setAssetSize(String assetSize) {
        this.assetSize = assetSize;
        notifyPropertyChanged(BR.assetSize);
    }
}
