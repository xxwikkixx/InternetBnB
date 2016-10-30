package com.fattin.hotspot.app;

import com.fattin.hotspot.helpers.Log;
import java.util.Hashtable;
import java.util.Map;
import org.acra.ACRAConstants;

public class InMemoryDatabase {
    private static final String LOG_TAG = "InMemoryDatabase";
    private Map<String, Object> settings;

    public InMemoryDatabase() {
        this.settings = new Hashtable();
    }

    public String getString(String key) {
        Log.m0d(LOG_TAG, "getString " + key);
        return this.settings.containsKey(key) ? (String) this.settings.get(key) : ACRAConstants.DEFAULT_STRING_VALUE;
    }

    public boolean getBoolean(String key) {
        Log.m0d(LOG_TAG, "getBoolean " + key);
        return this.settings.containsKey(key) ? ((Boolean) this.settings.get(key)).booleanValue() : false;
    }

    public synchronized void set(String key, Object value) {
        Log.m0d(LOG_TAG, "set " + key);
        try {
            this.settings.put(key, value);
        } catch (Exception e) {
            Log.m1e(LOG_TAG, e.getMessage(), e);
        }
    }
}
