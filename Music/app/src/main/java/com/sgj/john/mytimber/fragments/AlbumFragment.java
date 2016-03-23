package com.sgj.john.mytimber.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sgj.john.mytimber.R;
import com.sgj.john.mytimber.adapters.AlbumAdapter;
import com.sgj.john.mytimber.dataloaders.AlbumLoader;
import com.sgj.john.mytimber.models.Album;
import com.sgj.john.mytimber.utils.PreferencesUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by John on 2016/3/10.
 * 专辑
 */
public class AlbumFragment extends Fragment{

    private AlbumAdapter mAdapter;
    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;
    private RecyclerView.ItemDecoration itemDecoration;
    private PreferencesUtility mPreferences;
    private boolean isGrid;


    public static AlbumFragment newInstance(){
        AlbumFragment fragment = new AlbumFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferencesUtility.getInstance(getActivity());
        isGrid = mPreferences.isAlbumsInGrid();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_recyclerview, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);

        setLayoutManager();

        if(getActivity() != null){
            new loadAlbums().execute("");
        }

        return view;
    }

    private void setLayoutManager() {
        if(isGrid){
            layoutManager = new GridLayoutManager(getActivity(), 2);
        }else {
            layoutManager = new GridLayoutManager(getActivity(), 1);
        }
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private class loadAlbums extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
           if(getActivity() != null){
               mAdapter = new AlbumAdapter(getActivity(), AlbumLoader.getAllAlbums(getActivity()));
           }
            return "Executed";
        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            recyclerView.setAdapter(mAdapter);
            if(getActivity() != null){
                // TODO
            }
        }
    }
}
