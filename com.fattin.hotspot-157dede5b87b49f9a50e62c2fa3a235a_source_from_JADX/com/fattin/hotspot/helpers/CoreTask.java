package com.fattin.hotspot.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import org.acra.ACRAConstants;
import org.xbill.DNS.KEYRecord.Flags;

public class CoreTask {
    public static String DATA_FILE_PATH = null;
    public static final String LOG_TAG = "CoreTask";
    private Hashtable<String, String> runningProcesses;

    /* renamed from: com.fattin.hotspot.helpers.CoreTask.1 */
    class C00251 implements FilenameFilter {
        C00251() {
        }

        public boolean accept(File dir, String name) {
            try {
                Integer.parseInt(name);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    public CoreTask() {
        this.runningProcesses = new Hashtable();
    }

    public static void setPath(String path) {
        DATA_FILE_PATH = path;
    }

    public static boolean chmod(String file, String mode) {
        return NativeTask.runCommand("chmod " + mode + " " + file);
    }

    public ArrayList<String> readLinesFromFile(String filename) {
        Exception e;
        Throwable th;
        BufferedReader br = null;
        InputStream ins = null;
        ArrayList<String> lines = new ArrayList();
        File file = new File(filename);
        if (file.canRead()) {
            try {
                InputStream ins2 = new FileInputStream(file);
                try {
                    BufferedReader br2 = new BufferedReader(new InputStreamReader(ins2), Flags.FLAG2);
                    while (true) {
                        try {
                            String line = br2.readLine();
                            if (line == null) {
                                try {
                                    break;
                                } catch (Exception e2) {
                                    ins = ins2;
                                    br = br2;
                                }
                            } else {
                                lines.add(line.trim());
                            }
                        } catch (Exception e3) {
                            e = e3;
                            ins = ins2;
                            br = br2;
                        } catch (Throwable th2) {
                            th = th2;
                            ins = ins2;
                            br = br2;
                        }
                    }
                    ins2.close();
                    br2.close();
                    ins = ins2;
                    br = br2;
                } catch (Exception e4) {
                    e = e4;
                    ins = ins2;
                    try {
                        Log.m0d(LOG_TAG, "Unexpected error - Here is what I know: " + e.getMessage());
                        try {
                            ins.close();
                            br.close();
                        } catch (Exception e5) {
                        }
                        return lines;
                    } catch (Throwable th3) {
                        th = th3;
                        try {
                            ins.close();
                            br.close();
                        } catch (Exception e6) {
                        }
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    ins = ins2;
                    ins.close();
                    br.close();
                    throw th;
                }
            } catch (Exception e7) {
                e = e7;
                Log.m0d(LOG_TAG, "Unexpected error - Here is what I know: " + e.getMessage());
                ins.close();
                br.close();
                return lines;
            }
        }
        return lines;
    }

    public boolean writeLinesToFile(String filename, String lines) {
        Exception e;
        Throwable th;
        OutputStream out = null;
        Log.m0d(LOG_TAG, "Writing " + lines.length() + " bytes to file: " + filename);
        try {
            OutputStream out2 = new FileOutputStream(filename);
            try {
                out2.write(lines.getBytes());
                out2.flush();
                if (out2 != null) {
                    try {
                        out2.close();
                    } catch (IOException e2) {
                        out = out2;
                        return false;
                    }
                }
                out = out2;
                return true;
            } catch (Exception e3) {
                e = e3;
                out = out2;
                try {
                    Log.m0d(LOG_TAG, "Unexpected error - Here is what I know: " + e.getMessage());
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e4) {
                            return false;
                        }
                    }
                    return true;
                } catch (Throwable th2) {
                    th = th2;
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e5) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                out = out2;
                if (out != null) {
                    out.close();
                }
                throw th;
            }
        } catch (Exception e6) {
            e = e6;
            Log.m0d(LOG_TAG, "Unexpected error - Here is what I know: " + e.getMessage());
            if (out != null) {
                out.close();
            }
            return true;
        }
    }

    public boolean isNatEnabled() {
        return readLinesFromFile("/proc/sys/net/ipv4/ip_forward").contains("1");
    }

    public String getKernelVersion() {
        String version = ((String) readLinesFromFile("/proc/version").get(0)).split(" ")[2];
        Log.m0d(LOG_TAG, "Kernel version: " + version);
        return version;
    }

    public static boolean isNetfilterSupported() {
        if (new File("/proc/config.gz").exists()) {
            if (!Configuration.hasKernelFeature("CONFIG_NETFILTER=") || !Configuration.hasKernelFeature("CONFIG_IP_NF_IPTABLES=")) {
                return false;
            }
            if (!Configuration.hasKernelFeature("CONFIG_NF_NAT")) {
                return false;
            }
        } else if (!(new File("/proc/net/netfilter").exists() && new File("/proc/net/ip_tables_targets").exists())) {
            return false;
        }
        return true;
    }

    public static boolean isAccessControlSupported() {
        if (new File("/proc/config.gz").exists()) {
            if (!Configuration.hasKernelFeature("CONFIG_NETFILTER_XT_MATCH_MAC=")) {
                return false;
            }
        } else if (!new File("/proc/net/ip_tables_matches").exists() || Configuration.getDeviceType().equals(Configuration.DEVICE_DROIDX)) {
            return false;
        }
        return true;
    }

    public boolean isProcessRunning(String processName) throws Exception {
        boolean processIsRunning = false;
        Hashtable<String, String> tmpRunningProcesses = new Hashtable();
        for (File process : new File("/proc").listFiles(new C00251())) {
            String cmdLine = ACRAConstants.DEFAULT_STRING_VALUE;
            if (this.runningProcesses.containsKey(process.getAbsoluteFile().toString())) {
                cmdLine = (String) this.runningProcesses.get(process.getAbsoluteFile().toString());
            } else {
                ArrayList<String> cmdlineContent = readLinesFromFile(process.getAbsoluteFile() + "/cmdline");
                if (cmdlineContent != null && cmdlineContent.size() > 0) {
                    cmdLine = (String) cmdlineContent.get(0);
                }
            }
            tmpRunningProcesses.put(process.getAbsoluteFile().toString(), cmdLine);
            if (cmdLine.contains(processName)) {
                processIsRunning = true;
            }
        }
        this.runningProcesses = tmpRunningProcesses;
        return processIsRunning;
    }

    public static boolean hasRootPermission() {
        Log.m0d(LOG_TAG, "hasRootPermission");
        try {
            if (new File("/system/bin/su").exists() || new File("/system/xbin/su").exists()) {
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.m0d(LOG_TAG, "Can't obtain root - Here is what I know: " + e.getMessage());
            return false;
        }
    }

    public static boolean runRootCommand(String command) {
        Log.m0d(LOG_TAG, "runRootCommand ==> " + command);
        return NativeTask.runCommand(command);
    }

    public String getProp(String property) {
        return NativeTask.getProp(property);
    }

    public long[] getDataTraffic(String device) {
        long[] dataCount = new long[2];
        if (device != ACRAConstants.DEFAULT_STRING_VALUE) {
            Iterator it = readLinesFromFile("/proc/net/dev").iterator();
            while (it.hasNext()) {
                String line = (String) it.next();
                if (line.startsWith(device)) {
                    String[] values = line.replace(':', ' ').split(" +");
                    dataCount[0] = dataCount[0] + Long.parseLong(values[1]);
                    dataCount[1] = dataCount[1] + Long.parseLong(values[9]);
                }
            }
        }
        return dataCount;
    }

    public long getModifiedDate(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            return file.lastModified();
        }
        return -1;
    }
}
