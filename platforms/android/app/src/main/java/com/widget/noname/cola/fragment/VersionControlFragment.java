package com.widget.noname.cola.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import com.widget.noname.cola.MyApplication;
import com.widget.noname.cola.R;
import com.widget.noname.cola.WaveLoadingView;
import com.widget.noname.cola.adapter.VersionListRecyclerAdapter;
import com.widget.noname.cola.data.VersionData;
import com.widget.noname.cola.eventbus.MsgExtraZipFile;
import com.widget.noname.cola.listener.ExtractAdapter;
import com.widget.noname.cola.util.FileUtil;
import com.widget.noname.cola.util.JavaPathUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VersionControlFragment extends Fragment implements View.OnClickListener {

    private static final String GAME_FOLDER = "game";
    private static final String GAME_FILE = "game.js";
    private static final int MOD_K = 1024;

    private final DateFormat dateTimeFormat = SimpleDateFormat.getDateTimeInstance();
    @SuppressLint("SimpleDateFormat")
    private final DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private final DecimalFormat decimalFormat = new DecimalFormat("0.##");

    private WaveLoadingView waveLoadingView = null;
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
        adapter = new VersionListRecyclerAdapter();

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        versionListView.setLayoutManager(mLinearLayoutManager);
        versionListView.setAdapter(adapter);

        initWaveView(view);
        updateVersionList();
    }

    private void initWaveView(View view) {
        Resources resources = getResources();
        waveLoadingView = view.findViewById(R.id.wave_loading_view);
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

                String yuriPath = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/Android/data/yuri.nakamura.noname_android/";

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

                long length = FileUtil.folderSize(file);
                float size = FileUtil.fileSizeToMb(length);

                String suffix = " MB";
                if (size >= MOD_K) {
                    size = size / MOD_K;
                    suffix = " GB";
                }

                data.setSize(decimalFormat.format(size) + suffix);
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

    private void runOnUiThread(Runnable runnable) {
        FragmentActivity activity = getActivity();

        if (null != activity) {
            activity.runOnUiThread(runnable);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onExtraZipFile(MsgExtraZipFile msg) {

        switch (msg.getExtraType()) {
            case MsgExtraZipFile.EXTRA_TYPE_INTERNAL: {
                File root = JavaPathUtil.getAppRootFiles(getContext());
                String folder = dateFormat.format(new Date());
                unZipUri(msg.getUri(), root, folder);
                break;
            }
            case MsgExtraZipFile.EXTRA_TYPE_EXTERNAL_DOCUMENT: {
                PermissionX.init(this)
                        .permissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .request(new RequestCallback() {
                            @Override
                            public void onResult(boolean allGranted, @NonNull List<String> grantedList, @NonNull List<String> deniedList) {
                                if (allGranted) {
                                    File document = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                                    File root = new File(document.getAbsolutePath() + File.separator + "noname");

                                    if (!root.exists() || !root.isDirectory()) {
                                        root.mkdirs();
                                    }

                                    String folder = dateFormat.format(new Date());
                                    unZipUri(msg.getUri(), root, folder);
                                } else {
                                    Toast.makeText(getContext(), "未获取到SD卡权限，无法解压，请检查系统设置。", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                break;
            }
            case MsgExtraZipFile.EXTRA_TYPE_EXTERNAL: {

                break;
            }
        }
    }

    private void unZipUri(Uri uri, File dest, String folder) {
        waveLoadingView.setVisibility(View.VISIBLE);

        MyApplication.getThreadPool().execute(() -> {

            FileUtil.extractUriToGame(getContext(), uri, dest, folder, new ExtractAdapter() {

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
                        runOnUiThread(() -> updateVersionList());
                    });
                }
            });
        });
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

    private void updateVersionList() {
        PermissionX.init(this)
                .permissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .request(new RequestCallback() {
                    @Override
                    public void onResult(boolean allGranted, @NonNull List<String> grantedList, @NonNull List<String> deniedList) {
                        findAllGameFileInRootView(allGranted);
                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.import_game_button) {
            updateVersionList();
        }
    }
}
