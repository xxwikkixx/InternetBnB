package org.xbill.DNS;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WKSRecord extends Record {
    private static final long serialVersionUID = -9104259763909119805L;
    private byte[] address;
    private int protocol;
    private int[] services;

    public static class Protocol {
        public static final int ARGUS = 13;
        public static final int BBN_RCC_MON = 10;
        public static final int BR_SAT_MON = 76;
        public static final int CFTP = 62;
        public static final int CHAOS = 16;
        public static final int DCN_MEAS = 19;
        public static final int EGP = 8;
        public static final int EMCON = 14;
        public static final int GGP = 3;
        public static final int HMP = 20;
        public static final int ICMP = 1;
        public static final int IGMP = 2;
        public static final int IGP = 9;
        public static final int IPCV = 71;
        public static final int IPPC = 67;
        public static final int IRTP = 28;
        public static final int ISO_TP4 = 29;
        public static final int LEAF_1 = 25;
        public static final int LEAF_2 = 26;
        public static final int MERIT_INP = 32;
        public static final int MFE_NSP = 31;
        public static final int MIT_SUBNET = 65;
        public static final int MUX = 18;
        public static final int NETBLT = 30;
        public static final int NVP_II = 11;
        public static final int PRM = 21;
        public static final int PUP = 12;
        public static final int RDP = 27;
        public static final int RVD = 66;
        public static final int SAT_EXPAK = 64;
        public static final int SAT_MON = 69;
        public static final int SEP = 33;
        public static final int ST = 5;
        public static final int TCP = 6;
        public static final int TRUNK_1 = 23;
        public static final int TRUNK_2 = 24;
        public static final int UCL = 7;
        public static final int UDP = 17;
        public static final int WB_EXPAK = 79;
        public static final int WB_MON = 78;
        public static final int XNET = 15;
        public static final int XNS_IDP = 22;
        private static Mnemonic protocols;

        private Protocol() {
        }

        static {
            protocols = new Mnemonic("IP protocol", GGP);
            protocols.setMaximum(KEYRecord.PROTOCOL_ANY);
            protocols.setNumericAllowed(true);
            protocols.add(ICMP, "icmp");
            protocols.add(IGMP, "igmp");
            protocols.add(GGP, "ggp");
            protocols.add(ST, "st");
            protocols.add(TCP, "tcp");
            protocols.add(UCL, "ucl");
            protocols.add(EGP, "egp");
            protocols.add(IGP, "igp");
            protocols.add(BBN_RCC_MON, "bbn-rcc-mon");
            protocols.add(NVP_II, "nvp-ii");
            protocols.add(PUP, "pup");
            protocols.add(ARGUS, "argus");
            protocols.add(EMCON, "emcon");
            protocols.add(XNET, "xnet");
            protocols.add(CHAOS, "chaos");
            protocols.add(UDP, "udp");
            protocols.add(MUX, "mux");
            protocols.add(DCN_MEAS, "dcn-meas");
            protocols.add(HMP, "hmp");
            protocols.add(PRM, "prm");
            protocols.add(XNS_IDP, "xns-idp");
            protocols.add(TRUNK_1, "trunk-1");
            protocols.add(TRUNK_2, "trunk-2");
            protocols.add(LEAF_1, "leaf-1");
            protocols.add(LEAF_2, "leaf-2");
            protocols.add(RDP, "rdp");
            protocols.add(IRTP, "irtp");
            protocols.add(ISO_TP4, "iso-tp4");
            protocols.add(NETBLT, "netblt");
            protocols.add(MFE_NSP, "mfe-nsp");
            protocols.add(MERIT_INP, "merit-inp");
            protocols.add(SEP, "sep");
            protocols.add(CFTP, "cftp");
            protocols.add(SAT_EXPAK, "sat-expak");
            protocols.add(MIT_SUBNET, "mit-subnet");
            protocols.add(RVD, "rvd");
            protocols.add(IPPC, "ippc");
            protocols.add(SAT_MON, "sat-mon");
            protocols.add(IPCV, "ipcv");
            protocols.add(BR_SAT_MON, "br-sat-mon");
            protocols.add(WB_MON, "wb-mon");
            protocols.add(WB_EXPAK, "wb-expak");
        }

        public static String string(int type) {
            return protocols.getText(type);
        }

        public static int value(String s) {
            return protocols.getValue(s);
        }
    }

    public static class Service {
        public static final int AUTH = 113;
        public static final int BL_IDM = 142;
        public static final int BOOTPC = 68;
        public static final int BOOTPS = 67;
        public static final int CHARGEN = 19;
        public static final int CISCO_FNA = 130;
        public static final int CISCO_SYS = 132;
        public static final int CISCO_TNA = 131;
        public static final int CSNET_NS = 105;
        public static final int DAYTIME = 13;
        public static final int DCP = 93;
        public static final int DISCARD = 9;
        public static final int DOMAIN = 53;
        public static final int DSP = 33;
        public static final int ECHO = 7;
        public static final int EMFIS_CNTL = 141;
        public static final int EMFIS_DATA = 140;
        public static final int ERPC = 121;
        public static final int FINGER = 79;
        public static final int FTP = 21;
        public static final int FTP_DATA = 20;
        public static final int GRAPHICS = 41;
        public static final int HOSTNAME = 101;
        public static final int HOSTS2_NS = 81;
        public static final int INGRES_NET = 134;
        public static final int ISI_GL = 55;
        public static final int ISO_TSAP = 102;
        public static final int LA_MAINT = 51;
        public static final int LINK = 245;
        public static final int LOCUS_CON = 127;
        public static final int LOCUS_MAP = 125;
        public static final int LOC_SRV = 135;
        public static final int LOGIN = 49;
        public static final int METAGRAM = 99;
        public static final int MIT_DOV = 91;
        public static final int MPM = 45;
        public static final int MPM_FLAGS = 44;
        public static final int MPM_SND = 46;
        public static final int MSG_AUTH = 31;
        public static final int MSG_ICP = 29;
        public static final int NAMESERVER = 42;
        public static final int NETBIOS_DGM = 138;
        public static final int NETBIOS_NS = 137;
        public static final int NETBIOS_SSN = 139;
        public static final int NETRJS_1 = 71;
        public static final int NETRJS_2 = 72;
        public static final int NETRJS_3 = 73;
        public static final int NETRJS_4 = 74;
        public static final int NICNAME = 43;
        public static final int NI_FTP = 47;
        public static final int NI_MAIL = 61;
        public static final int NNTP = 119;
        public static final int NSW_FE = 27;
        public static final int NTP = 123;
        public static final int POP_2 = 109;
        public static final int PROFILE = 136;
        public static final int PWDGEN = 129;
        public static final int QUOTE = 17;
        public static final int RJE = 5;
        public static final int RLP = 39;
        public static final int RTELNET = 107;
        public static final int SFTP = 115;
        public static final int SMTP = 25;
        public static final int STATSRV = 133;
        public static final int SUNRPC = 111;
        public static final int SUPDUP = 95;
        public static final int SUR_MEAS = 243;
        public static final int SU_MIT_TG = 89;
        public static final int SWIFT_RVF = 97;
        public static final int TACACS_DS = 65;
        public static final int TACNEWS = 98;
        public static final int TELNET = 23;
        public static final int TFTP = 69;
        public static final int TIME = 37;
        public static final int USERS = 11;
        public static final int UUCP_PATH = 117;
        public static final int VIA_FTP = 63;
        public static final int X400 = 103;
        public static final int X400_SND = 104;
        private static Mnemonic services;

        private Service() {
        }

        static {
            services = new Mnemonic("TCP/UDP service", 3);
            services.setMaximum(Message.MAXLENGTH);
            services.setNumericAllowed(true);
            services.add(RJE, "rje");
            services.add(ECHO, "echo");
            services.add(DISCARD, "discard");
            services.add(USERS, "users");
            services.add(DAYTIME, "daytime");
            services.add(QUOTE, "quote");
            services.add(CHARGEN, "chargen");
            services.add(FTP_DATA, "ftp-data");
            services.add(FTP, "ftp");
            services.add(TELNET, "telnet");
            services.add(SMTP, "smtp");
            services.add(NSW_FE, "nsw-fe");
            services.add(MSG_ICP, "msg-icp");
            services.add(MSG_AUTH, "msg-auth");
            services.add(DSP, "dsp");
            services.add(TIME, "time");
            services.add(RLP, "rlp");
            services.add(GRAPHICS, "graphics");
            services.add(NAMESERVER, "nameserver");
            services.add(NICNAME, "nicname");
            services.add(MPM_FLAGS, "mpm-flags");
            services.add(MPM, "mpm");
            services.add(MPM_SND, "mpm-snd");
            services.add(NI_FTP, "ni-ftp");
            services.add(LOGIN, "login");
            services.add(LA_MAINT, "la-maint");
            services.add(DOMAIN, "domain");
            services.add(ISI_GL, "isi-gl");
            services.add(NI_MAIL, "ni-mail");
            services.add(VIA_FTP, "via-ftp");
            services.add(TACACS_DS, "tacacs-ds");
            services.add(BOOTPS, "bootps");
            services.add(BOOTPC, "bootpc");
            services.add(TFTP, "tftp");
            services.add(NETRJS_1, "netrjs-1");
            services.add(NETRJS_2, "netrjs-2");
            services.add(NETRJS_3, "netrjs-3");
            services.add(NETRJS_4, "netrjs-4");
            services.add(FINGER, "finger");
            services.add(HOSTS2_NS, "hosts2-ns");
            services.add(SU_MIT_TG, "su-mit-tg");
            services.add(MIT_DOV, "mit-dov");
            services.add(DCP, "dcp");
            services.add(SUPDUP, "supdup");
            services.add(SWIFT_RVF, "swift-rvf");
            services.add(TACNEWS, "tacnews");
            services.add(METAGRAM, "metagram");
            services.add(HOSTNAME, "hostname");
            services.add(ISO_TSAP, "iso-tsap");
            services.add(X400, "x400");
            services.add(X400_SND, "x400-snd");
            services.add(CSNET_NS, "csnet-ns");
            services.add(RTELNET, "rtelnet");
            services.add(POP_2, "pop-2");
            services.add(SUNRPC, "sunrpc");
            services.add(AUTH, "auth");
            services.add(SFTP, "sftp");
            services.add(UUCP_PATH, "uucp-path");
            services.add(NNTP, "nntp");
            services.add(ERPC, "erpc");
            services.add(NTP, "ntp");
            services.add(LOCUS_MAP, "locus-map");
            services.add(LOCUS_CON, "locus-con");
            services.add(PWDGEN, "pwdgen");
            services.add(CISCO_FNA, "cisco-fna");
            services.add(CISCO_TNA, "cisco-tna");
            services.add(CISCO_SYS, "cisco-sys");
            services.add(STATSRV, "statsrv");
            services.add(INGRES_NET, "ingres-net");
            services.add(LOC_SRV, "loc-srv");
            services.add(PROFILE, "profile");
            services.add(NETBIOS_NS, "netbios-ns");
            services.add(NETBIOS_DGM, "netbios-dgm");
            services.add(NETBIOS_SSN, "netbios-ssn");
            services.add(EMFIS_DATA, "emfis-data");
            services.add(EMFIS_CNTL, "emfis-cntl");
            services.add(BL_IDM, "bl-idm");
            services.add(SUR_MEAS, "sur-meas");
            services.add(LINK, "link");
        }

        public static String string(int type) {
            return services.getText(type);
        }

        public static int value(String s) {
            return services.getValue(s);
        }
    }

    WKSRecord() {
    }

    Record getObject() {
        return new WKSRecord();
    }

    public WKSRecord(Name name, int dclass, long ttl, InetAddress address, int protocol, int[] services) {
        super(name, 11, dclass, ttl);
        if (Address.familyOf(address) != 1) {
            throw new IllegalArgumentException("invalid IPv4 address");
        }
        this.address = address.getAddress();
        this.protocol = Record.checkU8("protocol", protocol);
        for (int checkU16 : services) {
            Record.checkU16("service", checkU16);
        }
        this.services = new int[services.length];
        System.arraycopy(services, 0, this.services, 0, services.length);
        Arrays.sort(this.services);
    }

    void rrFromWire(DNSInput in) throws IOException {
        int i;
        this.address = in.readByteArray(4);
        this.protocol = in.readU8();
        byte[] array = in.readByteArray();
        List list = new ArrayList();
        for (i = 0; i < array.length; i++) {
            for (int j = 0; j < 8; j++) {
                if (((1 << (7 - j)) & (array[i] & KEYRecord.PROTOCOL_ANY)) != 0) {
                    list.add(new Integer((i * 8) + j));
                }
            }
        }
        this.services = new int[list.size()];
        for (i = 0; i < list.size(); i++) {
            this.services[i] = ((Integer) list.get(i)).intValue();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void rdataFromString(org.xbill.DNS.Tokenizer r8, org.xbill.DNS.Name r9) throws java.io.IOException {
        /*
        r7 = this;
        r2 = r8.getString();
        r5 = 1;
        r5 = org.xbill.DNS.Address.toByteArray(r2, r5);
        r7.address = r5;
        r5 = r7.address;
        if (r5 != 0) goto L_0x0016;
    L_0x000f:
        r5 = "invalid address";
        r5 = r8.exception(r5);
        throw r5;
    L_0x0016:
        r2 = r8.getString();
        r5 = org.xbill.DNS.WKSRecord.Protocol.value(r2);
        r7.protocol = r5;
        r5 = r7.protocol;
        if (r5 >= 0) goto L_0x003c;
    L_0x0024:
        r5 = new java.lang.StringBuffer;
        r5.<init>();
        r6 = "Invalid IP protocol: ";
        r5 = r5.append(r6);
        r5 = r5.append(r2);
        r5 = r5.toString();
        r5 = r8.exception(r5);
        throw r5;
    L_0x003c:
        r1 = new java.util.ArrayList;
        r1.<init>();
    L_0x0041:
        r4 = r8.get();
        r5 = r4.isString();
        if (r5 != 0) goto L_0x006e;
    L_0x004b:
        r8.unget();
        r5 = r1.size();
        r5 = new int[r5];
        r7.services = r5;
        r0 = 0;
    L_0x0057:
        r5 = r1.size();
        if (r0 >= r5) goto L_0x0099;
    L_0x005d:
        r6 = r7.services;
        r5 = r1.get(r0);
        r5 = (java.lang.Integer) r5;
        r5 = r5.intValue();
        r6[r0] = r5;
        r0 = r0 + 1;
        goto L_0x0057;
    L_0x006e:
        r5 = r4.value;
        r3 = org.xbill.DNS.WKSRecord.Service.value(r5);
        if (r3 >= 0) goto L_0x0090;
    L_0x0076:
        r5 = new java.lang.StringBuffer;
        r5.<init>();
        r6 = "Invalid TCP/UDP service: ";
        r5 = r5.append(r6);
        r6 = r4.value;
        r5 = r5.append(r6);
        r5 = r5.toString();
        r5 = r8.exception(r5);
        throw r5;
    L_0x0090:
        r5 = new java.lang.Integer;
        r5.<init>(r3);
        r1.add(r5);
        goto L_0x0041;
    L_0x0099:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.xbill.DNS.WKSRecord.rdataFromString(org.xbill.DNS.Tokenizer, org.xbill.DNS.Name):void");
    }

    String rrToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(Address.toDottedQuad(this.address));
        sb.append(" ");
        sb.append(this.protocol);
        for (int append : this.services) {
            sb.append(new StringBuffer().append(" ").append(append).toString());
        }
        return sb.toString();
    }

    public InetAddress getAddress() {
        try {
            return InetAddress.getByAddress(this.address);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    public int getProtocol() {
        return this.protocol;
    }

    public int[] getServices() {
        return this.services;
    }

    void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        out.writeByteArray(this.address);
        out.writeU8(this.protocol);
        byte[] array = new byte[((this.services[this.services.length - 1] / 8) + 1)];
        for (int port : this.services) {
            int i = port / 8;
            array[i] = (byte) (array[i] | (1 << (7 - (port % 8))));
        }
        out.writeByteArray(array);
    }
}
