package org.xbill.DNS;

public class NSRecord extends SingleCompressedNameBase {
    private static final long serialVersionUID = 487170758138268838L;

    NSRecord() {
    }

    Record getObject() {
        return new NSRecord();
    }

    public NSRecord(Name name, int dclass, long ttl, Name target) {
        super(name, 2, dclass, ttl, target, "target");
    }

    public Name getTarget() {
        return getSingleName();
    }

    public Name getAdditionalName() {
        return getSingleName();
    }
}
