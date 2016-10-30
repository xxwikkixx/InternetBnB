package org.xbill.DNS;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.xbill.DNS.KEYRecord.Flags;
import org.xbill.DNS.utils.base16;

public class APLRecord extends Record {
    private static final long serialVersionUID = -1348173791712935864L;
    private List elements;

    static class 1 {
    }

    public static class Element {
        public final Object address;
        public final int family;
        public final boolean negative;
        public final int prefixLength;

        Element(int x0, boolean x1, Object x2, int x3, 1 x4) {
            this(x0, x1, x2, x3);
        }

        private Element(int family, boolean negative, Object address, int prefixLength) {
            this.family = family;
            this.negative = negative;
            this.address = address;
            this.prefixLength = prefixLength;
            if (!APLRecord.access$000(family, prefixLength)) {
                throw new IllegalArgumentException("invalid prefix length");
            }
        }

        public Element(boolean negative, InetAddress address, int prefixLength) {
            this(Address.familyOf(address), negative, address, prefixLength);
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            if (this.negative) {
                sb.append("!");
            }
            sb.append(this.family);
            sb.append(":");
            if (this.family == 1 || this.family == 2) {
                sb.append(((InetAddress) this.address).getHostAddress());
            } else {
                sb.append(base16.toString((byte[]) this.address));
            }
            sb.append("/");
            sb.append(this.prefixLength);
            return sb.toString();
        }

        public boolean equals(Object arg) {
            if (arg == null || !(arg instanceof Element)) {
                return false;
            }
            Element elt = (Element) arg;
            if (this.family == elt.family && this.negative == elt.negative && this.prefixLength == elt.prefixLength && this.address.equals(elt.address)) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return (this.negative ? 1 : 0) + (this.prefixLength + this.address.hashCode());
        }
    }

    static boolean access$000(int x0, int x1) {
        return validatePrefixLength(x0, x1);
    }

    APLRecord() {
    }

    Record getObject() {
        return new APLRecord();
    }

    private static boolean validatePrefixLength(int family, int prefixLength) {
        if (prefixLength < 0 || prefixLength >= KEYRecord.OWNER_ZONE) {
            return false;
        }
        if (family == 1 && prefixLength > 32) {
            return false;
        }
        if (family != 2 || prefixLength <= Flags.FLAG8) {
            return true;
        }
        return false;
    }

    public APLRecord(Name name, int dclass, long ttl, List elements) {
        super(name, 42, dclass, ttl);
        this.elements = new ArrayList(elements.size());
        for (Element o : elements) {
            if (o instanceof Element) {
                Element element = o;
                if (element.family == 1 || element.family == 2) {
                    this.elements.add(element);
                } else {
                    throw new IllegalArgumentException("unknown family");
                }
            }
            throw new IllegalArgumentException("illegal element");
        }
    }

    private static byte[] parseAddress(byte[] in, int length) throws WireParseException {
        if (in.length > length) {
            throw new WireParseException("invalid address length");
        } else if (in.length == length) {
            return in;
        } else {
            byte[] out = new byte[length];
            System.arraycopy(in, 0, out, 0, in.length);
            return out;
        }
    }

