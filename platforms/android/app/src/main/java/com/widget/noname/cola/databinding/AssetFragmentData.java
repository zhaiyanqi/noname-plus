package com.widget.noname.cola.databinding;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.widget.noname.cola.BR;

public class AssetFragmentData extends BaseObservable {
    private String assetPath = null;
    private String version = null;
    private String assetSize = null;
    private String githubVersion = null;
    private String githubChangeLog = null;
    private String giteeVersion = null;
    private String giteeChangeLog = null;
    private String codingVersion = null;
    private String codingChangeLog = null;

    @Bindable
    public String getGithubVersion() {
        return githubVersion;
    }

    @Bindable
    public String getGithubChangeLog() {
        return githubChangeLog;
    }

    @Bindable
    public String getGiteeVersion() {
        return giteeVersion;
    }

    @Bindable
    public String getGiteeChangeLog() {
        return giteeChangeLog;
    }

    @Bindable
    public String getCodingVersion() {
        return codingVersion;
    }

    public void setGithubVersion(String githubVersion) {
        this.githubVersion = githubVersion;
        notifyPropertyChanged(BR.githubVersion);
    }

    public void setGithubChangeLog(String githubChangeLog) {
        this.githubChangeLog = githubChangeLog;
        notifyPropertyChanged(BR.githubChangeLog);
    }

    public void setGiteeVersion(String giteeVersion) {
        this.giteeVersion = giteeVersion;
        notifyPropertyChanged(BR.giteeVersion);
    }

    public void setGiteeChangeLog(String giteeChangeLog) {
        this.giteeChangeLog = giteeChangeLog;
        notifyPropertyChanged(BR.giteeChangeLog);
    }

    public void setCodingVersion(String codingVersion) {
        this.codingVersion = codingVersion;
        notifyPropertyChanged(BR.codingVersion);
    }

    public void setCodingChangeLog(String codingChangeLog) {
        this.codingChangeLog = codingChangeLog;
        notifyPropertyChanged(BR.codingChangeLog);
    }

    @Bindable
    public String getCodingChangeLog() {
        return codingChangeLog;
    }

    @Bindable
    public String getAssetPath() {
        return assetPath;
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
