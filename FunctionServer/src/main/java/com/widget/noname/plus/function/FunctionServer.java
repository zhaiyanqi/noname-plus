package com.widget.noname.plus.function;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.lxj.xpopup.XPopup;
import com.tencent.mmkv.MMKV;
import com.widget.noname.cola.data.MessageType;
import com.widget.noname.cola.eventbus.MsgToActivity;
import com.widget.noname.plus.common.function.BaseFunction;
import com.widget.noname.plus.common.manager.FontManager;
import com.widget.noname.plus.common.manager.ThreadManager;
import com.widget.noname.plus.common.manager.WebViewManager;
import com.widget.noname.plus.common.util.FileConstant;
import com.widget.noname.plus.common.util.NetUtil;
import com.widget.noname.plus.server.NonameWebSocketServer;
import com.widget.noname.plus.server.R;

import org.greenrobot.eventbus.EventBus;

import java.net.UnknownHostException;
import java.util.Arrays;

public class FunctionServer extends BaseFunction implements View.OnClickListener, MessageAdapterListener {
    private static final String ROOT_URI = "file:///android_asset/js/start.html";
    private static final String JS_TAG = "jsBridge";

    private static final int MSG_UPDATE_SERVER_IPADDR = 1;
    private static final int MSG_UPDATE_SERVER_START = 2;
    private static final int MSG_UPDATE_SCREEN_MESSAGE = 3;

    private static final int SERVER_PORT = 8080;

    private static final Object serverLock = new Object();

    private static NonameWebSocketServer server = null;
    private Handler handler = null;

    private Button startButton = null;
    private RedDotTextView serverStatusView = null;
    private RecyclerView messageRecyclerView = null;
    private MessageRecyclerAdapter adapter = null;
    private int serverStatus = NonameWebSocketServer.SERVER_TYPE_STOP;

    private WebView webView = null;

    public FunctionServer(@NonNull Context context) {
        super(context);

        handler = new ServerHandler(Looper.getMainLooper());
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onInit() {
        super.onInit();

        webView = WebViewManager.obtain(getContext());
        WebViewManager.addMinWebViewToContainer(getContainer(), webView);
        WebViewManager.initWebview(webView, ROOT_URI, new Object() {
            @JavascriptInterface
            public void onServeIpSet() {
                Log.e("zyq", "onServeIpSet");
            }

            @JavascriptInterface
            public void onRecentIpsUpdate(String value) {
                Log.e("zyq", "onRecentIpsUpdate: value: " + value);
            }
        }, JS_TAG);
    }

    @Override
    public void onDeInit() {
        WebViewManager.destroy(webView);
        webView = null;

        super.onDeInit();
    }

    @Override
    public View onCreateView(Context context, @Nullable ViewGroup container) {
        return LayoutInflater.from(context).inflate(R.layout.function_server, container, false);
    }

    @Override
    public void onViewCreated(View view) {
        super.onViewCreated(view);

        startButton = view.findViewById(R.id.start_server_button);
        startButton.setTypeface(FontManager.getInstance().getTypeface());
        startButton.setOnClickListener(this);
        messageRecyclerView = view.findViewById(R.id.message_recycler);
        adapter = new MessageRecyclerAdapter();
        initMessageAdapter();
        adapter.setListener(this);

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        messageRecyclerView.setLayoutManager(mLinearLayoutManager);
        messageRecyclerView.setAdapter(adapter);

        serverStatusView = view.findViewById(R.id.server_status_red_dot);
    }

    private void initMessageAdapter() {
        adapter.addMessage("点击启动按钮创建服务器, 也可以直接点击下方ip设置并开始游戏⬇️");
        adapter.addMessage("可用服务器（下列地址点击可弹出菜单）");
        adapter.addMessage(new MessageData("159.75.51.253", MessageData.TYPE_IP));
        adapter.addMessage(new MessageData("47.99.105.222", MessageData.TYPE_IP));

        ThreadManager.getInstance().execute(() -> {
            ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData primaryClip = cm.getPrimaryClip();
            FragmentActivity activity = (FragmentActivity) getContext();
            int itemCount = 0;

            if (null != primaryClip) {
                itemCount = primaryClip.getItemCount();
            }

            if ((itemCount > 0) && (null != activity)) {
                ClipData.Item itemAt = primaryClip.getItemAt(itemCount - 1);
                if (itemAt != null && itemAt.getText() != null) {
                    String ip = itemAt.getText().toString();
                    if (NetUtil.ipCheck(ip)) {
                        addMessageToScreen("来自剪切板的ip：");
                        addIpaddrToScreen(ip);
                    }
                }
            }

            String json = MMKV.defaultMMKV().decodeString(FileConstant.IP_LIST_KEY);
            JSONArray array = JSON.parseArray(json);

            if (null != array && array.size() > 0) {

                addMessageToScreen("最近连接：");
                for (int i = 0; i < array.size(); i++) {
                    addIpaddrToScreen(array.get(i).toString());
                }
            }
        });
    }

    private void startLocalServer() {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            if (isServerStarted()) {
                addMessageToScreen("服务器运行中，请勿重复创建");

                return;
            }

            setServerStatus(NonameWebSocketServer.SERVER_TYPE_START);
            addMessageToScreen("服务器创建中，端口：" + SERVER_PORT);

            ThreadManager.getInstance().execute(() -> {
                try {
                    synchronized (serverLock) {
                        if (server != null) {
                            try {
                                server.stop();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } finally {
                                server = null;
                            }
                        }

                        server = new NonameWebSocketServer(8080, this::setServerStatus);
                        server.setReuseAddr(true);
                        server.start();
                    }

                    addMessageToScreen("服务器启动成功, 可以尝试连接以下几个地址进入服务器：");
                    addMessageToScreen("(下列ip可一点击复制、设置到游戏内或者设置并启动游戏)");

                    String[] ipaddr = NetUtil.getIpaddr();

                    for (String ip : ipaddr) {
                        addIpaddrToScreen(ip + ":" + SERVER_PORT);
                    }
                } catch (UnknownHostException e) {
                    addMessageToScreen("服务器创建失败，" + e.getLocalizedMessage());
                    try {
                        server.stop();
                        addMessageToScreen("服务器尝试停止");
                    } catch (InterruptedException interruptedException) {
                        addMessageToScreen("服务器清理失败，" + interruptedException.getLocalizedMessage());
                    }

                    e.printStackTrace();
                }
            });
        } else {
            handler.obtainMessage(MSG_UPDATE_SERVER_START).sendToTarget();
        }
    }

