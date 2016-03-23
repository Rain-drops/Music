package com.sgj.john.mytimber.adapters;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import com.sgj.john.mytimber.models.Artist;
import com.sgj.john.mytimber.utils.PreferencesUtility;
import com.sgj.john.mytimber.utils.TimberUtils;

import java.util.List;

/**
 * Created by John on 2016/3/9.
 */
public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ItemHoldel>{

    private List<Artist> artistList;
    private Activity mContext;
    private boolean isGrid;

    public ArtistAdapter(Activity context, List<Artist> list) {
        this.artistList = list;
        this.mContext = context;
        isGrid = PreferencesUtility.getInstance(mContext).isArtistsInGrid();
    }

    @Override
    public ItemHoldel onCreateViewHolder(ViewGroup parent, int viewType) {
        if(isGrid){
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_artist_grid, null);
            ItemHoldel holdel = new ItemHoldel(view);
            return holdel;
        }else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_artist, null);
            ItemHoldel holdel = new ItemHoldel(view);
            return holdel;
        }
    }

    @Override
    public void onBindViewHolder(ItemHoldel holder, int position) {

        Artist artist = artistList.get(position);
        holder.name.setText(artist.name);
        String albumNumber = TimberUtils.makeLabel(mContext, R.plurals.Nalbums, artist.albumCount);
        String songCount = TimberUtils.makeLabel(mContext, R.plurals.Nsongs, artist.songCount);

        holder.albums.setText(TimberUtils.makeCombinedString(mContext, albumNumber, songCount));

        /*LastFmClient.getInstance(mContext).getArtistInfo(new ArtistQuery(localItem.name), new ArtistInfoListener() {
            @Override
            public void artistInfoSucess(LastfmArtist artist) {
                if (artist != null && artist.mArtwork != null) {
                    if (isGrid) {
                        ImageLoader.getInstance().displayImage(artist.mArtwork.get(2).mUrl, itemHolder.artistImage,
                                new DisplayImageOptions.Builder().cacheInMemory(true)
                                        .cacheOnDisk(true)
                                        .showImageOnFail(R.drawable.ic_empty_music2)
                                        .resetViewBeforeLoading(true)
                                        .displayer(new FadeInBitmapDisplayer(400))
                                        .build(), new SimpleImageLoadingListener() {
                                    @Override
                                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                        if (isGrid && loadedImage != null) {
                                            new Palette.Builder(loadedImage).generate(new Palette.PaletteAsyncListener() {
                                                @Override
                                                public void onGenerated(Palette palette) {
                                                    int color = palette.getVibrantColor(Color.parseColor("#66000000"));
                                                    itemHolder.footer.setBackgroundColor(color);
                                                    Palette.Swatch swatch = palette.getVibrantSwatch();
                                                    int textColor;
                                                    if (swatch != null) {
                                                        textColor = getOpaqueColor(swatch.getTitleTextColor());
                                                    } else textColor = Color.parseColor("#ffffff");

                                                    itemHolder.name.setTextColor(textColor);
                                                    itemHolder.albums.setTextColor(textColor);
                                                }
                                            });
                                        }

                                    }

                                    @Override
                                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                        if (isGrid) {
                                            itemHolder.footer.setBackgroundColor(0);
                                            if (mContext != null) {
                                                int textColorPrimary = Config.textColorPrimary(mContext, Helpers.getATEKey(mContext));
                                                itemHolder.name.setTextColor(textColorPrimary);
                                                itemHolder.albums.setTextColor(textColorPrimary);
                                            }
                                        }
                                    }
                                });
                    } else {
                        ImageLoader.getInstance().displayImage(artist.mArtwork.get(1).mUrl, itemHolder.artistImage,
                                new DisplayImageOptions.Builder().cacheInMemory(true)
                                        .cacheOnDisk(true)
                                        .showImageOnFail(R.drawable.ic_empty_music2)
                                        .resetViewBeforeLoading(true)
                                        .displayer(new FadeInBitmapDisplayer(400))
                                        .build());
                    }
                }
            }

            @Override
            public void artistInfoFailed() {

            }
        });*/

        if(TimberUtils.isLollipop()){
            holder.artistImage.setTransitionName("transition_artist_art" + position);
        }

    }

    @Override
    public int getItemCount() {
        return artistList != null ? artistList.size() : 0;
    }

   public class ItemHoldel extends RecyclerView.ViewHolder implements View.OnClickListener{

        protected TextView name, albums;
        protected ImageView artistImage;
        protected View footer;

        public ItemHoldel(View view) {
            super(view);
            this.name = (TextView) view.findViewById(R.id.artist_name);
            this.albums = (TextView) view.findViewById(R.id.album_song_count);
            this.artistImage = (ImageView) view.findViewById(R.id.artistImage);
            this.footer = view.findViewById(R.id.footer);

            view.setOnClickListener(this);

        }

       @Override
       public void onClick(View v) {

       }
   }
}
