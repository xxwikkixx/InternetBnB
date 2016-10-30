package org.xbill.DNS;

import java.io.IOException;
import org.xbill.DNS.Tokenizer.Token;

public class ISDNRecord extends Record {
    private static final long serialVersionUID = -8730801385178968798L;
    private byte[] address;
    private byte[] subAddress;

    ISDNRecord() {
    }

    Record getObject() {
        return new ISDNRecord();
    }

    public ISDNRecord(Name name, int dclass, long ttl, String address, String subAddress) {
        super(name, 20, dclass, ttl);
        try {
            this.address = Record.byteArrayFromString(address);
            if (subAddress != null) {
                this.subAddress = Record.byteArrayFromString(subAddress);
            }
        } catch (TextParseException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    void rrFromWire(DNSInput in) throws IOException {
        this.address = in.readCountedString();
        if (in.remaining() > 0) {
            this.subAddress = in.readCountedString();
        }
    }

    void rdataFromString(Tokenizer st, Name origin) throws IOException {
        try {
            this.address = Record.byteArrayFromString(st.getString());
            Token t = st.get();
            if (t.isString()) {
                this.subAddress = Record.byteArrayFromString(t.value);
            } else {
                st.unget();
            }
        } catch (TextParseException e) {
            throw st.exception(e.getMessage());
        }
    }

    public String getAddress() {
        return Record.byteArrayToString(this.address, false);
    }

    public String getSubAddress() {
        if (this.subAddress == null) {
            return null;
        }
        return Record.byteArrayToString(this.subAddress, false);
    }

    void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        out.writeCountedString(this.address);
        if (this.subAddress != null) {
            out.writeCountedString(this.subAddress);
        }
    }

    String rrToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(Record.byteArrayToString(this.address, true));
        if (this.subAddress != null) {
            sb.append(" ");
            sb.append(Record.byteArrayToString(this.subAddress, true));
        }
        return sb.toString();
    }
}
