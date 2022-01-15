package com.widget.noname.cola;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.PathInterpolator;
import android.webkit.WebView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.alibaba.fastjson.JSON;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.impl.ConfirmPopupView;
import com.permissionx.guolindev.PermissionX;
import com.tencent.mmkv.MMKV;
import com.widget.noname.cola.adapter.LaunchViewPagerAdapter;
import com.widget.noname.cola.bridge.BridgeHelper;
import com.widget.noname.cola.bridge.OnJsBridgeCallback;
import com.widget.noname.cola.data.MessageType;
import com.widget.noname.cola.eventbus.MsgServerStatus;
import com.widget.noname.cola.eventbus.MsgToActivity;
import com.widget.noname.cola.eventbus.MsgVersionControl;
import com.widget.noname.cola.fragment.PagerHelper;
import com.widget.noname.cola.listener.ExtractListener;
import com.widget.noname.cola.util.FileConstant;
import com.widget.noname.cola.util.FileUtil;
import com.widget.noname.cola.util.JavaPathUtil;
import com.widget.noname.cola.view.RedDotTextView;
import com.widget.noname.plus.server.NonameWebSocketServer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LaunchActivity extends AppCompatActivity implements OnJsBridgeCallback,
        RadioGroup.OnCheckedChangeListener, ExtractListener {
    private static final String TAG = "LaunchActivity";

    @SuppressLint("SimpleDateFormat")
    private final DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    private BridgeHelper bridgeHelper = null;
    private RadioGroup radioGroup = null;
    private ViewPager2 viewPager = null;
    private LaunchViewPagerAdapter pagerAdapter = null;
    private RedDotTextView serverStatusView = null;
    private WebView webView = null;
    private WaveLoadingView waveLoadingView = null;
    private int importChoice = -1;
    private ObjectAnimator waveViewAnimator = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        hideSystemUI();
        initWebView();
        initViewPager();
        initWaveView();

        serverStatusView = findViewById(R.id.server_status_red_dot);

        Intent intent = getIntent();

        if ((null != intent) && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            showSingleChoiceDialog(data);
        } else {
            String path = MMKV.defaultMMKV().getString(FileConstant.GAME_PATH_KEY, null);

            if (null == path) {
                askExtraDefaultFile();
            }
        }
    }

    private void askExtraDefaultFile() {
        boolean fileExist = FileUtil.isAssetFileExist(this, "noname.zip");

        if (fileExist) {
            new XPopup.Builder(this)
                    .dismissOnBackPressed(false)
                    .dismissOnTouchOutside(false)
                    .asConfirm("资源目录不存在，是否导入内置资源包？", "", () -> {
                        File root = JavaPathUtil.getAppRootFiles(LaunchActivity.this);
                        String folder = dateFormat.format(new Date());
                        extraAssetFile(root, folder);
                    }).show();
        }
    }

    private void extraAssetFile(File dest, String folder) {
        waveLoadingView.setVisibility(View.VISIBLE);

        MyApplication.getThreadPool().execute(() -> {
            String asset = "noname.zip";
            FileUtil.extractAssetToGame(this, asset, dest, folder, this);
        });
    }

    private void initWaveView() {
        Resources resources = getResources();
        waveLoadingView = findViewById(R.id.wave_loading_view);
        waveLoadingView.setShapeType(WaveLoadingView.ShapeType.CIRCLE);
        waveLoadingView.setWaterLevelRatio(0);
        waveLoadingView.setBorderWidth(resources.getDimensionPixelSize(R.dimen.wave_border_width));
        waveLoadingView.setAmplitudeRatio(10);
        waveLoadingView.setWaveColor(resources.getColor(R.color.wave_view_wave_color));
        waveLoadingView.setBorderColor(resources.getColor(R.color.wave_view_border_color));
        waveLoadingView.setAnimDuration(3000);
        waveLoadingView.setCenterTitleColor(Color.WHITE);
        waveLoadingView.setCenterTitleFont(MyApplication.getTypeface());
    }

    private void initViewPager() {
        radioGroup = findViewById(R.id.button_layout);
        radioGroup.setOnCheckedChangeListener(this);

        viewPager = findViewById(R.id.view_pager);
        viewPager.setUserInputEnabled(false);

        pagerAdapter = new LaunchViewPagerAdapter(this);
        pagerAdapter.addFragment(PagerHelper.FRAGMENT_VERSION_CONTROL);
//        pagerAdapter.addFragment(PagerHelper.FRAGMENT_EXT_MANAGER);
        pagerAdapter.addFragment(PagerHelper.FRAGMENT_LOCAL_SERVER);
        pagerAdapter.addFragment(PagerHelper.FRAGMENT_ABOUT);

        viewPager.setAdapter(pagerAdapter);
        radioGroup.check(R.id.button_version_control);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        hideSystemUI();
    }

    private boolean startGameWhenSetIp = false;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MsgToActivity msg) {
        switch (msg.type) {
            case MessageType.SET_SERVER_IP: {
                startGameWhenSetIp = false;
                bridgeHelper.setServerIp((String) msg.obj);
                break;
            }
            case MessageType.SET_SERVER_IP_AND_START: {
                startGameWhenSetIp = true;
                bridgeHelper.setServerIp((String) msg.obj, true);
                break;
            }

            case MessageType.RESTART_APPLICATION: {
                askToQuit();
                break;
            }

            case MessageType.IMPORT_GAME_FILE: {
                File root = JavaPathUtil.getAppRootFiles(LaunchActivity.this);
                String folder = dateFormat.format(new Date());
                extraAssetFile(root, folder);
                break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServerStatusChange(MsgServerStatus msg) {
        switch (msg.getStatus()) {
            case NonameWebSocketServer.SERVER_TYPE_START: {
                serverStatusView.setText(R.string.server_start);
                break;
            }
            case NonameWebSocketServer.SERVER_TYPE_RUNNING: {
                serverStatusView.setText(R.string.server_running);
                break;
            }
            case NonameWebSocketServer.SERVER_TYPE_CLOSE: {
                serverStatusView.setText(R.string.server_close);
                break;
            }
            case NonameWebSocketServer.SERVER_TYPE_ERROR: {
                serverStatusView.setText(R.string.server_error);
                break;
            }
            case NonameWebSocketServer.SERVER_TYPE_STOP: {
                serverStatusView.setText(R.string.server_stop);
                break;
            }
        }
        serverStatusView.setStatus(msg.getStatus());
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

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    private void showSingleChoiceDialog(Uri data) {
        importChoice = 0;
        AlertDialog.Builder singleChoiceDialog =
                new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert);
        singleChoiceDialog.setTitle("请选择解压路径");
        singleChoiceDialog.setCancelable(false);

        String path = MMKV.defaultMMKV().getString(FileConstant.GAME_PATH_KEY, null);
        String[] importChoices = null;

        if (!TextUtils.isEmpty(path)) {
            importChoices = new String[]{
                    "覆盖当前版本「 " + path + " 」",
                    "私有目录，不需要额外权限（清除数据, 文件会丢失）",
                    "SD卡Document目录（游戏本体不丢失, 需要SD卡权限, 导入速度慢）"};
            singleChoiceDialog.setPositiveButton("确定",
                    (dialog, which) -> {
                        dialog.dismiss();
                        runOnUiThread(() -> {
                            if (importChoice != -1) {
                                radioGroup.check(R.id.button_version_control);
                                importAsset(importChoice, data);
                            }
                        });
                    });
        } else {
            importChoices = new String[]{
                    "私有目录，不需要额外权限（清除数据, 文件会丢失）",
                    "SD卡Document目录（游戏本体不丢失, 需要SD卡权限, 导入速度慢）"};
            singleChoiceDialog.setPositiveButton("确定",
                    (dialog, which) -> {
                        dialog.dismiss();
                        runOnUiThread(() -> {
                            if (importChoice != -1) {
                                radioGroup.check(R.id.button_version_control);
                                importAsset(importChoice + 1, data);
                            }
                        });
                    });
        }

        singleChoiceDialog.setSingleChoiceItems(importChoices, 0, (dialog, which) -> importChoice = which);
        singleChoiceDialog.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
        singleChoiceDialog.show();
    }

    // import asset
    private void importAsset(int type, Uri uri) {
        switch (type) {
            case MsgVersionControl.MSG_TYPE_EXTRA_CURRENT: {
                String path = MMKV.defaultMMKV().getString(FileConstant.GAME_PATH_KEY, null);

                if (null != path) {
                    File root = new File(path);
                    unZipUri(uri, root, "");
                }
                break;
            }
            case MsgVersionControl.MSG_TYPE_EXTRA_INTERNAL: {
                File root = JavaPathUtil.getAppRootFiles(this);
                String folder = dateFormat.format(new Date());
                unZipUri(uri, root, folder);
                break;
            }
            case MsgVersionControl.MSG_TYPE_EXTRA_EXTERNAL_DOCUMENT: {
                PermissionX.init(this)
                        .permissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .request((allGranted, grantedList, deniedList) -> {
                            if (allGranted) {
                                File document = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                                File root = new File(document.getAbsolutePath() + File.separator + "noname");

                                if (!root.exists() || !root.isDirectory()) {
                                    root.mkdirs();
                                }

                                File nomedia = new File(document.getAbsolutePath() + File.separator + "noname/.nomedia");

                                if (!nomedia.exists() || !nomedia.isFile()) {
                                    try {
                                        nomedia.createNewFile();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                String folder = dateFormat.format(new Date());
                                unZipUri(uri, root, folder);
                            } else {
                                Toast.makeText(LaunchActivity.this, "未获取到SD卡权限，无法解压，请检查系统设置。", Toast.LENGTH_SHORT).show();
                            }
                        });
                break;
            }
            case MsgVersionControl.MSG_TYPE_EXTRA_EXTERNAL: {

                break;
            }
        }
    }

    private void unZipUri(Uri uri, File dest, String folder) {
        waveLoadingView.setVisibility(View.VISIBLE);

        MyApplication.getThreadPool().execute(() -> {

            FileUtil.extractUriToGame(this, uri, dest, folder, this);
        });
    }

    private void hideWaveLoadingView() {

        if ((null != waveViewAnimator) && waveViewAnimator.isStarted()) {
            waveViewAnimator.cancel();
        }

        if (null == waveViewAnimator) {
            waveViewAnimator = ObjectAnimator.ofFloat(waveLoadingView, "alpha", 1f, 0);
            waveViewAnimator.setDuration(250);
            waveViewAnimator.setInterpolator(new PathInterpolator(0.33f, 0, 0.67f, 1f));
            waveViewAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    waveLoadingView.setVisibility(View.GONE);
                }
            });
        }

        waveViewAnimator.start();
    }

    private void initWebView() {
        webView = findViewById(R.id.web_view);
        bridgeHelper = new BridgeHelper(webView, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        WebViewManager.recycle(webView);
    }

    public void startGame(View view) {
        String path = MMKV.defaultMMKV().getString(FileConstant.GAME_PATH_KEY, null);

        if (path == null) {
            Toast.makeText(this, "游戏目录不正确，请检查版本设置。", Toast.LENGTH_SHORT).show();
            return;
        }

        File file = new File(path);

        if (!file.exists() || !file.isDirectory()) {
            Toast.makeText(this, "游戏目录不存在，请检查版本设置。", Toast.LENGTH_SHORT).show();
            return;
        }

        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
    }

    @Override
    public void onExtensionGet(String[] extensions) {
        if (null != extensions) {
            if (null != bridgeHelper) {
                for (String ext : extensions) {
                    bridgeHelper.getExtensionState(ext);
                }
            }
        }
    }

    @Override
    public void onExtensionStateGet(String ext, boolean state) {

    }

    @Override
    public void onServeIpSet() {
        if (startGameWhenSetIp) {
            startGame(null);
        }
    }

    @Override
    public void onPageStarted() {
        if (null != bridgeHelper) {
            bridgeHelper.getExtensions();
        }
    }

    @Override
    public void onRecentIpUpdate(String ips) {
        if (null != ips) {
            String[] split = ips.split(",");

            if (null != split) {
                List<String> ipList = new ArrayList<>();

                for (String ip : split) {
                    int index = ip.lastIndexOf(":8080");
                    if (index > 0) {
                        ip = ip.substring(0, index);
                    }

                    ipList.add(ip);
                }

                String json = JSON.toJSONString(ipList);
                MMKV.defaultMMKV().encode(FileConstant.IP_LIST_KEY, json);
            }
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int id) {
        int pos = -1;

        if (id == R.id.button_version_control) {
            pos = pagerAdapter.getItemPosition(PagerHelper.FRAGMENT_VERSION_CONTROL);
        } else if (id == R.id.button_extension_manage) {
            pos = pagerAdapter.getItemPosition(PagerHelper.FRAGMENT_EXT_MANAGER);
        } else if (id == R.id.button_local_server) {
            pos = pagerAdapter.getItemPosition(PagerHelper.FRAGMENT_LOCAL_SERVER);
        } else if (id == R.id.button_about) {
            pos = pagerAdapter.getItemPosition(PagerHelper.FRAGMENT_ABOUT);
        }

        if (pos >= 0) {
            viewPager.setCurrentItem(pos);
        }
    }

    @Override
    public void onExtractProgress(int progress) {
        runOnUiThread(() -> {
            waveLoadingView.setProgressValue(progress);
            waveLoadingView.setCenterTitle(String.valueOf(progress));
        });
    }

    @Override
    public void onExtractDone() {

    }

    @Override
    public void onExtractError() {

    }

    @Override
    public void onExtractCancel() {

    }

    @Override
    public void onExtractSaved(String path) {
        File file = new File(path);
        List<File> gameInPath = FileUtil.findGameInPath(file);

        if (gameInPath.size() > 0) {
            MMKV.defaultMMKV().putString(FileConstant.GAME_PATH_KEY, gameInPath.get(0).getPath());
        }

        runOnUiThread(() -> {
            waveLoadingView.setProgressValue(100);
            waveLoadingView.setCenterTitle(String.valueOf(100));
            MsgVersionControl msg = new MsgVersionControl();
            msg.setMsgType(MsgVersionControl.MSG_TYPE_UPDATE_LIST);
            EventBus.getDefault().post(msg);
            hideWaveLoadingView();
            askToQuit();
        });
    }

    private void askToQuit() {
        XPopup.Builder builder = new XPopup.Builder(this);
        builder.isClickThrough(false);
        builder.dismissOnTouchOutside(false);
        builder.dismissOnBackPressed(false);
        ConfirmPopupView confirmPopupView = builder.asConfirm("提示", "需要重启才能生效", () -> {
            Process.killProcess(Process.myPid());
        });
        confirmPopupView.isHideCancel = true;
        confirmPopupView.show();
    }
}