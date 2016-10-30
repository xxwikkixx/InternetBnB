package org.xbill.DNS;

import java.util.HashMap;

public final class Type {
    public static final int A = 1;
    public static final int A6 = 38;
    public static final int AAAA = 28;
    public static final int AFSDB = 18;
    public static final int ANY = 255;
    public static final int APL = 42;
    public static final int ATMA = 34;
    public static final int AXFR = 252;
    public static final int CERT = 37;
    public static final int CNAME = 5;
    public static final int DHCID = 49;
    public static final int DLV = 32769;
    public static final int DNAME = 39;
    public static final int DNSKEY = 48;
    public static final int DS = 43;
    public static final int EID = 31;
    public static final int GPOS = 27;
    public static final int HINFO = 13;
    public static final int IPSECKEY = 45;
    public static final int ISDN = 20;
    public static final int IXFR = 251;
    public static final int KEY = 25;
    public static final int KX = 36;
    public static final int LOC = 29;
    public static final int MAILA = 254;
    public static final int MAILB = 253;
    public static final int MB = 7;
    public static final int MD = 3;
    public static final int MF = 4;
    public static final int MG = 8;
    public static final int MINFO = 14;
    public static final int MR = 9;
    public static final int MX = 15;
    public static final int NAPTR = 35;
    public static final int NIMLOC = 32;
    public static final int NS = 2;
    public static final int NSAP = 22;
    public static final int NSAP_PTR = 23;
    public static final int NSEC = 47;
    public static final int NSEC3 = 50;
    public static final int NSEC3PARAM = 51;
    public static final int NULL = 10;
    public static final int NXT = 30;
    public static final int OPT = 41;
    public static final int PTR = 12;
    public static final int PX = 26;
    public static final int RP = 17;
    public static final int RRSIG = 46;
    public static final int RT = 21;
    public static final int SIG = 24;
    public static final int SOA = 6;
    public static final int SPF = 99;
    public static final int SRV = 33;
    public static final int SSHFP = 44;
    public static final int TKEY = 249;
    public static final int TSIG = 250;
    public static final int TXT = 16;
    public static final int WKS = 11;
    public static final int X25 = 19;
    private static TypeMnemonic types;

    private static class TypeMnemonic extends Mnemonic {
        private HashMap objects;

        public TypeMnemonic() {
            super("Type", Type.NS);
            setPrefix("TYPE");
            this.objects = new HashMap();
        }

        public void add(int val, String str, Record proto) {
            super.add(val, str);
            this.objects.put(Mnemonic.toInteger(val), proto);
        }

        public void check(int val) {
            Type.check(val);
        }

        public Record getProto(int val) {
            check(val);
            return (Record) this.objects.get(Mnemonic.toInteger(val));
        }
    }

    static {
        types = new TypeMnemonic();
        types.add(A, "A", new ARecord());
        types.add(NS, "NS", new NSRecord());
        types.add(MD, "MD", new MDRecord());
        types.add(MF, "MF", new MFRecord());
        types.add(CNAME, "CNAME", new CNAMERecord());
        types.add(SOA, "SOA", new SOARecord());
        types.add(MB, "MB", new MBRecord());
        types.add(MG, "MG", new MGRecord());
        types.add(MR, "MR", new MRRecord());
        types.add(NULL, "NULL", new NULLRecord());
        types.add(WKS, "WKS", new WKSRecord());
        types.add(PTR, "PTR", new PTRRecord());
        types.add(HINFO, "HINFO", new HINFORecord());
        types.add(MINFO, "MINFO", new MINFORecord());
        types.add(MX, "MX", new MXRecord());
        types.add(TXT, "TXT", new TXTRecord());
        types.add(RP, "RP", new RPRecord());
        types.add(AFSDB, "AFSDB", new AFSDBRecord());
        types.add(X25, "X25", new X25Record());
        types.add(ISDN, "ISDN", new ISDNRecord());
        types.add(RT, "RT", new RTRecord());
        types.add(NSAP, "NSAP", new NSAPRecord());
        types.add(NSAP_PTR, "NSAP-PTR", new NSAP_PTRRecord());
        types.add(SIG, "SIG", new SIGRecord());
        types.add(KEY, "KEY", new KEYRecord());
        types.add(PX, "PX", new PXRecord());
        types.add(GPOS, "GPOS", new GPOSRecord());
        types.add(AAAA, "AAAA", new AAAARecord());
        types.add(LOC, "LOC", new LOCRecord());
        types.add(NXT, "NXT", new NXTRecord());
        types.add(EID, "EID");
        types.add(NIMLOC, "NIMLOC");
        types.add(SRV, "SRV", new SRVRecord());
        types.add(ATMA, "ATMA");
        types.add(NAPTR, "NAPTR", new NAPTRRecord());
        types.add(KX, "KX", new KXRecord());
        types.add(CERT, "CERT", new CERTRecord());
        types.add(A6, "A6", new A6Record());
        types.add(DNAME, "DNAME", new DNAMERecord());
        types.add(OPT, "OPT", new OPTRecord());
        types.add(APL, "APL", new APLRecord());
        types.add(DS, "DS", new DSRecord());
        types.add(SSHFP, "SSHFP", new SSHFPRecord());
        types.add(IPSECKEY, "IPSECKEY", new IPSECKEYRecord());
        types.add(RRSIG, "RRSIG", new RRSIGRecord());
        types.add(NSEC, "NSEC", new NSECRecord());
        types.add(DNSKEY, "DNSKEY", new DNSKEYRecord());
        types.add(DHCID, "DHCID", new DHCIDRecord());
        types.add(NSEC3, "NSEC3", new NSEC3Record());
        types.add(NSEC3PARAM, "NSEC3PARAM", new NSEC3PARAMRecord());
        types.add(SPF, "SPF", new SPFRecord());
        types.add(TKEY, "TKEY", new TKEYRecord());
        types.add(TSIG, "TSIG", new TSIGRecord());
        types.add(IXFR, "IXFR");
        types.add(AXFR, "AXFR");
        types.add(MAILB, "MAILB");
        types.add(MAILA, "MAILA");
        types.add(ANY, "ANY");
        types.add(DLV, "DLV", new DLVRecord());
    }

    private Type() {
    }

    public static void check(int val) {
        if (val < 0 || val > Message.MAXLENGTH) {
            throw new InvalidTypeException(val);
        }
    }

    public static String string(int val) {
        return types.getText(val);
    }

    public static int value(String s, boolean numberok) {
        int val = types.getValue(s);
        if (val == -1 && numberok) {
            return types.getValue(new StringBuffer().append("TYPE").append(s).toString());
        }
        return val;
    }

    public static int value(String s) {
        return value(s, false);
    }

    static Record getProto(int val) {
        return types.getProto(val);
    }

    public static boolean isRR(int type) {
        switch (type) {
            case OPT /*41*/:
            case TKEY /*249*/:
            case TSIG /*250*/:
            case IXFR /*251*/:
            case AXFR /*252*/:
            case MAILB /*253*/:
            case MAILA /*254*/:
            case ANY /*255*/:
                return false;
            default:
                return true;
        }
    }
}
