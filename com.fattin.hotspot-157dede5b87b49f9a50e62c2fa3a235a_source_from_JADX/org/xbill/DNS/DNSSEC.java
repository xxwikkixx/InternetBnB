package org.xbill.DNS;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import org.xbill.DNS.WKSRecord.Protocol;
import org.xbill.DNS.WKSRecord.Service;

public class DNSSEC {
    private static final int ASN1_INT = 2;
    private static final int ASN1_SEQ = 48;
    private static final int DSA_LEN = 20;

    public static class Algorithm {
        public static final int DH = 2;
        public static final int DSA = 3;
        public static final int DSA_NSEC3_SHA1 = 6;
        public static final int ECC = 4;
        public static final int INDIRECT = 252;
        public static final int PRIVATEDNS = 253;
        public static final int PRIVATEOID = 254;
        public static final int RSAMD5 = 1;
        public static final int RSASHA1 = 5;
        public static final int RSASHA256 = 8;
        public static final int RSASHA512 = 10;
        public static final int RSA_NSEC3_SHA1 = 7;
        private static Mnemonic algs;

        private Algorithm() {
        }

        static {
            algs = new Mnemonic("DNSSEC algorithm", DH);
            algs.setMaximum(KEYRecord.PROTOCOL_ANY);
            algs.setNumericAllowed(true);
            algs.add(RSAMD5, "RSAMD5");
            algs.add(DH, "DH");
            algs.add(DSA, "DSA");
            algs.add(ECC, "ECC");
            algs.add(RSASHA1, "RSASHA1");
            algs.add(DSA_NSEC3_SHA1, "DSA-NSEC3-SHA1");
            algs.add(RSA_NSEC3_SHA1, "RSA-NSEC3-SHA1");
            algs.add(RSASHA256, "RSASHA256");
            algs.add(RSASHA512, "RSASHA512");
            algs.add(INDIRECT, "INDIRECT");
            algs.add(PRIVATEDNS, "PRIVATEDNS");
            algs.add(PRIVATEOID, "PRIVATEOID");
        }

        public static String string(int alg) {
            return algs.getText(alg);
        }

        public static int value(String s) {
            return algs.getValue(s);
        }
    }

    public static class DNSSECException extends Exception {
        DNSSECException(String s) {
            super(s);
        }
    }

    public static class IncompatibleKeyException extends IllegalArgumentException {
        IncompatibleKeyException() {
            super("incompatible keys");
        }
    }

    public static class KeyMismatchException extends DNSSECException {
        private KEYBase key;
        private SIGBase sig;

        KeyMismatchException(KEYBase key, SIGBase sig) {
            super(new StringBuffer().append("key ").append(key.getName()).append("/").append(Algorithm.string(key.getAlgorithm())).append("/").append(key.getFootprint()).append(" ").append("does not match signature ").append(sig.getSigner()).append("/").append(Algorithm.string(sig.getAlgorithm())).append("/").append(sig.getFootprint()).toString());
        }
    }

    public static class MalformedKeyException extends DNSSECException {
        MalformedKeyException(KEYBase rec) {
            super(new StringBuffer().append("Invalid key data: ").append(rec.rdataToString()).toString());
        }
    }

    public static class SignatureExpiredException extends DNSSECException {
        private Date now;
        private Date when;

        SignatureExpiredException(Date when, Date now) {
            super("signature expired");
            this.when = when;
            this.now = now;
        }

        public Date getExpiration() {
            return this.when;
        }

        public Date getVerifyTime() {
            return this.now;
        }
    }

    public static class SignatureNotYetValidException extends DNSSECException {
        private Date now;
        private Date when;

        SignatureNotYetValidException(Date when, Date now) {
            super("signature is not yet valid");
            this.when = when;
            this.now = now;
        }

        public Date getExpiration() {
            return this.when;
        }

        public Date getVerifyTime() {
            return this.now;
        }
    }

    public static class SignatureVerificationException extends DNSSECException {
        SignatureVerificationException() {
            super("signature verification failed");
        }
    }

    public static class UnsupportedAlgorithmException extends DNSSECException {
        UnsupportedAlgorithmException(int alg) {
            super(new StringBuffer().append("Unsupported algorithm: ").append(alg).toString());
        }
    }

