package org.xbill.DNS;

public final class Section {
    public static final int ADDITIONAL = 3;
    public static final int ANSWER = 1;
    public static final int AUTHORITY = 2;
    public static final int PREREQ = 1;
    public static final int QUESTION = 0;
    public static final int UPDATE = 2;
    public static final int ZONE = 0;
    private static String[] longSections;
    private static Mnemonic sections;
    private static String[] updateSections;

    static {
        sections = new Mnemonic("Message Section", ADDITIONAL);
        longSections = new String[4];
        updateSections = new String[4];
        sections.setMaximum(ADDITIONAL);
        sections.setNumericAllowed(true);
        sections.add(QUESTION, "qd");
        sections.add(PREREQ, "an");
        sections.add(UPDATE, "au");
        sections.add(ADDITIONAL, "ad");
        longSections[QUESTION] = "QUESTIONS";
        longSections[PREREQ] = "ANSWERS";
        longSections[UPDATE] = "AUTHORITY RECORDS";
        longSections[ADDITIONAL] = "ADDITIONAL RECORDS";
        updateSections[QUESTION] = "ZONE";
        updateSections[PREREQ] = "PREREQUISITES";
        updateSections[UPDATE] = "UPDATE RECORDS";
        updateSections[ADDITIONAL] = "ADDITIONAL RECORDS";
    }

    private Section() {
    }

    public static String string(int i) {
        return sections.getText(i);
    }

    public static String longString(int i) {
        sections.check(i);
        return longSections[i];
    }

    public static String updString(int i) {
        sections.check(i);
        return updateSections[i];
    }

    public static int value(String s) {
        return sections.getValue(s);
    }
}
