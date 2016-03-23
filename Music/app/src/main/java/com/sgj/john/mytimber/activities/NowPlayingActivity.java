package com.sgj.john.mytimber.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.sgj.john.mytimber.R;
import com.sgj.john.mytimber.fragments.TestFragment;
import com.sgj.john.mytimber.fragments.nowplaying.NowplayingFragmentTest;
import com.sgj.john.mytimber.utils.Constants;

/**
 * Created by John on 2016/3/14.
 */
public class NowPlayingActivity extends BaseActivity {

    private static final String TAG = "NowPlayingActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nowplaying);
        SharedPreferences prefs = getSharedPreferences(Constants.FRAGMENT_ID, Context.MODE_PRIVATE);
        String fragmentID = prefs.getString(Constants.NOWPLAYING_FRAGMENT_ID, Constants.TIMBER3);

        Fragment fragment = NowplayingFragmentTest.getNowplayingFragmentTest();
        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment).commit();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("now_playing_theme_value", false)){

            recreate();
        }
    }
}
