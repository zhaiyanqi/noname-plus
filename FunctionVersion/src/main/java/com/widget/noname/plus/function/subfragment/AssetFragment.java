package com.widget.noname.plus.function.subfragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.lxj.xpopup.XPopup;
import com.tencent.mmkv.MMKV;
import com.widget.noname.cola.MyApplication;
import com.widget.noname.cola.data.UpdateInfo;
import com.widget.noname.cola.eventbus.MsgVersionControl;
import com.widget.noname.cola.subfragment.AssetFragmentBinding;
import com.widget.noname.cola.util.FileUtil;
import com.widget.noname.cola.util.JavaPathUtil;
import com.widget.noname.plus.common.util.FileConstant;
import com.widget.noname.cola.databinding.AssetFragmentData;
import com.widget.noname.plus.function.version.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

public class AssetFragment extends Fragment implements RadioGroup.OnCheckedChangeListener {

    public static final String UPDATE_URL_GITHUB = "https://raw.githubusercontent.com/libccy/noname/master";

    public static final String UPDATE_URL_GITLAB = "https://gitlab.com/zhaiyanqi929/noname/-/raw/master";
    public static final String UPDATE_URL_CODING = "https://nakamurayuri.coding.net/p/noname/d/noname/git/raw/master";

    @SuppressLint("SimpleDateFormat")
    private final DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private final AssetFragmentData data = new AssetFragmentData();

    private final AtomicInteger downloaded = new AtomicInteger();
    private int allFiles = 0;

