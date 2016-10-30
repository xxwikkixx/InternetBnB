package org.xbill.DNS;

public final class Flags {
    public static final byte AA = (byte) 5;
    public static final byte AD = (byte) 10;
    public static final byte CD = (byte) 11;
    public static final int DO = 32768;
    public static final byte QR = (byte) 0;
    public static final byte RA = (byte) 8;
    public static final byte RD = (byte) 7;
    public static final byte TC = (byte) 6;
    private static Mnemonic flags;

    static {
        flags = new Mnemonic("DNS Header Flag", 3);
        flags.setMaximum(15);
        flags.setPrefix("FLAG");
        flags.setNumericAllowed(true);
        flags.add(0, "qr");
        flags.add(5, "aa");
        flags.add(6, "tc");
        flags.add(7, "rd");
        flags.add(8, "ra");
        flags.add(10, "ad");
        flags.add(11, "cd");
    }

    private Flags() {
    }

    public static String string(int i) {
        return flags.getText(i);
    }

    public static int value(String s) {
        return flags.getValue(s);
    }

    public static boolean isFlag(int index) {
        flags.check(index);
        if ((index < 1 || index > 4) && index < 12) {
            return true;
        }
        return false;
    }
}
