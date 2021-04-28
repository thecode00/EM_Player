package com.thecode.emplayer;

import java.io.Serializable;

public class Song implements Serializable {
    private long mId;
    private long mAlbumId;
    private String mTitle;
    private String mAlbum;
    private String mArtist;
    private long mDuration;
    private String mDataPath;

    public long getmId() {
        return mId;
    }

    public void setmId(long mId) {
        this.mId = mId;
    }

    public long getmAlbumId() {
        return mAlbumId;
    }

    public void setmAlbumId(long mAlbumId) {
        this.mAlbumId = mAlbumId;
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getmAlbum() {
        return mAlbum;
    }

    public void setmAlbum(String mAlbum) {
        this.mAlbum = mAlbum;
    }

    public String getmArtist() {
        return mArtist;
    }

    public void setmArtist(String mArtist) {
        this.mArtist = mArtist;
    }

    public long getmDuration() {
        return mDuration;
    }

    public void setmDuration(long mDuration) {
        this.mDuration = mDuration;
    }

    public String getmDataPath() {
        return mDataPath;
    }

    public void setmDataPath(String mDataPath) {
        this.mDataPath = mDataPath;
    }

    public Song(long mId, long mAlbumId, String mTitle, String mAlbum, String mArtist, long mDuration, String mDataPath) {
        this.mId = mId;
        this.mAlbumId = mAlbumId;
        this.mTitle = mTitle;
        this.mAlbum = mAlbum;
        this.mArtist = mArtist;
        this.mDuration = mDuration;
        this.mDataPath = mDataPath;
    }
}