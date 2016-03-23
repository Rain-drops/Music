package com.sgj.john.mytimber;

import android.app.Application;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by John on 2016/3/10.
 */
public class TimberApp extends Application {

    private static TimberApp mInstance;
    public static synchronized TimberApp getInstance(){
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        ImageLoaderConfiguration localImageLoaderConfiguration = new ImageLoaderConfiguration.Builder(mInstance).build();
        ImageLoader.getInstance().init(localImageLoaderConfiguration);

    }
}
