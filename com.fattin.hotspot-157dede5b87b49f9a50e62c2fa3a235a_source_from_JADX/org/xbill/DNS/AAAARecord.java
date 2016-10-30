package org.xbill.DNS;

import java.io.IOException;
import java.net.InetAddress;

public class AAAARecord extends Record {
    private static final long serialVersionUID = -4588601512069748050L;
    private InetAddress address;

    AAAARecord() {
    }

    Record getObject() {
        return new AAAARecord();
    }

    public AAAARecord(Name name, int dclass, long ttl, InetAddress address) {
        super(name, 28, dclass, ttl);
        if (Address.familyOf(address) != 2) {
            throw new IllegalArgumentException("invalid IPv6 address");
        }
        this.address = address;
    }

    void rrFromWire(DNSInput in) throws IOException {
        this.address = InetAddress.getByAddress(in.readByteArray(16));
    }

    void rdataFromString(Tokenizer st, Name origin) throws IOException {
        this.address = st.getAddress(2);
    }

    String rrToString() {
        return this.address.getHostAddress();
    }

    public InetAddress getAddress() {
        return this.address;
    }

    void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        out.writeByteArray(this.address.getAddress());
    }
}
