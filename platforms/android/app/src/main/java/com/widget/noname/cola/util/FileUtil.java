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

    public static void extractAll(Context context, Uri uri, String dest, ExtractListener listener) {
        Log.e("TAG", "extractAll, context: " + context + ", uri: " + uri + ", dest: " + dest);

        if (null != context) {
            String destPath = context.getExternalFilesDir(null).getPath() + "/" + dest;
            String tempPath = PathUtil.getImportAssetTempPath(context, null);
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
            } catch (IOException | InterruptedException e) {
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
