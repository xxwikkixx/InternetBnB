package com.fattin.hotspot.helpers;

import com.fattin.hotspot.C0000R;
import com.fattin.hotspot.app.MainApplication;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Date;
import org.xbill.DNS.KEYRecord;
import org.xbill.DNS.KEYRecord.Flags;

public class Util {
    private static final String LOG_TAG = "Util";

    public static long getCurrentTimeStamp() {
        return new Date().getTime();
    }

    public static String getFileChecksum(String file) throws Exception {
        Exception e;
        Throwable th;
        InputStream is = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            InputStream is2 = new FileInputStream(file);
            try {
                try {
                    new DigestInputStream(is2, md).close();
                } catch (IOException e2) {
                    Log.m1e(LOG_TAG, e2.getMessage(), e2);
                }
                byte[] digest = md.digest();
                StringBuilder sb = new StringBuilder();
                for (byte b : digest) {
                    sb.append(Integer.toString((b & KEYRecord.PROTOCOL_ANY) + KEYRecord.OWNER_ZONE, 16).substring(1));
                }
                return sb.toString();
            } catch (Exception e3) {
                e = e3;
                is = is2;
                try {
                    Log.m1e(LOG_TAG, e.getMessage(), e);
                    throw e;
                } catch (Throwable th2) {
                    th = th2;
                    try {
                        is.close();
                    } catch (IOException e22) {
                        Log.m1e(LOG_TAG, e22.getMessage(), e22);
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                is = is2;
                is.close();
                throw th;
            }
        } catch (Exception e4) {
            e = e4;
            Log.m1e(LOG_TAG, e.getMessage(), e);
            throw e;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String convertStreamToString(java.io.InputStream r7) {
        /*
        if (r7 == 0) goto L_0x0054;
    L_0x0002:
        r4 = new java.io.StringWriter;
        r4.<init>();
        r5 = 1024; // 0x400 float:1.435E-42 double:5.06E-321;
        r0 = new char[r5];
        r3 = new java.io.BufferedReader;	 Catch:{ UnsupportedEncodingException -> 0x002b, IOException -> 0x0038 }
        r5 = new java.io.InputStreamReader;	 Catch:{ UnsupportedEncodingException -> 0x002b, IOException -> 0x0038 }
        r6 = "UTF-8";
        r5.<init>(r7, r6);	 Catch:{ UnsupportedEncodingException -> 0x002b, IOException -> 0x0038 }
        r3.<init>(r5);	 Catch:{ UnsupportedEncodingException -> 0x002b, IOException -> 0x0038 }
    L_0x0017:
        r2 = r3.read(r0);	 Catch:{ UnsupportedEncodingException -> 0x002b, IOException -> 0x0038 }
        r5 = -1;
        if (r2 != r5) goto L_0x0026;
    L_0x001e:
        r7.close();	 Catch:{ IOException -> 0x004f }
    L_0x0021:
        r5 = r4.toString();
    L_0x0025:
        return r5;
    L_0x0026:
        r5 = 0;
        r4.write(r0, r5, r2);	 Catch:{ UnsupportedEncodingException -> 0x002b, IOException -> 0x0038 }
        goto L_0x0017;
    L_0x002b:
        r1 = move-exception;
        r1.printStackTrace();	 Catch:{ all -> 0x0045 }
        r7.close();	 Catch:{ IOException -> 0x0033 }
        goto L_0x0021;
    L_0x0033:
        r1 = move-exception;
        r1.printStackTrace();
        goto L_0x0021;
    L_0x0038:
        r1 = move-exception;
        r1.printStackTrace();	 Catch:{ all -> 0x0045 }
        r7.close();	 Catch:{ IOException -> 0x0040 }
        goto L_0x0021;
    L_0x0040:
        r1 = move-exception;
        r1.printStackTrace();
        goto L_0x0021;
    L_0x0045:
        r5 = move-exception;
        r7.close();	 Catch:{ IOException -> 0x004a }
    L_0x0049:
        throw r5;
    L_0x004a:
        r1 = move-exception;
        r1.printStackTrace();
        goto L_0x0049;
    L_0x004f:
        r1 = move-exception;
        r1.printStackTrace();
        goto L_0x0021;
    L_0x0054:
        r5 = "";
        goto L_0x0025;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fattin.hotspot.helpers.Util.convertStreamToString(java.io.InputStream):java.lang.String");
    }

    public static boolean busyboxExist() {
        if (fileExist(CoreTask.DATA_FILE_PATH + "/bin/busybox")) {
            Log.m2i(LOG_TAG, "busybox found");
            return true;
        }
        Log.m2i(LOG_TAG, "busybox not found");
        return false;
    }

    private static boolean fileExist(String file) {
        return new File(file).exists();
    }

    private static boolean busyboxCheckBin() {
        File dir = new File(CoreTask.DATA_FILE_PATH + "/bin");
        if (dir.exists() || dir.mkdir()) {
            return true;
        }
        return false;
    }

    public static boolean busyboxInstall() {
        busyboxCheckBin();
        copyFile(CoreTask.DATA_FILE_PATH + "/bin/busybox", "0755", C0000R.raw.busybox);
        if (!busyboxExist()) {
            return false;
        }
        Log.m2i(LOG_TAG, "busyboxExist=true");
        return true;
    }

    public static String copyFile(String filename, String permission, int ressource) {
        String result = copyFile(filename, ressource);
        if (result != null) {
            return result;
        }
        if (!CoreTask.chmod(filename, permission)) {
            result = "Can't change file-permission for '" + filename + "'!";
        }
        return result;
    }

    private static String copyFile(String filename, int ressource) {
        File outFile = new File(filename);
        InputStream is = MainApplication.getAppContext().getResources().openRawResource(ressource);
        byte[] buf = new byte[Flags.FLAG5];
        try {
            OutputStream out = new FileOutputStream(outFile);
            while (true) {
                int len = is.read(buf);
                if (len <= 0) {
                    out.close();
                    is.close();
                    return null;
                }
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            return "Couldn't install file - " + filename + "!";
        }
    }
}
