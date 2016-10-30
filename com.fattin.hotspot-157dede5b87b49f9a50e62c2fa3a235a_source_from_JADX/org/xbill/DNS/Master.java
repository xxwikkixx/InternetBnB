package org.xbill.DNS;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Master {
    private int currentDClass;
    private long currentTTL;
    private int currentType;
    private long defaultTTL;
    private File file;
    private Generator generator;
    private List generators;
    private Master included;
    private Record last;
    private boolean needSOATTL;
    private boolean noExpandGenerate;
    private Name origin;
    private Tokenizer st;

    Master(File file, Name origin, long initialTTL) throws IOException {
        this.last = null;
        this.included = null;
        if (origin == null || origin.isAbsolute()) {
            this.file = file;
            this.st = new Tokenizer(file);
            this.origin = origin;
            this.defaultTTL = initialTTL;
            return;
        }
        throw new RelativeNameException(origin);
    }

    public Master(String filename, Name origin, long ttl) throws IOException {
        this(new File(filename), origin, ttl);
    }

    public Master(String filename, Name origin) throws IOException {
        this(new File(filename), origin, -1);
    }

    public Master(String filename) throws IOException {
        this(new File(filename), null, -1);
    }

    public Master(InputStream in, Name origin, long ttl) {
        this.last = null;
        this.included = null;
        if (origin == null || origin.isAbsolute()) {
            this.st = new Tokenizer(in);
            this.origin = origin;
            this.defaultTTL = ttl;
            return;
        }
        throw new RelativeNameException(origin);
    }

    public Master(InputStream in, Name origin) {
        this(in, origin, -1);
    }

    public Master(InputStream in) {
        this(in, null, -1);
    }

    private Name parseName(String s, Name origin) throws TextParseException {
        try {
            return Name.fromString(s, origin);
        } catch (TextParseException e) {
            throw this.st.exception(e.getMessage());
        }
    }

    private void parseTTLClassAndType() throws IOException {
        boolean seen_class = false;
        String s = this.st.getString();
        int value = DClass.value(s);
        this.currentDClass = value;
        if (value >= 0) {
            s = this.st.getString();
            seen_class = true;
        }
        this.currentTTL = -1;
        try {
            this.currentTTL = TTL.parseTTL(s);
            s = this.st.getString();
        } catch (NumberFormatException e) {
            if (this.defaultTTL >= 0) {
                this.currentTTL = this.defaultTTL;
            } else if (this.last != null) {
                this.currentTTL = this.last.getTTL();
            }
        }
        if (!seen_class) {
            value = DClass.value(s);
            this.currentDClass = value;
            if (value >= 0) {
                s = this.st.getString();
            } else {
                this.currentDClass = 1;
            }
        }
        value = Type.value(s);
        this.currentType = value;
        if (value < 0) {
            throw this.st.exception(new StringBuffer().append("Invalid type '").append(s).append("'").toString());
        } else if (this.currentTTL >= 0) {
        } else {
            if (this.currentType != 6) {
                throw this.st.exception("missing TTL");
            }
            this.needSOATTL = true;
            this.currentTTL = 0;
        }
    }

    private long parseUInt32(String s) {
        if (!Character.isDigit(s.charAt(0))) {
            return -1;
        }
        try {
            long l = Long.parseLong(s);
            if (l < 0 || l > 4294967295L) {
                return -1;
            }
            return l;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void startGenerate() throws IOException {
        String s = this.st.getIdentifier();
        int n = s.indexOf("-");
        if (n < 0) {
            throw this.st.exception(new StringBuffer().append("Invalid $GENERATE range specifier: ").append(s).toString());
        }
        long step;
        String startstr = s.substring(0, n);
        String endstr = s.substring(n + 1);
        String stepstr = null;
        n = endstr.indexOf("/");
        if (n >= 0) {
            stepstr = endstr.substring(n + 1);
            endstr = endstr.substring(0, n);
        }
        long start = parseUInt32(startstr);
        long end = parseUInt32(endstr);
        if (stepstr != null) {
            step = parseUInt32(stepstr);
        } else {
            step = 1;
        }
        if (start < 0 || end < 0 || start > end || step <= 0) {
            throw this.st.exception(new StringBuffer().append("Invalid $GENERATE range specifier: ").append(s).toString());
        }
        String nameSpec = this.st.getIdentifier();
        parseTTLClassAndType();
        if (Generator.supportedType(this.currentType)) {
            String rdataSpec = this.st.getIdentifier();
            this.st.getEOL();
            this.st.unget();
            this.generator = new Generator(start, end, step, nameSpec, this.currentType, this.currentDClass, this.currentTTL, rdataSpec, this.origin);
            if (this.generators == null) {
                this.generators = new ArrayList(1);
            }
            this.generators.add(this.generator);
            return;
        }
        throw this.st.exception(new StringBuffer().append("$GENERATE does not support ").append(Type.string(this.currentType)).append(" records").toString());
    }

    private void endGenerate() throws IOException {
        this.st.getEOL();
        this.generator = null;
    }

    private Record nextGenerated() throws IOException {
        try {
            return this.generator.nextRecord();
        } catch (TokenizerException e) {
            throw this.st.exception(new StringBuffer().append("Parsing $GENERATE: ").append(e.getBaseMessage()).toString());
        } catch (TextParseException e2) {
            throw this.st.exception(new StringBuffer().append("Parsing $GENERATE: ").append(e2.getMessage()).toString());
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public org.xbill.DNS.Record _nextRecord() throws java.io.IOException {
        /*
        r20 = this;
        r0 = r20;
        r4 = r0.included;
        if (r4 == 0) goto L_0x0016;
    L_0x0006:
        r0 = r20;
        r4 = r0.included;
        r15 = r4.nextRecord();
        if (r15 == 0) goto L_0x0011;
    L_0x0010:
        return r15;
    L_0x0011:
        r4 = 0;
        r0 = r20;
        r0.included = r4;
    L_0x0016:
        r0 = r20;
        r4 = r0.generator;
        if (r4 == 0) goto L_0x0025;
    L_0x001c:
        r15 = r20.nextGenerated();
        if (r15 != 0) goto L_0x0010;
    L_0x0022:
        r20.endGenerate();
    L_0x0025:
        r0 = r20;
        r4 = r0.st;
        r5 = 1;
        r6 = 0;
        r17 = r4.get(r5, r6);
        r0 = r17;
        r4 = r0.type;
        r5 = 2;
        if (r4 != r5) goto L_0x00b2;
    L_0x0036:
        r0 = r20;
        r4 = r0.st;
        r13 = r4.get();
        r4 = r13.type;
        r5 = 1;
        if (r4 == r5) goto L_0x0025;
    L_0x0043:
        r4 = r13.type;
        if (r4 != 0) goto L_0x0049;
    L_0x0047:
        r15 = 0;
        goto L_0x0010;
    L_0x0049:
        r0 = r20;
        r4 = r0.st;
        r4.unget();
        r0 = r20;
        r4 = r0.last;
        if (r4 != 0) goto L_0x0061;
    L_0x0056:
        r0 = r20;
        r4 = r0.st;
        r5 = "no owner";
        r4 = r4.exception(r5);
        throw r4;
    L_0x0061:
        r0 = r20;
        r4 = r0.last;
        r3 = r4.getName();
    L_0x0069:
        r20.parseTTLClassAndType();
        r0 = r20;
        r4 = r0.currentType;
        r0 = r20;
        r5 = r0.currentDClass;
        r0 = r20;
        r6 = r0.currentTTL;
        r0 = r20;
        r8 = r0.st;
        r0 = r20;
        r9 = r0.origin;
        r4 = org.xbill.DNS.Record.fromString(r3, r4, r5, r6, r8, r9);
        r0 = r20;
        r0.last = r4;
        r0 = r20;
        r4 = r0.needSOATTL;
        if (r4 == 0) goto L_0x00ac;
    L_0x008e:
        r0 = r20;
        r4 = r0.last;
        r4 = (org.xbill.DNS.SOARecord) r4;
        r18 = r4.getMinimum();
        r0 = r20;
        r4 = r0.last;
        r0 = r18;
        r4.setTTL(r0);
        r0 = r18;
        r2 = r20;
        r2.defaultTTL = r0;
        r4 = 0;
        r0 = r20;
        r0.needSOATTL = r4;
    L_0x00ac:
        r0 = r20;
        r15 = r0.last;
        goto L_0x0010;
    L_0x00b2:
        r0 = r17;
        r4 = r0.type;
        r5 = 1;
        if (r4 == r5) goto L_0x0025;
    L_0x00b9:
        r0 = r17;
        r4 = r0.type;
        if (r4 != 0) goto L_0x00c2;
    L_0x00bf:
        r15 = 0;
        goto L_0x0010;
    L_0x00c2:
        r0 = r17;
        r4 = r0.value;
        r5 = 0;
        r4 = r4.charAt(r5);
        r5 = 36;
        if (r4 != r5) goto L_0x01c2;
    L_0x00cf:
        r0 = r17;
        r0 = r0.value;
        r16 = r0;
        r4 = "$ORIGIN";
        r0 = r16;
        r4 = r0.equalsIgnoreCase(r4);
        if (r4 == 0) goto L_0x00f6;
    L_0x00df:
        r0 = r20;
        r4 = r0.st;
        r5 = org.xbill.DNS.Name.root;
        r4 = r4.getName(r5);
        r0 = r20;
        r0.origin = r4;
        r0 = r20;
        r4 = r0.st;
        r4.getEOL();
        goto L_0x0025;
    L_0x00f6:
        r4 = "$TTL";
        r0 = r16;
        r4 = r0.equalsIgnoreCase(r4);
        if (r4 == 0) goto L_0x0115;
    L_0x0100:
        r0 = r20;
        r4 = r0.st;
        r4 = r4.getTTL();
        r0 = r20;
        r0.defaultTTL = r4;
        r0 = r20;
        r4 = r0.st;
        r4.getEOL();
        goto L_0x0025;
    L_0x0115:
        r4 = "$INCLUDE";
        r0 = r16;
        r4 = r0.equalsIgnoreCase(r4);
        if (r4 == 0) goto L_0x0178;
    L_0x011f:
        r0 = r20;
        r4 = r0.st;
        r10 = r4.getString();
        r0 = r20;
        r4 = r0.file;
        if (r4 == 0) goto L_0x0172;
    L_0x012d:
        r0 = r20;
        r4 = r0.file;
        r14 = r4.getParent();
        r12 = new java.io.File;
        r12.<init>(r14, r10);
    L_0x013a:
        r0 = r20;
        r11 = r0.origin;
        r0 = r20;
        r4 = r0.st;
        r17 = r4.get();
        r4 = r17.isString();
        if (r4 == 0) goto L_0x015f;
    L_0x014c:
        r0 = r17;
        r4 = r0.value;
        r5 = org.xbill.DNS.Name.root;
        r0 = r20;
        r11 = r0.parseName(r4, r5);
        r0 = r20;
        r4 = r0.st;
        r4.getEOL();
    L_0x015f:
        r4 = new org.xbill.DNS.Master;
        r0 = r20;
        r5 = r0.defaultTTL;
        r4.<init>(r12, r11, r5);
        r0 = r20;
        r0.included = r4;
        r15 = r20.nextRecord();
        goto L_0x0010;
    L_0x0172:
        r12 = new java.io.File;
        r12.<init>(r10);
        goto L_0x013a;
    L_0x0178:
        r4 = "$GENERATE";
        r0 = r16;
        r4 = r0.equalsIgnoreCase(r4);
        if (r4 == 0) goto L_0x01a4;
    L_0x0182:
        r0 = r20;
        r4 = r0.generator;
        if (r4 == 0) goto L_0x0190;
    L_0x0188:
        r4 = new java.lang.IllegalStateException;
        r5 = "cannot nest $GENERATE";
        r4.<init>(r5);
        throw r4;
    L_0x0190:
        r20.startGenerate();
        r0 = r20;
        r4 = r0.noExpandGenerate;
        if (r4 == 0) goto L_0x019e;
    L_0x0199:
        r20.endGenerate();
        goto L_0x0025;
    L_0x019e:
        r15 = r20.nextGenerated();
        goto L_0x0010;
    L_0x01a4:
        r0 = r20;
        r4 = r0.st;
        r5 = new java.lang.StringBuffer;
        r5.<init>();
        r6 = "Invalid directive: ";
        r5 = r5.append(r6);
        r0 = r16;
        r5 = r5.append(r0);
        r5 = r5.toString();
        r4 = r4.exception(r5);
        throw r4;
    L_0x01c2:
        r0 = r17;
        r0 = r0.value;
        r16 = r0;
        r0 = r20;
        r4 = r0.origin;
        r0 = r20;
        r1 = r16;
        r3 = r0.parseName(r1, r4);
        r0 = r20;
        r4 = r0.last;
        if (r4 == 0) goto L_0x0069;
    L_0x01da:
        r0 = r20;
        r4 = r0.last;
        r4 = r4.getName();
        r4 = r3.equals(r4);
        if (r4 == 0) goto L_0x0069;
    L_0x01e8:
        r0 = r20;
        r4 = r0.last;
        r3 = r4.getName();
        goto L_0x0069;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.xbill.DNS.Master._nextRecord():org.xbill.DNS.Record");
    }

    public Record nextRecord() throws IOException {
        Record rec = null;
        try {
            rec = _nextRecord();
            return rec;
        } finally {
            if (rec == null) {
                this.st.close();
            }
        }
    }

    public void expandGenerate(boolean wantExpand) {
        this.noExpandGenerate = !wantExpand;
    }

    public Iterator generators() {
        if (this.generators != null) {
            return Collections.unmodifiableList(this.generators).iterator();
        }
        return Collections.EMPTY_LIST.iterator();
    }

    protected void finalize() {
        this.st.close();
    }
}
