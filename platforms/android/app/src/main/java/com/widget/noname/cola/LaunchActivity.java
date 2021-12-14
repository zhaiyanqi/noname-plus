package com.widget.noname.cola;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
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
import com.widget.noname.cola.eventbus.MsgVersionControl;
import com.widget.noname.cola.fragment.PagerHelper;
import com.widget.noname.cola.net.NonameWebSocketServer;
import com.widget.noname.cola.view.RedDotTextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class LaunchActivity extends AppCompatActivity implements OnJsBridgeCallback, RadioGroup.OnCheckedChangeListener {
    private static final String TAG = "LaunchActivity";

    private BridgeHelper bridgeHelper = null;
//    private WaveLoadingView waveLoadingView = null;

    private RadioGroup radioGroup = null;
    private ViewPager2 viewPager = null;
    private LaunchViewPagerAdapter pagerAdapter = null;
    private RedDotTextView serverStatusView = null;
    private WebView webView = null;
    private RelativeLayout rootView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        rootView = findViewById(R.id.root_view);
        hideSystemUI();
        initWebView();
        initViewPager();
        serverStatusView = findViewById(R.id.server_status_red_dot);

        Intent intent = getIntent();

        if ((null != intent) && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            showSingleChoiceDialog(data);
        }
    }

    private void initViewPager() {
        radioGroup = findViewById(R.id.button_layout);
        radioGroup.setOnCheckedChangeListener(this);

        viewPager = findViewById(R.id.view_pager);
        viewPager.setUserInputEnabled(false);

        pagerAdapter = new LaunchViewPagerAdapter(this);
        pagerAdapter.addFragment(PagerHelper.FRAGMENT_VERSION_CONTROL);
        pagerAdapter.addFragment(PagerHelper.FRAGMENT_EXT_MANAGER);
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

            case MessageType.RESTART_WEB_VIEW: {
                webView.removeAllViews();
                webView.destroy();

                initWebView();
                Toast.makeText(this, "版本切换完成", Toast.LENGTH_SHORT).show();
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

    private int yourChoice = -1;

    private void showSingleChoiceDialog(Uri data) {
        final String[] items = new String[]{"私有目录，不需要额外权限（清除数据文件会丢失）",
                "SD卡Document目录（清除数据，游戏本体不丢失, 需要SD卡权限）",
                "SD卡根目录（清除数据，游戏本体不丢失, 需要SD卡权限）"};
        yourChoice = 0;
        AlertDialog.Builder singleChoiceDialog =
                new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert);
        singleChoiceDialog.setTitle("请选择解压路径");
        singleChoiceDialog.setCancelable(false);
        singleChoiceDialog.setSingleChoiceItems(items, 0, (dialog, which) -> yourChoice = which);
        singleChoiceDialog.setPositiveButton("确定",
                (dialog, which) -> {
                    dialog.dismiss();
                    runOnUiThread(() -> {
                        if (yourChoice != -1) {
                            radioGroup.check(R.id.button_version_control);
                            MsgVersionControl msg = new MsgVersionControl();
                            msg.setUri(data);
                            msg.setMsgType(yourChoice + 1);
                            EventBus.getDefault().post(msg);
                        }
                    });
                });
        singleChoiceDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
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

    public void startGame() {
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
//        radioGroup.clearCheck();
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
            startGame();
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
        } else if (id == R.id.button_start_game) {
            startGame();
        }

        if (pos >= 0) {
            viewPager.setCurrentItem(pos);
        }
    }
}