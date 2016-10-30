package org.xbill.DNS;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;

public class SimpleResolver implements Resolver {
    public static final int DEFAULT_EDNS_PAYLOADSIZE = 1280;
    public static final int DEFAULT_PORT = 53;
    private static final short DEFAULT_UDPSIZE = (short) 512;
    private static String defaultResolver;
    private static int uniqueID;
    private InetSocketAddress address;
    private boolean ignoreTruncation;
    private InetSocketAddress localAddress;
    private OPTRecord queryOPT;
    private long timeoutValue;
    private TSIG tsig;
    private boolean useTCP;

    static {
        defaultResolver = "localhost";
        uniqueID = 0;
    }

    public SimpleResolver(String hostname) throws UnknownHostException {
        InetAddress addr;
        this.timeoutValue = 10000;
        if (hostname == null) {
            hostname = ResolverConfig.getCurrentConfig().server();
            if (hostname == null) {
                hostname = defaultResolver;
            }
        }
        if (hostname.equals("0")) {
            addr = InetAddress.getLocalHost();
        } else {
            addr = InetAddress.getByName(hostname);
        }
        this.address = new InetSocketAddress(addr, DEFAULT_PORT);
    }

    public SimpleResolver() throws UnknownHostException {
        this(null);
    }

    InetSocketAddress getAddress() {
        return this.address;
    }

    public static void setDefaultResolver(String hostname) {
        defaultResolver = hostname;
    }

    public void setPort(int port) {
        this.address = new InetSocketAddress(this.address.getAddress(), port);
    }

    public void setAddress(InetSocketAddress addr) {
        this.address = addr;
    }

    public void setAddress(InetAddress addr) {
        this.address = new InetSocketAddress(addr, this.address.getPort());
    }

    public void setLocalAddress(InetSocketAddress addr) {
        this.localAddress = addr;
    }

    public void setLocalAddress(InetAddress addr) {
        this.localAddress = new InetSocketAddress(addr, 0);
    }

    public void setTCP(boolean flag) {
        this.useTCP = flag;
    }

    public void setIgnoreTruncation(boolean flag) {
        this.ignoreTruncation = flag;
    }

    public void setEDNS(int level, int payloadSize, int flags, List options) {
        if (level == 0 || level == -1) {
            if (payloadSize == 0) {
                payloadSize = DEFAULT_EDNS_PAYLOADSIZE;
            }
            this.queryOPT = new OPTRecord(payloadSize, 0, level, flags, options);
            return;
        }
        throw new IllegalArgumentException("invalid EDNS level - must be 0 or -1");
    }

    public void setEDNS(int level) {
        setEDNS(level, 0, 0, null);
    }

    public void setTSIGKey(TSIG key) {
        this.tsig = key;
    }

    TSIG getTSIGKey() {
        return this.tsig;
    }

    public void setTimeout(int secs, int msecs) {
        this.timeoutValue = (((long) secs) * 1000) + ((long) msecs);
    }

    public void setTimeout(int secs) {
        setTimeout(secs, 0);
    }

    long getTimeout() {
        return this.timeoutValue;
    }

    private Message parseMessage(byte[] b) throws WireParseException {
        try {
            return new Message(b);
        } catch (IOException e) {
            IOException e2 = e;
            if (Options.check("verbose")) {
                e2.printStackTrace();
            }
            if (!(e2 instanceof WireParseException)) {
                e2 = new WireParseException("Error parsing message");
            }
            throw ((WireParseException) e2);
        }
    }

    private void verifyTSIG(Message query, Message response, byte[] b, TSIG tsig) {
        if (tsig != null) {
            int error = tsig.verify(response, b, query.getTSIG());
            if (Options.check("verbose")) {
                System.err.println(new StringBuffer().append("TSIG verify: ").append(Rcode.TSIGstring(error)).toString());
            }
        }
    }

    private void applyEDNS(Message query) {
        if (this.queryOPT != null && query.getOPT() == null) {
            query.addRecord(this.queryOPT, 3);
        }
    }

