package com.widget.noname.cola.fragment;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.lxj.xpopup.XPopup;
import com.widget.noname.cola.MyApplication;
import com.widget.noname.cola.R;
import com.widget.noname.cola.adapter.VersionControlViewPagerAdapter;
import com.widget.noname.cola.data.MessageType;
import com.widget.noname.cola.eventbus.MsgToActivity;
import com.widget.noname.cola.util.FileUtil;

import org.greenrobot.eventbus.EventBus;

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
        adapter = new VersionControlViewPagerAdapter(requireActivity());
        adapter.addFragment(PagerHelper.SUB_FRAGMENT_ASSET);
        adapter.addFragment(PagerHelper.SUB_FRAGMENT_VERSION);
        viewPager2.setAdapter(adapter);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> tab.setText(fragments[position])).attach();

        boolean fileExist = FileUtil.isAssetFileExist(getContext(), "noname.zip");
        TextView textView = view.findViewById(R.id.text_import_file);

        if (fileExist) {
            textView.setTypeface(MyApplication.getTypeface());
            textView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
            textView.getPaint().setAntiAlias(true);
            textView.setOnClickListener(v -> new XPopup.Builder(getContext())
                    .hasStatusBar(false)
                    .dismissOnBackPressed(false)
                    .dismissOnTouchOutside(false)
                    .asConfirm("是否导入内置资源包？", "", () -> {
                        textView.setClickable(false);
                        textView.setEnabled(false);
                        MsgToActivity msg = new MsgToActivity();
                        msg.type = MessageType.IMPORT_GAME_FILE;
                        EventBus.getDefault().post(msg);
                    }).show());
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    // from java call js
}
