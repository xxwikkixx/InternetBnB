package org.xbill.DNS;

public final class DClass {
    public static final int ANY = 255;
    public static final int CH = 3;
    public static final int CHAOS = 3;
    public static final int HESIOD = 4;
    public static final int HS = 4;
    public static final int IN = 1;
    public static final int NONE = 254;
    private static Mnemonic classes;

    private static class DClassMnemonic extends Mnemonic {
        public DClassMnemonic() {
            super("DClass", 2);
            setPrefix("CLASS");
        }

        public void check(int val) {
            DClass.check(val);
        }
    }

    static {
        classes = new DClassMnemonic();
        classes.add(IN, "IN");
        classes.add(CHAOS, "CH");
        classes.addAlias(CHAOS, "CHAOS");
        classes.add(HS, "HS");
        classes.addAlias(HS, "HESIOD");
        classes.add(NONE, "NONE");
        classes.add(ANY, "ANY");
    }

    private DClass() {
    }

    public static void check(int i) {
        if (i < 0 || i > Message.MAXLENGTH) {
            throw new InvalidDClassException(i);
        }
    }

    public static String string(int i) {
        return classes.getText(i);
    }

    public static int value(String s) {
        return classes.getValue(s);
    }
}
