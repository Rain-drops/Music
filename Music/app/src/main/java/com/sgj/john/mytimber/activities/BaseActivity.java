package com.sgj.john.mytimber.activities;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.sgj.john.mytimber.ITimberService;
import com.sgj.john.mytimber.MusicPlayer;
import com.sgj.john.mytimber.MusicService;
import com.sgj.john.mytimber.R;
import com.sgj.john.mytimber.subfragments.QuickControlsFragment;
import com.sgj.john.mytimber.listeners.MusicStateListener;
import com.sgj.john.mytimber.utils.TimberUtils;

import static com.sgj.john.mytimber.MusicPlayer.mService;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by John on 2016/3/9.
 */
public class BaseActivity extends AppCompatActivity implements ServiceConnection, MusicStateListener{

    private static final String TAG = "BaseActivity";

    private ArrayList<MusicStateListener> mMusicStateListener = new ArrayList<>();
    private MusicPlayer.ServiceToken mToken;
    private PlaybackStatus mPlaybackStatus;

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mService = ITimberService.Stub.asInterface(service);
        onMetaChanged();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mService = null;
    }

    @Override
    public void restartLoader() {
        // Let the listener know to update a list
        for(MusicStateListener listener : mMusicStateListener){
            if(listener != null){
                listener.restartLoader();
            }
        }
    }

    @Override
    public void onPlaylistChanged() {
        // Let the listener know to update a list
        for(MusicStateListener listener : mMusicStateListener){
            if(listener != null){
                listener.onPlaylistChanged();
            }
        }
    }

    @Override
    public void onMetaChanged() {
        //Let the listener know to the meta changed
        for (MusicStateListener listener : mMusicStateListener){
            if(listener != null){
                listener.onMetaChanged();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToken = MusicPlayer.bindToService(this, this);
        Log.d(TAG, " mToken = " + mToken);
        mPlaybackStatus = new PlaybackStatus(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        final IntentFilter filter = new IntentFilter();
        // Play and pause changes
        filter.addAction(MusicService.PLAYSTATE_CHANGED);
        // Track changes(乐曲声道、追踪搜索？变更)
        filter.addAction(MusicService.META_CHANGED);
        // Update the list, probably the playlist fragment's
        filter.addAction(MusicService.REFRESH);
        // If a playlist changed, notify us
        filter.addAction(MusicService.PLAYSTATE_CHANGED);
        // If there is an error playing a track
        filter.addAction(MusicService.TRACK_ERROR);

        registerReceiver(mPlaybackStatus, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onMetaChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        if(mToken != null){
            MusicPlayer.unbindFromService(mToken);
            mToken = null;
        }

        try {
            unregisterReceiver(mPlaybackStatus);
        } catch (final Throwable e) {

        }
        mMusicStateListener.clear();

    }


    public void setMusicStateListenerListener(MusicStateListener listener){
        if(listener == this){
            throw new UnsupportedOperationException("Override the method, don't add a listener");
        }
        if(listener != null){
            mMusicStateListener.add(listener);
        }
    }

    public void removeMusicStateListenerListener(MusicStateListener listener){
        if(listener != null){
            mMusicStateListener.remove(listener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if(!TimberUtils.hasEffectsPanel(BaseActivity.this)){
            menu.removeItem(R.id.action_equalizer);
        }
//        ATE.applyMenu(this, getATEKey(), menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.action_settings:
//                NavigationUtils.navigateToSettings(this);
                return true;
            case R.id.action_shuffle:
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MusicPlayer.shuffleAll(BaseActivity.this);
                    }
                }, 80);

                return true;
            case R.id.action_search:
//                NavigationUtils.navigateToSearch(this);
                return true;
            case R.id.action_equalizer:
//                NavigationUtils.navigateToEqualizer(this);
                return true;
        }


        return super.onOptionsItemSelected(item);
    }

    /**
     * TODO 设置面板幻灯片监听
     */
    public void setPanelSlideListeners(){

    }

    /**
     *
     */
    private final static class PlaybackStatus extends BroadcastReceiver{

        private final WeakReference<BaseActivity> mReference;

        public PlaybackStatus(BaseActivity activity) {
            this.mReference = new WeakReference<BaseActivity>(activity);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            BaseActivity baseActivity = mReference.get();
            if(baseActivity != null){
                if(action.equals(MusicService.META_CHANGED)){
                    baseActivity.onMetaChanged();
                }else if(action.equals(MusicService.PLAYSTATE_CHANGED)){
//                    baseActivity.mPlayPauseProgressButton.getPlayPauseButton().updateState();
                }else if(action.equals(MusicService.REFRESH)){
                    baseActivity.restartLoader();
                }else if(action.equals(MusicService.PLAYLIST_CHANGED)){
                    baseActivity.onPlaylistChanged();
                }else if(action.equals(MusicService.TRACK_ERROR)){
                    final String errorMsg = context.getString(R.string.error_playing_track,
                            intent.getStringExtra(MusicService.TrackErrorExtra.TRACK_NAME));
                    Toast.makeText(baseActivity, errorMsg, Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    /**
     * 初始化快速控制
     */
    public class initQuickControls extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            QuickControlsFragment fragment = QuickControlsFragment.getInstance();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.quickcontrols_container, fragment).commitAllowingStateLoss();
            return "Executed";
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            QuickControlsFragment.topContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, " QuickControlsFragment.topContainer.setOnClickListener ");
                }
            });
        }
    }

}
