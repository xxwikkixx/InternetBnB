package com.fattin.hotspot.netfilter;

import android.content.Intent;
import com.fattin.hotspot.app.MainApplication;
import com.fattin.hotspot.helpers.Log;
import com.fattin.hotspot.helpers.Util;
import com.fattin.hotspot.main.MainActivity;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

public class AccessListDatabase {
    private static final String LOG_TAG = "AccessListDatabase";
    private static Map<String, AccessListDataObject> access_list;

    static {
        access_list = new Hashtable();
    }

    public static synchronized AccessListDataObject getDeviceData(String device_mac_address) {
        AccessListDataObject accessListDataObject;
        synchronized (AccessListDatabase.class) {
            accessListDataObject = (AccessListDataObject) access_list.get(device_mac_address);
        }
        return accessListDataObject;
    }

    public static synchronized AccessListDataObject setDeviceData(AccessListDataObject device_data) {
        synchronized (AccessListDatabase.class) {
            try {
                long first_connected;
                if (!access_list.containsKey(device_data.getDeviceMacAddress())) {
                    first_connected = Util.getCurrentTimeStamp();
                } else if (getDeviceData(device_data.getDeviceMacAddress()).getFirstConnectedTimestamp() <= 0) {
                }
                first_connected = Util.getCurrentTimeStamp();
                access_list.put(device_data.getDeviceMacAddress(), device_data);
                getDeviceData(device_data.getDeviceMacAddress()).setFirstConnectedTimestamp(first_connected);
                sendAccessListChangeBroadcast();
                device_data = getDeviceData(device_data.getDeviceMacAddress());
            } catch (Exception e) {
                Log.m1e(LOG_TAG, e.getMessage(), e);
            }
        }
        return device_data;
    }

    public static synchronized Collection<AccessListDataObject> getAllDevicesData() {
        Collection<AccessListDataObject> values;
        synchronized (AccessListDatabase.class) {
            values = access_list.values();
        }
        return values;
    }

    public static synchronized void removeDeviceData(AccessListDataObject device_data) {
        synchronized (AccessListDatabase.class) {
            try {
                access_list.remove(device_data.getDeviceMacAddress());
                sendAccessListChangeBroadcast();
            } catch (Exception e) {
                Log.m1e(LOG_TAG, e.getMessage(), e);
            }
        }
    }

    public static void sendAccessListChangeBroadcast() {
        MainApplication.getAppContext().sendBroadcast(new Intent(MainActivity.ACTION_ACCESS_LIST_CHANGED));
    }

    public static void clear() {
        access_list.clear();
        sendAccessListChangeBroadcast();
    }
}
