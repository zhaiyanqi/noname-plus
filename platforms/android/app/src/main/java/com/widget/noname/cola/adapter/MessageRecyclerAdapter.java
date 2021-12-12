package com.widget.noname.cola.adapter;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.widget.noname.cola.R;
import com.widget.noname.cola.data.MessageData;
import com.widget.noname.cola.listener.MessageAdapterListener;

import java.util.ArrayList;
import java.util.List;

public class MessageRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<MessageData> list = new ArrayList<>();

    private MessageAdapterListener listener = null;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.message_text_view_layout, viewGroup, false);
        return new MessageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        MessageHolder holder = (MessageHolder) viewHolder;


        MessageData data = list.get(position);

        if (data.getType() == MessageData.TYPE_IP) {
            String ip = data.getMessage();
            SpannableString spannable = new SpannableString(ip);
            spannable.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    if (null != listener) {
                        listener.onIpaddrMsgClick(view, ip);
                    }
                }
            }, 0, ip.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            holder.getTextView().setText(spannable);
            holder.getTextView().setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            holder.getTextView().setText(data.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void addMessage(String msg) {
        MessageData data = new MessageData(msg);
        list.add(data);
        notifyItemChanged(list.indexOf(data));
    }

    public void addMessage(MessageData data) {
        list.add(data);
        notifyItemChanged(list.indexOf(data));
    }

    public static class MessageHolder extends RecyclerView.ViewHolder {

        private TextView textView = null;

        public MessageHolder(@NonNull View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.message_text_view);
        }

        public TextView getTextView() {
            return textView;
        }
    }

    public void setListener(MessageAdapterListener listener) {
        this.listener = listener;
    }
}
