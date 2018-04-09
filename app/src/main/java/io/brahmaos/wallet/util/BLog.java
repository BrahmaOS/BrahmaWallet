package io.brahmaos.wallet.util;

import com.orhanobut.logger.Logger;

/**
 * log manager
 */
public class BLog {

    private static boolean ENABLE = true;
    public static void init(boolean isDebug) {
        BLog.ENABLE = isDebug;
    }

    public static void v(String tag, String message) {
        if (ENABLE) {
            Logger.init(tag);
            Logger.v(message);
        }
    }

    public static void d(String tag, String message) {
        if (ENABLE) {
            Logger.init(tag);
            Logger.d(message);
        }
    }

    public static void i(String tag, String message) {
        if (ENABLE) {
            Logger.init(tag);
            Logger.i(message);
        }
    }

    public static void w(String tag, String message) {
        if (ENABLE) {
            Logger.init(tag);
            Logger.w(message);
        }
    }

    public static void e(String tag, String message) {
        if (ENABLE) {
            Logger.init(tag);
            Logger.e(message);
        }
    }

    public static void e(String tag, String message, Throwable tr) {
        if (ENABLE) {
            Logger.init(tag);
            Logger.e(tr, message);
        }
    }

    public static void json(String tag, String message) {
        if (ENABLE) {
            Logger.init(tag);
            Logger.json(message);
        }
    }
}
