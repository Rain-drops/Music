package com.sgj.john.mytimber.fragments.nowplaying;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sgj.john.mytimber.R;
import com.sgj.john.mytimber.listeners.MusicStateListener;
import com.sgj.john.mytimber.widgets.PlayPauseButton;

import net.steamcrafted.materialiconlib.MaterialIconView;

/**
 * Created by John on 2016/3/18.
 */
public class BaseNowplayingFragment extends Fragment implements MusicStateListener {

    ImageView albumart;
    ImageView shuffle;
    ImageView repeat;
    MaterialIconView previous, next;
    PlayPauseButton mPlayPause;
//    PlayPauseDrawable playPauseDrawable = new PlayPauseDrawable();
    FloatingActionButton playPauseFloating;
    View playPauseWrapper;

    String ateKey;
    int accentColor;

    TextView songtitle, songalbum, songartist, songduration, elapsedtime;
    SeekBar mProgress;

    RecyclerView recyclerView;
//    BaseQueueAdapter mAdapter;
//    TimelyView timelyView11, timelyView12, timelyView13, timelyView14, timelyView15;
    TextView hourColon;
    int[] timeArr = new int[]{0, 0, 0, 0, 0};
    Handler mElapsedTimeHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void restartLoader() {

    }

    @Override
    public void onPlaylistChanged() {

    }

    @Override
    public void onMetaChanged() {


    }

    public void setSongDetails(View view) {
        albumart = (ImageView) view.findViewById(R.id.album_art);
        shuffle = (ImageView) view.findViewById(R.id.shuffle);
        repeat = (ImageView) view.findViewById(R.id.repeat);
        next = (MaterialIconView) view.findViewById(R.id.next);
        previous = (MaterialIconView) view.findViewById(R.id.previous);
        mPlayPause = (PlayPauseButton) view.findViewById(R.id.playpause);
        playPauseFloating = (FloatingActionButton) view.findViewById(R.id.playpausefloating);
        playPauseWrapper = view.findViewById(R.id.playpausewrapper);

        songtitle = (TextView) view.findViewById(R.id.song_title);
        songalbum = (TextView) view.findViewById(R.id.song_album);
        songartist = (TextView) view.findViewById(R.id.song_artist);
        songduration = (TextView) view.findViewById(R.id.song_duration);
        elapsedtime = (TextView) view.findViewById(R.id.song_elapsed_time);

        mProgress = (SeekBar) view.findViewById(R.id.song_progress);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
    }

    public void setMusicStateListener() {

    }

}
