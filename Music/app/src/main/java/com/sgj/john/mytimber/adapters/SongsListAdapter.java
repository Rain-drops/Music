package com.sgj.john.mytimber.adapters;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.sgj.john.mytimber.MusicPlayer;
import com.sgj.john.mytimber.R;
import com.sgj.john.mytimber.models.Song;
import com.sgj.john.mytimber.utils.Helpers;
import com.sgj.john.mytimber.utils.TimberUtils;
import com.sgj.john.mytimber.widgets.MusicVisualizer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by John on 2016/3/9.
 */
public class SongsListAdapter extends RecyclerView.Adapter<SongsListAdapter.ItemHolder> {

    public static final String TAG = "SongsListAdapter";

    private AppCompatActivity mContext;
    List<Song> songs;
    private boolean isPlayList;

    public int currentlyPlayingPosition;

    private long[] songIDs;
    private int lastPosition = -1;
    private String ateKey;

    public SongsListAdapter(AppCompatActivity context, List<Song> songs, boolean isPlayList) {
        this.mContext = context;
        this.songs = songs;
        this.isPlayList = isPlayList;
        this.songIDs = getSongIds();
        this.ateKey = Helpers.getATEKey(mContext);
    }

    private long[] getSongIds() {
        long[] ret  = new long[getItemCount()];
        for (int i=0; i<getItemCount(); i++){
            ret[i] = songs.get(i).id;
        }
        return ret;

    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(isPlayList){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song_playlist, null);
            ItemHolder holder = new ItemHolder(v);
            return holder;
        }else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, null);
            ItemHolder holder = new ItemHolder(view);
            return holder;
        }

    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        Song localItem = songs.get(position);
        holder.title.setText(localItem.title);
        holder.artist.setText(localItem.artistName);

//        DisplayImageOptions options = new DisplayImageOptions.Builder()
//                .showStubImage(R.drawable.ic_stub)          // 设置图片下载期间显示的图片
//                .showImageForEmptyUri(R.drawable.ic_empty)  // 设置图片Uri为空或是错误的时候显示的图片
//                .showImageOnFail(R.drawable.ic_error)       // 设置图片加载或解码过程中发生错误显示的图片
//                .cacheInMemory(true)                        // 设置下载的图片是否缓存在内存中
//                .cacheOnDisc(true)                          // 设置下载的图片是否缓存在SD卡中
//                .displayer(new RoundedBitmapDisplayer(20))  // 设置成圆角图片
//                .build();
//        ImageLoader.getInstance().displayImage(url, imageView, options);

//        在 TimberApp 中 初始化
//        ImageLoader.getInstance().init(new ImageLoaderConfiguration.Builder(mInstance).build());
        ImageLoader.getInstance().displayImage(TimberUtils.getAlbumArtUri(localItem.albumId).toString(),
                holder.albumArt,
                new DisplayImageOptions.Builder().cacheInMemory(true).showImageOnFail(R.drawable.ic_empty_music2).resetViewBeforeLoading(true).build());

//        Glide.with(mContext).load(localItem.albumId).centerCrop().into(holder.albumArt);

        setOnPopupMenuListener(holder, position);

    }

    private void setOnPopupMenuListener(final ItemHolder holder, int position) {
        holder.popupMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupMenu menu = new PopupMenu(mContext, v);
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        return false;
                    }
                });
                menu.inflate(R.menu.popup_song);
                menu.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != songs ? songs.size() : 0);
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        protected TextView title, artist;
        protected ImageView albumArt, popupMenu;
        private MusicVisualizer visualizer;

        public ItemHolder(View view) {
            super(view);
            this.title = (TextView) view.findViewById(R.id.tv_song_title);
            this.artist = (TextView) view.findViewById(R.id.tv_song_artist);
            this.albumArt = (ImageView) view.findViewById(R.id.iv_albumArt);
            this.popupMenu = (ImageView) view.findViewById(R.id.iv_popup_menu);

            this.visualizer = (MusicVisualizer) view.findViewById(R.id.visualizer);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            final Handler handler = new Handler();
            Log.e(TAG, " -- songIDs -- " + songIDs.length);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MusicPlayer.playAll(mContext, songIDs, getAdapterPosition(), -1, TimberUtils.IdType.NA, false);
                    Handler handler1 = new Handler();
                    handler1.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            notifyItemChanged(currentlyPlayingPosition);
                            notifyItemChanged(getAdapterPosition());
                        }
                    }, 50);
                }
            }, 100);
        }
    }
}
