package com.fattin.hotspot.helpers;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;
import org.acra.ACRAConstants;

public class Installation {
    private static final String INSTALLATION = "INSTALLATION";
    private static String sID;

    static {
        sID = ACRAConstants.DEFAULT_STRING_VALUE;
    }

    public static synchronized String id(Context context) {
        String str;
        synchronized (Installation.class) {
            if (sID.isEmpty()) {
                File installation = new File(context.getFilesDir(), INSTALLATION);
                try {
                    if (!installation.exists()) {
                        writeInstallationFile(installation);
                    }
                    sID = readInstallationFile(installation);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            str = sID;
        }
        return str;
    }

    private static String readInstallationFile(File installation) throws IOException {
        RandomAccessFile f = new RandomAccessFile(installation, "r");
        byte[] bytes = new byte[((int) f.length())];
        f.readFully(bytes);
        f.close();
        return new String(bytes);
    }

    private static void writeInstallationFile(File installation) throws IOException {
        FileOutputStream out = new FileOutputStream(installation);
        out.write(UUID.randomUUID().toString().getBytes());
        out.close();
    }
}
