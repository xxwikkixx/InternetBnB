package org.xbill.DNS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Message implements Cloneable {
    public static final int MAXLENGTH = 65535;
    static final int TSIG_FAILED = 4;
    static final int TSIG_INTERMEDIATE = 2;
    static final int TSIG_SIGNED = 3;
    static final int TSIG_UNSIGNED = 0;
    static final int TSIG_VERIFIED = 1;
    private static RRset[] emptyRRsetArray;
    private static Record[] emptyRecordArray;
    private Header header;
    private TSIGRecord querytsig;
    private List[] sections;
    int sig0start;
    private int size;
    int tsigState;
    private int tsigerror;
    private TSIG tsigkey;
    int tsigstart;

    static {
        emptyRecordArray = new Record[TSIG_UNSIGNED];
        emptyRRsetArray = new RRset[TSIG_UNSIGNED];
    }

    private Message(Header header) {
        this.sections = new List[TSIG_FAILED];
        this.header = header;
    }

    public Message(int id) {
        this(new Header(id));
    }

    public Message() {
        this(new Header());
    }

    public static Message newQuery(Record r) {
        Message m = new Message();
        m.header.setOpcode(TSIG_UNSIGNED);
        m.header.setFlag(7);
        m.addRecord(r, TSIG_UNSIGNED);
        return m;
    }

    public static Message newUpdate(Name zone) {
        return new Update(zone);
    }

    Message(DNSInput in) throws IOException {
        this(new Header(in));
        boolean isUpdate = this.header.getOpcode() == 5;
        boolean truncated = this.header.getFlag(6);
        int i = TSIG_UNSIGNED;
        while (i < TSIG_FAILED) {
            try {
                int count = this.header.getCount(i);
                if (count > 0) {
                    this.sections[i] = new ArrayList(count);
                }
                for (int j = TSIG_UNSIGNED; j < count; j += TSIG_VERIFIED) {
                    int pos = in.current();
                    Record rec = Record.fromWire(in, i, isUpdate);
                    this.sections[i].add(rec);
                    if (rec.getType() == Type.TSIG) {
                        this.tsigstart = pos;
                    }
                    if (rec.getType() == 24 && ((SIGRecord) rec).getTypeCovered() == 0) {
                        this.sig0start = pos;
                    }
                }
                i += TSIG_VERIFIED;
            } catch (WireParseException e) {
                if (!truncated) {
                    throw e;
                }
            }
        }
        this.size = in.current();
    }

    public Message(byte[] b) throws IOException {
        this(new DNSInput(b));
    }

    public void setHeader(Header h) {
        this.header = h;
    }

    public Header getHeader() {
        return this.header;
    }

    public void addRecord(Record r, int section) {
        if (this.sections[section] == null) {
            this.sections[section] = new LinkedList();
        }
        this.header.incCount(section);
        this.sections[section].add(r);
    }

    public boolean removeRecord(Record r, int section) {
        if (this.sections[section] == null || !this.sections[section].remove(r)) {
            return false;
        }
        this.header.decCount(section);
        return true;
    }

    public void removeAllRecords(int section) {
        this.sections[section] = null;
        this.header.setCount(section, TSIG_UNSIGNED);
    }

    public boolean findRecord(Record r, int section) {
        return this.sections[section] != null && this.sections[section].contains(r);
    }

    public boolean findRecord(Record r) {
        int i = TSIG_VERIFIED;
        while (i <= TSIG_SIGNED) {
            if (this.sections[i] != null && this.sections[i].contains(r)) {
                return true;
            }
            i += TSIG_VERIFIED;
        }
        return false;
    }

    public boolean findRRset(Name name, int type, int section) {
        if (this.sections[section] == null) {
            return false;
        }
        for (int i = TSIG_UNSIGNED; i < this.sections[section].size(); i += TSIG_VERIFIED) {
            Record r = (Record) this.sections[section].get(i);
            if (r.getType() == type && name.equals(r.getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean findRRset(Name name, int type) {
        return findRRset(name, type, TSIG_VERIFIED) || findRRset(name, type, TSIG_INTERMEDIATE) || findRRset(name, type, TSIG_SIGNED);
    }

    public Record getQuestion() {
        List l = this.sections[TSIG_UNSIGNED];
        if (l == null || l.size() == 0) {
            return null;
        }
        return (Record) l.get(TSIG_UNSIGNED);
    }

    public TSIGRecord getTSIG() {
        int count = this.header.getCount(TSIG_SIGNED);
        if (count == 0) {
            return null;
        }
        Record rec = (Record) this.sections[TSIG_SIGNED].get(count - 1);
        if (rec.type != Type.TSIG) {
            return null;
        }
        return (TSIGRecord) rec;
    }

    public boolean isSigned() {
        return this.tsigState == TSIG_SIGNED || this.tsigState == TSIG_VERIFIED || this.tsigState == TSIG_FAILED;
    }

    public boolean isVerified() {
        return this.tsigState == TSIG_VERIFIED;
    }

    public OPTRecord getOPT() {
        Record[] additional = getSectionArray(TSIG_SIGNED);
        for (int i = TSIG_UNSIGNED; i < additional.length; i += TSIG_VERIFIED) {
            if (additional[i] instanceof OPTRecord) {
                return (OPTRecord) additional[i];
            }
        }
        return null;
    }

    public int getRcode() {
        int rcode = this.header.getRcode();
        OPTRecord opt = getOPT();
        if (opt != null) {
            return rcode + (opt.getExtendedRcode() << TSIG_FAILED);
        }
        return rcode;
    }

    public Record[] getSectionArray(int section) {
        if (this.sections[section] == null) {
            return emptyRecordArray;
        }
        List l = this.sections[section];
        return (Record[]) l.toArray(new Record[l.size()]);
    }

    private static boolean sameSet(Record r1, Record r2) {
        return r1.getRRsetType() == r2.getRRsetType() && r1.getDClass() == r2.getDClass() && r1.getName().equals(r2.getName());
    }

    public RRset[] getSectionRRsets(int section) {
        if (this.sections[section] == null) {
            return emptyRRsetArray;
        }
        List sets = new LinkedList();
        Record[] recs = getSectionArray(section);
        Set hash = new HashSet();
        int i = TSIG_UNSIGNED;
        while (i < recs.length) {
            Name name = recs[i].getName();
            boolean newset = true;
            if (hash.contains(name)) {
                for (int j = sets.size() - 1; j >= 0; j--) {
                    RRset set = (RRset) sets.get(j);
                    if (set.getType() == recs[i].getRRsetType() && set.getDClass() == recs[i].getDClass() && set.getName().equals(name)) {
                        set.addRR(recs[i]);
                        newset = false;
                        break;
                    }
                }
            }
            if (newset) {
                sets.add(new RRset(recs[i]));
                hash.add(name);
            }
            i += TSIG_VERIFIED;
        }
        return (RRset[]) sets.toArray(new RRset[sets.size()]);
    }

    void toWire(DNSOutput out) {
        this.header.toWire(out);
        Compression c = new Compression();
        for (int i = TSIG_UNSIGNED; i < TSIG_FAILED; i += TSIG_VERIFIED) {
            if (this.sections[i] != null) {
                for (int j = TSIG_UNSIGNED; j < this.sections[i].size(); j += TSIG_VERIFIED) {
                    ((Record) this.sections[i].get(j)).toWire(out, i, c);
                }
            }
        }
    }

    private int sectionToWire(DNSOutput out, int section, Compression c, int maxLength) {
        int n = this.sections[section].size();
        int pos = out.current();
        int rendered = TSIG_UNSIGNED;
        Record lastrec = null;
        for (int i = TSIG_UNSIGNED; i < n; i += TSIG_VERIFIED) {
            Record rec = (Record) this.sections[section].get(i);
            if (!(lastrec == null || sameSet(rec, lastrec))) {
                pos = out.current();
                rendered = i;
            }
            lastrec = rec;
            rec.toWire(out, section, c);
            if (out.current() > maxLength) {
                out.jump(pos);
                return n - rendered;
            }
        }
        return TSIG_UNSIGNED;
    }

    private boolean toWire(DNSOutput out, int maxLength) {
        if (maxLength < 12) {
            return false;
        }
        Header newheader = null;
        int tempMaxLength = maxLength;
        if (this.tsigkey != null) {
            tempMaxLength -= this.tsigkey.recordLength();
        }
        int startpos = out.current();
        this.header.toWire(out);
        Compression c = new Compression();
        for (int i = TSIG_UNSIGNED; i < TSIG_FAILED; i += TSIG_VERIFIED) {
            if (this.sections[i] != null) {
                int skipped = sectionToWire(out, i, c, tempMaxLength);
                if (skipped != 0) {
                    if (TSIG_UNSIGNED == null) {
                        newheader = (Header) this.header.clone();
                    }
                    if (i != TSIG_SIGNED) {
                        newheader.setFlag(6);
                    }
                    newheader.setCount(i, newheader.getCount(i) - skipped);
                    for (int j = i + TSIG_VERIFIED; j < TSIG_FAILED; j += TSIG_VERIFIED) {
                        newheader.setCount(j, TSIG_UNSIGNED);
                    }
                    out.save();
                    out.jump(startpos);
                    newheader.toWire(out);
                    out.restore();
                    if (this.tsigkey != null) {
                        TSIGRecord tsigrec = this.tsigkey.generate(this, out.toByteArray(), this.tsigerror, this.querytsig);
                        if (newheader == null) {
                            newheader = (Header) this.header.clone();
                        }
                        tsigrec.toWire(out, TSIG_SIGNED, c);
                        newheader.incCount(TSIG_SIGNED);
                        out.save();
                        out.jump(startpos);
                        newheader.toWire(out);
                        out.restore();
                    }
                    return true;
                }
            }
        }
        if (this.tsigkey != null) {
            TSIGRecord tsigrec2 = this.tsigkey.generate(this, out.toByteArray(), this.tsigerror, this.querytsig);
            if (newheader == null) {
                newheader = (Header) this.header.clone();
            }
            tsigrec2.toWire(out, TSIG_SIGNED, c);
            newheader.incCount(TSIG_SIGNED);
            out.save();
            out.jump(startpos);
            newheader.toWire(out);
            out.restore();
        }
        return true;
    }

    public byte[] toWire() {
        DNSOutput out = new DNSOutput();
        toWire(out);
        this.size = out.current();
        return out.toByteArray();
    }

    public byte[] toWire(int maxLength) {
        DNSOutput out = new DNSOutput();
        toWire(out, maxLength);
        this.size = out.current();
        return out.toByteArray();
    }

    public void setTSIG(TSIG key, int error, TSIGRecord querytsig) {
        this.tsigkey = key;
        this.tsigerror = error;
        this.querytsig = querytsig;
    }

    public int numBytes() {
        return this.size;
    }

    public String sectionToString(int i) {
        if (i > TSIG_SIGNED) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        Record[] records = getSectionArray(i);
        for (int j = TSIG_UNSIGNED; j < records.length; j += TSIG_VERIFIED) {
            Record rec = records[j];
            if (i == 0) {
                sb.append(new StringBuffer().append(";;\t").append(rec.name).toString());
                sb.append(new StringBuffer().append(", type = ").append(Type.string(rec.type)).toString());
                sb.append(new StringBuffer().append(", class = ").append(DClass.string(rec.dclass)).toString());
            } else {
                sb.append(rec);
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (getOPT() != null) {
            sb.append(new StringBuffer().append(this.header.toStringWithRcode(getRcode())).append("\n").toString());
        } else {
            sb.append(new StringBuffer().append(this.header).append("\n").toString());
        }
        if (isSigned()) {
            sb.append(";; TSIG ");
            if (isVerified()) {
                sb.append("ok");
            } else {
                sb.append("invalid");
            }
            sb.append('\n');
        }
        for (int i = TSIG_UNSIGNED; i < TSIG_FAILED; i += TSIG_VERIFIED) {
            if (this.header.getOpcode() != 5) {
                sb.append(new StringBuffer().append(";; ").append(Section.longString(i)).append(":\n").toString());
            } else {
                sb.append(new StringBuffer().append(";; ").append(Section.updString(i)).append(":\n").toString());
            }
            sb.append(new StringBuffer().append(sectionToString(i)).append("\n").toString());
        }
        sb.append(new StringBuffer().append(";; Message size: ").append(numBytes()).append(" bytes").toString());
        return sb.toString();
    }

    public Object clone() {
        Message m = new Message();
        for (int i = TSIG_UNSIGNED; i < this.sections.length; i += TSIG_VERIFIED) {
            if (this.sections[i] != null) {
                m.sections[i] = new LinkedList(this.sections[i]);
            }
        }
        m.header = (Header) this.header.clone();
        m.size = this.size;
        return m;
    }
}
