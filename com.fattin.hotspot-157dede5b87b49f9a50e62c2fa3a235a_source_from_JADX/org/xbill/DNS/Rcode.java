package org.xbill.DNS;

public final class Rcode {
    public static final int BADKEY = 17;
    public static final int BADMODE = 19;
    public static final int BADSIG = 16;
    public static final int BADTIME = 18;
    public static final int BADVERS = 16;
    public static final int FORMERR = 1;
    public static final int NOERROR = 0;
    public static final int NOTAUTH = 9;
    public static final int NOTIMP = 4;
    public static final int NOTIMPL = 4;
    public static final int NOTZONE = 10;
    public static final int NXDOMAIN = 3;
    public static final int NXRRSET = 8;
    public static final int REFUSED = 5;
    public static final int SERVFAIL = 2;
    public static final int YXDOMAIN = 6;
    public static final int YXRRSET = 7;
    private static Mnemonic rcodes;
    private static Mnemonic tsigrcodes;

    static {
        rcodes = new Mnemonic("DNS Rcode", SERVFAIL);
        tsigrcodes = new Mnemonic("TSIG rcode", SERVFAIL);
        rcodes.setMaximum(4095);
        rcodes.setPrefix("RESERVED");
        rcodes.setNumericAllowed(true);
        rcodes.add(NOERROR, "NOERROR");
        rcodes.add(FORMERR, "FORMERR");
        rcodes.add(SERVFAIL, "SERVFAIL");
        rcodes.add(NXDOMAIN, "NXDOMAIN");
        rcodes.add(NOTIMPL, "NOTIMP");
        rcodes.addAlias(NOTIMPL, "NOTIMPL");
        rcodes.add(REFUSED, "REFUSED");
        rcodes.add(YXDOMAIN, "YXDOMAIN");
        rcodes.add(YXRRSET, "YXRRSET");
        rcodes.add(NXRRSET, "NXRRSET");
        rcodes.add(NOTAUTH, "NOTAUTH");
        rcodes.add(NOTZONE, "NOTZONE");
        rcodes.add(BADVERS, "BADVERS");
        tsigrcodes.setMaximum(Message.MAXLENGTH);
        tsigrcodes.setPrefix("RESERVED");
        tsigrcodes.setNumericAllowed(true);
        tsigrcodes.addAll(rcodes);
        tsigrcodes.add(BADVERS, "BADSIG");
        tsigrcodes.add(BADKEY, "BADKEY");
        tsigrcodes.add(BADTIME, "BADTIME");
        tsigrcodes.add(BADMODE, "BADMODE");
    }

    private Rcode() {
    }

    public static String string(int i) {
        return rcodes.getText(i);
    }

    public static String TSIGstring(int i) {
        return tsigrcodes.getText(i);
    }

    public static int value(String s) {
        return rcodes.getValue(s);
    }
}