    private DNSSEC() {
    }

    private static void digestSIG(DNSOutput out, SIGBase sig) {
        out.writeU16(sig.getTypeCovered());
        out.writeU8(sig.getAlgorithm());
        out.writeU8(sig.getLabels());
        out.writeU32(sig.getOrigTTL());
        out.writeU32(sig.getExpire().getTime() / 1000);
        out.writeU32(sig.getTimeSigned().getTime() / 1000);
        out.writeU16(sig.getFootprint());
        sig.getSigner().toWireCanonical(out);
    }

    public static byte[] digestRRset(RRSIGRecord rrsig, RRset rrset) {
        DNSOutput out = new DNSOutput();
        digestSIG(out, rrsig);
        int size = rrset.size();
        Record[] records = new Record[size];
        Iterator it = rrset.rrs();
        Name name = rrset.getName();
        Name wild = null;
        int sigLabels = rrsig.getLabels() + 1;
        if (name.labels() > sigLabels) {
            wild = name.wild(name.labels() - sigLabels);
        }
        while (it.hasNext()) {
            size--;
            records[size] = (Record) it.next();
        }
        Arrays.sort(records);
        DNSOutput header = new DNSOutput();
        if (wild != null) {
            wild.toWireCanonical(header);
        } else {
            name.toWireCanonical(header);
        }
        header.writeU16(rrset.getType());
        header.writeU16(rrset.getDClass());
        header.writeU32(rrsig.getOrigTTL());
        for (Record rdataToWireCanonical : records) {
            out.writeByteArray(header.toByteArray());
            int lengthPosition = out.current();
            out.writeU16(0);
            out.writeByteArray(rdataToWireCanonical.rdataToWireCanonical());
            int rrlength = (out.current() - lengthPosition) - 2;
            out.save();
            out.jump(lengthPosition);
            out.writeU16(rrlength);
            out.restore();
        }
        return out.toByteArray();
    }

    public static byte[] digestMessage(SIGRecord sig, Message msg, byte[] previous) {
        DNSOutput out = new DNSOutput();
        digestSIG(out, sig);
        if (previous != null) {
            out.writeByteArray(previous);
        }
        msg.toWire(out);
        return out.toByteArray();
    }

    private static int BigIntegerLength(BigInteger i) {
        return (i.bitLength() + 7) / 8;
    }

    private static BigInteger readBigInteger(DNSInput in, int len) throws IOException {
        return new BigInteger(1, in.readByteArray(len));
    }

    private static BigInteger readBigInteger(DNSInput in) {
        return new BigInteger(1, in.readByteArray());
    }

    private static void writeBigInteger(DNSOutput out, BigInteger val) {
        byte[] b = val.toByteArray();
        if (b[0] == null) {
            out.writeByteArray(b, 1, b.length - 1);
        } else {
            out.writeByteArray(b);
        }
    }

