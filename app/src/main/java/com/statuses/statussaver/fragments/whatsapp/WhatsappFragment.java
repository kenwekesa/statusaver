package com.statuses.statussaver.fragments.whatsapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.statuses.statussaver.R;
import com.statuses.statussaver.adapter.ViewpagerAdapter;


public class WhatsappFragment extends Fragment {

    ViewPager viewPager;
    TabLayout tabLayout;

    public WhatsappFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_whatsapp, container, false);
        viewPager = (ViewPager) v.findViewById(R.id.viewPager_wa);
        tabLayout =  v.findViewById(R.id.tab_layout_wa);
        viewPager.setOffscreenPageLimit(2);
        ViewpagerAdapter adapter = new ViewpagerAdapter(getChildFragmentManager());
        adapter.addTabs("Images",new WhatsappImageFragment());
        adapter.addTabs("Videos",new WhatsappVideosFragment());
        adapter.addTabs("Saved", new SavedFragment());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        return v;
    }

}