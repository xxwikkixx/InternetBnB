package org.acra;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;
import org.acra.annotation.ReportsCrashes;
import org.xbill.DNS.KEYRecord;

public class ACRA {
    public static final ReportField[] DEFAULT_MAIL_REPORT_FIELDS;
    public static final ReportField[] DEFAULT_REPORT_FIELDS;
    public static final boolean DEV_LOGGING = false;
    public static final String LOG_TAG;
    public static final String PREF_ALWAYS_ACCEPT = "acra.alwaysaccept";
    public static final String PREF_DISABLE_ACRA = "acra.disable";
    public static final String PREF_ENABLE_ACRA = "acra.enable";
    public static final String PREF_ENABLE_DEVICE_ID = "acra.deviceid.enable";
    public static final String PREF_ENABLE_SYSTEM_LOGS = "acra.syslog.enable";
    public static final String PREF_LAST_VERSION_NR = "acra.lastVersionNr";
    public static final String PREF_USER_EMAIL_ADDRESS = "acra.user.email";
    private static ACRAConfiguration configProxy;
    private static ErrorReporter errorReporterSingleton;
    private static Application mApplication;
    private static OnSharedPreferenceChangeListener mPrefListener;
    private static ReportsCrashes mReportsCrashes;

    static class 1 implements OnSharedPreferenceChangeListener {
        1() {
        }

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (ACRA.PREF_DISABLE_ACRA.equals(key) || ACRA.PREF_ENABLE_ACRA.equals(key)) {
                ACRA.getErrorReporter().setEnabled(!ACRA.shouldDisableACRA(sharedPreferences) ? true : ACRA.DEV_LOGGING);
            }
        }
    }

    static /* synthetic */ class 2 {
        static final /* synthetic */ int[] $SwitchMap$org$acra$ReportingInteractionMode;

        static {
            $SwitchMap$org$acra$ReportingInteractionMode = new int[ReportingInteractionMode.values().length];
            try {
                $SwitchMap$org$acra$ReportingInteractionMode[ReportingInteractionMode.TOAST.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$org$acra$ReportingInteractionMode[ReportingInteractionMode.NOTIFICATION.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$org$acra$ReportingInteractionMode[ReportingInteractionMode.DIALOG.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    static {
        LOG_TAG = ACRA.class.getSimpleName();
        DEFAULT_MAIL_REPORT_FIELDS = new ReportField[]{ReportField.USER_COMMENT, ReportField.ANDROID_VERSION, ReportField.APP_VERSION_NAME, ReportField.BRAND, ReportField.PHONE_MODEL, ReportField.CUSTOM_DATA, ReportField.STACK_TRACE};
        DEFAULT_REPORT_FIELDS = new ReportField[]{ReportField.REPORT_ID, ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME, ReportField.PACKAGE_NAME, ReportField.FILE_PATH, ReportField.PHONE_MODEL, ReportField.BRAND, ReportField.PRODUCT, ReportField.ANDROID_VERSION, ReportField.BUILD, ReportField.TOTAL_MEM_SIZE, ReportField.AVAILABLE_MEM_SIZE, ReportField.CUSTOM_DATA, ReportField.IS_SILENT, ReportField.STACK_TRACE, ReportField.INITIAL_CONFIGURATION, ReportField.CRASH_CONFIGURATION, ReportField.DISPLAY, ReportField.USER_COMMENT, ReportField.USER_EMAIL, ReportField.USER_APP_START_DATE, ReportField.USER_CRASH_DATE, ReportField.DUMPSYS_MEMINFO, ReportField.LOGCAT, ReportField.INSTALLATION_ID, ReportField.DEVICE_FEATURES, ReportField.ENVIRONMENT, ReportField.SHARED_PREFERENCES, ReportField.SETTINGS_SYSTEM, ReportField.SETTINGS_SECURE};
    }

    public static void init(Application app) {
        if (mApplication != null) {
            throw new IllegalStateException("ACRA#init called more than once");
        }
        mApplication = app;
        mReportsCrashes = (ReportsCrashes) mApplication.getClass().getAnnotation(ReportsCrashes.class);
        if (mReportsCrashes == null) {
            Log.e(LOG_TAG, "ACRA#init called but no ReportsCrashes annotation on Application " + mApplication.getPackageName());
            return;
        }
        SharedPreferences prefs = getACRASharedPreferences();
        try {
            checkCrashResources();
            Log.d(LOG_TAG, "ACRA is enabled for " + mApplication.getPackageName() + ", intializing...");
            ErrorReporter errorReporter = new ErrorReporter(mApplication.getApplicationContext(), prefs, !shouldDisableACRA(prefs) ? true : DEV_LOGGING);
            errorReporter.setDefaultReportSenders();
            errorReporterSingleton = errorReporter;
        } catch (ACRAConfigurationException e) {
            Log.w(LOG_TAG, "Error : ", e);
        }
        mPrefListener = new 1();
        prefs.registerOnSharedPreferenceChangeListener(mPrefListener);
    }

    public static ErrorReporter getErrorReporter() {
        if (errorReporterSingleton != null) {
            return errorReporterSingleton;
        }
        throw new IllegalStateException("Cannot access ErrorReporter before ACRA#init");
    }

    private static boolean shouldDisableACRA(SharedPreferences prefs) {
        boolean z = true;
        boolean disableAcra = DEV_LOGGING;
        try {
            boolean enableAcra = prefs.getBoolean(PREF_ENABLE_ACRA, true);
            String str = PREF_DISABLE_ACRA;
            if (enableAcra) {
                z = DEV_LOGGING;
            }
            disableAcra = prefs.getBoolean(str, z);
        } catch (Exception e) {
        }
        return disableAcra;
    }

    static void checkCrashResources() throws ACRAConfigurationException {
        ReportsCrashes conf = getConfig();
        switch (2.$SwitchMap$org$acra$ReportingInteractionMode[conf.mode().ordinal()]) {
            case KEYRecord.PROTOCOL_TLS /*1*/:
                if (conf.resToastText() == 0) {
                    throw new ACRAConfigurationException("TOAST mode: you have to define the resToastText parameter in your application @ReportsCrashes() annotation.");
                }
            case KEYRecord.PROTOCOL_EMAIL /*2*/:
                if (conf.resNotifTickerText() == 0 || conf.resNotifTitle() == 0 || conf.resNotifText() == 0 || conf.resDialogText() == 0) {
                    throw new ACRAConfigurationException("NOTIFICATION mode: you have to define at least the resNotifTickerText, resNotifTitle, resNotifText, resDialogText parameters in your application @ReportsCrashes() annotation.");
                }
            case KEYRecord.PROTOCOL_DNSSEC /*3*/:
                if (conf.resDialogText() == 0) {
                    throw new ACRAConfigurationException("DIALOG mode: you have to define at least the resDialogText parameters in your application @ReportsCrashes() annotation.");
                }
            default:
        }
    }

    public static SharedPreferences getACRASharedPreferences() {
        ReportsCrashes conf = getConfig();
        if (ACRAConstants.DEFAULT_STRING_VALUE.equals(conf.sharedPreferencesName())) {
            return PreferenceManager.getDefaultSharedPreferences(mApplication);
        }
        return mApplication.getSharedPreferences(conf.sharedPreferencesName(), conf.sharedPreferencesMode());
    }

    public static ACRAConfiguration getConfig() {
        if (configProxy == null) {
            if (mApplication == null) {
                Log.w(LOG_TAG, "Calling ACRA.getConfig() before ACRA.init() gives you an empty configuration instance. You might prefer calling ACRA.getNewDefaultConfig(Application) to get an instance with default values taken from a @ReportsCrashes annotation.");
            }
            configProxy = getNewDefaultConfig(mApplication);
        }
        return configProxy;
    }

    public static void setConfig(ACRAConfiguration conf) {
        configProxy = conf;
    }

    public static ACRAConfiguration getNewDefaultConfig(Application app) {
        return new ACRAConfiguration((ReportsCrashes) app.getClass().getAnnotation(ReportsCrashes.class));
    }

    static boolean isDebuggable() {
        try {
            if ((mApplication.getPackageManager().getApplicationInfo(mApplication.getPackageName(), 0).flags & 2) > 0) {
                return true;
            }
            return DEV_LOGGING;
        } catch (NameNotFoundException e) {
            return DEV_LOGGING;
        }
    }

    static Application getApplication() {
        return mApplication;
    }
}
