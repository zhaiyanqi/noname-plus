package com.widget.noname.cola.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lxj.xpopup.XPopup;
import com.tencent.mmkv.MMKV;
import com.widget.noname.cola.R;
import com.widget.noname.cola.data.VersionData;
import com.widget.noname.cola.util.FileConstant;
import com.widget.noname.cola.util.FileUtil;

import java.util.ArrayList;
import java.util.List;

public class VersionListRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<VersionData> list = new ArrayList<>();
    private String currentPath = "null";

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.version_list_layout, parent, false);

        return new VersionHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        VersionHolder holder = (VersionHolder) viewHolder;

        VersionData data = list.get(position);

        holder.nameTextView.setText(data.getName());
        holder.sizeTextView.setText(data.getSize());
        holder.pathTextView.setText(data.getPath());
        holder.dateTextView.setText(data.getDate());
//        holder.itemView.setSelected(data.isSelected());
        holder.itemView.setSelected((null != currentPath) && currentPath.equals(data.getPath()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick(v, data);
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void onItemClick(View view, VersionData data) {
        if ((null == currentPath) || !currentPath.equals(data.getPath())) {
            new XPopup.Builder(view.getContext())
                    .hasStatusBar(false)
                    .animationDuration(120)
                    .hasShadowBg(false)
                    .isViewMode(true)
                    .atView(view)
                    .asAttachList(new String[]{"设置为游戏主体", "删除"}, null,
                            (position, text) -> {
                                if (position == 0) {
                                    String curPath = MMKV.defaultMMKV().getString(FileConstant.GAME_PATH_KEY, null);

                                    if (null != curPath) {
                                        FileUtil.backupWebContentToPath(view.getContext(), curPath, data.getPath());
                                    }

                                    MMKV.defaultMMKV().putString(FileConstant.GAME_PATH_KEY, data.getPath());
                                    currentPath = data.getPath();
                                    unSelectAll();
                                    data.setSelected(true);
                                    notifyDataSetChanged();
                                } else if (position == 1) {
//                                    MyApplication.getThreadPool().execute(() -> {
//                                        File file = new File(data.getPath());
//                                        if (file.exists()) {
//                                            boolean delete = file.delete();
//                                        }
//                                    });
//                                    Uri uri = Uri.parse(data.getPath());
//                                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//                                    intent.addCategory(Intent.CATEGORY_OPENABLE);
//                                    intent.setType("*/*");//想要展示的文件类型
//                                    intent.putExtra(, uri);
//                                    view.getContext().startActivity(intent);
                                }
                            })
                    .show();
        }
    }

    private void unSelectAll() {
        for (VersionData data : list) {
            data.setSelected(false);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void replaceList(List<VersionData> l) {
        list.clear();
        list.addAll(l);
        currentPath = MMKV.defaultMMKV().getString(FileConstant.GAME_PATH_KEY, "null");

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class VersionHolder extends RecyclerView.ViewHolder {

        private TextView nameTextView = null;
        private TextView sizeTextView = null;
        private TextView pathTextView = null;
        private TextView dateTextView = null;


        public VersionHolder(@NonNull View itemView) {
            super(itemView);

            nameTextView = itemView.findViewById(R.id.version_list_name);
            sizeTextView = itemView.findViewById(R.id.version_list_size);
            pathTextView = itemView.findViewById(R.id.version_list_path);
            dateTextView = itemView.findViewById(R.id.version_list_date);
        }
    }
}
