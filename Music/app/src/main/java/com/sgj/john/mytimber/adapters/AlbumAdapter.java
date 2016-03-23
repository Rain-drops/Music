package com.sgj.john.mytimber.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.sgj.john.mytimber.R;
import com.sgj.john.mytimber.models.Album;
import com.sgj.john.mytimber.utils.PreferencesUtility;
import com.sgj.john.mytimber.utils.TimberUtils;

import java.util.List;

/**
 * Created by John on 2016/3/9.
 */
public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ItemHolder>{

    private List<Album> arrayList;
    private Activity mContext;
    private boolean isGrid;

    public AlbumAdapter(Activity context, List<Album> list) {
        this.arrayList = list;
        this.mContext = context;
        this.isGrid = PreferencesUtility.getInstance(mContext).isAlbumsInGrid();
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(isGrid){
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_album_grid, null);
            ItemHolder holder = new ItemHolder(view);
            return  holder;
        }else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_album_list, null);
            ItemHolder holder = new ItemHolder(view);
            return holder;
        }
    }

    @Override
    public void onBindViewHolder(final ItemHolder holder, int position) {
        Album albumItem = arrayList.get(position);

        holder.artist.setText(albumItem.artistName);
        holder.title.setText(albumItem.title);

        ImageLoader.getInstance().displayImage(TimberUtils.getAlbumArtUri(albumItem.id).toString(),
                holder.albumArt,
                new DisplayImageOptions.Builder().cacheInMemory(true).showImageOnFail(
                        R.drawable.ic_empty_music2).resetViewBeforeLoading(true).displayer(
                        new FadeInBitmapDisplayer(400)).build(),
                new SimpleImageLoadingListener(){
                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        super.onLoadingCancelled(imageUri, view);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        if (isGrid) {
                            new Palette.Builder(loadedImage).generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(Palette palette) {
                                    Palette.Swatch swatch = palette.getVibrantSwatch();
                                    if (swatch != null) {
                                        int color = swatch.getRgb();
                                        holder.footer.setBackgroundColor(color);
                                        int textColor = TimberUtils.getBlackWhiteColor(swatch.getTitleTextColor());
                                        holder.title.setTextColor(textColor);
                                        holder.artist.setTextColor(textColor);
                                    } else {
                                        Palette.Swatch mutedSwatch = palette.getMutedSwatch();
                                        if (mutedSwatch != null) {
                                            int color = mutedSwatch.getRgb();
                                            holder.footer.setBackgroundColor(color);
                                            int textColor = TimberUtils.getBlackWhiteColor(mutedSwatch.getTitleTextColor());
                                            holder.title.setTextColor(textColor);
                                            holder.artist.setTextColor(textColor);
                                        }
                                    }


                                }
                            });
                        }
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        super.onLoadingFailed(imageUri, view, failReason);
                    }
                });

        if(TimberUtils.isLollipop()){
            holder.albumArt.setTransitionName("transition_album_art" + position);
        }

    }

    @Override
    public int getItemCount() {
        return (arrayList != null ? arrayList.size() : 0);
    }

    public void updateDataSet(List<Album> list){
        this.arrayList = list;
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        protected TextView title, artist;
        protected ImageView albumArt;
        protected View footer;

        public ItemHolder(View view) {

            super(view);
            this.title = (TextView) view.findViewById(R.id.album_title);
            this.artist = (TextView) view.findViewById(R.id.album_artist);
            this.albumArt = (ImageView) view.findViewById(R.id.album_art);
            this.footer = view.findViewById(R.id.footer);
            view.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            // TODO
        }
    }
}
