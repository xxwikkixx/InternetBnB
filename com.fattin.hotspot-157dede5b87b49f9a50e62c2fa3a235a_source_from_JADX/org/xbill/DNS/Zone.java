package org.xbill.DNS;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.TreeMap;

public class Zone implements Serializable {
    public static final int PRIMARY = 1;
    public static final int SECONDARY = 2;
    private static final long serialVersionUID = -9220510891189510942L;
    private RRset NS;
    private SOARecord SOA;
    private Map data;
    private int dclass;
    private boolean hasWild;
    private Name origin;
    private Object originNode;

    class ZoneIterator implements Iterator {
        private int count;
        private RRset[] current;
        private final Zone this$0;
        private boolean wantLastSOA;
        private Iterator zentries;

        ZoneIterator(Zone zone, boolean axfr) {
            this.this$0 = zone;
            synchronized (zone) {
                this.zentries = Zone.access$000(zone).entrySet().iterator();
            }
            this.wantLastSOA = axfr;
            RRset[] sets = Zone.access$200(zone, Zone.access$100(zone));
            this.current = new RRset[sets.length];
            int j = Zone.SECONDARY;
            for (int i = 0; i < sets.length; i += Zone.PRIMARY) {
                int type = sets[i].getType();
                if (type == 6) {
                    this.current[0] = sets[i];
                } else if (type == Zone.SECONDARY) {
                    this.current[Zone.PRIMARY] = sets[i];
                } else {
                    int j2 = j + Zone.PRIMARY;
                    this.current[j] = sets[i];
                    j = j2;
                }
            }
        }

        public boolean hasNext() {
            return this.current != null || this.wantLastSOA;
        }

