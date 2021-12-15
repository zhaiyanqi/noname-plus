package com.widget.noname.cola.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.widget.noname.cola.R;
import com.widget.noname.cola.adapter.VersionControlViewPagerAdapter;

public class VersionFragment extends Fragment {

    private final String[] fragments = new String[]{
            "资源", "版本"
    };
    private ViewPager2 viewPager2 = null;
    private VersionControlViewPagerAdapter adapter = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_version, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPager2 = view.findViewById(R.id.view_pager2);
        viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        adapter = new VersionControlViewPagerAdapter(getActivity());
        adapter.addFragment(PagerHelper.SUB_FRAGMENT_ASSET);
        adapter.addFragment(PagerHelper.SUB_FRAGMENT_VERSION);
        viewPager2.setAdapter(adapter);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(fragments[position]);
            }
        }).attach();
    }
}