    void rrFromWire(DNSInput in) throws IOException {
        this.elements = new ArrayList(1);
        while (in.remaining() != 0) {
            int family = in.readU16();
            int prefix = in.readU8();
            int length = in.readU8();
            boolean negative = (length & Flags.FLAG8) != 0;
            byte[] data = in.readByteArray(length & -129);
            if (validatePrefixLength(family, prefix)) {
                Element element;
                if (family == 1 || family == 2) {
                    element = new Element(negative, InetAddress.getByAddress(parseAddress(data, Address.addressLength(family))), prefix);
                } else {
                    element = new Element(family, negative, data, prefix, null);
                }
                this.elements.add(element);
            } else {
                throw new WireParseException("invalid prefix length");
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void rdataFromString(org.xbill.DNS.Tokenizer r18, org.xbill.DNS.Name r19) throws java.io.IOException {
        /*
        r17 = this;
        r15 = new java.util.ArrayList;
        r16 = 1;
        r15.<init>(r16);
        r0 = r17;
        r0.elements = r15;
    L_0x000b:
        r14 = r18.get();
        r15 = r14.isString();
        if (r15 != 0) goto L_0x0019;
    L_0x0015:
        r18.unget();
        return;
    L_0x0019:
        r8 = 0;
        r6 = 0;
        r9 = 0;
        r11 = r14.value;
        r13 = 0;
        r15 = "!";
        r15 = r11.startsWith(r15);
        if (r15 == 0) goto L_0x0029;
    L_0x0027:
        r8 = 1;
        r13 = 1;
    L_0x0029:
        r15 = 58;
        r4 = r11.indexOf(r15, r13);
        if (r4 >= 0) goto L_0x003a;
    L_0x0031:
        r15 = "invalid address prefix element";
        r0 = r18;
        r15 = r0.exception(r15);
        throw r15;
    L_0x003a:
        r15 = 47;
        r12 = r11.indexOf(r15, r4);
        if (r12 >= 0) goto L_0x004b;
    L_0x0042:
        r15 = "invalid address prefix element";
        r0 = r18;
        r15 = r0.exception(r15);
        throw r15;
    L_0x004b:
        r7 = r11.substring(r13, r4);
        r15 = r4 + 1;
        r2 = r11.substring(r15, r12);
        r15 = r12 + 1;
        r10 = r11.substring(r15);
        r6 = java.lang.Integer.parseInt(r7);	 Catch:{ NumberFormatException -> 0x006e }
        r15 = 1;
        if (r6 == r15) goto L_0x0078;
    L_0x0062:
        r15 = 2;
        if (r6 == r15) goto L_0x0078;
    L_0x0065:
        r15 = "unknown family";
        r0 = r18;
        r15 = r0.exception(r15);
        throw r15;
    L_0x006e:
        r5 = move-exception;
        r15 = "invalid family";
        r0 = r18;
        r15 = r0.exception(r15);
        throw r15;
    L_0x0078:
        r9 = java.lang.Integer.parseInt(r10);	 Catch:{ NumberFormatException -> 0x008b }
        r15 = validatePrefixLength(r6, r9);
        if (r15 != 0) goto L_0x0095;
    L_0x0082:
        r15 = "invalid prefix length";
        r0 = r18;
        r15 = r0.exception(r15);
        throw r15;
    L_0x008b:
        r5 = move-exception;
        r15 = "invalid prefix length";
        r0 = r18;
        r15 = r0.exception(r15);
        throw r15;
    L_0x0095:
        r3 = org.xbill.DNS.Address.toByteArray(r2, r6);
        if (r3 != 0) goto L_0x00b5;
    L_0x009b:
        r15 = new java.lang.StringBuffer;
        r15.<init>();
        r16 = "invalid IP address ";
        r15 = r15.append(r16);
        r15 = r15.append(r2);
        r15 = r15.toString();
        r0 = r18;
        r15 = r0.exception(r15);
        throw r15;
    L_0x00b5:
        r1 = java.net.InetAddress.getByAddress(r3);
        r0 = r17;
        r15 = r0.elements;
        r16 = new org.xbill.DNS.APLRecord$Element;
        r0 = r16;
        r0.<init>(r8, r1, r9);
        r15.add(r16);
        goto L_0x000b;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.xbill.DNS.APLRecord.rdataFromString(org.xbill.DNS.Tokenizer, org.xbill.DNS.Name):void");
    }

    String rrToString() {
        StringBuffer sb = new StringBuffer();
        Iterator it = this.elements.iterator();
        while (it.hasNext()) {
            sb.append((Element) it.next());
            if (it.hasNext()) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    public List getElements() {
        return this.elements;
    }

    private static int addressLength(byte[] addr) {
        for (int i = addr.length - 1; i >= 0; i--) {
            if (addr[i] != null) {
                return i + 1;
            }
        }
        return 0;
    }

    void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        for (Element element : this.elements) {
            byte[] data;
            int length;
            if (element.family == 1 || element.family == 2) {
                data = element.address.getAddress();
                length = addressLength(data);
            } else {
                data = (byte[]) element.address;
                length = data.length;
            }
            int wlength = length;
            if (element.negative) {
                wlength |= Flags.FLAG8;
            }
            out.writeU16(element.family);
            out.writeU8(element.prefixLength);
            out.writeU8(wlength);
            out.writeByteArray(data, 0, length);
        }
    }
}
