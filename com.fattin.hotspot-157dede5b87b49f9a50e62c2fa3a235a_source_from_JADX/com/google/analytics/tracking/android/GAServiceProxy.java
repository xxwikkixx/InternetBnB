package com.google.analytics.tracking.android;

import android.content.Context;
import android.content.Intent;
import com.google.analytics.tracking.android.AnalyticsGmsCoreClient.OnConnectedListener;
import com.google.analytics.tracking.android.AnalyticsGmsCoreClient.OnConnectionFailedListener;
import com.google.android.gms.analytics.internal.Command;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.xbill.DNS.KEYRecord;

class GAServiceProxy implements ServiceProxy, OnConnectedListener, OnConnectionFailedListener {
    private static final long FAILED_CONNECT_WAIT_TIME = 3000;
    private static final int MAX_TRIES = 2;
    private static final long RECONNECT_WAIT_TIME = 5000;
    private static final long SERVICE_CONNECTION_TIMEOUT = 300000;
    private volatile AnalyticsClient client;
    private Clock clock;
    private volatile int connectTries;
    private final Context ctx;
    private volatile Timer disconnectCheckTimer;
    private volatile Timer failedConnectTimer;
    private long idleTimeout;
    private volatile long lastRequestTime;
    private boolean pendingClearHits;
    private boolean pendingDispatch;
    private final Queue<HitParams> queue;
    private volatile Timer reConnectTimer;
    private volatile ConnectState state;
    private AnalyticsStore store;
    private AnalyticsStore testStore;
    private final AnalyticsThread thread;

    /* renamed from: com.google.analytics.tracking.android.GAServiceProxy.2 */
    class C00352 implements Runnable {
        C00352() {
        }

        public void run() {
            GAServiceProxy.this.sendQueue();
        }
    }

