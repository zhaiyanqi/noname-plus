package com.widget.noname.plus.function.version.subfragment;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class VersionControlFragment extends Fragment /*implements View.OnClickListener, VersionControlItemListener */ {

    private final DateFormat dateTimeFormat = SimpleDateFormat.getDateTimeInstance();

    private RecyclerView versionListView = null;
    //    private VersionListRecyclerAdapter adapter = null;
    private TextView loadingText = null;

//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_version_control, container, false);
//    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        Button startButton = view.findViewById(R.id.import_game_button);
//        startButton.setTypeface(MyApplication.getTypeface());
//        startButton.setOnClickListener(this);
//
//        loadingText = view.findViewById(R.id.loading_text);
//        loadingText.setTypeface(MyApplication.getTypeface());
//
//        versionListView = view.findViewById(R.id.version_list_recycler);
//        adapter = new VersionListRecyclerAdapter(getContext());
//        adapter.setItemClickListener(this);
//        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
//        versionListView.setLayoutManager(mLinearLayoutManager);
//        versionListView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(getContext(), R.anim.versioin_list_anim));
//        versionListView.setAdapter(adapter);
//        String json = MMKV.defaultMMKV().decodeString(FileConstant.VERSION_LIST_KEY);
//        JSONArray array = JSON.parseArray(json);
//
//        if (array != null) {
//            List<VersionData> lists = array.toJavaList(VersionData.class);
//            adapter.replaceList(lists);
//            loadingText.setVisibility(View.GONE);
//        } else {
//            onClick(startButton);
//        }
    }
//
//    private void findAllGameFileInRootView(boolean includeSd) {
//        MyApplication.getThreadPool().execute(() -> {
//            File root = JavaPathUtil.getAppRoot(getContext());
//            List<File> list = new ArrayList<>(FileUtil.findGameInPath(root));
//
//            if (includeSd) {
//                File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
//
//                if (sd != null) {
//                    File noname = new File(sd.getAbsoluteFile() + "/noname");
//
//                    if (!noname.exists()) {
//                        noname.mkdirs();
//                    }
//
//                    list.addAll(FileUtil.findGameInPath(noname));
//                }
//            }
//
//            List<VersionData> verList = new ArrayList<>();
//
//            for (int i = 0; i < list.size(); i++) {
//                File file = list.get(i);
//                VersionData data = new VersionData();
//                data.setDate(dateTimeFormat.format(file.lastModified()));
//                data.setName(file.getName());
//                data.setPath(file.getPath());
//                verList.add(data);
//            }
//
//            versionListView.post(() -> {
//                adapter.replaceList(verList);
//                loadingText.setVisibility(View.GONE);
//            });
//        });
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onExtraZipFile(MsgVersionControl msg) {
//
//        if (msg.getMsgType() == MsgVersionControl.MSG_TYPE_UPDATE_LIST) {
//            updateVersionList();
//        } else if (msg.getMsgType() == MsgVersionControl.MSG_TYPE_CHANGE_ASSET_FINISH) {
//            askToQuit();
//        }
//    }
//
//    private void askToQuit() {
//        XPopup.Builder builder = new XPopup.Builder(getContext());
//        builder.isClickThrough(false);
//        builder.dismissOnTouchOutside(false);
//        builder.dismissOnBackPressed(false);
//        ConfirmPopupView confirmPopupView = builder.asConfirm("提示", "需要重启才能生效", () -> {
//            Process.killProcess(Process.myPid());
//        });
//        confirmPopupView.isHideCancel = true;
//        confirmPopupView.show();
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        EventBus.getDefault().unregister(this);
//    }
//
//    private void updateVersionList() {
//        PermissionX.init(this)
//                .permissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                .request((allGranted, grantedList, deniedList) -> findAllGameFileInRootView(allGranted));
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//
//        EventBus.getDefault().register(this);
//    }
//
//    @Override
//    public void onClick(View v) {
//        if (v.getId() == R.id.import_game_button) {
//            loadingText.setVisibility(View.VISIBLE);
//            adapter.clearAll();
//            updateVersionList();
//        }
//    }
//
//    @SuppressLint("NotifyDataSetChanged")
//    @Override
//    public void onSetPathItemClick(VersionData data) {
//        String curPath = MMKV.defaultMMKV().getString(FileConstant.GAME_PATH_KEY, null);
//
//        if (null != curPath) {
//            FileUtil.backupWebContentToPath(getContext(), curPath, data.getPath());
//
//            MMKV.defaultMMKV().putString(FileConstant.GAME_PATH_KEY, data.getPath());
//            adapter.setCurrentPath(data.getPath());
//            data.setSelected(true);
//            adapter.notifyDataSetChanged();
//        } else {
//            MMKV.defaultMMKV().putString(FileConstant.GAME_PATH_KEY, data.getPath());
//            adapter.setCurrentPath(data.getPath());
//            data.setSelected(true);
//            adapter.notifyDataSetChanged();
//
//            MsgVersionControl msg = new MsgVersionControl();
//            msg.setMsgType(MsgVersionControl.MSG_TYPE_CHANGE_ASSET_FINISH);
//            EventBus.getDefault().post(msg);
//        }
//    }
//
//    @Override
//    public void onItemDelete(VersionData data) {
//        XPopup.Builder builder = new XPopup.Builder(getContext());
//        BasePopupView show = builder.dismissOnTouchOutside(false)
//                .dismissOnBackPressed(false)
//                .asLoading().show();
//
//        Observable.create(emitter -> {
//            try {
//                File file = new File(data.getPath());
//                delete(file);
//                emitter.onNext(true);
//            } catch (Exception e) {
//                emitter.onError(e);
//            }
//        }).subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(obj -> {
//                    boolean del = (boolean) obj;
//
//                    if (del) {
//                        Toast.makeText(getContext(), "删除成功", Toast.LENGTH_SHORT).show();
//                        MMKV.defaultMMKV().putString(FileConstant.GAME_PATH_KEY, null);
//                    }
//
//                    updateVersionList();
//                    show.smartDismiss();
//                }, throwable -> {
//                    Toast.makeText(getContext(), "删除失败" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
//                    show.smartDismiss();
//                    throwable.printStackTrace();
//                });
//    }
//
//    public void delete(File file) {
//        if (!file.exists()) return;
//
//        if (!file.isFile() && file.list() != null) {
//            File[] files = file.listFiles();
//
//            if (files != null) {
//                for (File a : files) {
//                    delete(a);
//                }
//            }
//        }
//
//        file.delete();
//        System.out.println("删除了" + file.getName());
//    }
}
