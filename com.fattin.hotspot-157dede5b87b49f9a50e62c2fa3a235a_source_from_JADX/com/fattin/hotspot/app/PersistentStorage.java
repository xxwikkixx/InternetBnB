package com.fattin.hotspot.app;

import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import com.fattin.hotspot.helpers.Log;
import org.acra.ACRAConstants;

public class PersistentStorage {
    private static final String LOG_TAG = "PersistentStorage";

    public String getString(String key) {
        Log.m0d(LOG_TAG, "get " + key);
        return PreferenceManager.getDefaultSharedPreferences(MainApplication.getAppContext()).getString(key, ACRAConstants.DEFAULT_STRING_VALUE);
    }

    public synchronized void putString(String key, String value) {
        Log.m0d(LOG_TAG, "set " + key);
        Editor editor;
        if (value == null || value.isEmpty()) {
            editor = PreferenceManager.getDefaultSharedPreferences(MainApplication.getAppContext()).edit();
            editor.remove(key);
            editor.commit();
        } else {
            editor = PreferenceManager.getDefaultSharedPreferences(MainApplication.getAppContext()).edit();
            editor.putString(key, value);
            editor.commit();
        }
    }

    public boolean getBoolean(String key) {
        Log.m0d(LOG_TAG, "get " + key);
        return PreferenceManager.getDefaultSharedPreferences(MainApplication.getAppContext()).getBoolean(key, false);
    }

    public synchronized void putBoolean(String key, boolean value) {
        Log.m0d(LOG_TAG, "set " + key);
        Editor editor = PreferenceManager.getDefaultSharedPreferences(MainApplication.getAppContext()).edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
}