    /* renamed from: com.google.analytics.tracking.android.GAServiceProxy.3 */
    static /* synthetic */ class C00363 {
        static final /* synthetic */ int[] f0x3c5f147c;

        static {
            f0x3c5f147c = new int[ConnectState.values().length];
            try {
                f0x3c5f147c[ConnectState.CONNECTED_LOCAL.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                f0x3c5f147c[ConnectState.CONNECTED_SERVICE.ordinal()] = GAServiceProxy.MAX_TRIES;
            } catch (NoSuchFieldError e2) {
            }
            try {
                f0x3c5f147c[ConnectState.DISCONNECTED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    private enum ConnectState {
        CONNECTING,
        CONNECTED_SERVICE,
        CONNECTED_LOCAL,
        BLOCKED,
        PENDING_CONNECTION,
        PENDING_DISCONNECT,
        DISCONNECTED
    }

    private class DisconnectCheckTask extends TimerTask {
        private DisconnectCheckTask() {
        }

        public void run() {
            if (GAServiceProxy.this.state == ConnectState.CONNECTED_SERVICE && GAServiceProxy.this.queue.isEmpty() && GAServiceProxy.this.lastRequestTime + GAServiceProxy.this.idleTimeout < GAServiceProxy.this.clock.currentTimeMillis()) {
                Log.iDebug("Disconnecting due to inactivity");
                GAServiceProxy.this.disconnectFromService();
                return;
            }
            GAServiceProxy.this.disconnectCheckTimer.schedule(new DisconnectCheckTask(), GAServiceProxy.this.idleTimeout);
        }
    }

    private class FailedConnectTask extends TimerTask {
        private FailedConnectTask() {
        }

        public void run() {
            if (GAServiceProxy.this.state == ConnectState.CONNECTING) {
                GAServiceProxy.this.useStore();
            }
        }
    }

    private static class HitParams {
        private final List<Command> commands;
        private final long hitTimeInMilliseconds;
        private final String path;
        private final Map<String, String> wireFormatParams;

        public HitParams(Map<String, String> wireFormatParams, long hitTimeInMilliseconds, String path, List<Command> commands) {
            this.wireFormatParams = wireFormatParams;
            this.hitTimeInMilliseconds = hitTimeInMilliseconds;
            this.path = path;
            this.commands = commands;
        }

        public Map<String, String> getWireFormatParams() {
            return this.wireFormatParams;
        }

        public long getHitTimeInMilliseconds() {
            return this.hitTimeInMilliseconds;
        }

        public String getPath() {
            return this.path;
        }

        public List<Command> getCommands() {
            return this.commands;
        }
    }

    private class ReconnectTask extends TimerTask {
        private ReconnectTask() {
        }

        public void run() {
            GAServiceProxy.this.connectToService();
        }
    }

    class 1 implements Clock {
        1() {
        }

        public long currentTimeMillis() {
            return System.currentTimeMillis();
        }
    }

    GAServiceProxy(Context ctx, AnalyticsThread thread, AnalyticsStore store) {
        this.queue = new ConcurrentLinkedQueue();
        this.idleTimeout = SERVICE_CONNECTION_TIMEOUT;
        this.testStore = store;
        this.ctx = ctx;
        this.thread = thread;
        this.clock = new 1();
        this.connectTries = 0;
        this.state = ConnectState.DISCONNECTED;
    }

    GAServiceProxy(Context ctx, AnalyticsThread thread) {
        this(ctx, thread, null);
    }

    void setClock(Clock clock) {
        this.clock = clock;
    }

    public void putHit(Map<String, String> wireFormatParams, long hitTimeInMilliseconds, String path, List<Command> commands) {
        Log.iDebug("putHit called");
        this.queue.add(new HitParams(wireFormatParams, hitTimeInMilliseconds, path, commands));
        sendQueue();
    }

    public void dispatch() {
        switch (C00363.f0x3c5f147c[this.state.ordinal()]) {
            case KEYRecord.PROTOCOL_TLS /*1*/:
                dispatchToStore();
            case MAX_TRIES /*2*/:
            default:
                this.pendingDispatch = true;
        }
    }

    public void clearHits() {
        Log.iDebug("clearHits called");
        this.queue.clear();
        switch (C00363.f0x3c5f147c[this.state.ordinal()]) {
            case KEYRecord.PROTOCOL_TLS /*1*/:
                this.store.clearHits(0);
                this.pendingClearHits = false;
            case MAX_TRIES /*2*/:
                this.client.clearHits();
                this.pendingClearHits = false;
            default:
                this.pendingClearHits = true;
        }
    }

    private Timer cancelTimer(Timer timer) {
        if (timer != null) {
            timer.cancel();
        }
        return null;
    }

    private void clearAllTimers() {
        this.reConnectTimer = cancelTimer(this.reConnectTimer);
        this.failedConnectTimer = cancelTimer(this.failedConnectTimer);
        this.disconnectCheckTimer = cancelTimer(this.disconnectCheckTimer);
    }

    public void createService() {
        if (this.client == null) {
            this.client = new AnalyticsGmsCoreClient(this.ctx, this, this);
            connectToService();
        }
    }

    void createService(AnalyticsClient client) {
        if (this.client == null) {
            this.client = client;
            connectToService();
        }
    }

    public void setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void sendQueue() {
        /*
        r7 = this;
        monitor-enter(r7);
        r0 = java.lang.Thread.currentThread();	 Catch:{ all -> 0x0061 }
        r1 = r7.thread;	 Catch:{ all -> 0x0061 }
        r1 = r1.getThread();	 Catch:{ all -> 0x0061 }
        r0 = r0.equals(r1);	 Catch:{ all -> 0x0061 }
        if (r0 != 0) goto L_0x0021;
    L_0x0011:
        r0 = r7.thread;	 Catch:{ all -> 0x0061 }
        r0 = r0.getQueue();	 Catch:{ all -> 0x0061 }
        r1 = new com.google.analytics.tracking.android.GAServiceProxy$2;	 Catch:{ all -> 0x0061 }
        r1.<init>();	 Catch:{ all -> 0x0061 }
        r0.add(r1);	 Catch:{ all -> 0x0061 }
    L_0x001f:
        monitor-exit(r7);
        return;
    L_0x0021:
        r0 = r7.pendingClearHits;	 Catch:{ all -> 0x0061 }
        if (r0 == 0) goto L_0x0028;
    L_0x0025:
        r7.clearHits();	 Catch:{ all -> 0x0061 }
    L_0x0028:
        r0 = com.google.analytics.tracking.android.GAServiceProxy.C00363.f0x3c5f147c;	 Catch:{ all -> 0x0061 }
        r1 = r7.state;	 Catch:{ all -> 0x0061 }
        r1 = r1.ordinal();	 Catch:{ all -> 0x0061 }
        r0 = r0[r1];	 Catch:{ all -> 0x0061 }
        switch(r0) {
            case 1: goto L_0x0036;
            case 2: goto L_0x006c;
            case 3: goto L_0x00a6;
            default: goto L_0x0035;
        };	 Catch:{ all -> 0x0061 }
    L_0x0035:
        goto L_0x001f;
    L_0x0036:
        r0 = r7.queue;	 Catch:{ all -> 0x0061 }
        r0 = r0.isEmpty();	 Catch:{ all -> 0x0061 }
        if (r0 != 0) goto L_0x0064;
    L_0x003e:
        r0 = r7.queue;	 Catch:{ all -> 0x0061 }
        r6 = r0.poll();	 Catch:{ all -> 0x0061 }
        r6 = (com.google.analytics.tracking.android.GAServiceProxy.HitParams) r6;	 Catch:{ all -> 0x0061 }
        r0 = "Sending hit to store";
        com.google.analytics.tracking.android.Log.iDebug(r0);	 Catch:{ all -> 0x0061 }
        r0 = r7.store;	 Catch:{ all -> 0x0061 }
        r1 = r6.getWireFormatParams();	 Catch:{ all -> 0x0061 }
        r2 = r6.getHitTimeInMilliseconds();	 Catch:{ all -> 0x0061 }
        r4 = r6.getPath();	 Catch:{ all -> 0x0061 }
        r5 = r6.getCommands();	 Catch:{ all -> 0x0061 }
        r0.putHit(r1, r2, r4, r5);	 Catch:{ all -> 0x0061 }
        goto L_0x0036;
    L_0x0061:
        r0 = move-exception;
        monitor-exit(r7);
        throw r0;
    L_0x0064:
        r0 = r7.pendingDispatch;	 Catch:{ all -> 0x0061 }
        if (r0 == 0) goto L_0x001f;
    L_0x0068:
        r7.dispatchToStore();	 Catch:{ all -> 0x0061 }
        goto L_0x001f;
    L_0x006c:
        r0 = r7.queue;	 Catch:{ all -> 0x0061 }
        r0 = r0.isEmpty();	 Catch:{ all -> 0x0061 }
        if (r0 != 0) goto L_0x009c;
    L_0x0074:
        r0 = r7.queue;	 Catch:{ all -> 0x0061 }
        r6 = r0.peek();	 Catch:{ all -> 0x0061 }
        r6 = (com.google.analytics.tracking.android.GAServiceProxy.HitParams) r6;	 Catch:{ all -> 0x0061 }
        r0 = "Sending hit to service";
        com.google.analytics.tracking.android.Log.iDebug(r0);	 Catch:{ all -> 0x0061 }
        r0 = r7.client;	 Catch:{ all -> 0x0061 }
        r1 = r6.getWireFormatParams();	 Catch:{ all -> 0x0061 }
        r2 = r6.getHitTimeInMilliseconds();	 Catch:{ all -> 0x0061 }
        r4 = r6.getPath();	 Catch:{ all -> 0x0061 }
        r5 = r6.getCommands();	 Catch:{ all -> 0x0061 }
        r0.sendHit(r1, r2, r4, r5);	 Catch:{ all -> 0x0061 }
        r0 = r7.queue;	 Catch:{ all -> 0x0061 }
        r0.poll();	 Catch:{ all -> 0x0061 }
        goto L_0x006c;
    L_0x009c:
        r0 = r7.clock;	 Catch:{ all -> 0x0061 }
        r0 = r0.currentTimeMillis();	 Catch:{ all -> 0x0061 }
        r7.lastRequestTime = r0;	 Catch:{ all -> 0x0061 }
        goto L_0x001f;
    L_0x00a6:
        r0 = "Need to reconnect";
        com.google.analytics.tracking.android.Log.iDebug(r0);	 Catch:{ all -> 0x0061 }
        r0 = r7.queue;	 Catch:{ all -> 0x0061 }
        r0 = r0.isEmpty();	 Catch:{ all -> 0x0061 }
        if (r0 != 0) goto L_0x001f;
    L_0x00b3:
        r7.connectToService();	 Catch:{ all -> 0x0061 }
        goto L_0x001f;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.analytics.tracking.android.GAServiceProxy.sendQueue():void");
    }

    private void dispatchToStore() {
        this.store.dispatch();
        this.pendingDispatch = false;
    }

    private synchronized void useStore() {
        if (this.state != ConnectState.CONNECTED_LOCAL) {
            clearAllTimers();
            Log.iDebug("falling back to local store");
            if (this.testStore != null) {
                this.store = this.testStore;
            } else {
                GAServiceManager instance = GAServiceManager.getInstance();
                instance.initialize(this.ctx, this.thread);
                this.store = instance.getStore();
            }
            this.state = ConnectState.CONNECTED_LOCAL;
            sendQueue();
        }
    }

    private synchronized void connectToService() {
        if (this.client == null || this.state == ConnectState.CONNECTED_LOCAL) {
            Log.m10w("client not initialized.");
            useStore();
        } else {
            try {
                this.connectTries++;
                cancelTimer(this.failedConnectTimer);
                this.state = ConnectState.CONNECTING;
                this.failedConnectTimer = new Timer("Failed Connect");
                this.failedConnectTimer.schedule(new FailedConnectTask(), FAILED_CONNECT_WAIT_TIME);
                Log.iDebug("connecting to Analytics service");
                this.client.connect();
            } catch (SecurityException e) {
                Log.m10w("security exception on connectToService");
                useStore();
            }
        }
    }

    private synchronized void disconnectFromService() {
        if (this.client != null && this.state == ConnectState.CONNECTED_SERVICE) {
            this.state = ConnectState.PENDING_DISCONNECT;
            this.client.disconnect();
        }
    }

    public synchronized void onConnected() {
        this.failedConnectTimer = cancelTimer(this.failedConnectTimer);
        this.connectTries = 0;
        Log.iDebug("Connected to service");
        this.state = ConnectState.CONNECTED_SERVICE;
        sendQueue();
        this.disconnectCheckTimer = cancelTimer(this.disconnectCheckTimer);
        this.disconnectCheckTimer = new Timer("disconnect check");
        this.disconnectCheckTimer.schedule(new DisconnectCheckTask(), this.idleTimeout);
    }

    public synchronized void onDisconnected() {
        if (this.state == ConnectState.PENDING_DISCONNECT) {
            Log.iDebug("Disconnected from service");
            clearAllTimers();
            this.state = ConnectState.DISCONNECTED;
        } else {
            Log.iDebug("Unexpected disconnect.");
            this.state = ConnectState.PENDING_CONNECTION;
            if (this.connectTries < MAX_TRIES) {
                fireReconnectAttempt();
            } else {
                useStore();
            }
        }
    }

    public synchronized void onConnectionFailed(int errorCode, Intent resolution) {
        this.state = ConnectState.PENDING_CONNECTION;
        if (this.connectTries < MAX_TRIES) {
            Log.m10w("Service unavailable (code=" + errorCode + "), will retry.");
            fireReconnectAttempt();
        } else {
            Log.m10w("Service unavailable (code=" + errorCode + "), using local store.");
            useStore();
        }
    }

    private void fireReconnectAttempt() {
        this.reConnectTimer = cancelTimer(this.reConnectTimer);
        this.reConnectTimer = new Timer("Service Reconnect");
        this.reConnectTimer.schedule(new ReconnectTask(), RECONNECT_WAIT_TIME);
    }
}
