package com.fattin.hotspot.webserver;

import com.fattin.hotspot.helpers.Log;
import java.net.Socket;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;

public class ClientConnectionThread extends Thread {
    private static final String LOG_TAG = "ClientConnectionThread";
    private static final String PATTERN_ALL = "*";
    private static final String THREAD_NAME = "FattinHttpClientConnection";
    private BasicHttpContext httpContext;
    private HttpService httpService;
    private BasicHttpProcessor httpproc;
    private HttpRequestHandlerRegistry registry;
    private Socket socket;

    public ClientConnectionThread(Socket socket, HttpRequestHandler httpRequestHandler) {
        super(THREAD_NAME);
        this.socket = null;
        this.httpproc = null;
        this.httpContext = null;
        this.httpService = null;
        this.registry = null;
        Log.m0d(LOG_TAG, "Constructor -> begin");
        this.socket = socket;
        this.httpproc = new BasicHttpProcessor();
        this.httpContext = new BasicHttpContext();
        this.httpService = new HttpService(this.httpproc, new NoConnectionReuseStrategy(), new DefaultHttpResponseFactory());
        this.registry = new HttpRequestHandlerRegistry();
        this.registry.register(PATTERN_ALL, httpRequestHandler);
        this.httpService.setHandlerResolver(this.registry);
        Log.m0d(LOG_TAG, "Constructor -> end");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void run() {
        /*
        r8 = this;
        super.run();
        r5 = "ClientConnectionThread";
        r6 = "run -> start";
        com.fattin.hotspot.helpers.Log.m0d(r5, r6);
        r3 = new org.apache.http.impl.DefaultHttpServerConnection;
        r3.<init>();
        r2 = new org.apache.http.params.BasicHttpParams;	 Catch:{ ConnectionClosedException -> 0x0094, SSLException -> 0x00ba, Exception -> 0x00e0 }
        r2.<init>();	 Catch:{ ConnectionClosedException -> 0x0094, SSLException -> 0x00ba, Exception -> 0x00e0 }
        r5 = 10000; // 0x2710 float:1.4013E-41 double:4.9407E-320;
        org.apache.http.params.HttpConnectionParams.setConnectionTimeout(r2, r5);	 Catch:{ ConnectionClosedException -> 0x0094, SSLException -> 0x00ba, Exception -> 0x00e0 }
        r5 = 10000; // 0x2710 float:1.4013E-41 double:4.9407E-320;
        org.apache.http.params.HttpConnectionParams.setSoTimeout(r2, r5);	 Catch:{ ConnectionClosedException -> 0x0094, SSLException -> 0x00ba, Exception -> 0x00e0 }
        r5 = 1;
        org.apache.http.params.HttpConnectionParams.setStaleCheckingEnabled(r2, r5);	 Catch:{ ConnectionClosedException -> 0x0094, SSLException -> 0x00ba, Exception -> 0x00e0 }
        r5 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
        org.apache.http.params.HttpConnectionParams.setLinger(r2, r5);	 Catch:{ ConnectionClosedException -> 0x0094, SSLException -> 0x00ba, Exception -> 0x00e0 }
        r5 = "ClientConnectionThread";
        r6 = "run -> before serverConnection.bind";
        com.fattin.hotspot.helpers.Log.m0d(r5, r6);	 Catch:{ ConnectionClosedException -> 0x0094, SSLException -> 0x00ba, Exception -> 0x00e0 }
        r5 = r8.socket;	 Catch:{ ConnectionClosedException -> 0x0094, SSLException -> 0x00ba, Exception -> 0x00e0 }
        r3.bind(r5, r2);	 Catch:{ ConnectionClosedException -> 0x0094, SSLException -> 0x00ba, Exception -> 0x00e0 }
        r5 = "ClientConnectionThread";
        r6 = "run -> after serverConnection.bind";
        com.fattin.hotspot.helpers.Log.m0d(r5, r6);	 Catch:{ ConnectionClosedException -> 0x0094, SSLException -> 0x00ba, Exception -> 0x00e0 }
        r4 = com.fattin.hotspot.app.DataProvider.getUserAuthenticationToken();	 Catch:{ ConnectionClosedException -> 0x0094, SSLException -> 0x00ba, Exception -> 0x00e0 }
        r1 = com.fattin.hotspot.app.DataProvider.getHotspotAuthToken();	 Catch:{ ConnectionClosedException -> 0x0094, SSLException -> 0x00ba, Exception -> 0x00e0 }
        r5 = r8.httpContext;	 Catch:{ ConnectionClosedException -> 0x0094, SSLException -> 0x00ba, Exception -> 0x00e0 }
        r6 = "user_auth_token";
        r5.setAttribute(r6, r4);	 Catch:{ ConnectionClosedException -> 0x0094, SSLException -> 0x00ba, Exception -> 0x00e0 }
        r5 = r8.httpContext;	 Catch:{ ConnectionClosedException -> 0x0094, SSLException -> 0x00ba, Exception -> 0x00e0 }
        r6 = "hotspot_token";
        r5.setAttribute(r6, r1);	 Catch:{ ConnectionClosedException -> 0x0094, SSLException -> 0x00ba, Exception -> 0x00e0 }
        r5 = r8.httpContext;	 Catch:{ ConnectionClosedException -> 0x0094, SSLException -> 0x00ba, Exception -> 0x00e0 }
        r6 = "client_ip";
        r7 = r3.getRemoteAddress();	 Catch:{ ConnectionClosedException -> 0x0094, SSLException -> 0x00ba, Exception -> 0x00e0 }
        r7 = r7.getHostAddress();	 Catch:{ ConnectionClosedException -> 0x0094, SSLException -> 0x00ba, Exception -> 0x00e0 }
        r5.setAttribute(r6, r7);	 Catch:{ ConnectionClosedException -> 0x0094, SSLException -> 0x00ba, Exception -> 0x00e0 }
        r5 = r8.httpContext;	 Catch:{ ConnectionClosedException -> 0x0094, SSLException -> 0x00ba, Exception -> 0x00e0 }
        r6 = "hostname";
        r7 = r3.getRemoteAddress();	 Catch:{ ConnectionClosedException -> 0x0094, SSLException -> 0x00ba, Exception -> 0x00e0 }
        r7 = r7.getHostName();	 Catch:{ ConnectionClosedException -> 0x0094, SSLException -> 0x00ba, Exception -> 0x00e0 }
        r5.setAttribute(r6, r7);	 Catch:{ ConnectionClosedException -> 0x0094, SSLException -> 0x00ba, Exception -> 0x00e0 }
        r5 = "ClientConnectionThread";
        r6 = "run -> before httpService.handleRequest";
        com.fattin.hotspot.helpers.Log.m0d(r5, r6);	 Catch:{ ConnectionClosedException -> 0x0094, SSLException -> 0x00ba, Exception -> 0x00e0 }
        r5 = r8.httpService;	 Catch:{ ConnectionClosedException -> 0x0094, SSLException -> 0x00ba, Exception -> 0x00e0 }
        r6 = r8.httpContext;	 Catch:{ ConnectionClosedException -> 0x0094, SSLException -> 0x00ba, Exception -> 0x00e0 }
        r5.handleRequest(r3, r6);	 Catch:{ ConnectionClosedException -> 0x0094, SSLException -> 0x00ba, Exception -> 0x00e0 }
        r5 = "ClientConnectionThread";
        r6 = "run -> after httpService.handleRequest";
        com.fattin.hotspot.helpers.Log.m0d(r5, r6);	 Catch:{ ConnectionClosedException -> 0x0094, SSLException -> 0x00ba, Exception -> 0x00e0 }
        r5 = r3.isOpen();	 Catch:{ Exception -> 0x0123 }
        if (r5 == 0) goto L_0x008c;
    L_0x0089:
        r3.close();	 Catch:{ Exception -> 0x0123 }
    L_0x008c:
        r5 = "ClientConnectionThread";
        r6 = "run -> end";
        com.fattin.hotspot.helpers.Log.m0d(r5, r6);
    L_0x0093:
        return;
    L_0x0094:
        r0 = move-exception;
        r5 = "ClientConnectionThread";
        r6 = r0.getMessage();	 Catch:{ all -> 0x0106 }
        com.fattin.hotspot.helpers.Log.m4w(r5, r6, r0);	 Catch:{ all -> 0x0106 }
        r5 = r3.isOpen();	 Catch:{ Exception -> 0x00af }
        if (r5 == 0) goto L_0x00a7;
    L_0x00a4:
        r3.close();	 Catch:{ Exception -> 0x00af }
    L_0x00a7:
        r5 = "ClientConnectionThread";
        r6 = "run -> end";
        com.fattin.hotspot.helpers.Log.m0d(r5, r6);
        goto L_0x0093;
    L_0x00af:
        r0 = move-exception;
        r5 = "ClientConnectionThread";
        r6 = r0.getMessage();
        com.fattin.hotspot.helpers.Log.m1e(r5, r6, r0);
        goto L_0x00a7;
    L_0x00ba:
        r0 = move-exception;
        r5 = "ClientConnectionThread";
        r6 = r0.getMessage();	 Catch:{ all -> 0x0106 }
        com.fattin.hotspot.helpers.Log.m4w(r5, r6, r0);	 Catch:{ all -> 0x0106 }
        r5 = r3.isOpen();	 Catch:{ Exception -> 0x00d5 }
        if (r5 == 0) goto L_0x00cd;
    L_0x00ca:
        r3.close();	 Catch:{ Exception -> 0x00d5 }
    L_0x00cd:
        r5 = "ClientConnectionThread";
        r6 = "run -> end";
        com.fattin.hotspot.helpers.Log.m0d(r5, r6);
        goto L_0x0093;
    L_0x00d5:
        r0 = move-exception;
        r5 = "ClientConnectionThread";
        r6 = r0.getMessage();
        com.fattin.hotspot.helpers.Log.m1e(r5, r6, r0);
        goto L_0x00cd;
    L_0x00e0:
        r0 = move-exception;
        r5 = "ClientConnectionThread";
        r6 = r0.getMessage();	 Catch:{ all -> 0x0106 }
        com.fattin.hotspot.helpers.Log.m1e(r5, r6, r0);	 Catch:{ all -> 0x0106 }
        r5 = r3.isOpen();	 Catch:{ Exception -> 0x00fb }
        if (r5 == 0) goto L_0x00f3;
    L_0x00f0:
        r3.close();	 Catch:{ Exception -> 0x00fb }
    L_0x00f3:
        r5 = "ClientConnectionThread";
        r6 = "run -> end";
        com.fattin.hotspot.helpers.Log.m0d(r5, r6);
        goto L_0x0093;
    L_0x00fb:
        r0 = move-exception;
        r5 = "ClientConnectionThread";
        r6 = r0.getMessage();
        com.fattin.hotspot.helpers.Log.m1e(r5, r6, r0);
        goto L_0x00f3;
    L_0x0106:
        r5 = move-exception;
        r6 = r3.isOpen();	 Catch:{ Exception -> 0x0118 }
        if (r6 == 0) goto L_0x0110;
    L_0x010d:
        r3.close();	 Catch:{ Exception -> 0x0118 }
    L_0x0110:
        r6 = "ClientConnectionThread";
        r7 = "run -> end";
        com.fattin.hotspot.helpers.Log.m0d(r6, r7);
        throw r5;
    L_0x0118:
        r0 = move-exception;
        r6 = "ClientConnectionThread";
        r7 = r0.getMessage();
        com.fattin.hotspot.helpers.Log.m1e(r6, r7, r0);
        goto L_0x0110;
    L_0x0123:
        r0 = move-exception;
        r5 = "ClientConnectionThread";
        r6 = r0.getMessage();
        com.fattin.hotspot.helpers.Log.m1e(r5, r6, r0);
        goto L_0x008c;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fattin.hotspot.webserver.ClientConnectionThread.run():void");
    }
}
