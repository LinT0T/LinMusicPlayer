package com.lint0t.linmusicplayer.bean;

import android.graphics.Bitmap;

public class Bean {
    private String name,singer,time,path,id;
    private Bitmap bitmap;
    public Bean() {
    }

    public Bean(String name, String singer, String time, String path, String id, Bitmap bitmap) {
        this.name = name;
        this.singer = singer;
        this.time = time;
        this.path = path;
        this.id = id;
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


}
