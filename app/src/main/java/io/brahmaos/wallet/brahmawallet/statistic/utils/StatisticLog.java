package io.brahmaos.wallet.brahmawallet.statistic.utils;

import android.util.Log;

public class StatisticLog {
    private static boolean DEBUG = true;

    public static void setDEBUG(boolean debug) {
        DEBUG = debug;
    }

    public static void LogD(String tag, String msg) {
        if(DEBUG) {
            Log.d(tag, msg);
        }
    }

    public void LogV(String tag, String msg) {
        if(DEBUG) {
            Log.v(tag, msg);
        }
    }

    public static void LogE(String tag, String msg) {
        if(DEBUG) {
            Log.e(tag, msg);
        }
    }
}
