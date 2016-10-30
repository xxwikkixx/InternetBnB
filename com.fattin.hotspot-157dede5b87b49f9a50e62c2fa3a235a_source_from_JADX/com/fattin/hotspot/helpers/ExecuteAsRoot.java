package com.fattin.hotspot.helpers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import org.acra.ACRAConstants;

public class ExecuteAsRoot {
    public static final String LOG_TAG = "ExecuteAsRoot";
    public String command;
    private String output;

    public static ExecuteAsRoot getInstance(String command) {
        return new ExecuteAsRoot(command);
    }

    public ExecuteAsRoot(String command) {
        this.command = ACRAConstants.DEFAULT_STRING_VALUE;
        this.output = ACRAConstants.DEFAULT_STRING_VALUE;
        this.command = command;
    }

    public static synchronized boolean canRunRootCommands() {
        boolean retval;
        synchronized (ExecuteAsRoot.class) {
            retval = false;
            try {
                Process suProcess = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
                DataInputStream osRes = new DataInputStream(suProcess.getInputStream());
                if (!(os == null || osRes == null)) {
                    boolean exitSu;
                    os.writeBytes("id\n");
                    os.flush();
                    String currUid = osRes.readLine();
                    if (currUid == null) {
                        retval = false;
                        exitSu = false;
                        Log.m0d(LOG_TAG, "Can't get root access or denied by user");
                    } else if (currUid.contains("uid=0")) {
                        retval = true;
                        exitSu = true;
                        Log.m0d(LOG_TAG, "Root access granted");
                    } else {
                        retval = false;
                        exitSu = true;
                        Log.m0d(LOG_TAG, "Root access rejected: " + currUid);
                    }
                    if (exitSu) {
                        os.writeBytes("exit\n");
                        os.flush();
                    }
                }
            } catch (Exception e) {
                retval = false;
                Log.m0d(LOG_TAG, "Root access rejected [" + e.getClass().getName() + "] : " + e.getMessage());
            }
        }
        return retval;
    }

    public synchronized boolean execute(String command) {
        setCommand(command);
        return execute();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final synchronized boolean execute() {
        /*
        r12 = this;
        monitor-enter(r12);
        r5 = 0;
        r9 = r12.command;	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r9 = r9.isEmpty();	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        if (r9 != 0) goto L_0x00a2;
    L_0x000a:
        r9 = java.lang.Runtime.getRuntime();	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r10 = "su";
        r6 = r9.exec(r10);	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r3 = new java.io.DataOutputStream;	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r9 = r6.getOutputStream();	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r3.<init>(r9);	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r4 = new java.io.DataInputStream;	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r9 = r6.getInputStream();	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r4.<init>(r9);	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r0 = new java.io.BufferedReader;	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r9 = new java.io.InputStreamReader;	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r9.<init>(r4);	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r0.<init>(r9);	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r9 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r10 = r12.command;	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r10 = java.lang.String.valueOf(r10);	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r9.<init>(r10);	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r10 = "\n";
        r9 = r9.append(r10);	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r9 = r9.toString();	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r3.writeBytes(r9);	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r3.flush();	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r9 = "ExecuteAsRoot";
        r10 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r11 = "executing command=";
        r10.<init>(r11);	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r11 = r12.command;	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r10 = r10.append(r11);	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r10 = r10.toString();	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        com.fattin.hotspot.helpers.Log.m0d(r9, r10);	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r9 = "exit\n";
        r3.writeBytes(r9);	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r3.flush();	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r7 = r6.waitFor();	 Catch:{ Exception -> 0x00c1, IOException -> 0x00ca, SecurityException -> 0x00da }
        r9 = 255; // 0xff float:3.57E-43 double:1.26E-321;
        if (r9 == r7) goto L_0x00b8;
    L_0x0071:
        r5 = 1;
        r9 = "ExecuteAsRoot";
        r10 = "command successful";
        com.fattin.hotspot.helpers.Log.m2i(r9, r10);	 Catch:{ Exception -> 0x00c1, IOException -> 0x00ca, SecurityException -> 0x00da }
    L_0x0079:
        if (r5 == 0) goto L_0x00a2;
    L_0x007b:
        r8 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r8.<init>();	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
    L_0x0080:
        r2 = r0.readLine();	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        if (r2 != 0) goto L_0x00d6;
    L_0x0086:
        r9 = r8.toString();	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r12.output = r9;	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r9 = "ExecuteAsRoot";
        r10 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r11 = "command output=";
        r10.<init>(r11);	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r11 = r12.output;	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r10 = r10.append(r11);	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        r10 = r10.toString();	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        com.fattin.hotspot.helpers.Log.m0d(r9, r10);	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
    L_0x00a2:
        r9 = "ExecuteAsRoot";
        r10 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00d3 }
        r11 = "retval=";
        r10.<init>(r11);	 Catch:{ all -> 0x00d3 }
        r10 = r10.append(r5);	 Catch:{ all -> 0x00d3 }
        r10 = r10.toString();	 Catch:{ all -> 0x00d3 }
        com.fattin.hotspot.helpers.Log.m2i(r9, r10);	 Catch:{ all -> 0x00d3 }
        monitor-exit(r12);
        return r5;
    L_0x00b8:
        r5 = 0;
        r9 = "ExecuteAsRoot";
        r10 = "command failed";
        com.fattin.hotspot.helpers.Log.m2i(r9, r10);	 Catch:{ Exception -> 0x00c1, IOException -> 0x00ca, SecurityException -> 0x00da }
        goto L_0x0079;
    L_0x00c1:
        r1 = move-exception;
        r9 = "ExecuteAsRoot";
        r10 = "Error executing command as root";
        com.fattin.hotspot.helpers.Log.m4w(r9, r10, r1);	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        goto L_0x0079;
    L_0x00ca:
        r1 = move-exception;
        r9 = "ExecuteAsRoot";
        r10 = "Can't get root access";
        com.fattin.hotspot.helpers.Log.m4w(r9, r10, r1);	 Catch:{ all -> 0x00d3 }
        goto L_0x00a2;
    L_0x00d3:
        r9 = move-exception;
        monitor-exit(r12);
        throw r9;
    L_0x00d6:
        r8.append(r2);	 Catch:{ IOException -> 0x00ca, SecurityException -> 0x00da, Exception -> 0x00e3 }
        goto L_0x0080;
    L_0x00da:
        r1 = move-exception;
        r9 = "ExecuteAsRoot";
        r10 = "Can't get root access";
        com.fattin.hotspot.helpers.Log.m4w(r9, r10, r1);	 Catch:{ all -> 0x00d3 }
        goto L_0x00a2;
    L_0x00e3:
        r1 = move-exception;
        r9 = "ExecuteAsRoot";
        r10 = "Error executing internal operation";
        com.fattin.hotspot.helpers.Log.m4w(r9, r10, r1);	 Catch:{ all -> 0x00d3 }
        goto L_0x00a2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fattin.hotspot.helpers.ExecuteAsRoot.execute():boolean");
    }

    public synchronized void setCommand(String command) {
        this.command = command;
    }

    public synchronized String getOutput() {
        return this.output;
    }
}
