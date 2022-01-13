package com.widget.noname.cola.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;

import com.hzy.lib7z.IExtractCallback;
import com.hzy.lib7z.Z7Extractor;
import com.widget.noname.cola.MyApplication;
import com.widget.noname.cola.eventbus.MsgVersionControl;
import com.widget.noname.cola.listener.ExtractListener;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.progress.ProgressMonitor;

import org.greenrobot.eventbus.EventBus;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
    private static final String TAG = "FileUtil";
    private static final int MOD_K = 1024;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.##");
    private static final String GAME_FOLDER = "game";
    private static final String GAME_FILE = "game.js";

    public static String getFileSize(File file) {
        long length = FileUtil.folderSize(file);
        float size = FileUtil.fileSizeToMb(length);

        String suffix = " MB";
        if (size >= MOD_K) {
            size = size / MOD_K;
            suffix = " GB";
        }

        return DECIMAL_FORMAT.format(size) + suffix;
    }

    public static float fileSizeToMb(long size) {

        float result = size * 1f / MOD_K;
        result = result / MOD_K;

        return result;
    }

    public static long folderSize(File directory) {
        long length = 0;

        if ((null != directory) && directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (null != files) {
                for (File file : files) {
                    if (file.isFile())
                        length += file.length();
                    else
                        length += folderSize(file);
                }
            }
        } else if ((null != directory) && directory.isFile()) {
            length = directory.length();
        }

        return length;
    }

    public static void extractUriToGame(Context context, Uri uri, File root, String folder,
                                        ExtractListener listener) {

        if (null != context) {
            boolean is7Zip = uri.getPath().endsWith(".7z");
            String destPath = root.getPath() + File.separator + folder;
            String tempPath = root.getPath() + File.separator;
            String tempName = is7Zip ? "temp.7z" : "temp.zip";
            ZipFile zipFile = null;
            File file = null;

            try {
                file = new File(tempPath + tempName);
                File destFile = new File(destPath);

                if (!destFile.exists() || !destFile.isDirectory()) {
                    boolean mkdirs = destFile.mkdirs();
                    Log.v(TAG, "extractAll, destFile: " + destFile + ", mkdirs: " + mkdirs);

                    if (!mkdirs) {
                        if (null != listener) {
                            listener.onExtractError();

                            return;
                        }
                    }
                }

                copyUriToFile(context, uri, tempPath, tempName, listener);

                if (is7Zip) {
                    final File tempFile = file;
                    Z7Extractor.extractFile(tempPath + tempName, destPath, new IExtractCallback() {
                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onGetFileNum(int fileNum) {

                        }

                        @Override
                        public void onProgress(String name, long size) {
                            if (null != listener) {
                                listener.onExtractProgress(50 + 2);
                            }
                        }

                        @Override
                        public void onError(int errorCode, String message) {
                            if (null != listener) {
                                listener.onExtractError();
                            }
                        }

                        @Override
                        public void onSucceed() {
                            boolean delete = tempFile.delete();
                            String finalDestPath = root.getPath() + File.separator + folder;

                            File tempFolder = new File(destPath);
                            tempFolder.renameTo(new File(finalDestPath));
                            Log.v(TAG, "extractUriToGame, file: " + tempFile + ", delete: " + delete);

                            if (null != listener) {
                                listener.onExtractDone();
                            }
                        }
                    });
                } else {
                    zipFile = new ZipFile(file);
                    zipFile.setRunInThread(true);
                    ProgressMonitor progressMonitor = zipFile.getProgressMonitor();
                    zipFile.extractAll(destPath);

                    while (!progressMonitor.getState().equals(ProgressMonitor.State.READY)) {
                        if (null != listener) {
                            listener.onExtractProgress(50 + (progressMonitor.getPercentDone() / 2));
                        }

                        Thread.sleep(100);
                    }

                    if (progressMonitor.getResult().equals(ProgressMonitor.Result.SUCCESS)) {
                        if (null != listener) {
                            listener.onExtractDone();
                        }
                    } else if (progressMonitor.getResult().equals(ProgressMonitor.Result.ERROR)) {
                        if (null != listener) {
                            listener.onExtractError();
                        }
                    } else if (progressMonitor.getResult().equals(ProgressMonitor.Result.CANCELLED)) {
                        if (null != listener) {
                            listener.onExtractCancel();
                        }
                    }

                    boolean delete = file.delete();
                    String finalDestPath = root.getPath() + File.separator + folder;

                    File tempFolder = new File(destPath);
                    tempFolder.renameTo(new File(finalDestPath));
                    Log.v(TAG, "extractUriToGame, file: " + file + ", delete: " + delete);
                }

            } catch (Exception e) {
                if (null != listener) {
                    listener.onExtractError();
                }

                if (null != file) {
                    boolean delete = file.delete();
                    Log.v(TAG, "extractUriToGame, failed, file: " + file + ", delete: " + delete);
                }

                e.printStackTrace();
            } finally {
                safeClose(zipFile);

                if (null != listener) {
                    listener.onExtractSaved(destPath);
                }
            }
        }
    }

    public static void extractAssetToGame(Context context, String assetName, File root, String folder,
                                          ExtractListener listener) {

        if (null != context) {
            String destPath = root.getPath() + File.separator + folder;
            String tempPath = root.getPath() + File.separator;
            String tempName = "temp.zip";
            ZipFile zipFile = null;
            File file = null;

            try {
                file = new File(tempPath + tempName);
                File destFile = new File(destPath);

                if (!destFile.exists() || !destFile.isDirectory()) {
                    boolean mkdirs = destFile.mkdirs();
                    Log.v(TAG, "extractAll, destFile: " + destFile + ", mkdirs: " + mkdirs);

                    if (!mkdirs) {
                        if (null != listener) {
                            listener.onExtractError();

                            return;
                        }
                    }
                }

                copyAssetToFile(context, assetName, tempPath, tempName, listener);

                zipFile = new ZipFile(file);
                zipFile.setRunInThread(true);
                ProgressMonitor progressMonitor = zipFile.getProgressMonitor();
                zipFile.extractAll(destPath);

                while (!progressMonitor.getState().equals(ProgressMonitor.State.READY)) {
                    if (null != listener) {
                        listener.onExtractProgress(50 + (progressMonitor.getPercentDone() / 2));
                    }

                    Thread.sleep(100);
                }

                if (progressMonitor.getResult().equals(ProgressMonitor.Result.SUCCESS)) {
                    if (null != listener) {
                        listener.onExtractDone();
                    }
                } else if (progressMonitor.getResult().equals(ProgressMonitor.Result.ERROR)) {
                    if (null != listener) {
                        listener.onExtractError();
                    }
                } else if (progressMonitor.getResult().equals(ProgressMonitor.Result.CANCELLED)) {
                    if (null != listener) {
                        listener.onExtractCancel();
                    }
                }

                boolean delete = file.delete();

                String finalDestPath = root.getPath() + File.separator + folder;

                File tempFolder = new File(destPath);
                tempFolder.renameTo(new File(finalDestPath));

                Log.v(TAG, "extractUriToGame, file: " + file + ", delete: " + delete);
            } catch (Exception e) {
                if (null != listener) {
                    listener.onExtractError();
                }

                if (null != file) {
                    boolean delete = file.delete();
                    Log.v(TAG, "extractUriToGame, failed, file: " + file + ", delete: " + delete);
                }

                e.printStackTrace();
            } finally {
                safeClose(zipFile);

                if (null != listener) {
                    listener.onExtractSaved(destPath);
                }
            }
        }
    }

    public static void copyUriToFile(Context context, Uri uri, String destPath, String destName,
                                     ExtractListener listener) throws IOException {

        String outFileName = destPath + destName;
        File dir = new File(destPath);
        File dbf = new File(outFileName);

        if (!dir.exists()) {
            boolean ret = dir.mkdirs();
            Log.v(TAG, "copyUriToFile, mkdir: " + ret + ", dir: " + dir);
        }

        if (dbf.exists()) {
            boolean ret = dbf.delete();
            Log.v(TAG, "copyUriToFile, delete: " + ret + ", file: " + dbf);
        }

        OutputStream os = new FileOutputStream(outFileName);
        InputStream is = context.getContentResolver().openInputStream(uri);
        int available = is.available();

        byte[] buffer = new byte[1024];
        int length;
        float copyLength = 0;
        long lastTime = SystemClock.uptimeMillis();
        long curTIme = 0;

        while ((length = is.read(buffer)) > 0) {
            os.write(buffer, 0, length);
            copyLength += length;
            curTIme = SystemClock.uptimeMillis();

            if (curTIme - lastTime > 200) {
                lastTime = curTIme;

                if (null != listener) {
                    listener.onExtractProgress((int) ((copyLength / available) * 50));
                }
            }
        }

        os.flush();
        safeClose(os, is);
    }

    public static void copyAssetToFile(Context context, String assetName, String destPath, String destName,
                                       ExtractListener listener) throws IOException {

        String outFileName = destPath + destName;
        File dir = new File(destPath);
        File dbf = new File(outFileName);

        if (!dir.exists()) {
            boolean ret = dir.mkdirs();
            Log.v(TAG, "copyUriToFile, mkdir: " + ret + ", dir: " + dir);
        }

        if (dbf.exists()) {
            boolean ret = dbf.delete();
            Log.v(TAG, "copyUriToFile, delete: " + ret + ", file: " + dbf);
        }

        OutputStream os = new FileOutputStream(outFileName);
        InputStream is = context.getAssets().open(assetName);
        int available = is.available();

        byte[] buffer = new byte[1024];
        int length;
        float copyLength = 0;
        long lastTime = SystemClock.uptimeMillis();
        long curTIme = 0;

        while ((length = is.read(buffer)) > 0) {
            os.write(buffer, 0, length);
            copyLength += length;
            curTIme = SystemClock.uptimeMillis();

            if (curTIme - lastTime > 200) {
                lastTime = curTIme;

                if (null != listener) {
                    listener.onExtractProgress((int) ((copyLength / available) * 50));
                }
            }
        }

        os.flush();
        safeClose(os, is);
    }

    private static void safeClose(Closeable... closeables) {
        if (null != closeables) {
            for (Closeable cl : closeables) {
                try {
                    if (null != cl) {
                        cl.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static final String DO_NOT_DEL_PATH = "files";
    private static final String CACHE_CODE_PATH = "code_cache";

    public static void backupWebContentToPath(Context context, String curPath, String toPath) {

        if (null != context) {
            MyApplication.getThreadPool().execute(() -> {
                try {
                    File root = context.getFilesDir().getParentFile();

                    if (root != null) {
                        String[] files = new String[]{
                                "app_database",
                                "app_textures",
                                "app_webview",
                                "cache",
                                "shared_prefs"
                        };

                        String rootPath = root.getPath() + File.separator;
                        String backPath = curPath + "/backup/";
                        String restorePath = toPath + "/backup/";

                        // 1.backup
                        for (String file : files) {
                            copy(rootPath + file + File.separator,
                                    backPath + file + File.separator);
                        }

                        // 2.restore
                        File restoreFile = new File(restorePath);

                        if (restoreFile.exists() && restoreFile.isDirectory()) {
                            for (String file : files) {
                                String from = restorePath + file + File.separator;
                                String to = rootPath + file + File.separator;
                                File del = new File(to);
                                del.delete();
                                copy(from, to);
                            }
                        }

                        MsgVersionControl msg = new MsgVersionControl();
                        msg.setMsgType(MsgVersionControl.MSG_TYPE_CHANGE_ASSET_FINISH);
                        EventBus.getDefault().post(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static void copyFileUsingFileChannels(String source, String dest) {
        try (FileChannel inputChannel = new FileInputStream(source).getChannel();
             FileChannel outputChannel = new FileOutputStream(dest).getChannel()) {
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copy(String fromFile, String toFile) {
        File[] currentFiles;
        File root = new File(fromFile);

        if (!root.exists()) {
            return;
        }

        currentFiles = root.listFiles();

        File targetDir = new File(toFile);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        if (null != currentFiles) {
            for (File currentFile : currentFiles) {
                if (currentFile.isDirectory()) {
                    copy(currentFile.getPath() + "/", toFile + currentFile.getName() + "/");
                } else {
                    copyFileUsingFileChannels(currentFile.getPath(), toFile + currentFile.getName());
                }
            }
        }
    }

    public static boolean isAssetFileExist(Context context, String url) {
        try {
            AssetManager assets = context.getAssets();
            String[] list = assets.list("");
            for (String file : list) {
                if (file.equals(url)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean checkIfGamePath(File file) {
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

    public static List<File> findGameInPath(File root) {
        ArrayList<File> list = new ArrayList<>();

        if (FileUtil.checkIfGamePath(root)) {
            list.add(root);
        }

        if (null != root) {
            File[] files = root.listFiles();

            if (null != files) {
                for (File file : files) {
                    if (FileUtil.checkIfGamePath(file)) {
                        list.add(file);
                    } else {
                        list.addAll(findGameInPath(file));
                    }
                }
            }
        }

        return list;
    }
}
