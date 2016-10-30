package org.xbill.DNS;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class ExtendedResolver implements Resolver {
    private static final int quantum = 5;
    private int lbStart;
    private boolean loadBalance;
    private List resolvers;
    private int retries;

    private static class Resolution implements ResolverListener {
        boolean done;
        Object[] inprogress;
        ResolverListener listener;
        int outstanding;
        Message query;
        Resolver[] resolvers;
        Message response;
        int retries;
        int[] sent;
        Throwable thrown;

        public Resolution(ExtendedResolver eres, Message query) {
            List l = ExtendedResolver.access$000(eres);
            this.resolvers = (Resolver[]) l.toArray(new Resolver[l.size()]);
            if (ExtendedResolver.access$100(eres)) {
                int nresolvers = this.resolvers.length;
                int start = ExtendedResolver.access$208(eres) % nresolvers;
                if (ExtendedResolver.access$200(eres) > nresolvers) {
                    ExtendedResolver.access$244(eres, nresolvers);
                }
                if (start > 0) {
                    Resolver[] shuffle = new Resolver[nresolvers];
                    for (int i = 0; i < nresolvers; i++) {
                        shuffle[i] = this.resolvers[(i + start) % nresolvers];
                    }
                    this.resolvers = shuffle;
                }
            }
            this.sent = new int[this.resolvers.length];
            this.inprogress = new Object[this.resolvers.length];
            this.retries = ExtendedResolver.access$300(eres);
            this.query = query;
        }

        public void send(int n) {
            int[] iArr = this.sent;
            iArr[n] = iArr[n] + 1;
            this.outstanding++;
            try {
                this.inprogress[n] = this.resolvers[n].sendAsync(this.query, this);
            } catch (Throwable t) {
                synchronized (this) {
                }
                this.thrown = t;
                this.done = true;
                if (this.listener == null) {
                    notifyAll();
                }
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public org.xbill.DNS.Message start() throws java.io.IOException {
            /*
            r5 = this;
            r4 = 0;
            r1 = r5.sent;	 Catch:{ Exception -> 0x0026 }
            r2 = 0;
            r3 = r1[r2];	 Catch:{ Exception -> 0x0026 }
            r3 = r3 + 1;
            r1[r2] = r3;	 Catch:{ Exception -> 0x0026 }
            r1 = r5.outstanding;	 Catch:{ Exception -> 0x0026 }
            r1 = r1 + 1;
            r5.outstanding = r1;	 Catch:{ Exception -> 0x0026 }
            r1 = r5.inprogress;	 Catch:{ Exception -> 0x0026 }
            r2 = 0;
            r3 = new java.lang.Object;	 Catch:{ Exception -> 0x0026 }
            r3.<init>();	 Catch:{ Exception -> 0x0026 }
            r1[r2] = r3;	 Catch:{ Exception -> 0x0026 }
            r1 = r5.resolvers;	 Catch:{ Exception -> 0x0026 }
            r2 = 0;
            r1 = r1[r2];	 Catch:{ Exception -> 0x0026 }
            r2 = r5.query;	 Catch:{ Exception -> 0x0026 }
            r1 = r1.send(r2);	 Catch:{ Exception -> 0x0026 }
        L_0x0025:
            return r1;
        L_0x0026:
            r0 = move-exception;
            r1 = r5.inprogress;
            r1 = r1[r4];
            r5.handleException(r1, r0);
            monitor-enter(r5);
        L_0x002f:
            r1 = r5.done;	 Catch:{ all -> 0x0041 }
            if (r1 != 0) goto L_0x0039;
        L_0x0033:
            r5.wait();	 Catch:{ InterruptedException -> 0x0037 }
            goto L_0x002f;
        L_0x0037:
            r1 = move-exception;
            goto L_0x002f;
        L_0x0039:
            monitor-exit(r5);	 Catch:{ all -> 0x0041 }
            r1 = r5.response;
            if (r1 == 0) goto L_0x0044;
        L_0x003e:
            r1 = r5.response;
            goto L_0x0025;
        L_0x0041:
            r1 = move-exception;
            monitor-exit(r5);	 Catch:{ all -> 0x0041 }
            throw r1;
        L_0x0044:
            r1 = r5.thrown;
            r1 = r1 instanceof java.io.IOException;
            if (r1 == 0) goto L_0x004f;
        L_0x004a:
            r1 = r5.thrown;
            r1 = (java.io.IOException) r1;
            throw r1;
        L_0x004f:
            r1 = r5.thrown;
            r1 = r1 instanceof java.lang.RuntimeException;
            if (r1 == 0) goto L_0x005a;
        L_0x0055:
            r1 = r5.thrown;
            r1 = (java.lang.RuntimeException) r1;
            throw r1;
        L_0x005a:
            r1 = r5.thrown;
            r1 = r1 instanceof java.lang.Error;
            if (r1 == 0) goto L_0x0065;
        L_0x0060:
            r1 = r5.thrown;
            r1 = (java.lang.Error) r1;
            throw r1;
        L_0x0065:
            r1 = new java.lang.IllegalStateException;
            r2 = "ExtendedResolver failure";
            r1.<init>(r2);
            throw r1;
            */
            throw new UnsupportedOperationException("Method not decompiled: org.xbill.DNS.ExtendedResolver.Resolution.start():org.xbill.DNS.Message");
        }

        public void startAsync(ResolverListener listener) {
            this.listener = listener;
            send(0);
        }

        public void receiveMessage(Object id, Message m) {
            if (Options.check("verbose")) {
                System.err.println("ExtendedResolver: received message");
            }
            synchronized (this) {
                if (this.done) {
                    return;
                }
                this.response = m;
                this.done = true;
                if (this.listener == null) {
                    notifyAll();
                    return;
                }
                this.listener.receiveMessage(this, this.response);
            }
        }

        public void handleException(Object id, Exception e) {
            if (Options.check("verbose")) {
                System.err.println(new StringBuffer().append("ExtendedResolver: got ").append(e).toString());
            }
            synchronized (this) {
                this.outstanding--;
                if (this.done) {
                    return;
                }
                int n = 0;
                while (n < this.inprogress.length && this.inprogress[n] != id) {
                    n++;
                }
                if (n == this.inprogress.length) {
                    return;
                }
                boolean startnext = false;
                if (this.sent[n] == 1 && n < this.resolvers.length - 1) {
                    startnext = true;
                }
                if (e instanceof InterruptedIOException) {
                    if (this.sent[n] < this.retries) {
                        send(n);
                    }
                    if (this.thrown == null) {
                        this.thrown = e;
                    }
                } else if (!(e instanceof SocketException)) {
                    this.thrown = e;
                } else if (this.thrown == null || (this.thrown instanceof InterruptedIOException)) {
                    this.thrown = e;
                }
                if (this.done) {
                    return;
                }
                if (startnext) {
                    send(n + 1);
                }
                if (this.done) {
                    return;
                }
                if (this.outstanding == 0) {
                    this.done = true;
                    if (this.listener == null) {
                        notifyAll();
                        return;
                    }
                }
                if (this.done) {
                    if (!(this.thrown instanceof Exception)) {
                        this.thrown = new RuntimeException(this.thrown.getMessage());
                    }
                    this.listener.handleException(this, (Exception) this.thrown);
                    return;
                }
            }
        }
    }

    static List access$000(ExtendedResolver x0) {
        return x0.resolvers;
    }

    static boolean access$100(ExtendedResolver x0) {
        return x0.loadBalance;
    }

    static int access$200(ExtendedResolver x0) {
        return x0.lbStart;
    }

    static int access$208(ExtendedResolver x0) {
        int i = x0.lbStart;
        x0.lbStart = i + 1;
        return i;
    }

    static int access$244(ExtendedResolver x0, int x1) {
        int i = x0.lbStart % x1;
        x0.lbStart = i;
        return i;
    }

    static int access$300(ExtendedResolver x0) {
        return x0.retries;
    }

    private void init() {
        this.resolvers = new ArrayList();
    }

    public ExtendedResolver() throws UnknownHostException {
        this.loadBalance = false;
        this.lbStart = 0;
        this.retries = 3;
        init();
        String[] servers = ResolverConfig.getCurrentConfig().servers();
        if (servers != null) {
            for (String simpleResolver : servers) {
                Resolver r = new SimpleResolver(simpleResolver);
                r.setTimeout(quantum);
                this.resolvers.add(r);
            }
            return;
        }
        this.resolvers.add(new SimpleResolver());
    }

    public ExtendedResolver(String[] servers) throws UnknownHostException {
        this.loadBalance = false;
        this.lbStart = 0;
        this.retries = 3;
        init();
        for (String simpleResolver : servers) {
            Resolver r = new SimpleResolver(simpleResolver);
            r.setTimeout(quantum);
            this.resolvers.add(r);
        }
    }

    public ExtendedResolver(Resolver[] res) throws UnknownHostException {
        this.loadBalance = false;
        this.lbStart = 0;
        this.retries = 3;
        init();
        for (Object add : res) {
            this.resolvers.add(add);
        }
    }

    public void setPort(int port) {
        for (int i = 0; i < this.resolvers.size(); i++) {
            ((Resolver) this.resolvers.get(i)).setPort(port);
        }
    }

    public void setTCP(boolean flag) {
        for (int i = 0; i < this.resolvers.size(); i++) {
            ((Resolver) this.resolvers.get(i)).setTCP(flag);
        }
    }

    public void setIgnoreTruncation(boolean flag) {
        for (int i = 0; i < this.resolvers.size(); i++) {
            ((Resolver) this.resolvers.get(i)).setIgnoreTruncation(flag);
        }
    }

    public void setEDNS(int level) {
        for (int i = 0; i < this.resolvers.size(); i++) {
            ((Resolver) this.resolvers.get(i)).setEDNS(level);
        }
    }

    public void setEDNS(int level, int payloadSize, int flags, List options) {
        for (int i = 0; i < this.resolvers.size(); i++) {
            ((Resolver) this.resolvers.get(i)).setEDNS(level, payloadSize, flags, options);
        }
    }

    public void setTSIGKey(TSIG key) {
        for (int i = 0; i < this.resolvers.size(); i++) {
            ((Resolver) this.resolvers.get(i)).setTSIGKey(key);
        }
    }

    public void setTimeout(int secs, int msecs) {
        for (int i = 0; i < this.resolvers.size(); i++) {
            ((Resolver) this.resolvers.get(i)).setTimeout(secs, msecs);
        }
    }

    public void setTimeout(int secs) {
        setTimeout(secs, 0);
    }

    public Message send(Message query) throws IOException {
        return new Resolution(this, query).start();
    }

    public Object sendAsync(Message query, ResolverListener listener) {
        Resolution res = new Resolution(this, query);
        res.startAsync(listener);
        return res;
    }

    public Resolver getResolver(int n) {
        if (n < this.resolvers.size()) {
            return (Resolver) this.resolvers.get(n);
        }
        return null;
    }

    public Resolver[] getResolvers() {
        return (Resolver[]) this.resolvers.toArray(new Resolver[this.resolvers.size()]);
    }

    public void addResolver(Resolver r) {
        this.resolvers.add(r);
    }

    public void deleteResolver(Resolver r) {
        this.resolvers.remove(r);
    }

    public void setLoadBalance(boolean flag) {
        this.loadBalance = flag;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }
}
