package org.xbill.DNS;

public final class Opcode {
    public static final int IQUERY = 1;
    public static final int NOTIFY = 4;
    public static final int QUERY = 0;
    public static final int STATUS = 2;
    public static final int UPDATE = 5;
    private static Mnemonic opcodes;

    static {
        opcodes = new Mnemonic("DNS Opcode", STATUS);
        opcodes.setMaximum(15);
        opcodes.setPrefix("RESERVED");
        opcodes.setNumericAllowed(true);
        opcodes.add(QUERY, "QUERY");
        opcodes.add(IQUERY, "IQUERY");
        opcodes.add(STATUS, "STATUS");
        opcodes.add(NOTIFY, "NOTIFY");
        opcodes.add(UPDATE, "UPDATE");
    }

    private Opcode() {
    }

    public static String string(int i) {
        return opcodes.getText(i);
    }

    public static int value(String s) {
        return opcodes.getValue(s);
    }
}
