package com.widget.noname.cola.util;

import android.content.Context;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;

import com.widget.noname.cola.listener.ExtractListener;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.progress.ProgressMonitor;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {
    private static final String TAG = "FileUtil";
    private static final int MOD_K = 1024;

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
        Log.e("zyq", "uri: " + uri);
        Log.e("zyq", "root: " + root.getAbsolutePath());
        Log.e("zyq", "folder: " + folder);

        if (null != context) {
            String destPath = root.getPath() + File.separator + folder;
            String tempPath = JavaPathUtil.getAppRootCachePath(context) + File.separator;
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

                copyUriToFile(context, uri, tempPath, tempName, listener);

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
                    Uri destUri = Uri.fromFile(new File(destPath));
                    listener.onExtractSaved(destUri.toString() + "/");
                }
            }
        }
    }

    public static void extractAll(Context context, Uri uri, String dest, ExtractListener listener) {
        Log.e("TAG", "extractAll, context: " + context + ", uri: " + uri + ", dest: " + dest);

        if (null != context) {
            String destPath = JavaPathUtil.getAppRootFilesPath(context);
            String tempPath = JavaPathUtil.getAppRootCachePath(context) + File.separator;
            String tempName = "temp.zip";
            ZipFile zipFile = null;

            try {
                copyUriToFile(context, uri, tempPath, tempName, listener);
                File file = new File(tempPath + tempName);
                File destFile = new File(destPath);

                if (!destFile.exists() || !destFile.isDirectory()) {
                    boolean mkdirs = destFile.mkdirs();
                    Log.v(TAG, "extractAll, destFile: " + destFile + ", mkdirs: " + mkdirs);
                }

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
                Log.v(TAG, "temp file: " + file + ", delete: " + delete);
            } catch (Exception e) {
                if (null != listener) {
                    listener.onExtractError();
                }

                e.printStackTrace();
            } finally {
                safeClose(zipFile);

                if (null != listener) {
                    Uri destUri = Uri.fromFile(new File(destPath));
                    listener.onExtractSaved(destUri.toString() + "/");
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

    private static void safeClose(Closeable... closeables) {
        if (null != closeables) {
            for (Closeable cl : closeables) {
                try {
                    if(null != cl) {
                        cl.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
