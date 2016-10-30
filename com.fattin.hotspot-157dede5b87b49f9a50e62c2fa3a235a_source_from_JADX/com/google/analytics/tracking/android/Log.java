package com.google.analytics.tracking.android;

import com.google.android.gms.common.util.VisibleForTesting;

public class Log {
    @VisibleForTesting
    static final String LOG_TAG = "GAV2";
    private static boolean sDebug;

    private Log() {
    }

    public static void setDebug(boolean debug) {
        sDebug = debug;
    }

    public static boolean isDebugEnabled() {
        return sDebug;
    }

    public static int m6d(String msg) {
        return android.util.Log.d(LOG_TAG, formatMessage(msg));
    }

    public static int dDebug(String msg) {
        if (sDebug) {
            return m6d(msg);
        }
        return 0;
    }

    public static int m7e(String msg) {
        return android.util.Log.e(LOG_TAG, formatMessage(msg));
    }

    public static int eDebug(String msg) {
        if (sDebug) {
            return m7e(msg);
        }
        return 0;
    }

    public static int m8i(String msg) {
        return android.util.Log.i(LOG_TAG, formatMessage(msg));
    }

    public static int iDebug(String msg) {
        if (sDebug) {
            return m8i(msg);
        }
        return 0;
    }

    public static int m9v(String msg) {
        return android.util.Log.v(LOG_TAG, formatMessage(msg));
    }

    public static int vDebug(String msg) {
        if (sDebug) {
            return m9v(msg);
        }
        return 0;
    }

    public static int m10w(String msg) {
        return android.util.Log.w(LOG_TAG, formatMessage(msg));
    }

    public static int wDebug(String msg) {
        if (sDebug) {
            return m10w(msg);
        }
        return 0;
    }

    private static String formatMessage(String msg) {
        return Thread.currentThread().toString() + ": " + msg;
    }
}
