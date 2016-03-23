package com.sgj.john.mytimber;

import android.annotation.TargetApi;
import android.media.browse.MediaBrowser;
import android.os.Build;
import android.os.Bundle;
import android.service.media.MediaBrowserService;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created by John on 2016/3/11.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class WearBrowserService extends MediaBrowserService {
    @Nullable
    @Override
    public BrowserRoot onGetRoot(String clientPackageName, int clientUid, Bundle rootHints) {
        return null;
    }

    @Override
    public void onLoadChildren(String parentId, Result<List<MediaBrowser.MediaItem>> result) {

    }
}
