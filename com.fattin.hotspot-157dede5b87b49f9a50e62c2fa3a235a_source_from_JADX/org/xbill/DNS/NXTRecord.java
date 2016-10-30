package org.xbill.DNS;

import java.io.IOException;
import java.util.BitSet;

public class NXTRecord extends Record {
    private static final long serialVersionUID = -8851454400765507520L;
    private BitSet bitmap;
    private Name next;

    NXTRecord() {
    }

    Record getObject() {
        return new NXTRecord();
    }

    public NXTRecord(Name name, int dclass, long ttl, Name next, BitSet bitmap) {
        super(name, 30, dclass, ttl);
        this.next = Record.checkName("next", next);
        this.bitmap = bitmap;
    }

    void rrFromWire(DNSInput in) throws IOException {
        this.next = new Name(in);
        this.bitmap = new BitSet();
        int bitmapLength = in.remaining();
        for (int i = 0; i < bitmapLength; i++) {
            int t = in.readU8();
            for (int j = 0; j < 8; j++) {
                if (((1 << (7 - j)) & t) != 0) {
                    this.bitmap.set((i * 8) + j);
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void rdataFromString(org.xbill.DNS.Tokenizer r5, org.xbill.DNS.Name r6) throws java.io.IOException {
        /*
        r4 = this;
        r2 = r5.getName(r6);
        r4.next = r2;
        r2 = new java.util.BitSet;
        r2.<init>();
        r4.bitmap = r2;
    L_0x000d:
        r0 = r5.get();
        r2 = r0.isString();
        if (r2 != 0) goto L_0x001b;
    L_0x0017:
        r5.unget();
        return;
    L_0x001b:
        r2 = r0.value;
        r3 = 1;
        r1 = org.xbill.DNS.Type.value(r2, r3);
        if (r1 <= 0) goto L_0x0028;
    L_0x0024:
        r2 = 128; // 0x80 float:1.794E-43 double:6.32E-322;
        if (r1 <= r2) goto L_0x0042;
    L_0x0028:
        r2 = new java.lang.StringBuffer;
        r2.<init>();
        r3 = "Invalid type: ";
        r2 = r2.append(r3);
        r3 = r0.value;
        r2 = r2.append(r3);
        r2 = r2.toString();
        r2 = r5.exception(r2);
        throw r2;
    L_0x0042:
        r2 = r4.bitmap;
        r2.set(r1);
        goto L_0x000d;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.xbill.DNS.NXTRecord.rdataFromString(org.xbill.DNS.Tokenizer, org.xbill.DNS.Name):void");
    }

    String rrToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.next);
        short length = this.bitmap.length();
        for (short i = (short) 0; i < length; i = (short) (i + 1)) {
            if (this.bitmap.get(i)) {
                sb.append(" ");
                sb.append(Type.string(i));
            }
        }
        return sb.toString();
    }

    public Name getNext() {
        return this.next;
    }

    public BitSet getBitmap() {
        return this.bitmap;
    }

    void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        this.next.toWire(out, null, canonical);
        int length = this.bitmap.length();
        int i = 0;
        int t = 0;
        while (i < length) {
            t |= this.bitmap.get(i) ? 1 << (7 - (i % 8)) : 0;
            if (i % 8 == 7 || i == length - 1) {
                out.writeU8(t);
                t = 0;
            }
            i++;
        }
    }
}
