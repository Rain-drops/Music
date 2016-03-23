package com.sgj.john.mytimber.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.sgj.john.mytimber.R;
import com.sgj.john.mytimber.adapters.ArtistAdapter;
import com.sgj.john.mytimber.dataloaders.ArtistLoader;
import com.sgj.john.mytimber.utils.PreferencesUtility;

/**
 * Created by John on 2016/3/10.
 * 艺术家
 */
public class ArtistFragment extends Fragment{

    private ArtistAdapter mAdapter;
    private GridLayoutManager layoutManager;
//    private StaggeredGridLayoutManager layoutManager;

    private boolean isGrid;
    private PreferencesUtility mPreferences;
    private RecyclerView recyclerView;

    public static ArtistFragment newInstance(){
        ArtistFragment fragment = new ArtistFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = new PreferencesUtility(getActivity());
        isGrid = mPreferences.isArtistsInGrid();
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
        setLayoutManaget();
        if(getActivity() != null){
            new loadArtists().execute("");
        }

        return view;
    }

    private void setLayoutManaget() {
        if(isGrid){
            layoutManager = new GridLayoutManager(getActivity(), 2);
        }else{
            layoutManager = new GridLayoutManager(getActivity(), 1);
        }
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private class loadArtists extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            if(getActivity() != null){
                mAdapter = new ArtistAdapter(getActivity(), ArtistLoader.getAllArtists(getActivity()));
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
