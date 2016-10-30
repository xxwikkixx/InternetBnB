package com.fattin.hotspot.helpers;

public class NativeTask {
    public static final String LOG_TAG = "NativeTask";

    public static native String getProp(String str);

    static {
        try {
            Log.m2i(LOG_TAG, "Trying to load libnativetask.so");
            System.loadLibrary("nativetask");
        } catch (UnsatisfiedLinkError ule) {
            Log.m1e(LOG_TAG, "Could not load libnativetask.so", ule);
        }
    }

    public static boolean runCommand(String command) {
        Log.m0d(LOG_TAG, "runCommand -> begin");
        Log.m0d(LOG_TAG, "runCommand -> command=" + command);
        ExecuteAsRoot exec = ExecuteAsRoot.getInstance(command);
        boolean exitValue = exec.execute();
        Log.m0d(LOG_TAG, "runCommand -> exitValue=" + exitValue);
        Log.m0d(LOG_TAG, "runCommand -> output=" + exec.getOutput());
        Log.m0d(LOG_TAG, "runCommand -> end");
        return exitValue;
    }
}
