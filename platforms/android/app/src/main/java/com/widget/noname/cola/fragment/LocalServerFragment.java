package com.widget.noname.cola.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.widget.noname.cola.MyApplication;
import com.widget.noname.cola.R;
import com.widget.noname.cola.adapter.MessageRecyclerAdapter;
import com.widget.noname.cola.data.MessageData;
import com.widget.noname.cola.eventbus.MsgServerStatus;
import com.widget.noname.cola.listener.MessageAdapterListener;
import com.widget.noname.cola.net.NonameWebSocketServer;
import com.widget.noname.cola.util.NetUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.net.UnknownHostException;
import java.util.Arrays;

public class LocalServerFragment extends Fragment implements View.OnClickListener, MessageAdapterListener {

    private static final int MSG_UPDATE_SERVER_IPADDR = 1;
    private static final int MSG_UPDATE_SERVER_START = 2;
    private static final int MSG_UPDATE_SCREEN_MESSAGE = 3;

    private static final int SERVER_PORT = 8080;

    private static final Object serverLock = new Object();

    private NonameWebSocketServer server = null;
    private Handler handler = null;

    private Button startButton = null;
    private RecyclerView messageRecyclerView = null;
    private MessageRecyclerAdapter adapter = null;
    private int serverStatus = NonameWebSocketServer.SERVER_TYPE_STOP;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new ServerHandler(Looper.getMainLooper());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_local_server, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        startButton = view.findViewById(R.id.start_server_button);
        startButton.setTypeface(MyApplication.getTypeface());
        startButton.setOnClickListener(this);
        messageRecyclerView = view.findViewById(R.id.message_recycler);
        adapter = new MessageRecyclerAdapter();
        adapter.addMessage("点击启动按钮创建服务器");
        adapter.setListener(this);

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        messageRecyclerView.setLayoutManager(mLinearLayoutManager);
        messageRecyclerView.setAdapter(adapter);
    }

    private void startLocalServer() {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            if (isServerStarted()) {
                addMessageToScreen("服务器运行中，请勿重复创建");

                return;
            }

            setServerStatus(NonameWebSocketServer.SERVER_TYPE_START);
            addMessageToScreen("服务器创建中，端口：" + SERVER_PORT);

            MyApplication.getThreadPool().execute(() -> {
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

                        server = new NonameWebSocketServer(LocalServerFragment.SERVER_PORT);

                        server.setReuseAddr(true);
                        server.start();
                    }

                    addMessageToScreen("服务器启动成功, 可以尝试连接以下几个地址进入服务器：");

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
        MyApplication.getThreadPool().execute(() -> {
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

//        final XPopup.Builder builder = new XPopup.Builder(getContext())
//                .watchView(view.findViewById(R.id.btnShowAttachPoint));
//        view.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                builder.asAttachList(new String[]{"置顶", "复制", "删除"}, null,
//                        new OnSelectListener() {
//                            @Override
//                            public void onSelect(int position, String text) {
//                                toast("click " + text);
//                            }
//                        })
//                        .show();
//                return false;
//            }
//        });


//        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
//        builder.setMessage("是否要设置 " + ip + " 为联机地址?");
//        builder.setTitle("提示");
//        builder.setPositiveButton("确定", (dialog, which) -> {
//            MsgToActivity msg = new MsgToActivity();
//            msg.type = MessageType.SET_SERVER_IP;
//            msg.obj = ip;
//            EventBus.getDefault().post(msg);
//        });
//        builder.setNeutralButton("取消", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//
//        final AlertDialog dialog = builder.create();
//        dialog.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Message msg) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServerStatusChange(MsgServerStatus msg) {
        switch (msg.getStatus()) {
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

        setServerStatus(msg.getStatus());
    }

    public void setServerStatus(int serverStatus) {
        this.serverStatus = serverStatus;
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
