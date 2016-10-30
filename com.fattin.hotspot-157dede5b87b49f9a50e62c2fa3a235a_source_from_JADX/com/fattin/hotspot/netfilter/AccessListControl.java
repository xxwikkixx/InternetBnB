package com.fattin.hotspot.netfilter;

import com.fattin.hotspot.connectivity.TetherManager;
import com.fattin.hotspot.helpers.CoreTask;
import com.fattin.hotspot.helpers.ExecuteAsRoot;
import com.fattin.hotspot.helpers.Log;
import com.fattin.hotspot.helpers.Util;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.acra.ACRAConstants;

public class AccessListControl {
    public static final String IPADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    private static final String LOG_TAG = "AccessListControl";
    public static final String MACADDRESS_PATTERN = "..:..:..:..:..:..";

    public static synchronized void allowAccessForMAC(String mac) {
        synchronized (AccessListControl.class) {
            Log.m0d(LOG_TAG, "allowAccessForMAC -> mac " + mac);
            if (mac.matches(MACADDRESS_PATTERN) && !isAccessAllowedForMAC(mac)) {
                CoreTask.runRootCommand(NetFilterManager.IPTABLES + "-t nat -I ACCESS_LIST -m state --state NEW,ESTABLISHED,RELATED,INVALID -m mac --mac-source " + mac + " -j ACCEPT;");
                String ip = getIPFromArpCache(mac);
                CoreTask.runRootCommand(NetFilterManager.IPTABLES + "-D ACCESS_LIST -s " + ip + " -j REJECT;" + NetFilterManager.IPTABLES + "-D ACCESS_LIST -s " + ip + " -j REJECT;");
            }
            NetFilterManager.update_nat_rules_checksum();
        }
    }

    public static synchronized void disallowAccessForMAC(String mac) {
        synchronized (AccessListControl.class) {
            Log.m0d(LOG_TAG, "disallowAccessForMAC -> mac " + mac);
            if (mac.matches(MACADDRESS_PATTERN) && isAccessAllowedForMAC(mac)) {
                CoreTask.runRootCommand(NetFilterManager.IPTABLES + "-t nat -D ACCESS_LIST -m state --state NEW,ESTABLISHED,RELATED,INVALID -m mac --mac-source " + mac + " -j ACCEPT;");
                CoreTask.runRootCommand(NetFilterManager.IPTABLES + "-A ACCESS_LIST -s " + getIPFromArpCache(mac) + " -j REJECT;");
                NetFilterManager.update_nat_rules_checksum();
            }
        }
    }

    public static synchronized boolean isAccessAllowedForMAC(String mac) {
        Exception e;
        Throwable th;
        boolean z;
        synchronized (AccessListControl.class) {
            Log.m0d(LOG_TAG, "isAccessAllowedForMAC -> mac " + mac);
            if (mac.matches(MACADDRESS_PATTERN)) {
                NetFilterManager.update_nat_rules_output_file();
                BufferedReader br = null;
                try {
                    BufferedReader br2 = new BufferedReader(new FileReader(NetFilterManager.IPTABLES_OUTPUT));
                    String line;
                    do {
                        try {
                            line = br2.readLine();
                            if (line == null) {
                                try {
                                    br2.close();
                                } catch (IOException e2) {
                                    Log.m1e(LOG_TAG, e2.getMessage(), e2);
                                }
                            }
                        } catch (Exception e3) {
                            e = e3;
                            br = br2;
                        } catch (Throwable th2) {
                            th = th2;
                            br = br2;
                        }
                    } while (!line.toLowerCase().contains(mac.toLowerCase()));
                    Log.m0d(LOG_TAG, "isAccessAllowedForMAC -> true");
                    try {
                        br2.close();
                    } catch (IOException e22) {
                        Log.m1e(LOG_TAG, e22.getMessage(), e22);
                    }
                    z = true;
                } catch (Exception e4) {
                    e = e4;
                    try {
                        Log.m1e(LOG_TAG, e.getMessage(), e);
                        try {
                            br.close();
                        } catch (IOException e222) {
                            Log.m1e(LOG_TAG, e222.getMessage(), e222);
                        }
                        Log.m0d(LOG_TAG, "isAccessAllowedForMAC -> false");
                        z = false;
                        return z;
                    } catch (Throwable th3) {
                        th = th3;
                        try {
                            br.close();
                        } catch (IOException e2222) {
                            Log.m1e(LOG_TAG, e2222.getMessage(), e2222);
                        }
                        throw th;
                    }
                }
            }
            Log.m0d(LOG_TAG, "isAccessAllowedForMAC -> false");
            z = false;
        }
        return z;
    }

