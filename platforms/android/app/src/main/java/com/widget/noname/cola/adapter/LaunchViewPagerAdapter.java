package com.widget.noname.cola.adapter;

import static com.widget.noname.cola.fragment.PagerHelper.FRAGMENT_EXT_MANAGER;
import static com.widget.noname.cola.fragment.PagerHelper.FRAGMENT_LOCAL_SERVER;
import static com.widget.noname.cola.fragment.PagerHelper.FRAGMENT_VERSION_CONTROL;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.widget.noname.cola.fragment.ExtManageFragment;
import com.widget.noname.cola.fragment.LocalServerFragment;
import com.widget.noname.cola.fragment.VersionControlFragment;

import java.util.ArrayList;
import java.util.List;

public class LaunchViewPagerAdapter extends FragmentStateAdapter {


    private final List<String> fragmentList = new ArrayList<>();

    public LaunchViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
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
            case FRAGMENT_VERSION_CONTROL: {
                fragment = new VersionControlFragment();
                break;
            }
            case FRAGMENT_EXT_MANAGER: {
                fragment = new ExtManageFragment();
                break;
            }
            case FRAGMENT_LOCAL_SERVER: {
                fragment = new LocalServerFragment();
                break;
            }
            default:
                fragment = new VersionControlFragment();
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
