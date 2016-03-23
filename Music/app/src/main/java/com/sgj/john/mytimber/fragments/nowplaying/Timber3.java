package com.sgj.john.mytimber.fragments.nowplaying;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sgj.john.mytimber.R;

/**
 * Created by John on 2016/3/18.
 */
public class Timber3 extends BaseNowplayingFragment {

    public static Timber3 getInstance(){
        Timber3 timber3 = new Timber3();
        return timber3;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timber3, container, false);

        setMusicStateListener();
        setSongDetails(view);

        return view;
    }


}
