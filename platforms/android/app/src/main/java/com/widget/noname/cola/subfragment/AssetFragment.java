package com.widget.noname.cola.subfragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.lxj.xpopup.XPopup;
import com.tencent.mmkv.MMKV;
import com.widget.noname.cola.MyApplication;
import com.widget.noname.cola.data.UpdateInfo;
import com.widget.noname.cola.databinding.AssetFragmentData;
import com.widget.noname.cola.util.FileConstant;
import com.widget.noname.cola.util.FileUtil;
import com.widget.noname.cola.util.JsPathUtil;

import java.io.File;
import java.util.Arrays;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AssetFragment extends Fragment {

    private static final String JS_TAG = "version_fragment";
    private static final String JS_FILE = "file:///android_asset/html/version_fragment.html";
    private static final String UPDATE_URL_GITHUB = "https://raw.githubusercontent.com/libccy/noname/master";
    private static final String UPDATE_URL_GITEE = "https://gitee.com/zhaiyanqi/noname/raw/master";
    private static final String UPDATE_URL_CODING = "https://nakamurayuri.coding.net/p/noname/d/noname/git/raw/master";

    private OkHttpClient httpClient;

    private final AssetFragmentData data = new AssetFragmentData();

    private WebView webView = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AssetFragmentBinding binding = AssetFragmentBinding.inflate(inflater);
        binding.setData(data);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String path = MMKV.defaultMMKV().getString(FileConstant.GAME_PATH_KEY, null);
        data.setAssetPath(path);

        if (null != path) {
            Observable.create(emitter -> emitter.onNext(FileUtil.getFileSize(new File(path))))
                    .subscribeOn(Schedulers.from(MyApplication.getThreadPool()))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(size -> data.setAssetSize(size.toString()));
        }

        initOkHttpClient();
        initWebView();
    }

    private void initOkHttpClient() {
        httpClient = new OkHttpClient();
        updateCodingVersionInfo();
        updateGithubVersionInfo();
    }

    private void updateGithubVersionInfo() {
        Observable.create(emitter -> {
            Request request = new Request.Builder()
                    .url(UPDATE_URL_GITHUB + "/game/update.js")
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                String res = (String) response.body().string();
                int index = res.indexOf("{");
                int lastIdx = res.lastIndexOf("}");

                if (index > -1 && lastIdx > -1) {
                    res = res.substring(index, lastIdx + 1);
                    UpdateInfo updateInfo = JSON.parseObject(res, UpdateInfo.class);
                    emitter.onNext(updateInfo);
                }
            } catch (Exception e) {
                emitter.onError(new Throwable("网络错误"));
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(obj -> {
                    UpdateInfo updateInfo = (UpdateInfo) obj;
                    updateInfo.setUrl(UPDATE_URL_GITHUB);
                    String version = "最新版本[Github源]：" + updateInfo.getVersion();
                    String[] changeLog = updateInfo.getChangeLog();

                    if (null != changeLog) {
                        String logPrefix = " 更新日志：";
                        String log = Arrays.toString(changeLog);
                        String logSuffix = "(点击更新)";
                        SpannableString spannableString = new SpannableString(version + logPrefix + log + logSuffix);
                        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#3BAFDA")), version.length(), spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
//                        data.dr(version);
//                        data.getGithubVersion().setChangeLog(changeLog);

                        //                        newVersionTextView.setText(spannableString);
//                        newVersionTextView.setOnClickListener(v -> this.askForUpdate(updateInfo));
                    } else {
//                        newVersionTextView.setText(version);
                    }
                }, throwable -> {
                    String version = "最新版本[Github源]：" + throwable.getMessage();
//                    newVersionTextView.setText(version);
//                    newVersionTextView.setOnClickListener(null);
                });
    }

    private void updateCodingVersionInfo() {
        Observable.create(emitter -> {
            Request request = new Request.Builder()
                    .url(UPDATE_URL_CODING + "/game/update.js")
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                String res = (String) response.body().string();
                int index = res.indexOf("{");
                int lastIdx = res.lastIndexOf("}");

                if (index > -1 && lastIdx > -1) {
                    res = res.substring(index, lastIdx + 1);
                    UpdateInfo updateInfo = JSON.parseObject(res, UpdateInfo.class);
                    emitter.onNext(updateInfo);
                }
            } catch (Exception e) {
                emitter.onError(new Throwable("网络错误"));
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(obj -> {
                    UpdateInfo updateInfo = (UpdateInfo) obj;
                    updateInfo.setUrl(UPDATE_URL_CODING);
                    String version = "最新版本[Coding源]：" + updateInfo.getVersion();
                    String[] changeLog = updateInfo.getChangeLog();

                    if (null != changeLog) {
                        String logPrefix = " 更新日志：";
                        String log = Arrays.toString(changeLog);
                        String logSuffix = "(点击更新)";
                        SpannableString spannableString = new SpannableString(version + logPrefix + log + logSuffix);
                        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#3BAFDA")), version.length(), spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        data.setGithubVersion(version);
                        data.setGithubChangeLog(log);
                    } else {
//                        newVersionCodingTextView.setText(version);
                    }
                }, throwable -> {
                    String version = "最新版本[Coding源]：" + throwable.getMessage();
//                    newVersionCodingTextView.setText(version);
//                    newVersionCodingTextView.setOnClickListener(null);
                });
    }

    private void askForUpdate(UpdateInfo info) {
        String title = "有新版本" + info.getUpdate() + "可用，是否下载？";
        StringBuilder sb = new StringBuilder();

        String[] changeLog = info.getChangeLog();

        if (null != changeLog) {
            for (String log : changeLog) {
                sb.append(log).append(";");
            }
        }

        new XPopup.Builder(getContext())
                .isViewMode(true)
                .asConfirm(title, sb.toString(), () -> {
                    goUpdate(info);
                }).show();
    }

    private void goUpdate(UpdateInfo info) {
        if (null != info) {
            Observable.create(emitter -> {
                Request request = new Request.Builder()
                        .url(info.getUrl() + "/game/source.js")
                        .build();
                try (Response response = httpClient.newCall(request).execute()) {
                    String res = (String) response.body().string();
                    Log.e("zyq", "res: " + res);
                    int index = res.indexOf("[");
                    int lastIdx = res.lastIndexOf("]");

                    if (index > -1 && lastIdx > -1) {
                        res = res.substring(index, lastIdx + 1);
                        JSONArray array = JSONArray.parseArray(res);
                        Log.e("zyq", "arr" + array);
//                        UpdateInfo updateInfo = JSON.parseObject(res, UpdateInfo.class);
//                        emitter.onNext(updateInfo);
                    }
                } catch (Exception e) {
                    emitter.onError(new Throwable("网络错误"));
                }
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        webView = new WebView(getContext());
        webView.setInitialScale(0);
        webView.setVerticalScrollBarEnabled(false);
        final WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);

        settings.setAllowFileAccess(true);

        //We don't save any form data in the application
        settings.setSaveFormData(false);
        settings.setSavePassword(false);

        // Jellybean rightfully tried to lock this down. Too bad they didn't give us a whitelist
        // while we do this
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setMediaPlaybackRequiresUserGesture(false);

        // Enable database
        // We keep this disabled because we use or shim to get around DOM_EXCEPTION_ERROR_16
        String databasePath = webView.getContext().getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        settings.setDatabaseEnabled(true);
        settings.setDatabasePath(databasePath);
        settings.setGeolocationDatabasePath(databasePath);
        settings.setDomStorageEnabled(true);

        settings.setGeolocationEnabled(true);
        settings.setAppCachePath(databasePath);
        settings.setAppCacheEnabled(true);
        webView.addJavascriptInterface(this, JS_TAG);
        webView.loadUrl(JS_FILE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        webView.destroy();
        webView = null;
    }

    // from js call java.
    @JavascriptInterface
    public String getUrl() {
        return JsPathUtil.getGameRootPath(getContext());
    }

    @JavascriptInterface
    public void onResourceLoad(String json) {
        Observable.create(emitter -> {
            UpdateInfo updateInfo = JSON.parseObject(json, UpdateInfo.class);

            if (null != updateInfo) {
                emitter.onNext(updateInfo.getVersion());
            }
        }).subscribeOn(Schedulers.from(MyApplication.getThreadPool()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(version -> data.setVersion(version.toString()));
    }
}

