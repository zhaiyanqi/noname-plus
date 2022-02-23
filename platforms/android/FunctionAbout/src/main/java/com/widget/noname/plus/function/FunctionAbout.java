package com.widget.noname.plus.function;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.widget.nonam.plus.function.R;
import com.widget.noname.plus.common.function.BaseFunction;

import java.util.Optional;

public class FunctionAbout extends BaseFunction {

    private static final String URL = "https://blog.gitzhai.site/archives/noname-plus-manual";

    public FunctionAbout(@NonNull Context context) {
        super(context);
    }

    public View onCreateView(Context context, @Nullable ViewGroup container) {
        return LayoutInflater.from(context).inflate(R.layout.function_about, container, false);
    }

    @Override
    protected void onViewCreated(View view) {
        super.onViewCreated(view);

        WebView webView = view.findViewById(R.id.web_view);

        webView.loadUrl(URL);
    }

    @Override
    public boolean hasView() {
        return false;
    }

    @Override
    public void onClick() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(URL);
        intent.setData(content_url);
        Optional.ofNullable(getContext()).ifPresent(c -> c.startActivity(intent));
    }
}
