package com.widget.noname.plus.function;

import java.util.Arrays;

public class UpdateInfo {
    private String version;
    private String update;
    private String[] changeLog;
    private String[] files;
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }

    public String[] getChangeLog() {
        return changeLog;
    }

    public void setChangeLog(String[] changeLog) {
        this.changeLog = changeLog;
    }

    public String[] getFiles() {
        return files;
    }

    public void setFiles(String[] files) {
        this.files = files;
    }

    @Override
    public String toString() {
        return "UpdateInfo{" +
                "version='" + version + '\'' +
                ", update='" + update + '\'' +
                ", changeLog=" + Arrays.toString(changeLog) +
                ", files=" + Arrays.toString(files) +
                '}';
    }
}
