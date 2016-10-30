package org.acra.log;

public interface ACRALog {
    int d(String str, String str2);

    int d(String str, String str2, Throwable th);

    int e(String str, String str2);

    int e(String str, String str2, Throwable th);

    String getStackTraceString(Throwable th);

    int i(String str, String str2);

    int i(String str, String str2, Throwable th);

    int v(String str, String str2);

    int v(String str, String str2, Throwable th);

    int w(String str, String str2);

    int w(String str, String str2, Throwable th);

    int w(String str, Throwable th);
}
