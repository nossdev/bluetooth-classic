package com.nossdev.plugins.bluetoothclassic;

import android.util.Log;
import java.util.Objects;

public final class Logger {

    private static final String TAG = "BluetoothClassic";
    private static final String EMOJI_PREFIX = "📡";

    private Logger() {}

    public static void debug(String message) {
        Log.d(TAG, String.format("%s %s %s", EMOJI_PREFIX, getCallerInfo(), message));
    }

    public static void info(String message) {
        Log.i(TAG, String.format("%s %s %s", EMOJI_PREFIX, getCallerInfo(), message));
    }

    public static void warn(String message) {
        Log.w(TAG, String.format("%s %s %s", EMOJI_PREFIX, getCallerInfo(), message));
    }

    public static void error(String message, Throwable e) {
        Log.e(TAG, String.format("%s %s %s", EMOJI_PREFIX, getCallerInfo(e), message), e);
    }

    public static void error(String message) {
        Log.e(TAG, String.format("%s %s %s", EMOJI_PREFIX, getCallerInfo(), message));
    }

    private static String getCallerInfo(Throwable t) {
        StackTraceElement[] stackTrace = t.getStackTrace();
        if (stackTrace.length > 0) {
            return getCallerInfo(stackTrace[0]);
        }
        return "[Unknown]";
    }

    private static String getCallerInfo() {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        if (stackTrace.length >= 4) {
            // stackTrace[3] is the caller of the logging method.
            return getCallerInfo(stackTrace[3]);
        }
        return "[Unknown]";
    }

    private static String getCallerInfo(StackTraceElement caller) {
        if (Objects.isNull(caller)) return "[Unknown]";
        if (caller.getMethodName().contains("lambda")) {
            return "[lambda]";
        }
        return "[" + caller.getFileName() + " " + caller.getMethodName() + ":" + caller.getLineNumber() + "]";
    }
}
