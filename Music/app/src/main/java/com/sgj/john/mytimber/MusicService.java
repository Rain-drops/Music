package com.sgj.john.mytimber;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.*;
import android.os.Process;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sgj.john.mytimber.helpers.MediaButtonIntentReceiver;
import com.sgj.john.mytimber.helpers.MusicPlaybackTrack;
import com.sgj.john.mytimber.provider.MusicPlaybackState;
import com.sgj.john.mytimber.provider.RecentStore;
import com.sgj.john.mytimber.provider.SongPlayCount;
import com.sgj.john.mytimber.utils.TimberUtils;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;
import java.util.TreeSet;

/**
 * Created by John on 2016/3/11.
 */
@SuppressLint("NewApi")
public class MusicService extends Service {

    public static final String PLAYSTATE_CHANGED = "com.naman14.timber.playstatechanged";
    public static final String POSITION_CHANGED = "com.naman14.timber.positionchanged";
    public static final String META_CHANGED = "com.naman14.timber.metachanged";
    public static final String QUEUE_CHANGED = "com.naman14.timber.queuechanged";
    public static final String PLAYLIST_CHANGED = "com.naman14.timber.playlistchanged";
    public static final String REPEATMODE_CHANGED = "com.naman14.timber.repeatmodechanged";
    public static final String SHUFFLEMODE_CHANGED = "com.naman14.timber.shufflemodechanged";
    public static final String TRACK_ERROR = "com.naman14.timber.trackerror";
    public static final String TIMBER_PACKAGE_NAME = "com.naman14.timber";
    public static final String MUSIC_PACKAGE_NAME = "com.android.music";
    public static final String SERVICECMD = "com.naman14.timber.musicservicecommand";
    public static final String TOGGLEPAUSE_ACTION = "com.naman14.timber.togglepause";
    public static final String PAUSE_ACTION = "com.naman14.timber.pause";
    public static final String STOP_ACTION = "com.naman14.timber.stop";
    public static final String PREVIOUS_ACTION = "com.naman14.timber.previous";
    public static final String PREVIOUS_FORCE_ACTION = "com.naman14.timber.previous.force";
    public static final String NEXT_ACTION = "fcom.naman14.timber.next";
    public static final String REPEAT_ACTION = "com.naman14.timber.repeat";
    public static final String SHUFFLE_ACTION = "com.naman14.timber.shuffle";
    public static final String FROM_MEDIA_BUTTON = "frommediabutton";
    public static final String REFRESH = "com.naman14.timber.refresh";
    public static final String UPDATE_LOCKSCREEN = "com.naman14.timber.updatelockscreen";
    public static final String CMDNAME = "command";
    public static final String CMDTOGGLEPAUSE = "togglepause";
    public static final String CMDSTOP = "stop";
    public static final String CMDPAUSE = "pause";
    public static final String CMDPLAY = "play";
    public static final String CMDPREVIOUS = "previous";
    public static final String CMDNEXT = "next";
    public static final String CMDNOTIF = "buttonId";
    public static final int NEXT = 2;
    public static final int LAST = 3;
    public static final int SHUFFLE_NONE = 0;
    public static final int SHUFFLE_NORMAL = 1;
    public static final int SHUFFLE_AUTO = 2;
    public static final int REPEAT_NONE = 0;
    public static final int REPEAT_CURRENT = 1;
    public static final int REPEAT_ALL = 2;
    public static final int MAX_HISTORY_SIZE = 1000;
    private static final String TAG = "MusicPlaybackService";
    private static final boolean D = false;
    private static final String SHUTDOWN = "com.naman14.timber.shutdown";
    private static final int IDCOLIDX = 0;
    private static final int TRACK_ENDED = 1;
    private static final int TRACK_WENT_TO_NEXT = 2;
    private static final int RELEASE_WAKELOCK = 3;
    private static final int SERVER_DIED = 4;
    private static final int FOCUSCHANGE = 5;
    private static final int FADEDOWN = 6;
    private static final int FADEUP = 7;
    private static final int IDLE_DELAY = 5 * 60 * 1000;
    private static final long REWIND_INSTEAD_PREVIOUS_THRESHOLD = 3000;
    private static final String[] PROJECTION = new String[]{
            "audio._id AS _id", MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID
    };
    private static final String[] ALBUM_PROJECTION = new String[]{
            MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.LAST_YEAR
    };
    private static final String[] NOTIFICATION_PROJECTION = new String[]{
            "audio._id AS _id", MediaStore.Audio.AudioColumns.ALBUM_ID, MediaStore.Audio.AudioColumns.TITLE,
            MediaStore.Audio.AudioColumns.ARTIST, MediaStore.Audio.AudioColumns.DURATION
    };
    private static final Shuffler mShuffler = new Shuffler();
    private static final int NOTIFY_MODE_NONE = 0;
    private static final int NOTIFY_MODE_FOREGROUND = 1;
    private static final int NOTIFY_MODE_BACKGROUND = 2;
    private static final String[] PROJECTION_MATRIX = new String[]{
            "_id", MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID
    };
    private static LinkedList<Integer> mHistory = new LinkedList<>();
    private final IBinder mBinder = new ServiceStub(this);
    private MultiPlayer mPlayer;
    private String mFileToPlay;
    private PowerManager.WakeLock mWakeLock;
    private AlarmManager mAlarmManager;
    private PendingIntent mShutdownIntent;
    private boolean mShutdownScheduled;
    private NotificationManagerCompat mNotificationManager;
    private Cursor mCursor;
    private Cursor mAlbumCursor;
    private AudioManager mAudioManager;
    private SharedPreferences mPreferences;
    private boolean mServiceInUse = false;
    private boolean mIsSupposedToBePlaying = false;
    private long mLastPlayedTime;
    private int mNotifyMode = NOTIFY_MODE_NONE;
    private long mNotificationPostTime = 0;
    private boolean mQueueIsSaveable = true;
    private boolean mPausedByTransientLossOfFocus = false;

    private MediaSessionCompat mSession;

    private ComponentName mMediaButtonReceiverComponent;

    private int mCardId;

    private int mPlayPos = -1;

    private int mNextPlayPos = -1;

    private int mOpenFailedCounter = 0;

    private int mMediaMountedCount = 0;

    private int mShuffleMode = SHUFFLE_NONE;

    private int mRepeatMode = REPEAT_NONE;

    private int mServiceStartId = -1;

    private ArrayList<MusicPlaybackTrack> mPlaylist = new ArrayList<MusicPlaybackTrack>(100);

    private long[] mAutoShuffleList = null;

