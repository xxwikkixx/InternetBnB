package org.acra.collector;

final class DropBoxCollector {
    private static final String NO_RESULT = "N/A";
    private static final String[] SYSTEM_TAGS;

    DropBoxCollector() {
    }

    static {
        SYSTEM_TAGS = new String[]{"system_app_anr", "system_app_wtf", "system_app_crash", "system_server_anr", "system_server_wtf", "system_server_crash", "BATTERY_DISCHARGE_INFO", "SYSTEM_RECOVERY_LOG", "SYSTEM_BOOT", "SYSTEM_LAST_KMSG", "APANIC_CONSOLE", "APANIC_THREADS", "SYSTEM_RESTART", "SYSTEM_TOMBSTONE", "data_app_strictmode"};
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String read(android.content.Context r26, java.lang.String[] r27) {
        /*
        r14 = org.acra.collector.Compatibility.getDropBoxServiceName();	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        if (r14 != 0) goto L_0x0009;
    L_0x0006:
        r21 = "N/A";
    L_0x0008:
        return r21;
    L_0x0009:
        r0 = r26;
        r4 = r0.getSystemService(r14);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r21 = r4.getClass();	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r22 = "getNextEntry";
        r23 = 2;
        r0 = r23;
        r0 = new java.lang.Class[r0];	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r23 = r0;
        r24 = 0;
        r25 = java.lang.String.class;
        r23[r24] = r25;	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r24 = 1;
        r25 = java.lang.Long.TYPE;	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r23[r24] = r25;	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r8 = r21.getMethod(r22, r23);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        if (r8 != 0) goto L_0x0032;
    L_0x002f:
        r21 = "";
        goto L_0x0008;
    L_0x0032:
        r20 = new android.text.format.Time;	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r20.<init>();	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r20.setToNow();	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r0 = r20;
        r0 = r0.minute;	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r21 = r0;
        r22 = org.acra.ACRA.getConfig();	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r22 = r22.dropboxCollectionMinutes();	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r21 = r21 - r22;
        r0 = r21;
        r1 = r20;
        r1.minute = r0;	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r21 = 0;
        r20.normalize(r21);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r21 = 0;
        r18 = r20.toMillis(r21);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r16 = new java.util.ArrayList;	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r16.<init>();	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r21 = org.acra.ACRA.getConfig();	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r21 = r21.includeDropBoxSystemTags();	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        if (r21 == 0) goto L_0x0077;
    L_0x006a:
        r21 = SYSTEM_TAGS;	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r21 = java.util.Arrays.asList(r21);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r0 = r16;
        r1 = r21;
        r0.addAll(r1);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
    L_0x0077:
        if (r27 == 0) goto L_0x008b;
    L_0x0079:
        r0 = r27;
        r0 = r0.length;	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r21 = r0;
        if (r21 <= 0) goto L_0x008b;
    L_0x0080:
        r21 = java.util.Arrays.asList(r27);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r0 = r16;
        r1 = r21;
        r0.addAll(r1);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
    L_0x008b:
        r21 = r16.isEmpty();	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        if (r21 == 0) goto L_0x0095;
    L_0x0091:
        r21 = "No tag configured for collection.";
        goto L_0x0008;
    L_0x0095:
        r5 = new java.lang.StringBuilder;	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r5.<init>();	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r11 = r16.iterator();	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
    L_0x009e:
        r21 = r11.hasNext();	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        if (r21 == 0) goto L_0x01cc;
    L_0x00a4:
        r15 = r11.next();	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r15 = (java.lang.String) r15;	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r21 = "Tag: ";
        r0 = r21;
        r21 = r5.append(r0);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r0 = r21;
        r21 = r0.append(r15);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r22 = 10;
        r21.append(r22);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r21 = 2;
        r0 = r21;
        r0 = new java.lang.Object[r0];	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r21 = r0;
        r22 = 0;
        r21[r22] = r15;	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r22 = 1;
        r23 = java.lang.Long.valueOf(r18);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r21[r22] = r23;	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r0 = r21;
        r7 = r8.invoke(r4, r0);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        if (r7 != 0) goto L_0x00f3;
    L_0x00d9:
        r21 = "Nothing.";
        r0 = r21;
        r21 = r5.append(r0);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r22 = 10;
        r21.append(r22);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        goto L_0x009e;
    L_0x00e7:
        r6 = move-exception;
        r21 = org.acra.ACRA.LOG_TAG;
        r22 = "DropBoxManager not available.";
        android.util.Log.i(r21, r22);
    L_0x00ef:
        r21 = "N/A";
        goto L_0x0008;
    L_0x00f3:
        r21 = r7.getClass();	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r22 = "getText";
        r23 = 1;
        r0 = r23;
        r0 = new java.lang.Class[r0];	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r23 = r0;
        r24 = 0;
        r25 = java.lang.Integer.TYPE;	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r23[r24] = r25;	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r9 = r21.getMethod(r22, r23);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r22 = r7.getClass();	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r23 = "getTimeMillis";
        r21 = 0;
        r21 = (java.lang.Class[]) r21;	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r0 = r22;
        r1 = r23;
        r2 = r21;
        r10 = r0.getMethod(r1, r2);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r22 = r7.getClass();	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r23 = "close";
        r21 = 0;
        r21 = (java.lang.Class[]) r21;	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r0 = r22;
        r1 = r23;
        r2 = r21;
        r3 = r0.getMethod(r1, r2);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
    L_0x0133:
        if (r7 == 0) goto L_0x009e;
    L_0x0135:
        r21 = 0;
        r21 = (java.lang.Object[]) r21;	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r0 = r21;
        r21 = r10.invoke(r7, r0);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r21 = (java.lang.Long) r21;	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r12 = r21.longValue();	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r0 = r20;
        r0.set(r12);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r21 = "@";
        r0 = r21;
        r21 = r5.append(r0);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r22 = r20.format2445();	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r21 = r21.append(r22);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r22 = 10;
        r21.append(r22);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r21 = 1;
        r0 = r21;
        r0 = new java.lang.Object[r0];	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r21 = r0;
        r22 = 0;
        r23 = 500; // 0x1f4 float:7.0E-43 double:2.47E-321;
        r23 = java.lang.Integer.valueOf(r23);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r21[r22] = r23;	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r0 = r21;
        r17 = r9.invoke(r7, r0);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r17 = (java.lang.String) r17;	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        if (r17 == 0) goto L_0x01b4;
    L_0x017b:
        r21 = "Text: ";
        r0 = r21;
        r21 = r5.append(r0);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r0 = r21;
        r1 = r17;
        r21 = r0.append(r1);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r22 = 10;
        r21.append(r22);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
    L_0x0190:
        r21 = 0;
        r21 = (java.lang.Object[]) r21;	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r0 = r21;
        r3.invoke(r7, r0);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r21 = 2;
        r0 = r21;
        r0 = new java.lang.Object[r0];	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r21 = r0;
        r22 = 0;
        r21[r22] = r15;	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r22 = 1;
        r23 = java.lang.Long.valueOf(r12);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r21[r22] = r23;	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r0 = r21;
        r7 = r8.invoke(r4, r0);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        goto L_0x0133;
    L_0x01b4:
        r21 = "Not Text!";
        r0 = r21;
        r21 = r5.append(r0);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        r22 = 10;
        r21.append(r22);	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        goto L_0x0190;
    L_0x01c2:
        r6 = move-exception;
        r21 = org.acra.ACRA.LOG_TAG;
        r22 = "DropBoxManager not available.";
        android.util.Log.i(r21, r22);
        goto L_0x00ef;
    L_0x01cc:
        r21 = r5.toString();	 Catch:{ SecurityException -> 0x00e7, NoSuchMethodException -> 0x01c2, IllegalArgumentException -> 0x01d2, IllegalAccessException -> 0x01dc, InvocationTargetException -> 0x01e6, NoSuchFieldException -> 0x01f0 }
        goto L_0x0008;
    L_0x01d2:
        r6 = move-exception;
        r21 = org.acra.ACRA.LOG_TAG;
        r22 = "DropBoxManager not available.";
        android.util.Log.i(r21, r22);
        goto L_0x00ef;
    L_0x01dc:
        r6 = move-exception;
        r21 = org.acra.ACRA.LOG_TAG;
        r22 = "DropBoxManager not available.";
        android.util.Log.i(r21, r22);
        goto L_0x00ef;
    L_0x01e6:
        r6 = move-exception;
        r21 = org.acra.ACRA.LOG_TAG;
        r22 = "DropBoxManager not available.";
        android.util.Log.i(r21, r22);
        goto L_0x00ef;
    L_0x01f0:
        r6 = move-exception;
        r21 = org.acra.ACRA.LOG_TAG;
        r22 = "DropBoxManager not available.";
        android.util.Log.i(r21, r22);
        goto L_0x00ef;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.acra.collector.DropBoxCollector.read(android.content.Context, java.lang.String[]):java.lang.String");
    }
}
