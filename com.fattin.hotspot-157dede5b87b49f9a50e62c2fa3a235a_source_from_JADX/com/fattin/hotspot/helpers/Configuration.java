package com.fattin.hotspot.helpers;

import android.os.Build.VERSION;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import org.acra.ACRAConstants;

public class Configuration {
    public static final String DEVICE_ALLY = "ally";
    public static final String DEVICE_BLADE = "blade";
    public static final String DEVICE_DREAM = "dream";
    public static final String DEVICE_DROIDX = "droidx";
    public static final String DEVICE_GALAXY1X = "galaxy1x";
    public static final String DEVICE_GALAXY2X = "galaxy2x";
    public static final String DEVICE_GENERIC = "generic";
    public static final String DEVICE_LEGEND = "legend";
    public static final String DEVICE_MOMENT = "moment";
    public static final String DEVICE_NEXUSONE = "nexusone";
    public static final String DRIVER_HOSTAP = "hostap";
    public static final String DRIVER_SOFTAP_GOG = "softap_gog";
    public static final String DRIVER_SOFTAP_HTC1 = "softap_htc1";
    public static final String DRIVER_SOFTAP_HTC2 = "softap_htc2";
    public static final String DRIVER_TIWLAN0 = "tiwlan0";
    public static final String DRIVER_WEXT = "wext";

    public static String getDeviceType() {
        if (new File("/system/lib/modules/bcm4329.ko").exists()) {
            return DEVICE_NEXUSONE;
        }
        if (new File("/system/libmodules/bcm4325.ko").exists()) {
            if (Integer.parseInt(VERSION.SDK) >= 4) {
                return DEVICE_GALAXY2X;
            }
            return DEVICE_GALAXY1X;
        } else if (new File("/system/lib/modules/tiap_drv.ko").exists() && new File("/system/bin/Hostapd").exists() && new File("/system/etc/wifi/fw_tiwlan_ap.bin").exists() && new File("/system/etc/wifi/tiwlan_ap.ini").exists()) {
            return DEVICE_DROIDX;
        } else {
            if (new File("/system/lib/modules/tiwlan_drv.ko").exists() && new File("/system/etc/wifi/Fw1273_CHIP.bin").exists()) {
                return DEVICE_LEGEND;
            }
            if (new File("/system/lib/modules/wlan.ko").exists()) {
                return DEVICE_DREAM;
            }
            if (new File("/lib/modules/dhd.ko").exists() && new File("/etc/rtecdc.bin").exists()) {
                return DEVICE_MOMENT;
            }
            if (new File("/system/lib/modules/wireless.ko").exists() && new File("/system/etc/wl/rtecdc.bin").exists() && new File("/system/etc/wl/nvram.txt").exists()) {
                return DEVICE_ALLY;
            }
            if (new File("/system/wifi/ar6000.ko").exists() && new File("/system/bin/hostapd").exists()) {
                return DEVICE_BLADE;
            }
            return DEVICE_GENERIC;
        }
    }

    public static String getWifiInterfaceDriver(String deviceType) {
        if (deviceType.equals(DEVICE_DREAM)) {
            return DRIVER_TIWLAN0;
        }
        if (deviceType.equals(DEVICE_NEXUSONE) && hasKernelFeature("CONFIG_BCM4329_SOFTAP=")) {
            if (Integer.parseInt(VERSION.SDK) >= 8) {
                return DRIVER_SOFTAP_HTC2;
            }
            return DRIVER_SOFTAP_HTC1;
        } else if (deviceType.equals(DEVICE_NEXUSONE) && (new File("/etc/firmware/fw_bcm4329_apsta.bin").exists() || new File("/vendor/firmware/fw_bcm4329_apsta.bin").exists())) {
            return DRIVER_SOFTAP_GOG;
        } else {
            if (deviceType.equals(DEVICE_DROIDX) || deviceType.equals(DEVICE_BLADE)) {
                return DRIVER_HOSTAP;
            }
            return DRIVER_WEXT;
        }
    }

    public static String getEncryptionAutoMethod(String deviceType) {
        if (deviceType.equals(DEVICE_LEGEND) || deviceType.equals(DEVICE_NEXUSONE)) {
            return "iwconfig";
        }
        return "wpa_supplicant";
    }

    public static boolean enableFixPersist() {
        if ((new File("/system/lib/modules/tiwlan_drv.ko").exists() && new File("/system/etc/wifi/fw_wlan1271.bin").exists() && getWifiInterfaceDriver(getDeviceType()).equals(DRIVER_WEXT)) || getDeviceType().equals(DEVICE_LEGEND)) {
            return true;
        }
        return false;
    }

    public static boolean enableFixRoute() {
        if (new File("/system/etc/iproute2/rt_tables").exists() && NativeTask.getProp("ro.product.manufacturer").equalsIgnoreCase("HTC")) {
            return true;
        }
        return false;
    }

    public static boolean hasKernelFeature(String feature) {
        try {
            File cfg = new File("/proc/config.gz");
            if (!cfg.exists()) {
                return true;
            }
            GZIPInputStream gzin = new GZIPInputStream(new FileInputStream(cfg));
            String line = ACRAConstants.DEFAULT_STRING_VALUE;
            BufferedReader in = new BufferedReader(new InputStreamReader(gzin));
            do {
                line = in.readLine();
                if (line == null) {
                    gzin.close();
                    return false;
                }
            } while (!line.startsWith(feature));
            gzin.close();
            in.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