    private MusicPlayerHandler mPlayerHandler;
    private final AudioManager.OnAudioFocusChangeListener mAudioFocusListener = new AudioManager.OnAudioFocusChangeListener() {

        @Override
        public void onAudioFocusChange(final int focusChange) {
            mPlayerHandler.obtainMessage(FOCUSCHANGE, focusChange, 0).sendToTarget();
        }
    };
    private HandlerThread mHandlerThread;
    private BroadcastReceiver mUnmountReceiver = null;
    private MusicPlaybackState mPlaybackStateStore;
    private boolean mShowAlbumArtOnLockscreen;
    private SongPlayCount mSongPlayCount;
    private RecentStore mRecentStore;

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String command = intent.getStringExtra(CMDNAME);

            // 命令处理(播放、暂停、下一首等。。)
            handleCommandIntent(intent);

        }
    };

    /**
     * 命令处理(播放、暂停、下一首等。。)
     * @param intent
     */
    private void handleCommandIntent(Intent intent) {
        final String action = intent.getAction();
        final String command = SERVICECMD.equals(action) ? intent.getStringExtra(CMDNAME) : null;

        Log.d(TAG, " handleCommandIntent : action = " + action + ", command = " + command);
        if (CMDNEXT.equals(command) || NEXT_ACTION.equals(action)) { // 下一首
            gotoNext(true);
        } else if (CMDPREVIOUS.equals(command) || PREVIOUS_ACTION.equals(action)
                || PREVIOUS_FORCE_ACTION.equals(action)) { // 上一首
            prev(PREVIOUS_FORCE_ACTION.equals(action));
        } else if (CMDTOGGLEPAUSE.equals(command) || TOGGLEPAUSE_ACTION.equals(action)) { // 切换 播放/暂停
            if (isPlaying()) {
                pause();
                mPausedByTransientLossOfFocus = false;
            } else {
                play();
            }
        } else if (CMDPAUSE.equals(command) || PAUSE_ACTION.equals(action)) { // 暂停
            pause();
            mPausedByTransientLossOfFocus = false;
        } else if (CMDPLAY.equals(command)) { // 开始
            play();
        } else if (CMDSTOP.equals(command) || STOP_ACTION.equals(action)) { // 停止
            pause();
            mPausedByTransientLossOfFocus = false;
            seek(0);
//            releaseServiceUiAndStop();
        } else if (REPEAT_ACTION.equals(action)) {
//            cycleRepeat();
        } else if (SHUFFLE_ACTION.equals(action)) {
//            cycleShuffle();
        }


    }

    private ContentObserver mMediaStoreObserver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service bound, intent = " + intent);

        cancelShutdown();

        mServiceInUse = true;

        return mBinder;
    }

    private void cancelShutdown() {
        Log.d(TAG, "Cancelling delayed shutdown, scheduled = " + mShutdownScheduled);

        if(mShutdownScheduled){
            mAlarmManager.cancel(mShutdownIntent);
            mShutdownScheduled = false;
        }

    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "Service unbound");

        mServiceInUse = false;
        saveQueue(true);

        return super.onUnbind(intent);
    }

    private void saveQueue(boolean full) {
        if (!mQueueIsSaveable) {
            return;
        }

        final SharedPreferences.Editor editor = mPreferences.edit();
        if (full) {
//            mPlaybackStateStore.saveState(mPlaylist,
//                    mShuffleMode != SHUFFLE_NONE ? mHistory : null);
            editor.putInt("cardid", mCardId);
        }
        editor.putInt("curpos", mPlayPos);
        if (mPlayer.isInitialized()) {
            editor.putLong("seekpos", mPlayer.position());
        }
        editor.putInt("repeatmode", mRepeatMode);
        editor.putInt("shufflemode", mShuffleMode);
        editor.apply();
    }

    @Override
    public void onRebind(Intent intent) {
        cancelShutdown();
        mServiceInUse = true;
        super.onRebind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Creating service");

        mNotificationManager = NotificationManagerCompat.from(this);

        mHandlerThread = new HandlerThread("MusicPlayerHandler", Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();

        mPlayerHandler = new MusicPlayerHandler(this, mHandlerThread.getLooper());

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        // 进程间的交互(广播监听机制)
        mMediaButtonReceiverComponent = new ComponentName(
                getPackageName(), MediaButtonIntentReceiver.class.getName());
        mAudioManager.registerMediaButtonEventReceiver(mMediaButtonReceiverComponent);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            setUpMediaSession();
        }

        mPreferences = getSharedPreferences("Service", 0);
        mCardId = getCardId();
        // 注册外部存储监听器
        registerExternalStorageListener();

        mPlayer = new MultiPlayer(this);
        mPlayer.setHandler(mPlayerHandler);

        // Initialize the intent filter and each action
        final IntentFilter filter = new IntentFilter();
        filter.addAction(SERVICECMD);
        filter.addAction(TOGGLEPAUSE_ACTION);
        filter.addAction(PAUSE_ACTION);
        filter.addAction(STOP_ACTION);
        filter.addAction(NEXT_ACTION);
        filter.addAction(PREVIOUS_ACTION);
        filter.addAction(PREVIOUS_FORCE_ACTION);
        filter.addAction(REPEAT_ACTION);
        filter.addAction(SHUFFLE_ACTION);
        // Attach the broadcast listener
        registerReceiver(mIntentReceiver, filter);

        mMediaStoreObserver = new MediaStoreObserver(mPlayerHandler);
        getContentResolver().registerContentObserver(
                MediaStore.Audio.Media.INTERNAL_CONTENT_URI, true, mMediaStoreObserver);
        getContentResolver().registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, mMediaStoreObserver);

        // Initialize the wake lock
        final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        mWakeLock.setReferenceCounted(false);


        final Intent shutdownIntent = new Intent(this, MusicService.class);
        shutdownIntent.setAction(SHUTDOWN);

        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mShutdownIntent = PendingIntent.getService(this, 0, shutdownIntent, 0);
        //延迟关闭
        scheduleDelayedShutdown();
        //在权限检查后重新加载队列
