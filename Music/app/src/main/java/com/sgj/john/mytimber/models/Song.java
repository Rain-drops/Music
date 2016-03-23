package com.sgj.john.mytimber.models;

/**
 * Created by John on 2016/3/9.
 */
public class Song {
    public final long id;
    public final long albumId;
    public final long artistId;
    public final int duration;
    public final int trackNumber;
    public final String albumName;
    public final String artistName;
    public final String title;

    public Song() {
        this.id = -1;
        this.albumId = -1;
        this.artistId = -1;
        this.title = "";
        this.artistName = "";
        this.albumName = "";
        this.duration = -1;
        this.trackNumber = -1;
    }

    public Song(long _id, long _albumId, long _artistId, String _title, String _artistName, String _albumName, int _duration, int _trackNumber) {
        this.id = _id;
        this.albumId = _albumId;
        this.artistId = _artistId;
        this.title = _title;
        this.artistName = _artistName;
        this.albumName = _albumName;
        this.duration = _duration;
        this.trackNumber = _trackNumber;
    }
}
