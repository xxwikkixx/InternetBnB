package org.xbill.DNS;

public final class ExtendedFlags {
    public static final int DO = 32768;
    private static Mnemonic extflags;

    static {
        extflags = new Mnemonic("EDNS Flag", 3);
        extflags.setMaximum(Message.MAXLENGTH);
        extflags.setPrefix("FLAG");
        extflags.setNumericAllowed(true);
        extflags.add(DO, "do");
    }

    private ExtendedFlags() {
    }

    public static String string(int i) {
        return extflags.getText(i);
    }

    public static int value(String s) {
        return extflags.getValue(s);
    }
}
