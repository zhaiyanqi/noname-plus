package com.widget.noname.plus.function;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.widget.noname.plus.server.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<MessageData> list = new ArrayList<>();
    private final DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
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
            spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#24ED2D")), 0, ip.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

            holder.getTextView().setText(spannable);
            holder.getTextView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != listener) {
                        listener.onIpaddrMsgClick(v, ip);
                    }
                }
            });
            holder.getTextView().setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            holder.getTextView().setText(data.getMessage());
        }

        holder.getDataTextView().setText(data.getDate());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void addMessage(String msg) {
        MessageData data = new MessageData(msg);
        addMessage(data);
    }

    public void addMessage(MessageData data) {
        data.setDate(dateFormat.format(new Date()));
        list.add(data);
        notifyItemChanged(list.indexOf(data));
    }

    public static class MessageHolder extends RecyclerView.ViewHolder {

        private TextView textView = null;
        private TextView dataTextView = null;


        public MessageHolder(@NonNull View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.message_text_view);
            dataTextView = itemView.findViewById(R.id.message_date_text_view);
        }

        public TextView getTextView() {
            return textView;
        }

        public TextView getDataTextView() {
            return dataTextView;
        }
    }

    public void setListener(MessageAdapterListener listener) {
        this.listener = listener;
    }
}
