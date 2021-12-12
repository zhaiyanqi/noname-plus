package com.widget.noname.cola;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.widget.noname.cola.adapter.LaunchViewPagerAdapter;
import com.widget.noname.cola.bridge.BridgeHelper;
import com.widget.noname.cola.bridge.OnJsBridgeCallback;
import com.widget.noname.cola.data.MessageType;
import com.widget.noname.cola.eventbus.MsgServerStatus;
import com.widget.noname.cola.eventbus.MsgToActivity;
import com.widget.noname.cola.fragment.PagerHelper;
import com.widget.noname.cola.listener.ExtractAdapter;
import com.widget.noname.cola.net.NonameWebSocketServer;
import com.widget.noname.cola.util.FileUtil;
import com.widget.noname.cola.view.RedDotTextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class LaunchActivity extends AppCompatActivity implements OnJsBridgeCallback, RadioGroup.OnCheckedChangeListener {
    private static final String TAG = "LaunchActivity";

    private BridgeHelper bridgeHelper = null;
    private WaveLoadingView waveLoadingView = null;

    private RadioGroup radioGroup = null;
    private ViewPager2 viewPager = null;
    private LaunchViewPagerAdapter pagerAdapter = null;
    private RedDotTextView serverStatusView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        hideSystemUI();
        initWaveView();

        Intent intent = getIntent();

        if ((null != intent) && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            showSingleChoiceDialog(data);
        }

        initWebView();
        initViewPager();
        serverStatusView = findViewById(R.id.server_status_red_dot);
    }


    private final List<Integer> mPageButtonList = new ArrayList<>();

    private void initViewPager() {
        radioGroup = findViewById(R.id.button_layout);
        radioGroup.setOnCheckedChangeListener(this);

        viewPager = findViewById(R.id.view_pager);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                if (position < mPageButtonList.size()) {
                    radioGroup.check(mPageButtonList.get(position));
                }
            }
        });
        viewPager.setUserInputEnabled(false);

        pagerAdapter = new LaunchViewPagerAdapter(this);
        mPageButtonList.add(R.id.button_version_control);
        mPageButtonList.add(R.id.button_extension_manage);
        mPageButtonList.add(R.id.button_local_server);
        mPageButtonList.add(R.id.button_about);

        pagerAdapter.addFragment(PagerHelper.FRAGMENT_VERSION_CONTROL);
        pagerAdapter.addFragment(PagerHelper.FRAGMENT_EXT_MANAGER);
        pagerAdapter.addFragment(PagerHelper.FRAGMENT_LOCAL_SERVER);
        pagerAdapter.addFragment(PagerHelper.FRAGMENT_ABOUT);

        viewPager.setAdapter(pagerAdapter);
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
                Log.e("zyq", "set ip: " + msg.obj);
                startGameWhenSetIp = false;
                bridgeHelper.setServerIp((String) msg.obj);
                break;
            }
            case MessageType.SET_SERVER_IP_AND_START: {
                startGameWhenSetIp = true;
                bridgeHelper.setServerIp((String) msg.obj, true);
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
    }

    private void unZipUri(Uri uri) {
        MyApplication.getThreadPool().execute(() -> {
            FileUtil.extractAll(this, uri, "default", new ExtractAdapter() {

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
                public void onExtractSaved(String path) {
                    runOnUiThread(() -> {
                        waveLoadingView.setProgressValue(100);
                        waveLoadingView.setCenterTitle(String.valueOf(100));
                        Toast.makeText(LaunchActivity.this, "导入完成：" + path, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
    }

    private int yourChoice = 0;

    private void showSingleChoiceDialog(Uri data) {
        final String[] items = {"我是1", "我是2", "我是3", "我是4"};
        yourChoice = -1;
        AlertDialog.Builder singleChoiceDialog =
                new AlertDialog.Builder(this);
        singleChoiceDialog.setTitle("我是一个单选Dialog");
        // 第二个参数是默认选项，此处设置为0
        singleChoiceDialog.setSingleChoiceItems(items, 0,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        yourChoice = which;
                    }
                });
        singleChoiceDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        runOnUiThread(() -> {
                            if (yourChoice != -1) {
                                Toast.makeText(LaunchActivity.this, "任务执行中...", Toast.LENGTH_SHORT).show();
                                waveLoadingView.setVisibility(View.VISIBLE);
                                waveLoadingView.startAnimation();
                                waveLoadingView.setProgressValue(0);

                                unZipUri(data);
                            }
                        });
                    }
                });
        singleChoiceDialog.show();
    }

    private void initWebView() {
        WebView webView = findViewById(R.id.web_view);
        bridgeHelper = new BridgeHelper(webView, this);
    }

    public void testJavaBridge(View view) {
        if (null != bridgeHelper) {
            bridgeHelper.getExtensions();
        }
    }

    public void startGame(View view) {
        startActivity(new Intent(this, MainActivity.class));
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

        if (pos > 0) {
            viewPager.setCurrentItem(pos);
        }
    }
}