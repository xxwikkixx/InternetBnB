package org.xbill.DNS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Generator {
    private long current;
    public final int dclass;
    public long end;
    public final String namePattern;
    public final Name origin;
    public final String rdataPattern;
    public long start;
    public long step;
    public final long ttl;
    public final int type;

    public static boolean supportedType(int type) {
        Type.check(type);
        if (type == 12 || type == 5 || type == 39 || type == 1 || type == 28 || type == 2) {
            return true;
        }
        return false;
    }

    public Generator(long start, long end, long step, String namePattern, int type, int dclass, long ttl, String rdataPattern, Name origin) {
        if (start < 0 || end < 0 || start > end || step <= 0) {
            throw new IllegalArgumentException("invalid range specification");
        } else if (supportedType(type)) {
            DClass.check(dclass);
            this.start = start;
            this.end = end;
            this.step = step;
            this.namePattern = namePattern;
            this.type = type;
            this.dclass = dclass;
            this.ttl = ttl;
            this.rdataPattern = rdataPattern;
            this.origin = origin;
            this.current = start;
        } else {
            throw new IllegalArgumentException("unsupported type");
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.lang.String substitute(java.lang.String r23, long r24) throws java.io.IOException {
        /*
        r22 = this;
        r5 = 0;
        r12 = r23.getBytes();
        r11 = new java.lang.StringBuffer;
        r11.<init>();
        r6 = 0;
    L_0x000b:
        r0 = r12.length;
        r20 = r0;
        r0 = r20;
        if (r6 >= r0) goto L_0x021b;
    L_0x0012:
        r20 = r12[r6];
        r0 = r20;
        r0 = r0 & 255;
        r20 = r0;
        r0 = r20;
        r4 = (char) r0;
        if (r5 == 0) goto L_0x0026;
    L_0x001f:
        r11.append(r4);
        r5 = 0;
    L_0x0023:
        r6 = r6 + 1;
        goto L_0x000b;
    L_0x0026:
        r20 = 92;
        r0 = r20;
        if (r4 != r0) goto L_0x0041;
    L_0x002c:
        r20 = r6 + 1;
        r0 = r12.length;
        r21 = r0;
        r0 = r20;
        r1 = r21;
        if (r0 != r1) goto L_0x003f;
    L_0x0037:
        r20 = new org.xbill.DNS.TextParseException;
        r21 = "invalid escape character";
        r20.<init>(r21);
        throw r20;
    L_0x003f:
        r5 = 1;
        goto L_0x0023;
    L_0x0041:
        r20 = 36;
        r0 = r20;
        if (r4 != r0) goto L_0x0216;
    L_0x0047:
        r7 = 0;
        r9 = 0;
        r16 = 0;
        r2 = 10;
        r15 = 0;
        r20 = r6 + 1;
        r0 = r12.length;
        r21 = r0;
        r0 = r20;
        r1 = r21;
        if (r0 >= r1) goto L_0x0077;
    L_0x005a:
        r20 = r6 + 1;
        r20 = r12[r20];
        r21 = 36;
        r0 = r20;
        r1 = r21;
        if (r0 != r1) goto L_0x0077;
    L_0x0066:
        r6 = r6 + 1;
        r20 = r12[r6];
        r0 = r20;
        r0 = r0 & 255;
        r20 = r0;
        r0 = r20;
        r4 = (char) r0;
        r11.append(r4);
        goto L_0x0023;
    L_0x0077:
        r20 = r6 + 1;
        r0 = r12.length;
        r21 = r0;
        r0 = r20;
        r1 = r21;
        if (r0 >= r1) goto L_0x01b3;
    L_0x0082:
        r20 = r6 + 1;
        r20 = r12[r20];
        r21 = 123; // 0x7b float:1.72E-43 double:6.1E-322;
        r0 = r20;
        r1 = r21;
        if (r0 != r1) goto L_0x01b3;
    L_0x008e:
        r6 = r6 + 1;
        r20 = r6 + 1;
        r0 = r12.length;
        r21 = r0;
        r0 = r20;
        r1 = r21;
        if (r0 >= r1) goto L_0x00aa;
    L_0x009b:
        r20 = r6 + 1;
        r20 = r12[r20];
        r21 = 45;
        r0 = r20;
        r1 = r21;
        if (r0 != r1) goto L_0x00aa;
    L_0x00a7:
        r7 = 1;
        r6 = r6 + 1;
    L_0x00aa:
        r20 = r6 + 1;
        r0 = r12.length;
        r21 = r0;
        r0 = r20;
        r1 = r21;
        if (r0 >= r1) goto L_0x00ce;
    L_0x00b5:
        r6 = r6 + 1;
        r20 = r12[r6];
        r0 = r20;
        r0 = r0 & 255;
        r20 = r0;
        r0 = r20;
        r4 = (char) r0;
        r20 = 44;
        r0 = r20;
        if (r4 == r0) goto L_0x00ce;
    L_0x00c8:
        r20 = 125; // 0x7d float:1.75E-43 double:6.2E-322;
        r0 = r20;
        if (r4 != r0) goto L_0x0114;
    L_0x00ce:
        if (r7 == 0) goto L_0x00d1;
    L_0x00d0:
        r9 = -r9;
    L_0x00d1:
        r20 = 44;
        r0 = r20;
        if (r4 != r0) goto L_0x00fb;
    L_0x00d7:
        r20 = r6 + 1;
        r0 = r12.length;
        r21 = r0;
        r0 = r20;
        r1 = r21;
        if (r0 >= r1) goto L_0x00fb;
    L_0x00e2:
        r6 = r6 + 1;
        r20 = r12[r6];
        r0 = r20;
        r0 = r0 & 255;
        r20 = r0;
        r0 = r20;
        r4 = (char) r0;
        r20 = 44;
        r0 = r20;
        if (r4 == r0) goto L_0x00fb;
    L_0x00f5:
        r20 = 125; // 0x7d float:1.75E-43 double:6.2E-322;
        r0 = r20;
        if (r4 != r0) goto L_0x0138;
    L_0x00fb:
        r20 = 44;
        r0 = r20;
        if (r4 != r0) goto L_0x0171;
    L_0x0101:
        r20 = r6 + 1;
        r0 = r12.length;
        r21 = r0;
        r0 = r20;
        r1 = r21;
        if (r0 != r1) goto L_0x015c;
    L_0x010c:
        r20 = new org.xbill.DNS.TextParseException;
        r21 = "invalid base";
        r20.<init>(r21);
        throw r20;
    L_0x0114:
        r20 = 48;
        r0 = r20;
        if (r4 < r0) goto L_0x0120;
    L_0x011a:
        r20 = 57;
        r0 = r20;
        if (r4 <= r0) goto L_0x0128;
    L_0x0120:
        r20 = new org.xbill.DNS.TextParseException;
        r21 = "invalid offset";
        r20.<init>(r21);
        throw r20;
    L_0x0128:
        r20 = r4 + -48;
        r0 = r20;
        r4 = (char) r0;
        r20 = 10;
        r9 = r9 * r20;
        r0 = (long) r4;
        r20 = r0;
        r9 = r9 + r20;
        goto L_0x00aa;
    L_0x0138:
        r20 = 48;
        r0 = r20;
        if (r4 < r0) goto L_0x0144;
    L_0x013e:
        r20 = 57;
        r0 = r20;
        if (r4 <= r0) goto L_0x014c;
    L_0x0144:
        r20 = new org.xbill.DNS.TextParseException;
        r21 = "invalid width";
        r20.<init>(r21);
        throw r20;
    L_0x014c:
        r20 = r4 + -48;
        r0 = r20;
        r4 = (char) r0;
        r20 = 10;
        r16 = r16 * r20;
        r0 = (long) r4;
        r20 = r0;
        r16 = r16 + r20;
        goto L_0x00d7;
    L_0x015c:
        r6 = r6 + 1;
        r20 = r12[r6];
        r0 = r20;
        r0 = r0 & 255;
        r20 = r0;
        r0 = r20;
        r4 = (char) r0;
        r20 = 111; // 0x6f float:1.56E-43 double:5.5E-322;
        r0 = r20;
        if (r4 != r0) goto L_0x0190;
    L_0x016f:
        r2 = 8;
    L_0x0171:
        r20 = r6 + 1;
        r0 = r12.length;
        r21 = r0;
        r0 = r20;
        r1 = r21;
        if (r0 == r1) goto L_0x0188;
    L_0x017c:
        r20 = r6 + 1;
        r20 = r12[r20];
        r21 = 125; // 0x7d float:1.75E-43 double:6.2E-322;
        r0 = r20;
        r1 = r21;
        if (r0 == r1) goto L_0x01b1;
    L_0x0188:
        r20 = new org.xbill.DNS.TextParseException;
        r21 = "invalid modifiers";
        r20.<init>(r21);
        throw r20;
    L_0x0190:
        r20 = 120; // 0x78 float:1.68E-43 double:5.93E-322;
        r0 = r20;
        if (r4 != r0) goto L_0x0199;
    L_0x0196:
        r2 = 16;
        goto L_0x0171;
    L_0x0199:
        r20 = 88;
        r0 = r20;
        if (r4 != r0) goto L_0x01a3;
    L_0x019f:
        r2 = 16;
        r15 = 1;
        goto L_0x0171;
    L_0x01a3:
        r20 = 100;
        r0 = r20;
        if (r4 == r0) goto L_0x0171;
    L_0x01a9:
        r20 = new org.xbill.DNS.TextParseException;
        r21 = "invalid base";
        r20.<init>(r21);
        throw r20;
    L_0x01b1:
        r6 = r6 + 1;
    L_0x01b3:
        r13 = r24 + r9;
        r20 = 0;
        r20 = (r13 > r20 ? 1 : (r13 == r20 ? 0 : -1));
        if (r20 >= 0) goto L_0x01c3;
    L_0x01bb:
        r20 = new org.xbill.DNS.TextParseException;
        r21 = "invalid offset expansion";
        r20.<init>(r21);
        throw r20;
    L_0x01c3:
        r20 = 8;
        r20 = (r2 > r20 ? 1 : (r2 == r20 ? 0 : -1));
        if (r20 != 0) goto L_0x0201;
    L_0x01c9:
        r8 = java.lang.Long.toOctalString(r13);
    L_0x01cd:
        if (r15 == 0) goto L_0x01d3;
    L_0x01cf:
        r8 = r8.toUpperCase();
    L_0x01d3:
        r20 = 0;
        r20 = (r16 > r20 ? 1 : (r16 == r20 ? 0 : -1));
        if (r20 == 0) goto L_0x0211;
    L_0x01d9:
        r20 = r8.length();
        r0 = r20;
        r0 = (long) r0;
        r20 = r0;
        r20 = (r16 > r20 ? 1 : (r16 == r20 ? 0 : -1));
        if (r20 <= 0) goto L_0x0211;
    L_0x01e6:
        r0 = r16;
        r0 = (int) r0;
        r20 = r0;
        r21 = r8.length();
        r18 = r20 - r21;
        r19 = r18;
    L_0x01f3:
        r18 = r19 + -1;
        if (r19 <= 0) goto L_0x0211;
    L_0x01f7:
        r20 = 48;
        r0 = r20;
        r11.append(r0);
        r19 = r18;
        goto L_0x01f3;
    L_0x0201:
        r20 = 16;
        r20 = (r2 > r20 ? 1 : (r2 == r20 ? 0 : -1));
        if (r20 != 0) goto L_0x020c;
    L_0x0207:
        r8 = java.lang.Long.toHexString(r13);
        goto L_0x01cd;
    L_0x020c:
        r8 = java.lang.Long.toString(r13);
        goto L_0x01cd;
    L_0x0211:
        r11.append(r8);
        goto L_0x0023;
    L_0x0216:
        r11.append(r4);
        goto L_0x0023;
    L_0x021b:
        r20 = r11.toString();
        return r20;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.xbill.DNS.Generator.substitute(java.lang.String, long):java.lang.String");
    }

    public Record nextRecord() throws IOException {
        if (this.current > this.end) {
            return null;
        }
        Name name = Name.fromString(substitute(this.namePattern, this.current), this.origin);
        String rdata = substitute(this.rdataPattern, this.current);
        this.current += this.step;
        return Record.fromString(name, this.type, this.dclass, this.ttl, rdata, this.origin);
    }

    public Record[] expand() throws IOException {
        List list = new ArrayList();
        long i = this.start;
        while (i < this.end) {
            list.add(Record.fromString(Name.fromString(substitute(this.namePattern, this.current), this.origin), this.type, this.dclass, this.ttl, substitute(this.rdataPattern, this.current), this.origin));
            i += this.step;
        }
        return (Record[]) list.toArray(new Record[list.size()]);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("$GENERATE ");
        sb.append(new StringBuffer().append(this.start).append("-").append(this.end).toString());
        if (this.step > 1) {
            sb.append(new StringBuffer().append("/").append(this.step).toString());
        }
        sb.append(" ");
        sb.append(new StringBuffer().append(this.namePattern).append(" ").toString());
        sb.append(new StringBuffer().append(this.ttl).append(" ").toString());
        if (!(this.dclass == 1 && Options.check("noPrintIN"))) {
            sb.append(new StringBuffer().append(DClass.string(this.dclass)).append(" ").toString());
        }
        sb.append(new StringBuffer().append(Type.string(this.type)).append(" ").toString());
        sb.append(new StringBuffer().append(this.rdataPattern).append(" ").toString());
        return sb.toString();
    }
}
