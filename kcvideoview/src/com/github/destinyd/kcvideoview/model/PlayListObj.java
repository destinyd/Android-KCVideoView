package com.github.destinyd.kcvideoview.model;

import java.io.Serializable;

/**
 * Created by dd on 14-4-16.
 */
public class PlayListObj implements Serializable {
    private static final long serialVersionUID = 1253323389L;

    private int id;

    private int seconds;

    private int size;

    private String url;

    private int secondsCount;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getSecondsCount() {
        return secondsCount;
    }

    public void setSecondsCount(int secondsCount) {
        this.secondsCount = secondsCount;
    }
}
