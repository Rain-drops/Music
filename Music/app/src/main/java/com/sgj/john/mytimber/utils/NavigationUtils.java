package com.sgj.john.mytimber.utils;

import android.support.v4.app.Fragment;

import com.sgj.john.mytimber.fragments.nowplaying.Timber3;

/**
 * Created by John on 2016/3/18.
 */
public class NavigationUtils {

    public static Fragment getFragmentForNowplayingID(String fragmentID) {
        switch (fragmentID) {
//            case Constants.TIMBER1:
//                return new Timber1();
//            case Constants.TIMBER2:
//                return new Timber2();
            case Constants.TIMBER3:
                return new Timber3();
//            case Constants.TIMBER4:
//                return new Timber4();
            default:
                return new Timber3();
        }

    }
}
