package com.fattin.hotspot.app;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;
import com.fattin.hotspot.C0000R;
import com.fattin.hotspot.helpers.CoreTask;
import com.fattin.hotspot.helpers.Log;
import com.fattin.hotspot.helpers.Util;
import java.io.File;
import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(formKey = "dEYwUjR5WTlsdk1mOXNZUWlNZVJ4WEE6MQ", mode = ReportingInteractionMode.TOAST, resToastText = 2131099714)
public class MainApplication extends Application {
    private static final String LOG_TAG = "MainApplication";
    public static int UID;
    protected static MainApplication application;
    private static Context context;
    public boolean accessControlSupported;
    public boolean startupCheckFailed;
    public boolean startupCheckPerformed;

    /* renamed from: com.fattin.hotspot.app.MainApplication.1 */
    class C00241 implements Runnable {
        C00241() {
        }

        public void run() {
            if (null == null) {
                String message = Util.copyFile(CoreTask.DATA_FILE_PATH + "/bin/iptables", "0755", C0000R.raw.iptables);
                message = Util.copyFile(CoreTask.DATA_FILE_PATH + "/bin/ssl.key", "0644", C0000R.raw.pkcs12_key);
            }
            if (!Util.busyboxExist()) {
                Util.busyboxInstall();
            }
        }
    }

    public MainApplication() {
        this.startupCheckPerformed = false;
        this.accessControlSupported = true;
        this.startupCheckFailed = false;
    }

    static {
        context = null;
        application = null;
    }

    public static Context getAppContext() {
        return context;
    }

    public static void setApplication(Application a) {
        if (application == null) {
            application = (MainApplication) a;
        }
    }

    public static MainApplication getApplication() {
        return application;
    }

    public void onCreate() {
        ACRA.init(this);
        super.onCreate();
        Log.m0d(LOG_TAG, "onCreate -> begin");
        if (context == null) {
            context = getApplicationContext();
        }
        if (application == null) {
            application = this;
        }
        CoreTask.setPath(getApplicationContext().getFilesDir().getParent());
        checkDirs();
        UID = getApplicationInfo().uid;
        Log.m3v(LOG_TAG, "UID=" + UID);
        Log.m0d(LOG_TAG, "onCreate -> end");
    }

    public void displayToastMessage(String message) {
        Toast.makeText(getAppContext(), message, 1).show();
    }

    private void checkDirs() {
        int i = 0;
        if (new File(CoreTask.DATA_FILE_PATH).exists()) {
            String[] dirs = new String[]{"/bin", "/var", "/conf"};
            int length = dirs.length;
            while (i < length) {
                String dirname = dirs[i];
                File dir = new File(CoreTask.DATA_FILE_PATH + dirname);
                if (dir.exists()) {
                    Log.m0d(CoreTask.LOG_TAG, "Directory '" + dir.getAbsolutePath() + "' already exists!");
                } else if (!dir.mkdir()) {
                    displayToastMessage("Couldn't create " + dirname + " directory!");
                }
                i++;
            }
            return;
        }
        displayToastMessage("Application data-dir does not exist!");
    }

    public void installFiles() {
        new Thread(new C00241()).start();
    }
}
