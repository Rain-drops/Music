package com.sgj.john.mytimber.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sgj.john.mytimber.R;
import com.sgj.john.mytimber.adapters.SongsListAdapter;
import com.sgj.john.mytimber.dataloaders.SongLoader;
import com.sgj.john.mytimber.listeners.MusicStateListener;
import com.sgj.john.mytimber.widgets.DividerItemDecoration;

/**
 * Created by John on 2016/3/9.
 * 歌曲列表
 */
public class SongsFragment extends Fragment implements MusicStateListener{

    private SongsListAdapter mAdapter;
    private RecyclerView recyclerView;

    public static SongsFragment newInstance(){
        SongsFragment fragment = new SongsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_recyclerview, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        new loadSongs().execute("");
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void restartLoader() {

    }

    @Override
    public void onPlaylistChanged() {

    }

    @Override
    public void onMetaChanged() {
        if(mAdapter != null){
            mAdapter.notifyDataSetChanged();
        }
    }
    private class loadSongs extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            if(getActivity() != null){
                mAdapter = new SongsListAdapter((AppCompatActivity) getActivity(), SongLoader.getAllSongs(getActivity()), false);
            }
            return "Executed";
        }

        @Override
        protected void onPostExecute(String s) {
            recyclerView.setAdapter(mAdapter);
            if(getActivity() != null){
                //控制Item间的间隔（可绘制）, 通过ItemDecoration
                //控制Item增删的动画，请通过ItemAnimator
                recyclerView.addItemDecoration(new DividerItemDecoration());
            }

        }

        @Override
        protected void onPreExecute() {

        }
    }
}