        public Object next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            } else if (this.current == null) {
                this.wantLastSOA = false;
                return Zone.access$300(this.this$0, Zone.access$100(this.this$0), 6);
            } else {
                RRset[] rRsetArr = this.current;
                int i = this.count;
                this.count = i + Zone.PRIMARY;
                Object set = rRsetArr[i];
                if (this.count != this.current.length) {
                    return set;
                }
                this.current = null;
                while (this.zentries.hasNext()) {
                    Entry entry = (Entry) this.zentries.next();
                    if (!entry.getKey().equals(Zone.access$400(this.this$0))) {
                        RRset[] sets = Zone.access$200(this.this$0, entry.getValue());
                        if (sets.length != 0) {
                            this.current = sets;
                            this.count = 0;
                            return set;
                        }
                    }
                }
                return set;
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    static Map access$000(Zone x0) {
        return x0.data;
    }

    static Object access$100(Zone x0) {
        return x0.originNode;
    }

    static RRset[] access$200(Zone x0, Object x1) {
        return x0.allRRsets(x1);
    }

    static RRset access$300(Zone x0, Object x1, int x2) {
        return x0.oneRRset(x1, x2);
    }

    static Name access$400(Zone x0) {
        return x0.origin;
    }

    private void validate() throws IOException {
        this.originNode = exactName(this.origin);
        if (this.originNode == null) {
            throw new IOException(new StringBuffer().append(this.origin).append(": no data specified").toString());
        }
        RRset rrset = oneRRset(this.originNode, 6);
        if (rrset == null || rrset.size() != PRIMARY) {
            throw new IOException(new StringBuffer().append(this.origin).append(": exactly 1 SOA must be specified").toString());
        }
        this.SOA = (SOARecord) rrset.rrs().next();
        this.NS = oneRRset(this.originNode, SECONDARY);
        if (this.NS == null) {
            throw new IOException(new StringBuffer().append(this.origin).append(": no NS set specified").toString());
        }
    }

    private final void maybeAddRecord(Record record) throws IOException {
        int rtype = record.getType();
        Name name = record.getName();
        if (rtype == 6 && !name.equals(this.origin)) {
            throw new IOException(new StringBuffer().append("SOA owner ").append(name).append(" does not match zone origin ").append(this.origin).toString());
        } else if (name.subdomain(this.origin)) {
            addRecord(record);
        }
    }

    public Zone(Name zone, String file) throws IOException {
        this.dclass = PRIMARY;
        this.data = new TreeMap();
        if (zone == null) {
            throw new IllegalArgumentException("no zone name specified");
        }
        Master m = new Master(file, zone);
        this.origin = zone;
        while (true) {
            Record record = m.nextRecord();
            if (record != null) {
                maybeAddRecord(record);
            } else {
                validate();
                return;
            }
        }
    }

    public Zone(Name zone, Record[] records) throws IOException {
        this.dclass = PRIMARY;
        this.data = new TreeMap();
        if (zone == null) {
            throw new IllegalArgumentException("no zone name specified");
        }
        this.origin = zone;
        for (int i = 0; i < records.length; i += PRIMARY) {
            maybeAddRecord(records[i]);
        }
        validate();
    }

    private void fromXFR(ZoneTransferIn xfrin) throws IOException, ZoneTransferException {
        this.data = new TreeMap();
        this.origin = xfrin.getName();
        for (Record record : xfrin.run()) {
            maybeAddRecord(record);
        }
        if (xfrin.isAXFR()) {
            validate();
            return;
        }
        throw new IllegalArgumentException("zones can only be created from AXFRs");
    }

    public Zone(ZoneTransferIn xfrin) throws IOException, ZoneTransferException {
        this.dclass = PRIMARY;
        fromXFR(xfrin);
    }

    public Zone(Name zone, int dclass, String remote) throws IOException, ZoneTransferException {
        this.dclass = PRIMARY;
        ZoneTransferIn xfrin = ZoneTransferIn.newAXFR(zone, remote, null);
        xfrin.setDClass(dclass);
        fromXFR(xfrin);
    }

    public Name getOrigin() {
        return this.origin;
    }

    public RRset getNS() {
        return this.NS;
    }

    public SOARecord getSOA() {
        return this.SOA;
    }

    public int getDClass() {
        return this.dclass;
    }

    private synchronized Object exactName(Name name) {
        return this.data.get(name);
    }

    private synchronized RRset[] allRRsets(Object types) {
        RRset[] rRsetArr;
        if (types instanceof List) {
            List typelist = (List) types;
            rRsetArr = (RRset[]) typelist.toArray(new RRset[typelist.size()]);
        } else {
            rRsetArr = new RRset[PRIMARY];
            rRsetArr[0] = (RRset) types;
        }
        return rRsetArr;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized org.xbill.DNS.RRset oneRRset(java.lang.Object r7, int r8) {
        /*
        r6 = this;
        monitor-enter(r6);
        r4 = 255; // 0xff float:3.57E-43 double:1.26E-321;
        if (r8 != r4) goto L_0x0010;
    L_0x0005:
        r4 = new java.lang.IllegalArgumentException;	 Catch:{ all -> 0x000d }
        r5 = "oneRRset(ANY)";
        r4.<init>(r5);	 Catch:{ all -> 0x000d }
        throw r4;	 Catch:{ all -> 0x000d }
    L_0x000d:
        r4 = move-exception;
        monitor-exit(r6);
        throw r4;
    L_0x0010:
        r4 = r7 instanceof java.util.List;	 Catch:{ all -> 0x000d }
        if (r4 == 0) goto L_0x0030;
    L_0x0014:
        r0 = r7;
        r0 = (java.util.List) r0;	 Catch:{ all -> 0x000d }
        r2 = r0;
        r1 = 0;
    L_0x0019:
        r4 = r2.size();	 Catch:{ all -> 0x000d }
        if (r1 >= r4) goto L_0x003a;
    L_0x001f:
        r3 = r2.get(r1);	 Catch:{ all -> 0x000d }
        r3 = (org.xbill.DNS.RRset) r3;	 Catch:{ all -> 0x000d }
        r4 = r3.getType();	 Catch:{ all -> 0x000d }
        if (r4 != r8) goto L_0x002d;
    L_0x002b:
        monitor-exit(r6);
        return r3;
    L_0x002d:
        r1 = r1 + 1;
        goto L_0x0019;
    L_0x0030:
        r0 = r7;
        r0 = (org.xbill.DNS.RRset) r0;	 Catch:{ all -> 0x000d }
        r3 = r0;
        r4 = r3.getType();	 Catch:{ all -> 0x000d }
        if (r4 == r8) goto L_0x002b;
    L_0x003a:
        r3 = 0;
        goto L_0x002b;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.xbill.DNS.Zone.oneRRset(java.lang.Object, int):org.xbill.DNS.RRset");
    }

    private synchronized RRset findRRset(Name name, int type) {
        RRset rRset;
        Object types = exactName(name);
        if (types == null) {
            rRset = null;
        } else {
            rRset = oneRRset(types, type);
        }
        return rRset;
    }

    private synchronized void addRRset(Name name, RRset rrset) {
        if (!this.hasWild && name.isWild()) {
            this.hasWild = true;
        }
        Object types = this.data.get(name);
        if (types == null) {
            this.data.put(name, rrset);
        } else {
            int rtype = rrset.getType();
            if (types instanceof List) {
                List list = (List) types;
                for (int i = 0; i < list.size(); i += PRIMARY) {
                    if (((RRset) list.get(i)).getType() == rtype) {
                        list.set(i, rrset);
                        break;
                    }
                }
                list.add(rrset);
            } else {
                RRset set = (RRset) types;
                if (set.getType() == rtype) {
                    this.data.put(name, rrset);
                } else {
                    LinkedList list2 = new LinkedList();
                    list2.add(set);
                    list2.add(rrset);
                    this.data.put(name, list2);
                }
            }
        }
    }

    private synchronized void removeRRset(Name name, int type) {
        Object types = this.data.get(name);
        if (types != null) {
            if (types instanceof List) {
                List list = (List) types;
                int i = 0;
                while (i < list.size()) {
                    if (((RRset) list.get(i)).getType() == type) {
                        list.remove(i);
                        if (list.size() == 0) {
                            this.data.remove(name);
                        }
                    } else {
                        i += PRIMARY;
                    }
                }
            } else {
                if (((RRset) types).getType() == type) {
                    this.data.remove(name);
                }
            }
        }
    }

    private synchronized SetResponse lookup(Name name, int type) {
        SetResponse setResponse;
        if (name.subdomain(this.origin)) {
            Object types;
            int i;
            RRset rrset;
            int labels = name.labels();
            int olabels = this.origin.labels();
            int tlabels = olabels;
            while (tlabels <= labels) {
                Name tname;
                boolean isOrigin = tlabels == olabels;
                boolean isExact = tlabels == labels;
                if (isOrigin) {
                    tname = this.origin;
                } else if (isExact) {
                    tname = name;
                } else {
                    tname = new Name(name, labels - tlabels);
                }
                types = exactName(tname);
                if (types != null) {
                    if (!isOrigin) {
                        RRset ns = oneRRset(types, SECONDARY);
                        if (ns != null) {
                            setResponse = new SetResponse(3, ns);
                            break;
                        }
                    }
                    if (isExact && type == KEYRecord.PROTOCOL_ANY) {
                        setResponse = new SetResponse(6);
                        RRset[] sets = allRRsets(types);
                        for (i = 0; i < sets.length; i += PRIMARY) {
                            setResponse.addRRset(sets[i]);
                        }
                    } else {
                        if (!isExact) {
                            rrset = oneRRset(types, 39);
                            if (rrset != null) {
                                setResponse = new SetResponse(5, rrset);
                                break;
                            }
                        }
                        rrset = oneRRset(types, type);
                        if (rrset == null) {
                            rrset = oneRRset(types, 5);
                            if (rrset != null) {
                                setResponse = new SetResponse(4, rrset);
                                break;
                            }
                        }
                        setResponse = new SetResponse(6);
                        setResponse.addRRset(rrset);
                        break;
                        if (isExact) {
                            setResponse = SetResponse.ofType(SECONDARY);
                            break;
                        }
                    }
                }
                tlabels += PRIMARY;
            }
            if (this.hasWild) {
                for (i = 0; i < labels - olabels; i += PRIMARY) {
                    types = exactName(name.wild(i + PRIMARY));
                    if (types != null) {
                        rrset = oneRRset(types, type);
                        if (rrset != null) {
                            setResponse = new SetResponse(6);
                            setResponse.addRRset(rrset);
                            break;
                        }
                    }
                }
            }
            setResponse = SetResponse.ofType(PRIMARY);
        } else {
            setResponse = SetResponse.ofType(PRIMARY);
        }
        return setResponse;
    }

    public SetResponse findRecords(Name name, int type) {
        return lookup(name, type);
    }

    public RRset findExactMatch(Name name, int type) {
        Object types = exactName(name);
        if (types == null) {
            return null;
        }
        return oneRRset(types, type);
    }

    public void addRRset(RRset rrset) {
        addRRset(rrset.getName(), rrset);
    }

    public void addRecord(Record r) {
        Name name = r.getName();
        int rtype = r.getRRsetType();
        synchronized (this) {
            RRset rrset = findRRset(name, rtype);
            if (rrset == null) {
                addRRset(name, new RRset(r));
            } else {
                rrset.addRR(r);
            }
        }
    }

    public void removeRecord(Record r) {
        Name name = r.getName();
        int rtype = r.getRRsetType();
        synchronized (this) {
            RRset rrset = findRRset(name, rtype);
            if (rrset == null) {
                return;
            }
            if (rrset.size() == PRIMARY && rrset.first().equals(r)) {
                removeRRset(name, rtype);
            } else {
                rrset.deleteRR(r);
            }
        }
    }

    public Iterator iterator() {
        return new ZoneIterator(this, false);
    }

    public Iterator AXFR() {
        return new ZoneIterator(this, true);
    }

    private void nodeToString(StringBuffer sb, Object node) {
        RRset[] sets = allRRsets(node);
        for (int i = 0; i < sets.length; i += PRIMARY) {
            RRset rrset = sets[i];
            Iterator it = rrset.rrs();
            while (it.hasNext()) {
                sb.append(new StringBuffer().append(it.next()).append("\n").toString());
            }
            it = rrset.sigs();
            while (it.hasNext()) {
                sb.append(new StringBuffer().append(it.next()).append("\n").toString());
            }
        }
    }

    public synchronized String toMasterFile() {
        StringBuffer sb;
        sb = new StringBuffer();
        nodeToString(sb, this.originNode);
        for (Entry entry : this.data.entrySet()) {
            if (!this.origin.equals(entry.getKey())) {
                nodeToString(sb, entry.getValue());
            }
        }
        return sb.toString();
    }

    public String toString() {
        return toMasterFile();
    }
}
