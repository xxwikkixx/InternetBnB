package com.fattin.hotspot.netfilter;

import com.fattin.hotspot.helpers.Log;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

public class WhiteListDatabase {
    private static final String LOG_TAG = "WhiteListDatabase";
    private static Map<String, String> white_list;

    static {
        white_list = new Hashtable();
    }

    public static synchronized String getIPData(String ip) {
        String str;
        synchronized (WhiteListDatabase.class) {
            str = (String) white_list.get(ip);
        }
        return str;
    }

    public static synchronized void setIPData(String ip_data) {
        synchronized (WhiteListDatabase.class) {
            try {
                white_list.put(ip_data, ip_data);
            } catch (Exception e) {
                Log.m1e(LOG_TAG, e.getMessage(), e);
            }
        }
    }

    public static synchronized Collection<String> getAllIPsData() {
        Collection<String> values;
        synchronized (WhiteListDatabase.class) {
            values = white_list.values();
        }
        return values;
    }

    public static synchronized void removeIPData(String ip_data) {
        synchronized (WhiteListDatabase.class) {
            try {
                white_list.remove(ip_data);
            } catch (Exception e) {
                Log.m1e(LOG_TAG, e.getMessage(), e);
            }
        }
    }

    public static void clear() {
        white_list.clear();
    }
}
