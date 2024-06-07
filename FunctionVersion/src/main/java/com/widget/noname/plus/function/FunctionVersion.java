package com.widget.noname.plus.function;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.widget.noname.plus.common.function.BaseFunction;
import com.widget.noname.cola.util.FileUtil;
import com.widget.noname.plus.function.version.R;
import com.widget.noname.plus.function.adapter.VersionControlViewPagerAdapter;

public class FunctionVersion extends BaseFunction {
    public FunctionVersion(@NonNull Context context) {
        super(context);
    }

    @Override
    public View onCreateView(Context context, @Nullable ViewGroup container) {
        return LayoutInflater.from(context).inflate(R.layout.function_version, container, false);
    }

    @Override
    protected void onViewCreated(View view) {
        super.onViewCreated(view);

        viewPager2 = view.findViewById(R.id.view_pager2);
        viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        adapter = new VersionControlViewPagerAdapter((FragmentActivity) getContext());
        adapter.addFragment(PagerHelper.SUB_FRAGMENT_ASSET);
        adapter.addFragment(PagerHelper.SUB_FRAGMENT_VERSION);
        viewPager2.setAdapter(adapter);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> tab.setText(fragments[position])).attach();

        boolean fileExist = FileUtil.isAssetFileExist(getContext(), "noname.zip");
        TextView textView = view.findViewById(R.id.text_import_file);

        if (fileExist) {
//            textView.setTypeface(MyApplication.getTypeface());
            textView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
            textView.getPaint().setAntiAlias(true);
//            textView.setOnClickListener(v -> new XPopup.Builder(getContext())
//                    .hasStatusBar(false)
//                    .dismissOnBackPressed(false)
//                    .dismissOnTouchOutside(false)
//                    .asConfirm("是否导入内置资源包？", "", () -> {
////                        textView.setClickable(false);
////                        textView.setEnabled(false);
////                        MsgToActivity msg = new MsgToActivity();
////                        msg.type = MessageType.IMPORT_GAME_FILE;
////                        EventBus.getDefault().post(msg);
//                    }).show());
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    private final String[] fragments = new String[]{
            "资源", "版本"
    };

    private ViewPager2 viewPager2 = null;
    private VersionControlViewPagerAdapter adapter = null;
}