    private OkHttpClient httpClient;

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
        initData();
        initOkHttpClient();
        initView(view);
    }

    private void initView(View view) {
        RadioGroup radioGroup = view.findViewById(R.id.radio_group_update);
        radioGroup.setOnCheckedChangeListener(this);

        view.findViewById(R.id.button_click_ask_update).setOnClickListener(v -> askForUpdate());
    }

    @SuppressLint("CheckResult")
    private void initData() {
        String path = MMKV.defaultMMKV().getString(FileConstant.GAME_PATH_KEY, null);
        data.setAssetPath(path);

        // String url = MMKV.defaultMMKV().getString(FileConstant.UPDATE_URL_KEY, UPDATE_URL_GITLAB);
        // data.setUpdateUri(url);

        if (null != path) {
            Observable.create(emitter -> emitter.onNext(FileUtil.getFileSize(new File(path))))
                    .subscribeOn(Schedulers.from(MyApplication.getThreadPool()))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(size -> data.setAssetSize(size.toString()));
            Observable.create(emitter -> {
                        File updateFile = new File(path, "game/update.js");
                        if (updateFile.exists() && updateFile.isFile()) {
                            BufferedReader reader = new BufferedReader(new FileReader(updateFile));
                            StringBuilder stringBuilder = new StringBuilder();
                            String line;
                            String ls = System.getProperty("line.separator");
                            while ((line = reader.readLine()) != null) {
                                stringBuilder.append(line);
                                stringBuilder.append(ls);
                            }
                            reader.close();
                            String content = stringBuilder.toString();
                            int index = content.indexOf("{");
                            int lastIdx = content.lastIndexOf("}");
                            if (index > -1 && lastIdx > -1) {
                                content = content.substring(index, lastIdx + 1);
                                UpdateInfo updateInfo = JSON.parseObject(content, UpdateInfo.class);
                                if (updateInfo.getVersion() != null) emitter.onNext(updateInfo);
                                else emitter.onError(new Throwable("获取失败"));
                            }
                            else emitter.onError(new Throwable("获取失败"));
                        }
                        else {
                            emitter.onNext(new Throwable("获取失败"));
                        }
                    })
                    .subscribeOn(Schedulers.from(MyApplication.getThreadPool()))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(obj -> {
                        UpdateInfo updateInfo = (UpdateInfo) obj;
                        data.setVersion(updateInfo.getVersion());
                    }, throwable -> {
                        data.setVersion(throwable.getMessage());
                    });

        }
    }

    private void initOkHttpClient() {
        httpClient = new OkHttpClient();
        data.setUpdateStatus(AssetFragmentData.STATUS_CHECK_UPDATE);
    }

    @SuppressLint("CheckResult")
    private void updateVersionInfo(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        String updateUrl = url.endsWith("/") ? (url + "game/update.js") : (url + "/game/update.js");

        Observable.create(emitter -> {
            Request request = new Request.Builder()
                    .url(updateUrl)
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                String res = response.body().string();
                int index = res.indexOf("{");
                int lastIdx = res.lastIndexOf("}");

                if (index > -1 && lastIdx > -1) {
                    res = res.substring(index, lastIdx + 1);
                    UpdateInfo updateInfo = JSON.parseObject(res, UpdateInfo.class);
                    if (updateInfo.getVersion() != null) emitter.onNext(updateInfo);
                    else emitter.onError(new Throwable("获取失败"));
                } else {
                    emitter.onError(new Throwable("获取失败"));
                    data.setUpdateStatus(AssetFragmentData.STATUS_CHECK_UPDATE);
                }
            } catch (Exception e) {
                emitter.onError(new Throwable("网络错误"));
                data.setUpdateStatus(AssetFragmentData.STATUS_CHECK_UPDATE);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(obj -> {
                    UpdateInfo updateInfo = (UpdateInfo) obj;
                    data.setUpdateVersion(updateInfo.getVersion());
                    String[] changeLog = updateInfo.getChangeLog();

                    if (null != changeLog) {
                        data.setUpdateChangeLog(Arrays.toString(changeLog));
                    }

                    if (data.getVersion() != null) {
                        data.setUpdateStatus(data.getVersion().compareTo(data.getUpdateVersion()) < 0 ?
                                AssetFragmentData.STATUS_CLICK_UPDATE : AssetFragmentData.STATUS_NEWEST);
                    } else {
                        data.setUpdateStatus(AssetFragmentData.STATUS_CLICK_UPDATE);
                    }
                }, throwable -> {
                    data.setUpdateVersion(throwable.getMessage());
                    data.setUpdateChangeLog(throwable.getMessage());
                });
    }

    private void askForUpdate() {
        if (data.getUpdateStatus() == AssetFragmentData.STATUS_CHECK_UPDATE) {
            data.setUpdateStatus(AssetFragmentData.STATUS_CHECKING);
            updateVersionInfo(data.getUpdateUri());
        } else {
            String title = "有新版本" + data.getUpdateVersion() + "可用，是否下载？";
            String info = data.getUpdateChangeLog();

            new XPopup.Builder(getContext())
                    .isViewMode(true)
                    .asConfirm(title, info, this::goUpdate).show();
        }
    }

    private void goUpdate() {
        data.setUpdateStatus(AssetFragmentData.STATUS_UPDATING);
        Observable.create(emitter -> {
            Request request = new Request.Builder()
                    .url(data.getUpdateUri() + "/game/source.js")
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                String res = Objects.requireNonNull(response.body()).string();

                int index = res.indexOf("[");
                int lastIdx = res.lastIndexOf("]");

                if (index > -1 && lastIdx > -1) {
                    String assetPath = data.getAssetPath();

                    if (null == assetPath) {
                        File root = JavaPathUtil.getAppRootFiles(getContext());

                        if (null != root) {
                            String folder = dateFormat.format(new Date());
                            String destPath = root.getPath() + File.separator + folder;

                            File file = new File(destPath);
                            if (!file.exists() || !file.isDirectory()) {
                                file.mkdirs();
                            }

                            MMKV.defaultMMKV().putString(FileConstant.GAME_PATH_KEY, destPath);
                            data.setAssetPath(destPath);
                        }
                    }

                    res = res.substring(index, lastIdx + 1);
                    JSONArray array = JSONArray.parseArray(res);
                    List<String> files = array.toJavaList(String.class);
                    files.add("game/update.js");

                    allFiles = files.size() - 1;
                    downloaded.set(0);

                    String baseUrl = data.getUpdateUri();
                    baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";

                    Iterator<String> iterator = files.iterator();

                    while (iterator.hasNext()) {
                        String file = iterator.next();
                        download(baseUrl, file);
                    }
                } else {
                    data.setDownloadProgress("解析错误");
                }
            } catch (Exception e) {
                emitter.onError(new Throwable("网络错误"));
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public void download(final String baseUrl, String path) {
        Request request = new Request.Builder().url(baseUrl + path).build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.i("DOWNLOAD", "download failed: " + e.getMessage());
                call.cancel();
                incrementAndUpdateDownInfo();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Sink sink = null;
                BufferedSink bufferedSink = null;
                ResponseBody body = response.body();

                if (body == null) {
                    return;
                }

                try {
                    File dest = new File(data.getAssetPath() + "/" + path);
                    File parentFile = dest.getParentFile();

                    if ((null != parentFile) && !parentFile.exists()) {
                        parentFile.mkdirs();
                    }

                    sink = Okio.sink(dest);
                    bufferedSink = Okio.buffer(sink);
                    bufferedSink.writeAll(body.source());
                    bufferedSink.close();
                    incrementAndUpdateDownInfo();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("DOWNLOAD", "download failed");
                    data.setUpdateStatus(AssetFragmentData.STATUS_DOWNLOAD_FAIL);
                    incrementAndUpdateDownInfo();
                } finally {
                    if (bufferedSink != null) {
                        bufferedSink.close();
                    }

                    body.close();
                }
            }
        });
    }

    private final Object downLock = new Object();

    private void incrementAndUpdateDownInfo() {
        synchronized (downLock) {
            int now = downloaded.getAndIncrement();

            if (now == allFiles) {
                data.setDownloadProgress("");
                requireActivity().runOnUiThread(() -> {
                    // webView.reload();
                });
            } else {
                data.setDownloadProgress(now + "/" + allFiles);
            }
        }
    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        String checkUrl = null;

        if (checkedId == R.id.ratio_button_github) {
            checkUrl = UPDATE_URL_GITHUB;
        } else if (checkedId == R.id.ratio_button_gitee) {
            checkUrl = UPDATE_URL_GITLAB;
        } else if (checkedId == R.id.ratio_button_coding) {
            checkUrl = UPDATE_URL_CODING;
        }

        String url = MMKV.defaultMMKV().getString(FileConstant.UPDATE_URL_KEY, "");

        if (!Objects.equals(checkUrl, url)) {
            MMKV.defaultMMKV().putString(FileConstant.UPDATE_URL_KEY, checkUrl);
            data.setUpdateUri(checkUrl);
            data.setUpdateVersion("刷新中...");
            data.setUpdateChangeLog("刷新中...");
            data.setUpdateStatus(AssetFragmentData.STATUS_CHECKING);
            updateVersionInfo(checkUrl);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onExtraZipFile(MsgVersionControl msg) {

        if (msg.getMsgType() == MsgVersionControl.MSG_TYPE_UPDATE_LIST) {
            initData();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}

