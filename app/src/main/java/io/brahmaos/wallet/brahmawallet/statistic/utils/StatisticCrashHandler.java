package io.brahmaos.wallet.brahmawallet.statistic.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.util.Log;

import org.json.JSONObject;

import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.statistic.network.StatisticHttpUtils;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class StatisticCrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "StatisticCrashHandler";
    private static StatisticCrashHandler sInstance = new StatisticCrashHandler();
    private UncaughtExceptionHandler mDefaultCrashHandler;
    private Context mContext;

    private StatisticCrashHandler() {
    }

    public static StatisticCrashHandler getInstance() {
        return sInstance;
    }

    public void init(Context context) {
        if (!BrahmaConfig.getInstance().isStatisticAllowed()) {
            return;
        }
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        mContext = context.getApplicationContext();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!BrahmaConfig.getInstance().isStatisticAllowed()) {
            return;
        }
        try {
            StatisticHttpUtils.uploadCrashExceptions(mContext, dumpExceptionToSDCard(ex));
        } catch (Exception e) {
            StatisticLog.LogE(TAG, "" + e.toString());
        }
        ex.printStackTrace();
        if (mDefaultCrashHandler != null) {
            mDefaultCrashHandler.uncaughtException(thread, ex);
        } else {
            Process.killProcess(Process.myPid());
        }
    }

    private String dumpExceptionToSDCard(Throwable e) throws IOException {
        File dir = new File(mContext.getApplicationContext()
                .getFilesDir().getAbsolutePath() + "/crash_log");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        long current = System.currentTimeMillis();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(current));
        File file = new File(dir.getAbsolutePath() + "/" + current);
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            pw.println(time);
            dumpPhoneInfo(pw);//write device and app info
            pw.println();
            e.printStackTrace(pw);//write crash log
        } catch (Exception e1) {
            StatisticLog.LogE(TAG, "Dump crash info failed: " + e1.toString());
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
        return dir.getAbsolutePath();
    }

    private void dumpPhoneInfo(PrintWriter pw) throws PackageManager.NameNotFoundException {
        PackageManager pm = mContext.getPackageManager();
        PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
        pw.print("App Version: ");
        pw.print(pi.versionName);
        pw.print("_");
        pw.println(pi.versionCode);
        pw.print("System Version: ");
        pw.print(Build.VERSION.RELEASE);
        pw.print("_");
        pw.println(Build.VERSION.SDK_INT);
        pw.print("Vendor: ");
        pw.println(Build.MANUFACTURER);
        pw.print("Model: ");
        pw.println(Build.MODEL);
    }
}