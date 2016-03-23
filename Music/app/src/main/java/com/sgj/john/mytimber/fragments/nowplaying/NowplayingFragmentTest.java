package com.sgj.john.mytimber.fragments.nowplaying;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.sgj.john.mytimber.MusicPlayer;
import com.sgj.john.mytimber.R;
import com.sgj.john.mytimber.activities.BaseActivity;
import com.sgj.john.mytimber.adapters.BaseQueueAdapter;
import com.sgj.john.mytimber.listeners.MusicStateListener;
import com.sgj.john.mytimber.utils.TimberUtils;
import com.sgj.john.mytimber.widgets.PlayPauseButton;
import com.sgj.john.mytimber.widgets.PlayPauseDrawable;

import net.steamcrafted.materialiconlib.MaterialIconView;

/**
 * Created by John on 2016/3/18.
 */
public class NowplayingFragmentTest extends Fragment implements MusicStateListener{

    ImageView albumart;
    ImageView shuffle;
    ImageView repeat;
    MaterialIconView previous, next;
    PlayPauseDrawable playPauseDrawable = new PlayPauseDrawable();
    FloatingActionButton playPauseFloating;

    String ateKey;
    int accentColor;

    TextView songtitle, songalbum, songartist, songduration, elapsedtime;
    SeekBar mProgress;

    public Runnable mUpdateProgress = new Runnable() {
        @Override
        public void run() {
            if(mProgress != null){
                long position = MusicPlayer.position();
                mProgress.setProgress((int)position);
                if(elapsedtime != null && getActivity() != null){
                    elapsedtime.setText(TimberUtils.makeShortTimeString(getActivity(), position / 1000));
                }
            }
            if(MusicPlayer.isPlaying()){
                mProgress.postDelayed(mUpdateProgress, 50);
            }
        }
    };
    private boolean duetoplaypause = false;
    private final View.OnClickListener mFLoatingButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            duetoplaypause = true;
            playPauseDrawable.transformToPlay(true);
            playPauseDrawable.transformToPause(true);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MusicPlayer.playOrPause();
                }
            }, 250);
        }
    };

    public static NowplayingFragmentTest getNowplayingFragmentTest(){
        NowplayingFragmentTest fragment = new NowplayingFragmentTest();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timber3, container, false);
        setMusicStateListener();
        setSongDetails(view);



        return view;
    }

    private void setSongDetails(View view) {
        albumart = (ImageView) view.findViewById(R.id.album_art);
        shuffle = (ImageView) view.findViewById(R.id.shuffle);
        repeat = (ImageView) view.findViewById(R.id.repeat);
        next = (MaterialIconView) view.findViewById(R.id.next);
        previous = (MaterialIconView) view.findViewById(R.id.previous);
        playPauseFloating = (FloatingActionButton) view.findViewById(R.id.playpausefloating);

        songtitle = (TextView) view.findViewById(R.id.song_title);
        songalbum = (TextView) view.findViewById(R.id.song_album);
        songartist = (TextView) view.findViewById(R.id.song_artist);
        songduration = (TextView) view.findViewById(R.id.song_duration);
        elapsedtime = (TextView) view.findViewById(R.id.song_elapsed_time);


        mProgress = (SeekBar) view.findViewById(R.id.song_progress);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);

        if(toolbar != null){
            ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
            ActionBar bar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setTitle("");
        }

        if(playPauseFloating != null){
            // PorterDuff : 颜色渲染(绘制图层)。 MULTIPLY：取两层绘制交集
            playPauseFloating.setColorFilter(TimberUtils.getBlackWhiteColor(accentColor),
                    PorterDuff.Mode.MULTIPLY);
            playPauseFloating.setImageDrawable(playPauseDrawable);

            updatePlayPauseFloatingButton();
        }

        if(albumart != null){
            ImageLoader.getInstance().displayImage(
                    TimberUtils.getAlbumArtUri(MusicPlayer.getCurrentArtistId()).toString(),
                    albumart,
                    new DisplayImageOptions.Builder().cacheInMemory(true).
                            showImageOnFail(R.drawable.ic_empty_music2).build(),
                    new SimpleImageLoadingListener(){
                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            Bitmap bitmap = ImageLoader.getInstance().loadImageSync("drawable://" +
                            R.drawable.ic_empty_music2);
                            doAlbumArtStuff(bitmap);
                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            doAlbumArtStuff(loadedImage);
                        }
                    });
        }

        if(next != null){
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                          MusicPlayer.next();
                          notifyPlayingDrawableChange();
                        }
                    }, 200);
                }
            });
        }

        if(previous != null){
            previous.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MusicPlayer.previous(getActivity(), false);
                            notifyPlayingDrawableChange();
                        }
                    }, 200);
                }
            });
        }

        if(playPauseFloating != null){
            playPauseFloating.setOnClickListener(mFLoatingButtonListener);
        }

        setSongDetails();
    }

    private void setSongDetails() {
        updateSongDetails();

        setSeekBarListener();
    }

    private void setSeekBarListener() {
        if(mProgress != null){
            mProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    if(fromUser){
                        MusicPlayer.seek((long)progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
    }

    private void notifyPlayingDrawableChange() {

        int position = MusicPlayer.getQueuePosition();
        BaseQueueAdapter.currentlyPlayingPosition = position;

        updateShuffleState();
        updateRepeatState();

    }

    private void updateRepeatState() {


    }

    private void updateShuffleState() {

    }

    private void updatePlayPauseFloatingButton() {
        if (MusicPlayer.isPlaying())
            playPauseDrawable.transformToPause(false);
        else playPauseDrawable.transformToPlay(false);
    }

    private void doAlbumArtStuff(Bitmap loadedImage) {
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
        updateSongDetails();
    }

    private void updateSongDetails() {
        if (songtitle != null)
            songtitle.setText(MusicPlayer.getTrackName());

        if (songalbum != null)
            songalbum.setText(MusicPlayer.getAlbumName());

        if (songartist != null)
            songartist.setText(MusicPlayer.getArtistName());

        if(songduration != null && getActivity() != null){
            songduration.setText(TimberUtils.makeShortTimeString(getActivity(), MusicPlayer.duration()/1000));
        }

        if(mProgress != null){
            mProgress.setMax((int) MusicPlayer.duration());
            if(mUpdateProgress != null){
                mProgress.removeCallbacks(mUpdateProgress);
            }
            mProgress.postDelayed(mUpdateProgress, 10);
        }

        updateShuffleState();
        updateRepeatState();
    }

    public void setMusicStateListener() {
        ((BaseActivity) getActivity()).setMusicStateListenerListener(this);
    }
}
