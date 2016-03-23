package com.sgj.john.mytimber.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.sgj.john.mytimber.MusicPlayer;
import com.sgj.john.mytimber.R;
import com.sgj.john.mytimber.fragments.MainFragment;
import com.sgj.john.mytimber.subfragments.QuickControlsFragment;
import com.sgj.john.mytimber.slidinguppanel.SlidingUpPanelLayout;
import com.sgj.john.mytimber.utils.Constants;
import com.sgj.john.mytimber.utils.Helpers;
import com.sgj.john.mytimber.utils.TimberUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by John on 2016/3/9.
 */
public class MainActivity extends BaseActivity implements View.OnClickListener{

    private static MainActivity sMainActivity;
    private Context mContext;
    private static final String TAG = "MainActivity";
    private FrameLayout mFrameLayout;
    private DrawerLayout mDrawerLayout;
    SlidingUpPanelLayout panelLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    Handler navDrawerRunnable = new Handler();
    NavigationView navigationView;
    ImageView albumart;

    String action;
    private boolean isDarkTheme;

    Map<String, Runnable> navigationMap = new HashMap<String, Runnable>();

    Runnable runnable;
    Runnable navigateLibrary = new Runnable() {
        @Override
        public void run() {
            navigationView.getMenu().findItem(R.id.nav_library).setChecked(true);
            Fragment fragment = new MainFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
        }
    };
    Runnable navigateNowplaying = new Runnable() {
        public void run() {
            navigateLibrary.run();
            mContext.startActivity(new Intent(MainActivity.this, NowPlayingActivity.class));
            Log.d(TAG, "  -- navigateNowplaying --  ");
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        sMainActivity = this;
        action = getIntent().getAction();
        Log.d(TAG, " -- action -- " + action);

        isDarkTheme = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false);
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_main_one);

        mContext = this;

        navigationMap.put(Constants.NAVIGATE_LIBRARY, navigateLibrary);
        navigationMap.put(Constants.NAVIGATE_NOWPLAYING, navigateNowplaying);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        panelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);

        setPanelSlideListeners(panelLayout);

        init();


    }
    public void setPanelSlideListeners(SlidingUpPanelLayout panelLayout) {
        panelLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {

            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                View nowPlayingCard = QuickControlsFragment.topContainer;
                nowPlayingCard.setAlpha(1 - slideOffset);
            }

            @Override
            public void onPanelCollapsed(View panel) {
                View nowPlayingCard = QuickControlsFragment.topContainer;
                nowPlayingCard.setAlpha(1);
            }

            @Override
            public void onPanelExpanded(View panel) {
                View nowPlayingCard = QuickControlsFragment.topContainer;
                nowPlayingCard.setAlpha(0);
            }

            @Override
            public void onPanelAnchored(View panel) {

            }

            @Override
            public void onPanelHidden(View panel) {

            }
        });
    }

    private void init() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, R.string.settings, R.string.settings){

        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

//        MainFragment fragment = MainFragment.newInstance();
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.replace(R.id.fl_content_home, fragment);
//        transaction.commit();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
//        View header = navigationView.inflateHeaderView(R.layout.nav_header);
//        albumart = (ImageView) header.findViewById(R.id.album_art);



        navDrawerRunnable.postDelayed(new Runnable() {
            @Override
            public void run() {
                setupDrawerContent(navigationView);
            }
        }, 500);

        if (TimberUtils.isMarshmallow()) {
            Log.d(TAG, " -- TimberUtils.isMarshmallow() -- ");
            checkPermissionAndThenLoad();
        } else {
            Log.d(TAG, " -- ! TimberUtils.isMarshmallow() -- ");
            loadEverything();
        }

        addBackstackListener();

        if(Intent.ACTION_VIEW.equals(action)) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MusicPlayer.clearQueue();
                    MusicPlayer.openFile(getIntent().getData().getPath());
                    MusicPlayer.playOrPause();
                    navigateNowplaying.run();
                }
            }, 350);
        }

    }

    private void loadEverything() {
        Runnable navigation = navigationMap.get(action);
        if (navigation != null) {
            navigation.run();
        } else {
            navigateLibrary.run();
        }

        new initQuickControls().execute("");
    }

    private void checkPermissionAndThenLoad() {
        //check for permission
//        if (Nammu.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
//            loadEverything();
//        } else {
//            if (Nammu.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                Snackbar.make(panelLayout, "Timber will need to read external storage to display songs on your device.",
//                        Snackbar.LENGTH_INDEFINITE)
//                        .setAction("OK", new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                Nammu.askForPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE, permissionReadstorageCallback);
//                            }
//                        }).show();
//            } else {
//                Nammu.askForPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE, permissionReadstorageCallback);
//            }
//        }
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem item) {
                        updatePosition(item);
                        return true;
                    }
                });
    }

    private void addBackstackListener() {
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                getSupportFragmentManager().findFragmentById(R.id.fragment_container).onResume();
            }
        });
    }

    private void updatePosition(MenuItem item) {
        runnable = null;
        Intent intent = new Intent();
        switch (item.getItemId()){
            case R.id.nav_library:
                runnable = navigateLibrary;
                break;
            case R.id.nav_playlists:
                mDrawerLayout.closeDrawers();
                ToastShow("playlists");
                break;
            case R.id.nav_nowplaying:
                mDrawerLayout.closeDrawers();
//                ToastShow("nowplaying");
//                NavigationUtils.navigateToNowplaying(MainActivity.this, false);

                intent.setClass(this, NowPlayingActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_queue:
                mDrawerLayout.closeDrawers();
                ToastShow("queue");
//                runnable = navigateQueue;

                break;
            case R.id.nav_settings:
                mDrawerLayout.closeDrawers();
                ToastShow("settings");
//                NavigationUtils.navigateToSettings(MainActivity.this);
                break;
            case R.id.nav_help:
//                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setAction(Intent.ACTION_VIEW);
                Uri uri = Uri.parse("mailto:710637881@qq.com");
                intent.setData(uri);
                startActivity(intent);
                break;
            case R.id.nav_about:
                mDrawerLayout.closeDrawers();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Helpers.showAbout(MainActivity.this);
                    }
                }, 350);
                break;
        }
        if(runnable != null){
            item.setChecked(true);
            mDrawerLayout.closeDrawers();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                  runnable.run();
                }
            }, 350);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                Toast.makeText(this, "heheheh", Toast.LENGTH_SHORT).show();
                break;
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

    }

    private void ToastShow(String str){
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }
}