    private static PublicKey toRSAPublicKey(KEYBase r) throws IOException, GeneralSecurityException {
        DNSInput in = new DNSInput(r.getKey());
        int exponentLength = in.readU8();
        if (exponentLength == 0) {
            exponentLength = in.readU16();
        }
        BigInteger exponent = readBigInteger(in, exponentLength);
        return KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(readBigInteger(in), exponent));
    }

    private static PublicKey toDSAPublicKey(KEYBase r) throws IOException, GeneralSecurityException, MalformedKeyException {
        DNSInput in = new DNSInput(r.getKey());
        int t = in.readU8();
        if (t > 8) {
            throw new MalformedKeyException(r);
        }
        BigInteger q = readBigInteger(in, DSA_LEN);
        BigInteger p = readBigInteger(in, (t * 8) + 64);
        BigInteger g = readBigInteger(in, (t * 8) + 64);
        return KeyFactory.getInstance("DSA").generatePublic(new DSAPublicKeySpec(readBigInteger(in, (t * 8) + 64), p, q, g));
    }

    static PublicKey toPublicKey(KEYBase r) throws DNSSECException {
        int alg = r.getAlgorithm();
        switch (alg) {
            case KEYRecord.PROTOCOL_TLS /*1*/:
            case TKEYRecord.DELETE /*5*/:
            case Service.ECHO /*7*/:
            case Protocol.EGP /*8*/:
            case Protocol.BBN_RCC_MON /*10*/:
                return toRSAPublicKey(r);
            case KEYRecord.PROTOCOL_DNSSEC /*3*/:
            case Protocol.TCP /*6*/:
                return toDSAPublicKey(r);
            default:
                try {
                    throw new UnsupportedAlgorithmException(alg);
                } catch (IOException e) {
                    throw new MalformedKeyException(r);
                } catch (GeneralSecurityException e2) {
                    throw new DNSSECException(e2.toString());
                }
        }
    }

    private static byte[] fromRSAPublicKey(RSAPublicKey key) {
        DNSOutput out = new DNSOutput();
        BigInteger exponent = key.getPublicExponent();
        BigInteger modulus = key.getModulus();
        int exponentLength = BigIntegerLength(exponent);
        if (exponentLength < KEYRecord.OWNER_ZONE) {
            out.writeU8(exponentLength);
        } else {
            out.writeU8(0);
            out.writeU16(exponentLength);
        }
        writeBigInteger(out, exponent);
        writeBigInteger(out, modulus);
        return out.toByteArray();
    }

    private static byte[] fromDSAPublicKey(DSAPublicKey key) {
        DNSOutput out = new DNSOutput();
        BigInteger q = key.getParams().getQ();
        BigInteger p = key.getParams().getP();
        BigInteger g = key.getParams().getG();
        BigInteger y = key.getY();
        out.writeU8((p.toByteArray().length - 64) / 8);
        writeBigInteger(out, q);
        writeBigInteger(out, p);
        writeBigInteger(out, g);
        writeBigInteger(out, y);
        return out.toByteArray();
    }

    static byte[] fromPublicKey(PublicKey key, int alg) throws DNSSECException {
        switch (alg) {
            case KEYRecord.PROTOCOL_TLS /*1*/:
            case TKEYRecord.DELETE /*5*/:
            case Service.ECHO /*7*/:
            case Protocol.EGP /*8*/:
            case Protocol.BBN_RCC_MON /*10*/:
                if (key instanceof RSAPublicKey) {
                    return fromRSAPublicKey((RSAPublicKey) key);
                }
                throw new IncompatibleKeyException();
            case KEYRecord.PROTOCOL_DNSSEC /*3*/:
            case Protocol.TCP /*6*/:
                if (key instanceof DSAPublicKey) {
                    return fromDSAPublicKey((DSAPublicKey) key);
                }
                throw new IncompatibleKeyException();
            default:
                throw new UnsupportedAlgorithmException(alg);
        }
    }

    public static String algString(int alg) throws UnsupportedAlgorithmException {
        switch (alg) {
            case KEYRecord.PROTOCOL_TLS /*1*/:
                return "MD5withRSA";
            case KEYRecord.PROTOCOL_DNSSEC /*3*/:
            case Protocol.TCP /*6*/:
                return "SHA1withDSA";
            case TKEYRecord.DELETE /*5*/:
            case Service.ECHO /*7*/:
                return "SHA1withRSA";
            case Protocol.EGP /*8*/:
                return "SHA256withRSA";
            case Protocol.BBN_RCC_MON /*10*/:
                return "SHA512withRSA";
            default:
                throw new UnsupportedAlgorithmException(alg);
        }
    }

    private static byte[] DSASignaturefromDNS(byte[] dns) throws DNSSECException, IOException {
        if (dns.length != 41) {
            throw new SignatureVerificationException();
        }
        DNSInput in = new DNSInput(dns);
        DNSOutput out = new DNSOutput();
        int t = in.readU8();
        byte[] r = in.readByteArray(DSA_LEN);
        int rlen = DSA_LEN;
        if (r[0] < null) {
            rlen = DSA_LEN + 1;
        }
        byte[] s = in.readByteArray(DSA_LEN);
        int slen = DSA_LEN;
        if (s[0] < null) {
            slen = DSA_LEN + 1;
        }
        out.writeU8(ASN1_SEQ);
        out.writeU8((rlen + slen) + 4);
        out.writeU8(ASN1_INT);
        out.writeU8(rlen);
        if (rlen > DSA_LEN) {
            out.writeU8(0);
        }
        out.writeByteArray(r);
        out.writeU8(ASN1_INT);
        out.writeU8(slen);
        if (slen > DSA_LEN) {
            out.writeU8(0);
        }
        out.writeByteArray(s);
        return out.toByteArray();
    }

    private static byte[] DSASignaturetoDNS(byte[] key, int t) throws IOException {
        DNSInput in = new DNSInput(key);
        DNSOutput out = new DNSOutput();
        out.writeU8(t);
        if (in.readU8() != ASN1_SEQ) {
            throw new IOException();
        }
        int seqlen = in.readU8();
        if (in.readU8() != ASN1_INT) {
            throw new IOException();
        }
        int rlen = in.readU8();
        if (rlen == 21) {
            if (in.readU8() != 0) {
                throw new IOException();
            }
        } else if (rlen != DSA_LEN) {
            throw new IOException();
        }
        out.writeByteArray(in.readByteArray(DSA_LEN));
        if (in.readU8() != ASN1_INT) {
            throw new IOException();
        }
        int slen = in.readU8();
        if (slen == 21) {
            if (in.readU8() != 0) {
                throw new IOException();
            }
        } else if (slen != DSA_LEN) {
            throw new IOException();
        }
        out.writeByteArray(in.readByteArray(DSA_LEN));
        return out.toByteArray();
    }

    private static void verify(PublicKey key, int alg, byte[] data, byte[] signature) throws DNSSECException {
        if (key instanceof DSAPublicKey) {
            try {
                signature = DSASignaturefromDNS(signature);
            } catch (IOException e) {
                throw new IllegalStateException();
            }
        }
        try {
            Signature s = Signature.getInstance(algString(alg));
            s.initVerify(key);
            s.update(data);
            if (!s.verify(signature)) {
                throw new SignatureVerificationException();
            }
        } catch (GeneralSecurityException e2) {
            throw new DNSSECException(e2.toString());
        }
    }

    private static boolean matches(SIGBase sig, KEYBase key) {
        return key.getAlgorithm() == sig.getAlgorithm() && key.getFootprint() == sig.getFootprint() && key.getName().equals(sig.getSigner());
    }

    public static void verify(RRset rrset, RRSIGRecord rrsig, DNSKEYRecord key) throws DNSSECException {
        if (matches(rrsig, key)) {
            Date now = new Date();
            if (now.compareTo(rrsig.getExpire()) > 0) {
                throw new SignatureExpiredException(rrsig.getExpire(), now);
            } else if (now.compareTo(rrsig.getTimeSigned()) < 0) {
                throw new SignatureNotYetValidException(rrsig.getTimeSigned(), now);
            } else {
                verify(key.getPublicKey(), rrsig.getAlgorithm(), digestRRset(rrsig, rrset), rrsig.getSignature());
                return;
            }
        }
        throw new KeyMismatchException(key, rrsig);
    }

    private static byte[] sign(PrivateKey privkey, PublicKey pubkey, int alg, byte[] data, String provider) throws DNSSECException {
        Signature s;
        if (provider != null) {
            try {
                s = Signature.getInstance(algString(alg), provider);
            } catch (GeneralSecurityException e) {
                throw new DNSSECException(e.toString());
            }
        }
        s = Signature.getInstance(algString(alg));
        s.initSign(privkey);
        s.update(data);
        byte[] signature = s.sign();
        if (pubkey instanceof DSAPublicKey) {
            try {
                signature = DSASignaturetoDNS(signature, (BigIntegerLength(((DSAPublicKey) pubkey).getParams().getP()) - 64) / 8);
            } catch (IOException e2) {
                throw new IllegalStateException();
            }
        }
        return signature;
    }

    static void checkAlgorithm(PrivateKey key, int alg) throws UnsupportedAlgorithmException {
        switch (alg) {
            case KEYRecord.PROTOCOL_TLS /*1*/:
            case TKEYRecord.DELETE /*5*/:
            case Service.ECHO /*7*/:
            case Protocol.EGP /*8*/:
            case Protocol.BBN_RCC_MON /*10*/:
                if (!(key instanceof RSAPrivateKey)) {
                    throw new IncompatibleKeyException();
                }
            case KEYRecord.PROTOCOL_DNSSEC /*3*/:
            case Protocol.TCP /*6*/:
                if (!(key instanceof DSAPrivateKey)) {
                    throw new IncompatibleKeyException();
                }
            default:
                throw new UnsupportedAlgorithmException(alg);
        }
    }

    public static RRSIGRecord sign(RRset rrset, DNSKEYRecord key, PrivateKey privkey, Date inception, Date expiration) throws DNSSECException {
        return sign(rrset, key, privkey, inception, expiration, null);
    }

    public static RRSIGRecord sign(RRset rrset, DNSKEYRecord key, PrivateKey privkey, Date inception, Date expiration, String provider) throws DNSSECException {
        int alg = key.getAlgorithm();
        checkAlgorithm(privkey, alg);
        RRSIGRecord rrsig = new RRSIGRecord(rrset.getName(), rrset.getDClass(), rrset.getTTL(), rrset.getType(), alg, rrset.getTTL(), expiration, inception, key.getFootprint(), key.getName(), null);
        rrsig.setSignature(sign(privkey, key.getPublicKey(), alg, digestRRset(rrsig, rrset), provider));
        return rrsig;
    }

    static SIGRecord signMessage(Message message, SIGRecord previous, KEYRecord key, PrivateKey privkey, Date inception, Date expiration) throws DNSSECException {
        int alg = key.getAlgorithm();
        checkAlgorithm(privkey, alg);
        SIGRecord sig = new SIGRecord(Name.root, KEYRecord.PROTOCOL_ANY, 0, 0, alg, 0, expiration, inception, key.getFootprint(), key.getName(), null);
        DNSOutput out = new DNSOutput();
        digestSIG(out, sig);
        if (previous != null) {
            out.writeByteArray(previous.getSignature());
        }
        message.toWire(out);
        sig.setSignature(sign(privkey, key.getPublicKey(), alg, out.toByteArray(), null));
        return sig;
    }

    static void verifyMessage(Message message, byte[] bytes, SIGRecord sig, SIGRecord previous, KEYRecord key) throws DNSSECException {
        if (matches(sig, key)) {
            Date now = new Date();
            if (now.compareTo(sig.getExpire()) > 0) {
                throw new SignatureExpiredException(sig.getExpire(), now);
            } else if (now.compareTo(sig.getTimeSigned()) < 0) {
                throw new SignatureNotYetValidException(sig.getTimeSigned(), now);
            } else {
                DNSOutput out = new DNSOutput();
                digestSIG(out, sig);
                if (previous != null) {
                    out.writeByteArray(previous.getSignature());
                }
                Header header = (Header) message.getHeader().clone();
                header.decCount(3);
                out.writeByteArray(header.toWire());
                out.writeByteArray(bytes, 12, message.sig0start - 12);
                verify(key.getPublicKey(), sig.getAlgorithm(), out.toByteArray(), sig.getSignature());
                return;
            }
        }
        throw new KeyMismatchException(key, sig);
    }

    static byte[] generateDS(DNSKEYRecord key, int digestid) {
        MessageDigest digest;
        DNSOutput out = new DNSOutput();
        out.writeU16(key.getFootprint());
        out.writeU8(key.getAlgorithm());
        out.writeU8(digestid);
        switch (digestid) {
            case KEYRecord.PROTOCOL_TLS /*1*/:
                digest = MessageDigest.getInstance("sha-1");
                break;
            case ASN1_INT /*2*/:
                digest = MessageDigest.getInstance("sha-256");
                break;
            default:
                try {
                    throw new IllegalArgumentException(new StringBuffer().append("unknown DS digest type ").append(digestid).toString());
                } catch (NoSuchAlgorithmException e) {
                    throw new IllegalStateException("no message digest support");
                }
        }
        digest.update(key.getName().toWire());
        digest.update(key.rdataToWireCanonical());
        out.writeByteArray(digest.digest());
        return out.toByteArray();
    }
}
