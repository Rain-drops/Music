package com.sgj.john.mytimber.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;

import com.sgj.john.mytimber.R;
import com.sgj.john.mytimber.adapters.AlbumAdapter;
import com.sgj.john.mytimber.adapters.MyPageAdapter;
import com.sgj.john.mytimber.utils.PreferencesUtility;

/**
 * Created by John on 2016/3/9.
 */
public class MainFragment extends Fragment {

    public static final String TAG = "MainFragment";

    PreferencesUtility mPreferences;
    ViewPager viewPager;
    MyPageAdapter mAdapter;


    public static MainFragment newInstance(){
        MainFragment mainFragment = new MainFragment();
        return mainFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        if(viewPager != null){
            setupViewPager(viewPager);
            viewPager.setOffscreenPageLimit(2);
        }
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);


        return view;
    }

    private void setupViewPager(ViewPager viewPager) {
        mAdapter = new MyPageAdapter(getChildFragmentManager());
        mAdapter.addFragment(SongsFragment.newInstance(), "歌曲");
        mAdapter.addFragment(AlbumFragment.newInstance(), "专辑");
        mAdapter.addFragment(ArtistFragment.newInstance(), "艺术家");
        viewPager.setAdapter(mAdapter);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
