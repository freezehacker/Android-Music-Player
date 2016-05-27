package com.demo.vita.vitamusicplayer;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

/**
 * Created by sjk on 2016/3/19.
 */
public class MusicBean extends DataSupport {

    @Column(nullable = false)
    private String title;

    private String artist;

    @Column(ignore = true)
    private String detail;

    private String album;

    // 默认不喜欢，如果喜欢就是收藏了该歌曲
    @Column(defaultValue = "false")
    private boolean isLike;

    private int ducation;

    private String lyrics;

    private String url;

    @Column(unique = true)
    private int songId;     // 注意，这个id是从另一数据库那里拿过来的，跟本数据库的id字段不一样

    private Bitmap cover;

    private int albumId;

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    public int getAlbumId() {
        return albumId;
    }

    public Bitmap getCover() {
        return cover;
    }

    public void setCover(Bitmap cover) {
        this.cover = cover;
    }

    public String getLyrics() {
        return lyrics;
    }

    public boolean isLike() {
        return isLike;
    }

    public void setIsLike(boolean isLike) {
        this.isLike = isLike;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getDucation() {
        return ducation;
    }

    public void setDucation(int ducation) {
        this.ducation = ducation;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }
}
