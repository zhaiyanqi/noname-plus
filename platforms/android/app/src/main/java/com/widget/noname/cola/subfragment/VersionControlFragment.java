package com.widget.noname.cola.subfragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Process;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.permissionx.guolindev.PermissionX;
import com.tencent.mmkv.MMKV;
import com.widget.noname.cola.MyApplication;
import com.widget.noname.cola.R;
import com.widget.noname.cola.adapter.VersionListRecyclerAdapter;
import com.widget.noname.cola.data.VersionData;
import com.widget.noname.cola.eventbus.MsgVersionControl;
import com.widget.noname.cola.listener.VersionControlItemListener;
import com.widget.noname.cola.util.FileConstant;
import com.widget.noname.cola.util.FileUtil;
import com.widget.noname.cola.util.JavaPathUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class VersionControlFragment extends Fragment implements View.OnClickListener, VersionControlItemListener {

    private static final String GAME_FOLDER = "game";
    private static final String GAME_FILE = "game.js";

    private final DateFormat dateTimeFormat = SimpleDateFormat.getDateTimeInstance();

    private RecyclerView versionListView = null;
    private VersionListRecyclerAdapter adapter = null;
    private TextView loadingText = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_version_control, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button startButton = view.findViewById(R.id.import_game_button);
        startButton.setTypeface(MyApplication.getTypeface());
        startButton.setOnClickListener(this);

        loadingText = view.findViewById(R.id.loading_text);
        loadingText.setTypeface(MyApplication.getTypeface());

        versionListView = view.findViewById(R.id.version_list_recycler);
        adapter = new VersionListRecyclerAdapter(getContext());
        adapter.setItemClickListener(this);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        versionListView.setLayoutManager(mLinearLayoutManager);
        versionListView.setAdapter(adapter);

        updateVersionList();
    }

    private void findAllGameFileInRootView(boolean includeSd) {
        MyApplication.getThreadPool().execute(() -> {
            File root = JavaPathUtil.getAppRoot(getContext());
            List<File> list = new ArrayList<>(findGameInPath(root));

            if (includeSd) {
                File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

                if (sd != null) {
                    File noname = new File(sd.getAbsoluteFile() + "/noname");

                    if (!noname.exists()) {
                        noname.mkdirs();
                    }

                    list.addAll(findGameInPath(noname));
                }

                String yuriPath = "/sdcard/Android/data/yuri.nakamura.noname_android";

                File yuri = new File(yuriPath);

                if (yuri.exists() && yuri.isDirectory()) {
                    list.addAll(findGameInPath(yuri));
                }
            }

            List<VersionData> verList = new ArrayList<>();

            for (int i = 0; i < list.size(); i++) {
                File file = list.get(i);
                VersionData data = new VersionData();
                data.setDate(dateTimeFormat.format(file.lastModified()));
                data.setName(file.getName());
                data.setPath(file.getPath());
                verList.add(data);
            }

            versionListView.post(() -> {
                adapter.replaceList(verList);
                loadingText.setVisibility(View.GONE);
            });
        });
    }

    private List<File> findGameInPath(File root) {
        ArrayList<File> list = new ArrayList<>();

        if (checkIfGamePath(root)) {
            list.add(root);
        }

        if (null != root) {
            File[] files = root.listFiles();

            if (null != files) {
                for (File file : files) {
                    if (checkIfGamePath(file)) {
                        list.add(file);
                    } else {
                        list.addAll(findGameInPath(file));
                    }
                }
            }
        }

        return list;
    }

    private boolean checkIfGamePath(File file) {
        if (null != file) {
            File[] gameFolders = file.listFiles(dir -> dir.isDirectory() && GAME_FOLDER.equals(dir.getName()));

            if (null != gameFolders && gameFolders.length == 1) {
                File gameFolder = gameFolders[0];
                File[] gameJs = gameFolder.listFiles(f -> f.isFile() && GAME_FILE.equals(f.getName()));

                return (null != gameJs) && (gameJs.length > 0);
            }
        }

        return false;
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onExtraZipFile(MsgVersionControl msg) {

        if (msg.getMsgType() == MsgVersionControl.MSG_TYPE_UPDATE_LIST) {
            updateVersionList();
        } else if (msg.getMsgType() == MsgVersionControl.MSG_TYPE_CHANGE_ASSET_FINISH) {
            FragmentActivity activity = getActivity();

            if (null != activity) {
                activity.finish();
            }

            new Handler().postDelayed(() -> {
                try {
                    Process.killProcess(Process.myPid());
                } catch (Exception ignored) {
                }
            }, 200);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void updateVersionList() {
        PermissionX.init(this)
                .permissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .request((allGranted, grantedList, deniedList) -> findAllGameFileInRootView(allGranted));
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.import_game_button) {
            updateVersionList();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onSetPathItemClick(VersionData data) {
        String curPath = MMKV.defaultMMKV().getString(FileConstant.GAME_PATH_KEY, null);

        if (null != curPath) {
            FileUtil.backupWebContentToPath(getContext(), curPath, data.getPath());
        }

        MMKV.defaultMMKV().putString(FileConstant.GAME_PATH_KEY, data.getPath());
        adapter.setCurrentPath(data.getPath());
        data.setSelected(true);
        adapter.notifyDataSetChanged();
    }
}
