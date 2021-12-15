package com.widget.noname.cola.adapter;

import static com.widget.noname.cola.fragment.PagerHelper.SUB_FRAGMENT_ASSET;
import static com.widget.noname.cola.fragment.PagerHelper.SUB_FRAGMENT_VERSION;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.widget.noname.cola.fragment.EmptyFragment;
import com.widget.noname.cola.fragment.ExtManageFragment;
import com.widget.noname.cola.subfragment.VersionControlFragment;

import java.util.ArrayList;
import java.util.List;

public class VersionControlViewPagerAdapter extends FragmentStateAdapter {

    private final List<String> fragmentList = new ArrayList<>();

    public VersionControlViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public void addFragment(String fragment) {
        fragmentList.add(fragment);
        notifyItemChanged(fragmentList.indexOf(fragment));
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment = null;
        String frag = fragmentList.get(position);

        switch (frag) {
            case SUB_FRAGMENT_VERSION: {
                fragment = new VersionControlFragment();
                break;
            }
            case SUB_FRAGMENT_ASSET: {
                fragment = new ExtManageFragment();
                break;
            }
            default:
                fragment = new EmptyFragment();
                break;
        }

        return fragment;
    }

    @Override
    public int getItemCount() {
        return fragmentList.size();
    }

    public int getItemPosition(String fragment) {
        return fragmentList.indexOf(fragment);
    }
}