    public static boolean updateAccessListDatabaseFromArpCache(boolean onlyReachables, int reachableTimeout) {
        Exception e;
        Throwable th;
        Log.m2i(LOG_TAG, "updateAccessListDatabaseFromArpCache");
        String tetherableWifiIface = TetherManager.getSingleton().getTetherableWifiIface();
        BufferedReader bufferedReader = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));
            while (true) {
                try {
                    String line = br.readLine();
                    if (line == null) {
                        try {
                            break;
                        } catch (IOException e2) {
                            Log.m1e(LOG_TAG, e2.getMessage(), e2);
                        }
                    } else {
                        String[] splitted = line.split(" +");
                        if (splitted != null && splitted.length >= 4) {
                            String ip = splitted[0];
                            String mac = splitted[3];
                            String iface = splitted[5];
                            if (ip.matches(IPADDRESS_PATTERN) && mac.matches(MACADDRESS_PATTERN) && iface.toLowerCase().equals(tetherableWifiIface.toLowerCase())) {
                                boolean isReachable = false;
                                if (onlyReachables) {
                                    isReachable = isReachable(ip);
                                }
                                if (!onlyReachables || isReachable) {
                                    AccessListDataObject deviceData = AccessListDatabase.getDeviceData(mac);
                                    if (deviceData == null) {
                                        Log.m2i(LOG_TAG, "adding device " + mac);
                                        AccessListDatabase.setDeviceData(new AccessListDataObject(mac, ACRAConstants.DEFAULT_STRING_VALUE, ip)).setDeviceIPAddress(ip);
                                    } else {
                                        Log.m2i(LOG_TAG, "updating device " + mac);
                                        deviceData.setDeviceIPAddress(ip);
                                        deviceData.setLastAccessTimestamp(Util.getCurrentTimeStamp());
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e3) {
                    e = e3;
                    bufferedReader = br;
                } catch (Throwable th2) {
                    th = th2;
                    bufferedReader = br;
                }
            }
            br.close();
            bufferedReader = br;
            return true;
        } catch (Exception e4) {
            e = e4;
            try {
                Log.m1e(LOG_TAG, e.getMessage(), e);
                try {
                    bufferedReader.close();
                } catch (IOException e22) {
                    Log.m1e(LOG_TAG, e22.getMessage(), e22);
                }
                return false;
            } catch (Throwable th3) {
                th = th3;
                try {
                    bufferedReader.close();
                } catch (IOException e222) {
                    Log.m1e(LOG_TAG, e222.getMessage(), e222);
                }
                throw th;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String getMacFromArpCache(java.lang.String r9) {
        /*
        r6 = 0;
        if (r9 != 0) goto L_0x0005;
    L_0x0003:
        r4 = r6;
    L_0x0004:
        return r4;
    L_0x0005:
        r0 = 0;
        r1 = new java.io.BufferedReader;	 Catch:{ Exception -> 0x005d }
        r7 = new java.io.FileReader;	 Catch:{ Exception -> 0x005d }
        r8 = "/proc/net/arp";
        r7.<init>(r8);	 Catch:{ Exception -> 0x005d }
        r1.<init>(r7);	 Catch:{ Exception -> 0x005d }
    L_0x0012:
        r3 = r1.readLine();	 Catch:{ Exception -> 0x0095, all -> 0x0092 }
        if (r3 != 0) goto L_0x001e;
    L_0x0018:
        r1.close();	 Catch:{ IOException -> 0x0086 }
        r0 = r1;
    L_0x001c:
        r4 = r6;
        goto L_0x0004;
    L_0x001e:
        r7 = " +";
        r5 = r3.split(r7);	 Catch:{ Exception -> 0x0095, all -> 0x0092 }
        if (r5 == 0) goto L_0x0012;
    L_0x0026:
        r7 = r5.length;	 Catch:{ Exception -> 0x0095, all -> 0x0092 }
        r8 = 4;
        if (r7 < r8) goto L_0x0012;
    L_0x002a:
        r7 = 0;
        r7 = r5[r7];	 Catch:{ Exception -> 0x0095, all -> 0x0092 }
        r7 = r9.equals(r7);	 Catch:{ Exception -> 0x0095, all -> 0x0092 }
        if (r7 == 0) goto L_0x0012;
    L_0x0033:
        r7 = 3;
        r4 = r5[r7];	 Catch:{ Exception -> 0x0095, all -> 0x0092 }
        r7 = "..:..:..:..:..:..";
        r7 = r4.matches(r7);	 Catch:{ Exception -> 0x0095, all -> 0x0092 }
        if (r7 == 0) goto L_0x004d;
    L_0x003e:
        r1.close();	 Catch:{ IOException -> 0x0042 }
        goto L_0x0004;
    L_0x0042:
        r2 = move-exception;
        r6 = "AccessListControl";
        r7 = r2.getMessage();
        com.fattin.hotspot.helpers.Log.m1e(r6, r7, r2);
        goto L_0x0004;
    L_0x004d:
        r1.close();	 Catch:{ IOException -> 0x0052 }
    L_0x0050:
        r4 = r6;
        goto L_0x0004;
    L_0x0052:
        r2 = move-exception;
        r7 = "AccessListControl";
        r8 = r2.getMessage();
        com.fattin.hotspot.helpers.Log.m1e(r7, r8, r2);
        goto L_0x0050;
    L_0x005d:
        r2 = move-exception;
    L_0x005e:
        r7 = "AccessListControl";
        r8 = r2.getMessage();	 Catch:{ all -> 0x0076 }
        com.fattin.hotspot.helpers.Log.m1e(r7, r8, r2);	 Catch:{ all -> 0x0076 }
        r0.close();	 Catch:{ IOException -> 0x006b }
        goto L_0x001c;
    L_0x006b:
        r2 = move-exception;
        r7 = "AccessListControl";
        r8 = r2.getMessage();
        com.fattin.hotspot.helpers.Log.m1e(r7, r8, r2);
        goto L_0x001c;
    L_0x0076:
        r6 = move-exception;
    L_0x0077:
        r0.close();	 Catch:{ IOException -> 0x007b }
    L_0x007a:
        throw r6;
    L_0x007b:
        r2 = move-exception;
        r7 = "AccessListControl";
        r8 = r2.getMessage();
        com.fattin.hotspot.helpers.Log.m1e(r7, r8, r2);
        goto L_0x007a;
    L_0x0086:
        r2 = move-exception;
        r7 = "AccessListControl";
        r8 = r2.getMessage();
        com.fattin.hotspot.helpers.Log.m1e(r7, r8, r2);
        r0 = r1;
        goto L_0x001c;
    L_0x0092:
        r6 = move-exception;
        r0 = r1;
        goto L_0x0077;
    L_0x0095:
        r2 = move-exception;
        r0 = r1;
        goto L_0x005e;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fattin.hotspot.netfilter.AccessListControl.getMacFromArpCache(java.lang.String):java.lang.String");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String getIPFromArpCache(java.lang.String r9) {
        /*
        if (r9 != 0) goto L_0x0005;
    L_0x0002:
        r3 = "";
    L_0x0004:
        return r3;
    L_0x0005:
        r0 = 0;
        r1 = new java.io.BufferedReader;	 Catch:{ Exception -> 0x0072 }
        r6 = new java.io.FileReader;	 Catch:{ Exception -> 0x0072 }
        r7 = "/proc/net/arp";
        r6.<init>(r7);	 Catch:{ Exception -> 0x0072 }
        r1.<init>(r6);	 Catch:{ Exception -> 0x0072 }
    L_0x0012:
        r4 = r1.readLine();	 Catch:{ Exception -> 0x00ab, all -> 0x00a8 }
        if (r4 != 0) goto L_0x001f;
    L_0x0018:
        r1.close();	 Catch:{ IOException -> 0x009b }
        r0 = r1;
    L_0x001c:
        r3 = "";
        goto L_0x0004;
    L_0x001f:
        r6 = " +";
        r5 = r4.split(r6);	 Catch:{ Exception -> 0x00ab, all -> 0x00a8 }
        r6 = "..:..:..:..:..:..";
        r6 = r9.matches(r6);	 Catch:{ Exception -> 0x00ab, all -> 0x00a8 }
        if (r6 == 0) goto L_0x0012;
    L_0x002d:
        if (r5 == 0) goto L_0x0012;
    L_0x002f:
        r6 = r5.length;	 Catch:{ Exception -> 0x00ab, all -> 0x00a8 }
        r7 = 4;
        if (r6 < r7) goto L_0x0012;
    L_0x0033:
        r6 = r9.toLowerCase();	 Catch:{ Exception -> 0x00ab, all -> 0x00a8 }
        r7 = 3;
        r7 = r5[r7];	 Catch:{ Exception -> 0x00ab, all -> 0x00a8 }
        r7 = r7.toLowerCase();	 Catch:{ Exception -> 0x00ab, all -> 0x00a8 }
        r6 = r6.equals(r7);	 Catch:{ Exception -> 0x00ab, all -> 0x00a8 }
        if (r6 == 0) goto L_0x0012;
    L_0x0044:
        r6 = 0;
        r3 = r5[r6];	 Catch:{ Exception -> 0x00ab, all -> 0x00a8 }
        r1.close();	 Catch:{ Exception -> 0x00ab, all -> 0x00a8 }
        r6 = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
        r6 = r3.matches(r6);	 Catch:{ Exception -> 0x00ab, all -> 0x00a8 }
        if (r6 == 0) goto L_0x0061;
    L_0x0052:
        r1.close();	 Catch:{ IOException -> 0x0056 }
        goto L_0x0004;
    L_0x0056:
        r2 = move-exception;
        r6 = "AccessListControl";
        r7 = r2.getMessage();
        com.fattin.hotspot.helpers.Log.m1e(r6, r7, r2);
        goto L_0x0004;
    L_0x0061:
        r1.close();	 Catch:{ IOException -> 0x0067 }
    L_0x0064:
        r3 = "";
        goto L_0x0004;
    L_0x0067:
        r2 = move-exception;
        r6 = "AccessListControl";
        r7 = r2.getMessage();
        com.fattin.hotspot.helpers.Log.m1e(r6, r7, r2);
        goto L_0x0064;
    L_0x0072:
        r2 = move-exception;
    L_0x0073:
        r6 = "AccessListControl";
        r7 = r2.getMessage();	 Catch:{ all -> 0x008b }
        com.fattin.hotspot.helpers.Log.m1e(r6, r7, r2);	 Catch:{ all -> 0x008b }
        r0.close();	 Catch:{ IOException -> 0x0080 }
        goto L_0x001c;
    L_0x0080:
        r2 = move-exception;
        r6 = "AccessListControl";
        r7 = r2.getMessage();
        com.fattin.hotspot.helpers.Log.m1e(r6, r7, r2);
        goto L_0x001c;
    L_0x008b:
        r6 = move-exception;
    L_0x008c:
        r0.close();	 Catch:{ IOException -> 0x0090 }
    L_0x008f:
        throw r6;
    L_0x0090:
        r2 = move-exception;
        r7 = "AccessListControl";
        r8 = r2.getMessage();
        com.fattin.hotspot.helpers.Log.m1e(r7, r8, r2);
        goto L_0x008f;
    L_0x009b:
        r2 = move-exception;
        r6 = "AccessListControl";
        r7 = r2.getMessage();
        com.fattin.hotspot.helpers.Log.m1e(r6, r7, r2);
        r0 = r1;
        goto L_0x001c;
    L_0x00a8:
        r6 = move-exception;
        r0 = r1;
        goto L_0x008c;
    L_0x00ab:
        r2 = move-exception;
        r0 = r1;
        goto L_0x0073;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fattin.hotspot.netfilter.AccessListControl.getIPFromArpCache(java.lang.String):java.lang.String");
    }

    public static boolean isMacInArpCache(String mac) {
        Exception e;
        Throwable th;
        if (mac == null) {
            return false;
        }
        BufferedReader br = null;
        try {
            BufferedReader br2 = new BufferedReader(new FileReader("/proc/net/arp"));
            while (true) {
                try {
                    String line = br2.readLine();
                    if (line == null) {
                        try {
                            br2.close();
                            br = br2;
                            return false;
                        } catch (IOException e2) {
                            Log.m1e(LOG_TAG, e2.getMessage(), e2);
                            br = br2;
                            return false;
                        }
                    }
                    String[] splitted = line.split(" +");
                    if (mac.matches(MACADDRESS_PATTERN) && splitted != null && splitted.length >= 4 && mac.toLowerCase().equals(splitted[3].toLowerCase())) {
                        try {
                            break;
                        } catch (IOException e22) {
                            Log.m1e(LOG_TAG, e22.getMessage(), e22);
                        }
                    }
                } catch (Exception e3) {
                    e = e3;
                    br = br2;
                } catch (Throwable th2) {
                    th = th2;
                    br = br2;
                }
            }
            br2.close();
            return true;
        } catch (Exception e4) {
            e = e4;
            try {
                Log.m1e(LOG_TAG, e.getMessage(), e);
                try {
                    br.close();
                    return false;
                } catch (IOException e222) {
                    Log.m1e(LOG_TAG, e222.getMessage(), e222);
                    return false;
                }
            } catch (Throwable th3) {
                th = th3;
                try {
                    br.close();
                } catch (IOException e2222) {
                    Log.m1e(LOG_TAG, e2222.getMessage(), e2222);
                }
                throw th;
            }
        }
    }

    public static synchronized boolean isReachable(String ip) {
        boolean z = false;
        synchronized (AccessListControl.class) {
            if (!ip.isEmpty()) {
                if (ip.matches(IPADDRESS_PATTERN)) {
                    ExecuteAsRoot exec = ExecuteAsRoot.getInstance(CoreTask.DATA_FILE_PATH + "/bin/busybox arping -I " + TetherManager.getSingleton().getTetherableWifiIface() + " -c 1 " + ip);
                    if (exec.execute() && exec.getOutput().contains("Received 1 replies")) {
                        z = true;
                    }
                }
            }
        }
        return z;
    }
}
