package com.sgj.john.mytimber.listeners;

/**
 * Created by John on 2016/3/9.
 */
public interface MusicStateListener {
    /**
     * Called when {@link com.sgj.john.MusicService#REFRESH} is invoked
     */
    void restartLoader();

    /**
     * Called when {@link com.sgj.john.MusicService#PLAYLIST_CHANGED} is invoked
     */
    void onPlaylistChanged();

    /**
     * Called when {@link com.sgj.john.MusicService#META_CHANGED} is invoked
     */
    void onMetaChanged();
}
