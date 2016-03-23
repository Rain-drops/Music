package com.sgj.john.mytimber.adapters;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sgj.john.mytimber.MusicPlayer;
import com.sgj.john.mytimber.R;
import com.sgj.john.mytimber.models.Song;
import com.sgj.john.mytimber.utils.TimberUtils;
import com.sgj.john.mytimber.widgets.MusicVisualizer;

import java.util.List;

/**
 * Created by John on 2016/3/21.
 */
public class BaseQueueAdapter extends RecyclerView.Adapter<BaseQueueAdapter.ItemHolder> {

    public static int currentlyPlayingPosition;
    private List<Song> arraylist;
    private AppCompatActivity mContext;

    public BaseQueueAdapter(AppCompatActivity context, List<Song> list){
        this.arraylist = list;
        this.mContext = context;
        currentlyPlayingPosition = MusicPlayer.getQueuePosition();
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_song_playlist, null);
        ItemHolder holder = new ItemHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        Song song = arraylist.get(position);
        holder.title.setText(song.title);
        holder.artist.setText(song.artistName);

        if(MusicPlayer.getCurrentAudioId() == song.id) {

            holder.title.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
            if(MusicPlayer.isPlaying()){
                holder.visualizer.setColor(mContext.getResources().getColor(R.color.colorAccent));
                holder.visualizer.setVisibility(View.VISIBLE);
            }
        }else {
            holder.title.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
            holder.visualizer.setVisibility(View.GONE);
        }
        ImageLoader.getInstance().displayImage(TimberUtils.getAlbumArtUri(song.albumId).toString(),
                holder.albumArt,
                new DisplayImageOptions.Builder().cacheInMemory(true).
                        showImageOnFail(R.drawable.ic_empty_music2).build());
        setOnPopupMenuListener(holder, position);
    }

    private void setOnPopupMenuListener(ItemHolder holder, int position) {
        holder.popupMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupMenu menu = new PopupMenu(mContext, v);
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.popup_song_play:

                                break;
                            case R.id.popup_song_play_next:

                                break;
                            case R.id.popup_song_goto_album:

                                break;
                            case R.id.popup_song_goto_artist:

                                break;
                            case R.id.popup_song_addto_queue:

                                break;
                            case R.id.popup_song_addto_playlist:

                                break;

                        }

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
        return (arraylist != null ? arraylist.size() : 0);
    }

    /**
     *
     */
    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        protected TextView title, artist;
        protected ImageView albumArt, popupMenu;
        private MusicVisualizer visualizer;

        public ItemHolder(View itemView) {
            super(itemView);

            this.title = (TextView) itemView.findViewById(R.id.tv_song_title);
            this.artist = (TextView) itemView.findViewById(R.id.tv_song_artist);
            this.albumArt = (ImageView) itemView.findViewById(R.id.iv_albumArt);
            this.popupMenu = (ImageView) itemView.findViewById(R.id.iv_popup_menu);
            visualizer = (MusicVisualizer) itemView.findViewById(R.id.visualizer);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MusicPlayer.setQueuePosition(getAdapterPosition());

                    Handler handler1 = new Handler();
                    handler.postDelayed(new Runnable() {
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