    private boolean isServerStarted() {
        return (serverStatus == NonameWebSocketServer.SERVER_TYPE_START)
                || (serverStatus == NonameWebSocketServer.SERVER_TYPE_RUNNING);
    }

    private void stopLocalServer() {
        ThreadManager.getInstance().execute(() -> {
            synchronized (serverLock) {
                if (null != server) {
                    try {
                        server.stop();
                        addMessageToScreen("本地服务器已停止。");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        addMessageToScreen("操作失败：" + Arrays.toString(e.getStackTrace()));
                    } finally {
                        server = null;
                    }
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopLocalServer();
    }

    private void addMessageToScreen(String msg) {
        Message message = handler.obtainMessage();
        message.what = MSG_UPDATE_SCREEN_MESSAGE;
        message.obj = msg;
        message.sendToTarget();
    }

    private void addIpaddrToScreen(String ip) {
        Message message = handler.obtainMessage();
        message.what = MSG_UPDATE_SERVER_IPADDR;
        message.obj = ip;
        message.sendToTarget();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.start_server_button) {
            if (isServerStarted()) {
                stopLocalServer();
            } else {
                startLocalServer();
            }

        }
    }

    @Override
    public void onIpaddrMsgClick(View view, String ip) {
        new XPopup.Builder(getContext())
                .hasStatusBar(false)
                .animationDuration(120)
                .hasShadowBg(false)
                .isViewMode(true)
                .atView(view)
                .asAttachList(new String[]{"复制", "设置为联机ip", "设置ip并开始游戏", "取消"}, null,
                        (position, text) -> {
                            if (position == 0) {
                                setToClipboard(ip);
                            } else if (1 == position) {
                                handler.postDelayed(() -> {
                                    MsgToActivity msg = new MsgToActivity();
                                    msg.type = MessageType.SET_SERVER_IP;
                                    msg.obj = ip;
                                    EventBus.getDefault().post(msg);
                                }, 300);

                            } else if (2 == position) {
                                MsgToActivity msg = new MsgToActivity();
                                msg.type = MessageType.SET_SERVER_IP_AND_START;
                                msg.obj = ip;
                                EventBus.getDefault().post(msg);
                            }
                        })
                .show();
    }

    private void setToClipboard(String string) {
        ThreadManager.getInstance().execute(() -> {
            ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mClipData = ClipData.newPlainText("Label", string);
            cm.setPrimaryClip(mClipData);

            ClipData primaryClip = cm.getPrimaryClip();
            int itemCount = primaryClip.getItemCount();
            FragmentActivity activity = (FragmentActivity) getContext();

            if ((itemCount > 0) && (null != activity)) {
                ClipData.Item itemAt = primaryClip.getItemAt(itemCount - 1);
                CharSequence text1 = itemAt.getText();

                activity.runOnUiThread(() -> {
                    Toast.makeText(getContext(), "已复制：" + text1, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    public void setServerStatus(int serverStatus) {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            handler.post(() -> setServerStatus(serverStatus));
            return;
        }

        if (!isAlive) {
            return;
        }

        this.serverStatus = serverStatus;
        serverStatusView.setStatus(serverStatus);

        switch (serverStatus) {
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

        switch (serverStatus) {
            case NonameWebSocketServer.SERVER_TYPE_START:
            case NonameWebSocketServer.SERVER_TYPE_RUNNING: {
                startButton.setText(R.string.btn_text_server_end);
                break;
            }
            case NonameWebSocketServer.SERVER_TYPE_CLOSE:
            case NonameWebSocketServer.SERVER_TYPE_ERROR:
            case NonameWebSocketServer.SERVER_TYPE_STOP: {
                startButton.setText(R.string.btn_text_server_start);
                break;
            }
        }
    }

    private class ServerHandler extends Handler {

        public ServerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MSG_UPDATE_SERVER_IPADDR: {
                    String ip = String.valueOf(msg.obj);
                    MessageData data = new MessageData(ip, MessageData.TYPE_IP);
                    adapter.addMessage(data);
                    messageRecyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                    break;
                }

                case MSG_UPDATE_SCREEN_MESSAGE: {
                    adapter.addMessage(String.valueOf(msg.obj));
                    messageRecyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                    break;
                }

                case MSG_UPDATE_SERVER_START: {
                    startLocalServer();
                    break;
                }
            }
        }
    }
}
