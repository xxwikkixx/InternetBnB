package org.xbill.DNS;

import java.io.IOException;
import java.util.Arrays;

public abstract class EDNSOption {
    private final int code;

    public static class Code {
        public static final int CLIENT_SUBNET = 20730;
        public static final int NSID = 3;
        private static Mnemonic codes;

        private Code() {
        }

        static {
            codes = new Mnemonic("EDNS Option Codes", 2);
            codes.setMaximum(Message.MAXLENGTH);
            codes.setPrefix("CODE");
            codes.setNumericAllowed(true);
            codes.add(NSID, "NSID");
            codes.add(CLIENT_SUBNET, "CLIENT_SUBNET");
        }

        public static String string(int code) {
            return codes.getText(code);
        }

        public static int value(String s) {
            return codes.getValue(s);
        }
    }

    abstract void optionFromWire(DNSInput dNSInput) throws IOException;

    abstract String optionToString();

    abstract void optionToWire(DNSOutput dNSOutput);

    public EDNSOption(int code) {
        this.code = Record.checkU16("code", code);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append(Code.string(this.code));
        sb.append(": ");
        sb.append(optionToString());
        sb.append("}");
        return sb.toString();
    }

    public int getCode() {
        return this.code;
    }

    byte[] getData() {
        DNSOutput out = new DNSOutput();
        optionToWire(out);
        return out.toByteArray();
    }

    static EDNSOption fromWire(DNSInput in) throws IOException {
        int code = in.readU16();
        int length = in.readU16();
        if (in.remaining() < length) {
            throw new WireParseException("truncated option");
        }
        EDNSOption option;
        int save = in.saveActive();
        in.setActive(length);
        switch (code) {
            case KEYRecord.PROTOCOL_DNSSEC /*3*/:
                option = new NSIDOption();
                break;
            case Code.CLIENT_SUBNET /*20730*/:
                option = new ClientSubnetOption();
                break;
            default:
                option = new GenericEDNSOption(code);
                break;
        }
        option.optionFromWire(in);
        in.restoreActive(save);
        return option;
    }

    public static EDNSOption fromWire(byte[] b) throws IOException {
        return fromWire(new DNSInput(b));
    }

    void toWire(DNSOutput out) {
        out.writeU16(this.code);
        int lengthPosition = out.current();
        out.writeU16(0);
        optionToWire(out);
        out.writeU16At((out.current() - lengthPosition) - 2, lengthPosition);
    }

    public byte[] toWire() throws IOException {
        DNSOutput out = new DNSOutput();
        toWire(out);
        return out.toByteArray();
    }

    public boolean equals(Object arg) {
        if (arg == null || !(arg instanceof EDNSOption)) {
            return false;
        }
        EDNSOption opt = (EDNSOption) arg;
        if (this.code == opt.code) {
            return Arrays.equals(getData(), opt.getData());
        }
        return false;
    }

    public int hashCode() {
        int hashval = 0;
        for (byte b : getData()) {
            hashval += (hashval << 3) + (b & KEYRecord.PROTOCOL_ANY);
        }
        return hashval;
    }
}