    private int maxUDPSize(Message query) {
        OPTRecord opt = query.getOPT();
        if (opt == null) {
            return KEYRecord.OWNER_HOST;
        }
        return opt.getPayloadSize();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public org.xbill.DNS.Message send(org.xbill.DNS.Message r18) throws java.io.IOException {
        /*
        r17 = this;
        r2 = "verbose";
        r2 = org.xbill.DNS.Options.check(r2);
        if (r2 == 0) goto L_0x003e;
    L_0x0008:
        r2 = java.lang.System.err;
        r3 = new java.lang.StringBuffer;
        r3.<init>();
        r15 = "Sending to ";
        r3 = r3.append(r15);
        r0 = r17;
        r15 = r0.address;
        r15 = r15.getAddress();
        r15 = r15.getHostAddress();
        r3 = r3.append(r15);
        r15 = ":";
        r3 = r3.append(r15);
        r0 = r17;
        r15 = r0.address;
        r15 = r15.getPort();
        r3 = r3.append(r15);
        r3 = r3.toString();
        r2.println(r3);
    L_0x003e:
        r2 = r18.getHeader();
        r2 = r2.getOpcode();
        if (r2 != 0) goto L_0x005b;
    L_0x0048:
        r12 = r18.getQuestion();
        if (r12 == 0) goto L_0x005b;
    L_0x004e:
        r2 = r12.getType();
        r3 = 252; // 0xfc float:3.53E-43 double:1.245E-321;
        if (r2 != r3) goto L_0x005b;
    L_0x0056:
        r13 = r17.sendAXFR(r18);
    L_0x005a:
        return r13;
    L_0x005b:
        r18 = r18.clone();
        r18 = (org.xbill.DNS.Message) r18;
        r17.applyEDNS(r18);
        r0 = r17;
        r2 = r0.tsig;
        if (r2 == 0) goto L_0x0074;
    L_0x006a:
        r0 = r17;
        r2 = r0.tsig;
        r3 = 0;
        r0 = r18;
        r2.apply(r0, r3);
    L_0x0074:
        r2 = 65535; // 0xffff float:9.1834E-41 double:3.23786E-319;
        r0 = r18;
        r4 = r0.toWire(r2);
        r5 = r17.maxUDPSize(r18);
        r14 = 0;
        r2 = java.lang.System.currentTimeMillis();
        r0 = r17;
        r15 = r0.timeoutValue;
        r6 = r2 + r15;
    L_0x008c:
        r0 = r17;
        r2 = r0.useTCP;
        if (r2 != 0) goto L_0x0095;
    L_0x0092:
        r2 = r4.length;
        if (r2 <= r5) goto L_0x0096;
    L_0x0095:
        r14 = 1;
    L_0x0096:
        if (r14 == 0) goto L_0x00b1;
    L_0x0098:
        r0 = r17;
        r2 = r0.localAddress;
        r0 = r17;
        r3 = r0.address;
        r10 = org.xbill.DNS.TCPClient.sendrecv(r2, r3, r4, r6);
    L_0x00a4:
        r2 = r10.length;
        r3 = 12;
        if (r2 >= r3) goto L_0x00be;
    L_0x00a9:
        r2 = new org.xbill.DNS.WireParseException;
        r3 = "invalid DNS header - too short";
        r2.<init>(r3);
        throw r2;
    L_0x00b1:
        r0 = r17;
        r2 = r0.localAddress;
        r0 = r17;
        r3 = r0.address;
        r10 = org.xbill.DNS.UDPClient.sendrecv(r2, r3, r4, r5, r6);
        goto L_0x00a4;
    L_0x00be:
        r2 = 0;
        r2 = r10[r2];
        r2 = r2 & 255;
        r2 = r2 << 8;
        r3 = 1;
        r3 = r10[r3];
        r3 = r3 & 255;
        r9 = r2 + r3;
        r2 = r18.getHeader();
        r11 = r2.getID();
        if (r9 == r11) goto L_0x0109;
    L_0x00d6:
        r2 = new java.lang.StringBuffer;
        r2.<init>();
        r3 = "invalid message id: expected ";
        r2 = r2.append(r3);
        r2 = r2.append(r11);
        r3 = "; got id ";
        r2 = r2.append(r3);
        r2 = r2.append(r9);
        r8 = r2.toString();
        if (r14 == 0) goto L_0x00fb;
    L_0x00f5:
        r2 = new org.xbill.DNS.WireParseException;
        r2.<init>(r8);
        throw r2;
    L_0x00fb:
        r2 = "verbose";
        r2 = org.xbill.DNS.Options.check(r2);
        if (r2 == 0) goto L_0x008c;
    L_0x0103:
        r2 = java.lang.System.err;
        r2.println(r8);
        goto L_0x008c;
    L_0x0109:
        r0 = r17;
        r13 = r0.parseMessage(r10);
        r0 = r17;
        r2 = r0.tsig;
        r0 = r17;
        r1 = r18;
        r0.verifyTSIG(r1, r13, r10, r2);
        if (r14 != 0) goto L_0x005a;
    L_0x011c:
        r0 = r17;
        r2 = r0.ignoreTruncation;
        if (r2 != 0) goto L_0x005a;
    L_0x0122:
        r2 = r13.getHeader();
        r3 = 6;
        r2 = r2.getFlag(r3);
        if (r2 == 0) goto L_0x005a;
    L_0x012d:
        r14 = 1;
        goto L_0x008c;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.xbill.DNS.SimpleResolver.send(org.xbill.DNS.Message):org.xbill.DNS.Message");
    }

    public Object sendAsync(Message query, ResolverListener listener) {
        Integer id;
        String qname;
        synchronized (this) {
            int i = uniqueID;
            uniqueID = i + 1;
            id = new Integer(i);
        }
        Record question = query.getQuestion();
        if (question != null) {
            qname = question.getName().toString();
        } else {
            qname = "(none)";
        }
        String name = new StringBuffer().append(getClass()).append(": ").append(qname).toString();
        Thread thread = new ResolveThread(this, query, id, listener);
        thread.setName(name);
        thread.setDaemon(true);
        thread.start();
        return id;
    }

    private Message sendAXFR(Message query) throws IOException {
        ZoneTransferIn xfrin = ZoneTransferIn.newAXFR(query.getQuestion().getName(), this.address, this.tsig);
        xfrin.setTimeout((int) (getTimeout() / 1000));
        xfrin.setLocalAddress(this.localAddress);
        try {
            xfrin.run();
            List<Record> records = xfrin.getAXFR();
            Message response = new Message(query.getHeader().getID());
            response.getHeader().setFlag(5);
            response.getHeader().setFlag(0);
            response.addRecord(query.getQuestion(), 0);
            for (Record addRecord : records) {
                response.addRecord(addRecord, 1);
            }
            return response;
        } catch (ZoneTransferException e) {
            throw new WireParseException(e.getMessage());
        }
    }
}
