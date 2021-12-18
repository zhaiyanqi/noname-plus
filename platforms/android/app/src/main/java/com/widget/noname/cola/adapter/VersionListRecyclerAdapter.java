package com.widget.noname.cola.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.lxj.xpopup.XPopup;
import com.tencent.mmkv.MMKV;
import com.widget.noname.cola.R;
import com.widget.noname.cola.data.VersionData;
import com.widget.noname.cola.listener.VersionControlItemListener;
import com.widget.noname.cola.util.FileConstant;

import java.util.ArrayList;
import java.util.List;

public class VersionListRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private VersionControlItemListener listener = null;
    private final List<VersionData> list = new ArrayList<>();
    private String currentPath = "null";
    private Context context = null;

    public VersionListRecyclerAdapter(Context context) {
        this.context = context;
    }

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
            new XPopup.Builder(context)
                    .hasStatusBar(false)
                    .animationDuration(120)
                    .hasShadowBg(false)
                    .isViewMode(true)
                    .atView(view)
                    .asAttachList(new String[]{"设置为游戏主体", "删除"}, null,
                            (position, text) -> {
                                if (position == 0) {
                                    setGamePath(data);
                                } else {
                                    delGamePath(data);
                                }
                            })
                    .show();
        } else {
            new XPopup.Builder(context)
                    .hasStatusBar(false)
                    .animationDuration(120)
                    .hasShadowBg(false)
                    .isViewMode(true)
                    .atView(view)
                    .asAttachList(new String[]{"删除"}, null,
                            (position, text) -> {
                                if (position == 0) {
                                    delGamePath(data);
                                }
                            })
                    .show();
        }
    }

    private void setGamePath(VersionData data) {
        XPopup.Builder builder = new XPopup.Builder(context);
        builder.asConfirm("提示", "需要重启才能生效, 是否设置为当前版本", () -> {
            unSelectAll();

            if (null != listener) {
                listener.onSetPathItemClick(data);
            }
        }).show();
    }

    private void delGamePath(VersionData data) {
        XPopup.Builder builder = new XPopup.Builder(context);
        builder.asConfirm("提示", "是否删除 " + data.getPath(), () -> {
            if (null != listener) {
                listener.onItemDelete(data);
            }
        }).show();
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
        String json = JSON.toJSONString(list);
        MMKV.defaultMMKV().encode(FileConstant.VERSION_LIST_KEY, json);

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setItemClickListener(VersionControlItemListener listener) {
        this.listener = listener;
    }

    public void clearAll() {
        list.clear();
        notifyDataSetChanged();
    }

    public void setCurrentPath(String path) {
        currentPath = path;
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