//        reloadQueueAfterPermissionCheck();
        notifyChange(QUEUE_CHANGED);
        notifyChange(META_CHANGED);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Got new intent " + intent + ", startId = " + startId);
        mServiceStartId = startId;
        if(intent != null){
            final String action = intent.getAction();
            if(SHUTDOWN.equals(action)){
                mShutdownScheduled = false;
                releaseServiceUiAndStop();
                return START_NOT_STICKY;
            }
            handleCommandIntent(intent);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void registerExternalStorageListener() {
        //取消安装接收器
        if (mUnmountReceiver == null) {

        }
    }

    private int getCardId() {
        return 0;
    }

    /**
     * 耳机监听
     */
    private void setUpMediaSession() {
        //Android系统会把来自蓝牙控制器或者是耳机等其他设备的操作事件转换成Media Button事件传递出来，
        // 如果我们的应用程序需要监听这些事件并做出相应的响应，就需要注册MEDIA_BUTTON的action，
        // 接收到这些事件之后，再传递给音乐播放模块进行控制处理。
        mSession = new MediaSessionCompat(this, "Timber");

        mSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPause() {
                pause();
                mPausedByTransientLossOfFocus = false;
            }

            @Override
            public void onPlay() {
                play();
            }

            @Override
            public void onSeekTo(long pos) {
                seek(pos);
            }

            @Override
            public void onSkipToNext() {
                gotoNext(true);
            }

            @Override
            public void onSkipToPrevious() {
                prev(false);
            }

            @Override
            public void onStop() {
                pause();
                mPausedByTransientLossOfFocus = false;
                seek(0);
                releaseServiceUiAndStop();
            }
        });
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
    }

    private void releaseServiceUiAndStop() {

    }

    public interface TrackErrorExtra {

        String TRACK_NAME = "trackname";
    }

    private static final class MusicPlayerHandler extends Handler {
        private final WeakReference<MusicService> mService;
        private float mCurrentVolume = 1.0f;


        public MusicPlayerHandler(final MusicService service, final Looper looper) {
            super(looper);
            mService = new WeakReference<MusicService>(service);
        }


        @Override
        public void handleMessage(final Message msg) {
            Log.d(TAG, " -- MusicPlayerHandler -- ");
            final MusicService service = mService.get();
            if (service == null) {
                return;
            }

            synchronized (service) {
                switch (msg.what) {
                    case FADEDOWN:
                        mCurrentVolume -= .05f;
                        if (mCurrentVolume > .2f) {
                            sendEmptyMessageDelayed(FADEDOWN, 10);
                        } else {
                            mCurrentVolume = .2f;
                        }
                        service.mPlayer.setVolume(mCurrentVolume);
                        break;
                    case FADEUP:
                        mCurrentVolume += .01f;
                        if (mCurrentVolume < 1.0f) {
                            sendEmptyMessageDelayed(FADEUP, 10);
                        } else {
                            mCurrentVolume = 1.0f;
                        }
                        service.mPlayer.setVolume(mCurrentVolume);
                        break;
                    case SERVER_DIED:

                        break;
                    case TRACK_WENT_TO_NEXT:

                        break;
                    case TRACK_ENDED:

                        break;
                    case RELEASE_WAKELOCK:
                        service.mWakeLock.release();
                        break;
                    case FOCUSCHANGE:

                        break;
                    default:
                        break;
                }
            }
        }
    }

    private static final class Shuffler {

        private final LinkedList<Integer> mHistoryOfNumbers = new LinkedList<Integer>();

        private final TreeSet<Integer> mPreviousNumbers = new TreeSet<Integer>();

        private final Random mRandom = new Random();

        private int mPrevious;


        public Shuffler() {
            super();
        }


        public int nextInt(final int interval) {
            int next;
            do {
                next = mRandom.nextInt(interval);
            } while (next == mPrevious && interval > 1
                    && !mPreviousNumbers.contains(Integer.valueOf(next)));
            mPrevious = next;
            mHistoryOfNumbers.add(mPrevious);
            mPreviousNumbers.add(mPrevious);
            cleanUpHistory();
            return next;
        }


        private void cleanUpHistory() {
            if (!mHistoryOfNumbers.isEmpty() && mHistoryOfNumbers.size() >= MAX_HISTORY_SIZE) {
                for (int i = 0; i < Math.max(1, MAX_HISTORY_SIZE / 2); i++) {
                    mPreviousNumbers.remove(mHistoryOfNumbers.removeFirst());
                }
            }
        }
    }

    private static final class TrackErrorInfo {
        public long mId;
        public String mTrackName;

        public TrackErrorInfo(long id, String trackName) {
            mId = id;
            mTrackName = trackName;
        }
    }

    private static final class MultiPlayer implements MediaPlayer.OnErrorListener,
            MediaPlayer.OnCompletionListener {

        private final WeakReference<MusicService> mService;

        private MediaPlayer mCurrentMediaPlayer = new MediaPlayer();

        private MediaPlayer mNextMediaPlayer;

        private Handler mHandler;

        private boolean mIsInitialized = false;

        private String mNextMediaPath;


        public MultiPlayer(final MusicService service) {
            mService = new WeakReference<MusicService>(service);
            mCurrentMediaPlayer.setWakeMode(mService.get(), PowerManager.PARTIAL_WAKE_LOCK);

        }

        //指定装载path路径所代表的文件
        public void setDataSource(final String path) {
            mIsInitialized = setDataSourceImpl(mCurrentMediaPlayer, path);
            if (mIsInitialized) {
                setNextDataSource(null);
            }
        }


        private boolean setDataSourceImpl(final MediaPlayer player, final String path) {
            try {
                // 重置MediaPlayer进入未初始化状态
                player.reset();
                // 当MediaPlayer调用prepare()方法时触发该监听器
                player.setOnPreparedListener(null);
                if (path.startsWith("content://")) {
                    // 指定装载uri所代表的文件
                    player.setDataSource(mService.get(), Uri.parse(path));
                } else {
                    //指定装载path路径所代表的文件
                    player.setDataSource(path);
                }
                //设置音频流的类型
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                //准备播放（装载音频）
                player.prepare();
            } catch (final IOException todo) {

                return false;
            } catch (final IllegalArgumentException todo) {

                return false;
            }
            // 为Media Player的播放完成事件绑定事件监听器
            player.setOnCompletionListener(this);
            // 为MediaPlayer的播放错误事件绑定事件监听器
            player.setOnErrorListener(this);
            return true;
        }


        public void setNextDataSource(final String path) {
            mNextMediaPath = null;
            try {
                // 设置当前流媒体播放完毕,下一个播放的MediaPlayer
                mCurrentMediaPlayer.setNextMediaPlayer(null);
            } catch (IllegalArgumentException e) {
                Log.i(TAG, "Next media player is current one, continuing");
            } catch (IllegalStateException e) {
                Log.e(TAG, "Media player not initialized!");
                return;
            }
            if (mNextMediaPlayer != null) {
                mNextMediaPlayer.release();
                mNextMediaPlayer = null;
            }
            if (path == null) {
                return;
            }
            mNextMediaPlayer = new MediaPlayer();
            mNextMediaPlayer.setWakeMode(mService.get(), PowerManager.PARTIAL_WAKE_LOCK);
            mNextMediaPlayer.setAudioSessionId(getAudioSessionId());
            if (setDataSourceImpl(mNextMediaPlayer, path)) {
                mNextMediaPath = path;
                mCurrentMediaPlayer.setNextMediaPlayer(mNextMediaPlayer);
            } else {
                if (mNextMediaPlayer != null) {
                    mNextMediaPlayer.release();
                    mNextMediaPlayer = null;
                }
            }
        }


        public void setHandler(final Handler handler) {
            mHandler = handler;
        }


        public boolean isInitialized() {
            return mIsInitialized;
        }


        public void start() {
            mCurrentMediaPlayer.start();
        }


        public void stop() {
            mCurrentMediaPlayer.reset();
            mIsInitialized = false;
        }

        //释放媒体资源。
        public void release() {
            mCurrentMediaPlayer.release();
        }


        public void pause() {
            mCurrentMediaPlayer.pause();
        }


        public long duration() {
            return mCurrentMediaPlayer.getDuration();
        }


        public long position() {
            return mCurrentMediaPlayer.getCurrentPosition();
        }

        // 寻找指定的时间位置
        public long seek(final long whereto) {
            mCurrentMediaPlayer.seekTo((int) whereto);
            return whereto;
        }

        // 设置播放器的音量
        public void setVolume(final float vol) {
            mCurrentMediaPlayer.setVolume(vol, vol);
        }

        public int getAudioSessionId() {
            return mCurrentMediaPlayer.getAudioSessionId();
        }

        public void setAudioSessionId(final int sessionId) {
            mCurrentMediaPlayer.setAudioSessionId(sessionId);
        }

        @Override
        public boolean onError(final MediaPlayer mp, final int what, final int extra) {
            Log.w(TAG, "Music Server Error what: " + what + " extra: " + extra);
            switch (what) {
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                   
                    return true;
                default:
                    break;
            }
            return false;
        }


        @Override
        public void onCompletion(final MediaPlayer mp) {
            if (mp == mCurrentMediaPlayer && mNextMediaPlayer != null) {
                mCurrentMediaPlayer.release();
                mCurrentMediaPlayer = mNextMediaPlayer;
                mNextMediaPath = null;
                mNextMediaPlayer = null;
                mHandler.sendEmptyMessage(TRACK_WENT_TO_NEXT);
            } else {
                mService.get().mWakeLock.acquire(30000);
                mHandler.sendEmptyMessage(TRACK_ENDED);
                mHandler.sendEmptyMessage(RELEASE_WAKELOCK);
            }
        }
    }


    /**
     *
     */
    private static final class ServiceStub extends ITimberService.Stub {

        private final WeakReference<MusicService> mService;

        private ServiceStub(final MusicService service) {
            mService = new WeakReference<MusicService>(service);
        }


        @Override
        public void openFile(final String path) throws RemoteException {
            mService.get().openFile(path);
        }

        @Override
        public void open(final long[] list, final int position, long sourceId, int sourceType)
                throws RemoteException {
            mService.get().open(list, position, sourceId, TimberUtils.IdType.getTypeById(sourceType));
        }

        @Override
        public void stop() throws RemoteException {
            mService.get().stop();
        }

        @Override
        public void pause() throws RemoteException {
            mService.get().pause();
        }


        @Override
        public void play() throws RemoteException {
            Log.e(TAG, " This is play()");
            mService.get().play();
        }

        @Override
        public void prev(boolean forcePrevious) throws RemoteException {
            mService.get().prev(forcePrevious);
        }

        @Override
        public void next() throws RemoteException {
            mService.get().gotoNext(true);
        }

        @Override
        public void enqueue(final long[] list, final int action, long sourceId, int sourceType)
                throws RemoteException {
            mService.get().enqueue(list, action, sourceId, TimberUtils.IdType.getTypeById(sourceType));
        }

        @Override
        public void moveQueueItem(final int from, final int to) throws RemoteException {
            mService.get().moveQueueItem(from, to);
        }

        @Override
        public void refresh() throws RemoteException {
            mService.get().refresh();
        }

        @Override
        public void playlistChanged() throws RemoteException {
            mService.get().playlistChanged();
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return mService.get().isPlaying();
        }

        @Override
        public long[] getQueue() throws RemoteException {
            return mService.get().getQueue();
        }

        @Override
        public long getQueueItemAtPosition(int position) throws RemoteException {
            return mService.get().getQueueItemAtPosition(position);
        }

        @Override
        public int getQueueSize() throws RemoteException {
            return mService.get().getQueueSize();
        }

        @Override
        public int getQueueHistoryPosition(int position) throws RemoteException {
            return mService.get().getQueueHistoryPosition(position);
        }

        @Override
        public int getQueueHistorySize() throws RemoteException {
            return mService.get().getQueueHistorySize();
        }

        @Override
        public int[] getQueueHistoryList() throws RemoteException {
            return mService.get().getQueueHistoryList();
        }

        @Override
        public long duration() throws RemoteException {
            return mService.get().duration();
        }

        @Override
        public long position() throws RemoteException {
            return mService.get().position();
        }

        @Override
        public long seek(final long position) throws RemoteException {
            return mService.get().seek(position);
        }

        @Override
        public void seekRelative(final long deltaInMs) throws RemoteException {
            mService.get().seekRelative(deltaInMs);
        }

        @Override
        public long getAudioId() throws RemoteException {
            return mService.get().getAudioId();
        }

        @Override
        public MusicPlaybackTrack getCurrentTrack() throws RemoteException {
            return mService.get().getCurrentTrack();
        }

        @Override
        public MusicPlaybackTrack getTrack(int index) throws RemoteException {
            return mService.get().getTrack(index);
        }

        @Override
        public long getNextAudioId() throws RemoteException {
            return mService.get().getNextAudioId();
        }

        @Override
        public long getPreviousAudioId() throws RemoteException {
            return mService.get().getPreviousAudioId();
        }

        @Override
        public long getArtistId() throws RemoteException {
            return mService.get().getArtistId();
        }

        @Override
        public long getAlbumId() throws RemoteException {
            return mService.get().getAlbumId();
        }

        @Override
        public String getArtistName() throws RemoteException {
            return mService.get().getArtistName();
        }

        @Override
        public String getTrackName() throws RemoteException {
            return mService.get().getTrackName();
        }

        @Override
        public String getAlbumName() throws RemoteException {
            return mService.get().getAlbumName();
        }

        @Override
        public String getPath() throws RemoteException {
            return mService.get().getPath();
        }

        @Override
        public int getQueuePosition() throws RemoteException {
            return mService.get().getQueuePosition();
        }

        @Override
        public void setQueuePosition(final int index) throws RemoteException {
            mService.get().setQueuePosition(index);
        }

        @Override
        public int getShuffleMode() throws RemoteException {
            return mService.get().getShuffleMode();
        }

        @Override
        public void setShuffleMode(final int shufflemode) throws RemoteException {
            mService.get().setShuffleMode(shufflemode);
        }

        @Override
        public int getRepeatMode() throws RemoteException {
            return mService.get().getRepeatMode();
        }

        @Override
        public void setRepeatMode(final int repeatmode) throws RemoteException {
            mService.get().setRepeatMode(repeatmode);
        }

        @Override
        public int removeTracks(final int first, final int last) throws RemoteException {
            return mService.get().removeTracks(first, last);
        }


        @Override
        public int removeTrack(final long id) throws RemoteException {
            return mService.get().removeTrack(id);
        }


        @Override
        public boolean removeTrackAtPosition(final long id, final int position)
                throws RemoteException {
            return mService.get().removeTrackAtPosition(id, position);
        }


        @Override
        public int getMediaMountedCount() throws RemoteException {
            return mService.get().getMediaMountedCount();
        }


        @Override
        public int getAudioSessionId() throws RemoteException {
            return mService.get().getAudioSessionId();
        }


        @Override
        public void setLockscreenAlbumArt(boolean enabled) {
            mService.get().setLockscreenAlbumArt(enabled);
        }

    }

    private void setLockscreenAlbumArt(boolean enabled) {

    }

    public int getAudioSessionId() {
        synchronized (this) {
            return mPlayer.getAudioSessionId();
        }
    }

    private int getMediaMountedCount() {
        return 0;
    }

    private boolean removeTrackAtPosition(long id, int position) {
        return false;
    }

    private int removeTrack(long id) {
        return 0;
    }

    private int removeTracks(int first, int last) {
        return 0;
    }

    private void setRepeatMode(int repeatmode) {

    }

    private int getRepeatMode() {
        return 0;
    }
    //设置播放模式
    private void setShuffleMode(int shufflemode) {
        synchronized (this) {
            if (mShuffleMode == shufflemode && mPlaylist.size() > 0) {
                return;
            }

            mShuffleMode = shufflemode;
            if (mShuffleMode == SHUFFLE_AUTO) {
                if (makeAutoShuffleList()) {
                    mPlaylist.clear();
                    doAutoShuffleUpdate();
                    mPlayPos = 0;
                    openCurrentAndNext();
                    play();
                    notifyChange(META_CHANGED);
                    return;
                } else {
                    mShuffleMode = SHUFFLE_NONE;
                }
            } else {
                setNextTrack();
            }
            saveQueue(false);
            notifyChange(SHUFFLEMODE_CHANGED);
        }
    }

    private void doAutoShuffleUpdate() {
        boolean notify = false;
        if (mPlayPos > 10) {
            removeTracks(0, mPlayPos - 9);
            notify = true;
        }
        final int toAdd = 7 - (mPlaylist.size() - (mPlayPos < 0 ? -1 : mPlayPos));
        for (int i = 0; i < toAdd; i++) {
            int lookback = mHistory.size();
            int idx = -1;
            while (true) {
                idx = mShuffler.nextInt(mAutoShuffleList.length);
                if (!wasRecentlyUsed(idx, lookback)) {
                    break;
                }
                lookback /= 2;
            }
            mHistory.add(idx);
            if (mHistory.size() > MAX_HISTORY_SIZE) {
                mHistory.remove(0);
            }
            mPlaylist.add(new MusicPlaybackTrack(mAutoShuffleList[idx], -1, TimberUtils.IdType.NA, -1));
            notify = true;
        }
        if (notify) {
            notifyChange(QUEUE_CHANGED);
        }
    }

    private boolean wasRecentlyUsed(int idx, int lookbacksize) {
        if (lookbacksize == 0) {
            return false;
        }
        final int histsize = mHistory.size();
        if (histsize < lookbacksize) {
            lookbacksize = histsize;
        }
        final int maxidx = histsize - 1;
        for (int i = 0; i < lookbacksize; i++) {
            final long entry = mHistory.get(maxidx - i);
            if (entry == idx) {
                return true;
            }
        }
        return false;
    }

    private boolean makeAutoShuffleList() {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{
                            MediaStore.Audio.Media._ID
                    }, MediaStore.Audio.Media.IS_MUSIC + "=1", null, null);
            if (cursor == null || cursor.getCount() == 0) {
                return false;
            }
            final int len = cursor.getCount();
            final long[] list = new long[len];
            for (int i = 0; i < len; i++) {
                cursor.moveToNext();
                list[i] = cursor.getLong(0);
            }
            mAutoShuffleList = list;
            return true;
        } catch (final RuntimeException e) {
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return false;
    }

    /**
     * 随机模式
     * @return
     */
    private int getShuffleMode() {
        return mShuffleMode;
    }

    private void setQueuePosition(int index) {
        synchronized (this) {
            stop(false);
            mPlayPos = index;
            openCurrentAndNext();
            play();
            notifyChange(META_CHANGED);
            if (mShuffleMode == SHUFFLE_AUTO) {
                doAutoShuffleUpdate();
            }
        }
    }
    //获取队列位置
    private int getQueuePosition() {
        synchronized (this) {
            return mPlayPos;
        }
    }

    private String getPath() {
        return null;
    }

    private String getAlbumName() {
        synchronized (this){
            if(mCursor == null){
                return null;
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM));
        }
    }

    private String getTrackName() {
        synchronized (this){
            if(mCursor == null){
                return null;
            }
        }
        return mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE));
    }

    private String getArtistName() {
        synchronized (this){
            if(mCursor == null){
                return null;
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST));
        }
    }

    private long getAlbumId() {
        return 0;
    }

    private long getArtistId() {
        return 0;
    }

    private long getPreviousAudioId() {
        return 0;
    }

    private long getNextAudioId() {
        return 0;
    }
    // 获取 mPlaylist 中的单条对象
    private MusicPlaybackTrack getTrack(int index) {
        if (index >= 0 && index < mPlaylist.size() && mPlayer.isInitialized()) {
            return mPlaylist.get(index);
        }

        return null;
    }

    private MusicPlaybackTrack getCurrentTrack() {
        return getTrack(mPlayPos);
    }

    private long getAudioId() {
        MusicPlaybackTrack track = getCurrentTrack();
        if (track != null) {
            return track.mId;
        }

        return -1;
    }

    private void seekRelative(long deltaInMs) {
        synchronized (this) {
            if (mPlayer.isInitialized()) {
                final long newPos = position() + deltaInMs;
                final long duration = duration();
                if (newPos < 0) {
                    prev(true);
                    // seek to the new duration + the leftover position
                    seek(duration() + newPos);
                } else if (newPos >= duration) {
                    gotoNext(true);
                    // seek to the leftover duration
                    seek(newPos - duration);
                } else {
                    seek(newPos);
                }
            }
        }
    }

    private long seek(long position) {
        Log.d(TAG, " seek , position = " + position);
        if (mPlayer.isInitialized()) {
            if (position < 0) {
                position = 0;
            } else if (position > mPlayer.duration()) {
                position = mPlayer.duration();
            }
            long result = mPlayer.seek(position);
            notifyChange(POSITION_CHANGED);
            return result;
        }
        return -1;
    }

    private long position() {
        if (mPlayer.isInitialized()) {
            return mPlayer.position();
        }
        return -1;
    }

    private long duration() {
        if (mPlayer.isInitialized()) {
            return mPlayer.duration();
        }
        return -1;
    }

    private int[] getQueueHistoryList() {
        return new int[0];
    }

    private int getQueueHistorySize() {
        return 0;
    }

    private int getQueueHistoryPosition(int position) {
        return 0;
    }

    private int getQueueSize() {
        return 0;
    }

    private long getQueueItemAtPosition(int position) {
        return 0;
    }
    // 获取歌曲列表中对象的mId
    private long[] getQueue() {
        synchronized (this) {
            final int len = mPlaylist.size();
            final long[] list = new long[len];
            for (int i = 0; i < len; i++) {
                list[i] = mPlaylist.get(i).mId;
            }
            return list;
        }
    }

    private boolean isPlaying() {
        return mIsSupposedToBePlaying;
    }

    private void playlistChanged() {

    }

    private void refresh() {
        notifyChange(REFRESH);
    }

    private void moveQueueItem(int from, int to) {

    }

    private void enqueue(long[] list, int action, long sourceId, TimberUtils.IdType typeById) {

    }

    private void gotoNext(boolean force) {
        Log.d(TAG, " Going to next track ");
        synchronized (this){
            if(mPlaylist.size() <= 0){
                Log.d(TAG, " No paly queue ");
                scheduleDelayedShutdown();
                return;
            }
            int pos = mNextPlayPos;
            if(pos < 0){
                pos = getNextPosition(force);
            }
            if(pos < 0){
                setIsSupposedToBePlaying(false, true);
                return;
            }
            stop(false);
            setAndRecordPlayPos(pos);
            openCurrentAndNext();
            play();
            notifyChange(META_CHANGED);
        }

    }
    //设置并记录播放点
    private void setAndRecordPlayPos(int nextPos) {
        synchronized (this){
            if(mShuffleMode != SHUFFLE_NONE){
                mHistory.add(mPlayPos);
                if(mHistory.size() > MAX_HISTORY_SIZE){
                    mHistory.remove(0);
                }
            }
            mPlayPos = nextPos;
        }
    }

    private void prev(boolean forcePrevious) {
        Log.d(TAG, "Going to previous track");
        synchronized (this) {
            boolean goPrevious = getRepeatMode() != REPEAT_CURRENT &&
                    (position() < REWIND_INSTEAD_PREVIOUS_THRESHOLD || forcePrevious);

            if (goPrevious) {
                if (D) Log.d(TAG, "Going to previous track");
                int pos = getPreviousPlayPosition(true);

                if (pos < 0) {
                    return;
                }
                mNextPlayPos = mPlayPos;
                mPlayPos = pos;
                stop(false);
                openCurrent();
                play(false);
                notifyChange(META_CHANGED);
            } else {
                if (D) Log.d(TAG, "Going to beginning of track");
                seek(0);
                play(false);
            }
        }
    }
    // 获取之前播放的歌曲
    private int getPreviousPlayPosition(boolean removeFromHistory) {
        synchronized (this) {
            if (mShuffleMode == SHUFFLE_NORMAL) {

                final int histsize = mHistory.size();
                if (histsize == 0) {
                    return -1;
                }
                final Integer pos = mHistory.get(histsize - 1);
                if (removeFromHistory) {
                    mHistory.remove(histsize - 1);
                }
                return pos.intValue();
            } else {
                if (mPlayPos > 0) {
                    return mPlayPos - 1;
                } else {
                    return mPlaylist.size() - 1;
                }
            }
        }
    }

    private void openCurrent() {
        openCurrentAndMaybeNext(false);
    }

    public void play() {
        Log.d(TAG, " -- play() -- ");
        play(true);
    }

    private void play(boolean createNewNextTrack) {
        int status = mAudioManager.requestAudioFocus(mAudioFocusListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        Log.d(TAG, "Starting playback: audio focus request status = " + status);

        if(status != AudioManager.AUDIOFOCUS_REQUEST_GRANTED){//申请 AudioFocus 失败
            Log.d(TAG, " AUDIOFOCUS_REQUEST_FAILED ");
            return;
        }
        //AudioEffect是android audio framework(android 音频框架)提供的音频效果控制的基类
        final Intent intent = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);// 打开音频效果。
        intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());//包含使用效果的音频会话ID。
        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName()); //包含调用应用程序的包名。
        sendBroadcast(intent);
        // 注册一个MedioButtonReceiver广播监听(监听耳机按钮)
        mAudioManager.registerMediaButtonEventReceiver(new ComponentName(getPackageName(),
                MediaButtonIntentReceiver.class.getName()));

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            mSession.setActive(true);

            if(createNewNextTrack){
                setNextTrack();
            }else {
                setNextTrack(mNextPlayPos);
            }
        }
        if(mPlayer.mIsInitialized){
            //歌曲时长
            final long duration = mPlayer.duration();
            if(mRepeatMode != REPEAT_CURRENT && duration > 2000
                    && mPlayer.position() >= duration - 2000){
                gotoNext(true);
            }
            mPlayer.start();
            Log.e(TAG, " -- mPlayer.start() -- ");
            mPlayerHandler.removeMessages(FADEDOWN);
            mPlayerHandler.sendEmptyMessage(FADEUP);

            setIsSupposedToBePlaying(true, true);

            cancelShutdown();

            updateNotification();

            notifyChange(META_CHANGED);
        }else if (mPlaylist.size() <= 0){
            setShuffleMode(SHUFFLE_AUTO);
        }
    }

    private void updateNotification() {

    }

    private void setIsSupposedToBePlaying(boolean value, boolean notify) {
        if (mIsSupposedToBePlaying != value) {
            mIsSupposedToBePlaying = value;


            if (!mIsSupposedToBePlaying) {
                scheduleDelayedShutdown();
                mLastPlayedTime = System.currentTimeMillis();
            }

            if (notify) {
                notifyChange(PLAYSTATE_CHANGED);
            }
        }
    }

    private void setNextTrack(int position) {
        mNextPlayPos = position;
        if (D) Log.d(TAG, "setNextTrack: next play position = " + mNextPlayPos);
        if (mNextPlayPos >= 0 && mPlaylist != null && mNextPlayPos < mPlaylist.size()) {
            final long id = mPlaylist.get(mNextPlayPos).mId;
            mPlayer.setNextDataSource(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" + id);
        } else {
            mPlayer.setNextDataSource(null);
        }
    }

    private void setNextTrack() {
        setNextTrack(getNextPosition(false));
    }

    private void pause() {
        Log.d(TAG, " Pauseing playback ");
        synchronized (this){
            mPlayerHandler.removeMessages(FADEUP);
            if(mIsSupposedToBePlaying){
                final Intent intent = new Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
                intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
                intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
                sendBroadcast(intent);

                mPlayer.pause();
                notifyChange(META_CHANGED);
                setIsSupposedToBePlaying(false, true);

            }
        }
    }

    private void stop() {

    }


    private void stop(boolean b) {
    }

    private void open(long[] list, int position, long sourceId, TimberUtils.IdType sourceType) {
        Log.v(TAG, " songIds = " + list.length + ", position = " + position
                + ", sourceId = " + sourceId);
        synchronized (this) {
            if (mShuffleMode == SHUFFLE_AUTO) {
                mShuffleMode = SHUFFLE_NORMAL;// 顺序播放
            }
            final long oldId = getAudioId();
            final int listlength = list.length;
            boolean newlist = true;
            if (mPlaylist.size() == listlength) {
                newlist = false;
                for (int i = 0; i < listlength; i++) {
                    if (list[i] != mPlaylist.get(i).mId) {
                        newlist = true;
                        break;
                    }
                }
            }
            if (newlist) {
                addToPlayList(list, -1, sourceId, sourceType);
                notifyChange(QUEUE_CHANGED);
            }
            if (position >= 0) {
                mPlayPos = position;
            } else {
                mPlayPos = mShuffler.nextInt(mPlaylist.size());
            }
            mHistory.clear();
            openCurrentAndNext();
            if (oldId != getAudioId()) {
                notifyChange(META_CHANGED);
            }
        }
    }

    private void openCurrentAndNext() {
        openCurrentAndMaybeNext(true);
    }

    private void openCurrentAndMaybeNext(boolean openNext) {
        synchronized (this) {
            closeCursor();

            if (mPlaylist.size() == 0) {
                return;
            }
            stop(false);

            boolean shutdown = false;

            updateCursor(mPlaylist.get(mPlayPos).mId);
            while (true) {
                if (mCursor != null
                        && openFile(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/"
                        + mCursor.getLong(IDCOLIDX))) {
                    break;
                }

                closeCursor();
                if (mOpenFailedCounter++ < 10 && mPlaylist.size() > 1) {
                    final int pos = getNextPosition(false);
                    if (pos < 0) {
                        shutdown = true;
                        break;
                    }
                    mPlayPos = pos;
                    stop(false);
                    mPlayPos = pos;
                    updateCursor(mPlaylist.get(mPlayPos).mId);
                } else {
                    mOpenFailedCounter = 0;
                    Log.w(TAG, "Failed to open file for playback");
                    shutdown = true;
                    break;
                }
            }

            if (shutdown) {
                scheduleDelayedShutdown();
                if (mIsSupposedToBePlaying) {
                    mIsSupposedToBePlaying = false;
                    notifyChange(PLAYSTATE_CHANGED);
                }
            } else if (openNext) {
                setNextTrack();
            }
        }
    }
    // 延迟关闭
    private void scheduleDelayedShutdown() {
        Log.v(TAG, "Scheduling shutdown in " + IDLE_DELAY + " ms");
        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + IDLE_DELAY, mShutdownIntent);
        mShutdownScheduled = true;
    }

    private int getNextPosition(boolean force) {
        if (mPlaylist == null || mPlaylist.isEmpty()) {
            return -1;
        }
        if (!force && mRepeatMode == REPEAT_CURRENT) {
            if (mPlayPos < 0) {
                return 0;
            }
            return mPlayPos;
        } else if (mShuffleMode == SHUFFLE_NORMAL) {
            final int numTracks = mPlaylist.size();


            final int[] trackNumPlays = new int[numTracks];
            for (int i = 0; i < numTracks; i++) {
                trackNumPlays[i] = 0;
            }


            final int numHistory = mHistory.size();
            for (int i = 0; i < numHistory; i++) {
                final int idx = mHistory.get(i).intValue();
                if (idx >= 0 && idx < numTracks) {
                    trackNumPlays[idx]++;
                }
            }

            if (mPlayPos >= 0 && mPlayPos < numTracks) {
                trackNumPlays[mPlayPos]++;
            }

            int minNumPlays = Integer.MAX_VALUE;
            int numTracksWithMinNumPlays = 0;
            for (int i = 0; i < trackNumPlays.length; i++) {
                if (trackNumPlays[i] < minNumPlays) {
                    minNumPlays = trackNumPlays[i];
                    numTracksWithMinNumPlays = 1;
                } else if (trackNumPlays[i] == minNumPlays) {
                    numTracksWithMinNumPlays++;
                }
            }


            if (minNumPlays > 0 && numTracksWithMinNumPlays == numTracks
                    && mRepeatMode != REPEAT_ALL && !force) {
                return -1;
            }


            int skip = mShuffler.nextInt(numTracksWithMinNumPlays);
            for (int i = 0; i < trackNumPlays.length; i++) {
                if (trackNumPlays[i] == minNumPlays) {
                    if (skip == 0) {
                        return i;
                    } else {
                        skip--;
                    }
                }
            }

            if (D)
                Log.e(TAG, "Getting the next position resulted did not get a result when it should have");
            return -1;
        } else if (mShuffleMode == SHUFFLE_AUTO) {
            doAutoShuffleUpdate();
            return mPlayPos + 1;
        } else {
            if (mPlayPos >= mPlaylist.size() - 1) {
                if (mRepeatMode == REPEAT_NONE && !force) {
                    return -1;
                } else if (mRepeatMode == REPEAT_ALL || force) {
                    return 0;
                }
                return -1;
            } else {
                return mPlayPos + 1;
            }
        }
    }

    private void updateCursor(long trackId) {
        updateCursor("_id=" + trackId, null);
    }

    private void updateCursor(final String selection, final String[] selectionArgs) {
        synchronized (this) {
            closeCursor();
            mCursor = openCursorAndGoToFirst(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    PROJECTION, selection, selectionArgs);
        }
//        updateAlbumCursor();
    }

    private Cursor openCursorAndGoToFirst(Uri uri, String[] projection,
                                          String selection, String[] selectionArgs) {
        Cursor c = getContentResolver().query(uri, projection,
                selection, selectionArgs, null);
        if (c == null) {
            return null;
        }
        if (!c.moveToFirst()) {
            c.close();
            return null;
        }
        return c;
    }


    private void addToPlayList(long[] list, int position, long sourceId, TimberUtils.IdType sourceType) {
        final int addlen = list.length;
        if (position < 0) {
            mPlaylist.clear();
            position = 0;
        }

        mPlaylist.ensureCapacity(mPlaylist.size() + addlen);
        if (position > mPlaylist.size()) {
            position = mPlaylist.size();
        }

        final ArrayList<MusicPlaybackTrack> arrayList = new ArrayList<MusicPlaybackTrack>(addlen);
        for (int i = 0; i < list.length; i++) {
            arrayList.add(new MusicPlaybackTrack(list[i], sourceId, sourceType, i));
        }

        mPlaylist.addAll(position, arrayList);

        if (mPlaylist.size() == 0) {
            closeCursor();
            notifyChange(META_CHANGED);
        }
    }

    private void closeCursor() {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        if (mAlbumCursor != null) {
            mAlbumCursor.close();
            mAlbumCursor = null;
        }
    }

    private boolean openFile(String path) {
        Log.d(TAG, "openFile: path = " + path);
        synchronized (this) {
            if (path == null) {
                return false;
            }

            if (mCursor == null) {
                Uri uri = Uri.parse(path);
                boolean shouldAddToPlaylist = true;
                long id = -1;
                try {
                    id = Long.valueOf(uri.getLastPathSegment());
                } catch (NumberFormatException ex) {
                    // Ignore
                }

                if (id != -1 && path.startsWith(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString())) {
                    updateCursor(uri);

                } else if (id != -1 && path.startsWith(
                        MediaStore.Files.getContentUri("external").toString())) {
                    updateCursor(id);

                } else if (path.startsWith("content://downloads/")) {

                    String mpUri = getValueForDownloadedFile(this, uri, "mediaprovider_uri");
                    if (D) Log.i(TAG, "Downloaded file's MP uri : " + mpUri);
                    if (!TextUtils.isEmpty(mpUri)) {
                        if (openFile(mpUri)) {
                            notifyChange(META_CHANGED);
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        updateCursorForDownloadedFile(this, uri);
                        shouldAddToPlaylist = false;
                    }

                } else {
                    String where = MediaStore.Audio.Media.DATA + "=?";
                    String[] selectionArgs = new String[]{path};
                    updateCursor(where, selectionArgs);
                }
                try {
                    if (mCursor != null && shouldAddToPlaylist) {
                        mPlaylist.clear();
                        mPlaylist.add(new MusicPlaybackTrack(
                                mCursor.getLong(IDCOLIDX), -1, TimberUtils.IdType.NA, -1));
                        notifyChange(QUEUE_CHANGED);
                        mPlayPos = 0;
                        mHistory.clear();
                    }
                } catch (final UnsupportedOperationException ex) {
                    // Ignore
                }
            }

            mFileToPlay = path;
            mPlayer.setDataSource(mFileToPlay);
            if (mPlayer.isInitialized()) {
                mOpenFailedCounter = 0;
                return true;
            }

            String trackName = getTrackName();
            if (TextUtils.isEmpty(trackName)) {
                trackName = path;
            }
            sendErrorMessage(trackName);

            stop(true);
            return false;
        }
    }

    private String getValueForDownloadedFile(MusicService musicService, Uri uri, String mediaprovider_uri) {
        return null;
    }

    private void updateCursorForDownloadedFile(MusicService musicService, Uri uri) {

    }

    private void sendErrorMessage(String trackName) {

    }

    private void updateCursor(Uri uri) {

    }

    public void notifyChange(final String what){
        Log.d(TAG, "notifyChange: what = " + what);

        // Update the lockscreen controls( 更新屏幕控制? )
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            updateMediaSession(what);
        }

        if(what.equals(POSITION_CHANGED)){ //位置改变 ?
            return;
        }

        final Intent intent = new Intent(what);
        intent.putExtra("id", getAudioId());
        intent.putExtra("artist", getArtistName());
        intent.putExtra("album", getAlbumName());
        intent.putExtra("track", getTrackName());
        intent.putExtra("playing", isPlaying());

        sendStickyBroadcast(intent);

        final Intent musicIntent = new Intent(intent);
        musicIntent.setAction(what.replace(TIMBER_PACKAGE_NAME, MUSIC_PACKAGE_NAME));
        sendStickyBroadcast(musicIntent);

        if(what.equals(META_CHANGED)){//位置改变
//            mRecentStore.addSongId(getAudioId());
//            mSongPlayCount.bumpSongCount(getAudioId());
        }else if(what.equals(QUEUE_CHANGED)){//队列改变
            saveQueue(true);
            if(isPlaying()){
                if(mNextPlayPos >= 0 && mNextPlayPos < mPlaylist.size()
                        && getShuffleMode() != SHUFFLE_NONE){
                    setNextTrack(mNextPlayPos);
                }else {
                    setNextTrack();
                }
            }

        }else{
            saveQueue(false);
        }

        if(what.equals(PLAYSTATE_CHANGED)){//播放状态改变
            updateNotification();
        }
    }

    private void updateMediaSession(String what) {

    }

    private class MediaStoreObserver extends ContentObserver implements Runnable {

        private static final long REFRESH_DELAY = 500;
        private Handler mHandler;

        public MediaStoreObserver(Handler handler) {
            super(handler);
            mHandler = handler;
        }

        @Override
        public void onChange(boolean selfChange) {


            mHandler.removeCallbacks(this);
            mHandler.postDelayed(this, REFRESH_DELAY);
        }

        @Override
        public void run() {

            Log.e("ELEVEN", "calling refresh!");
            refresh();
        }
    }


}
