package com.fattin.hotspot.activity;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.view.Menu;
import android.view.MenuItem;
import com.fattin.hotspot.C0000R;
import com.fattin.hotspot.app.BusinessLogic;
import com.fattin.hotspot.app.Constants;
import com.fattin.hotspot.app.DataProvider;
import com.fattin.hotspot.app.DetachableResultReceiver;
import com.fattin.hotspot.app.DetachableResultReceiver.Callback;
import com.fattin.hotspot.app.GlobalStates;
import com.fattin.hotspot.app.MainApplication;
import com.fattin.hotspot.helpers.CoreTask;
import com.fattin.hotspot.helpers.Log;
import com.fattin.hotspot.main.MainService;
import com.fattin.hotspot.sm.StateMachine;
import com.google.analytics.tracking.android.EasyTracker;
import org.acra.ACRAConstants;
import org.xbill.DNS.KEYRecord.Flags;

public abstract class AbstractActivity extends Activity {
    protected static final int ACCESS_LIST_REQUEST_CODE = 393218;
    private static final int BASE = 393216;
    protected static final int DIALOG_DATAPLAN_NOTICE = 393229;
    protected static final int DIALOG_MESSAGE = 393227;
    protected static final int DIALOG_MESSAGE_EXIT = 393228;
    protected static final int EULA_REQUEST_CODE = 393220;
    public static final String EXTRA_MESSAGE = "com.fattin.hotspot.app.EXTRA_MESSAGE";
    public static final String EXTRA_RESULT_RECEIVER = "com.fattin.hotspot.app.EXTRA_RESULT_RECEIVER";
    public static final String EXTRA_TITLE = "com.fattin.hotspot.app.EXTRA_TITLE";
    protected static final int PREFERENCE_REQUEST_CODE = 393217;
    protected static final int USER_LOGIN_REQUEST_CODE = 393219;
    protected String LOG_TAG;
    private ActivityResultReceiverCallback mActivityReceiverCallback;
    private DetachableResultReceiver mActivityResultReceiver;
    protected boolean mBound;
    protected final ServiceConnection mConnection;
    protected Messenger mService;
    private ProgressDialog progressDialog;

    /* renamed from: com.fattin.hotspot.activity.AbstractActivity.1 */
    class C00011 implements ServiceConnection {
        C00011() {
        }

        public void onServiceConnected(ComponentName className, IBinder service) {
            AbstractActivity.this.mService = new Messenger(service);
            AbstractActivity.this.mBound = true;
            Log.m0d(AbstractActivity.this.LOG_TAG, "Service bound");
            AbstractActivity.this.sendActivityResultReceiver();
        }

        public void onServiceDisconnected(ComponentName className) {
            AbstractActivity.this.mActivityResultReceiver.clearCallback();
            AbstractActivity.this.mService = null;
            AbstractActivity.this.mBound = false;
            Log.m0d(AbstractActivity.this.LOG_TAG, "Service unbound");
        }
    }

    /* renamed from: com.fattin.hotspot.activity.AbstractActivity.2 */
    class C00022 implements OnClickListener {
        C00022() {
        }

        public void onClick(DialogInterface dialog, int which) {
        }
    }

    /* renamed from: com.fattin.hotspot.activity.AbstractActivity.3 */
    class C00033 implements OnClickListener {
        C00033() {
        }

        public void onClick(DialogInterface dialog, int which) {
            AbstractActivity.this.exitApplication();
        }
    }

    class ActivityResultReceiverCallback implements Callback {
        ActivityResultReceiverCallback() {
        }

        public void onBegin(String title, String message, Bundle resultData) {
            Log.m0d(AbstractActivity.this.LOG_TAG, "ActivityResultReceiverCallback.onBegin");
            AbstractActivity.this.startProgress(title, message);
        }

        public void onSuccess(Bundle resultData) {
            Log.m0d(AbstractActivity.this.LOG_TAG, "ActivityResultReceiverCallback.onSuccess");
            AbstractActivity.this.stopProgress();
            AbstractActivity.this.updateUI();
        }

        public void onServiceStarting(Bundle resultData) {
            Log.m0d(AbstractActivity.this.LOG_TAG, "ActivityResultReceiverCallback.onServiceStarting");
            onBegin("Starting services", "please wait...", resultData);
        }

        public void onServiceStarted(Bundle resultData) {
            Log.m0d(AbstractActivity.this.LOG_TAG, "ActivityResultReceiverCallback.onServiceStarted");
            onSuccess(resultData);
        }

        public void onServiceStartingFailure(String error_msg, Bundle resultData) {
            Log.m0d(AbstractActivity.this.LOG_TAG, "ActivityResultReceiverCallback.onServiceStartingFailure");
            onFailure(error_msg, resultData);
        }

        public void onServiceStopping(Bundle resultData) {
            Log.m0d(AbstractActivity.this.LOG_TAG, "ActivityResultReceiverCallback.onServiceStopping");
            onBegin("Stopping services", "please wait...", resultData);
        }

        public void onServiceStopped(Bundle resultData) {
            Log.m0d(AbstractActivity.this.LOG_TAG, "ActivityResultReceiverCallback.onServiceStopped");
            onSuccess(resultData);
        }

        public void onFailure(String message, Bundle resultData) {
            Log.m0d(AbstractActivity.this.LOG_TAG, "ActivityResultReceiverCallback.onFailure");
            AbstractActivity.this.stopProgress();
            AbstractActivity.this.updateUI();
            if (message != null && !message.isEmpty()) {
                resultData.putString(AbstractActivity.EXTRA_TITLE, "Attention!");
                resultData.putString(AbstractActivity.EXTRA_MESSAGE, message);
                AbstractActivity.this.showDialog(AbstractActivity.DIALOG_MESSAGE, resultData);
            }
        }

        public void onUnauthorized(Bundle resultData) {
            Log.m0d(AbstractActivity.this.LOG_TAG, "ActivityResultReceiverCallback.onUnauthorized");
            AbstractActivity.this.stopProgress();
            AbstractActivity.this.stopBackgroundServices();
            AbstractActivity.this.showLoginActivity();
        }

        public void onReceiveResult(int resultCode, Bundle resultData) {
            Log.m0d(AbstractActivity.this.LOG_TAG, "ActivityResultReceiverCallback.onReceiveResult");
        }
    }

    public AbstractActivity() {
        this.LOG_TAG = "AbstractActivity";
        this.mActivityReceiverCallback = null;
        this.mActivityResultReceiver = null;
        this.mService = null;
        this.mConnection = new C00011();
    }

    protected void startProgress(String title, String message) {
        setProgressBarIndeterminateVisibility(true);
        if (title != null && message != null && !title.isEmpty() && !message.isEmpty()) {
            this.progressDialog.setTitle(title);
            this.progressDialog.setMessage(message);
            try {
                this.progressDialog.show();
            } catch (Exception e) {
                Log.m1e(this.LOG_TAG, e.getMessage(), e);
            }
        }
    }

    protected void stopProgress() {
        setProgressBarIndeterminateVisibility(false);
        try {
            this.progressDialog.dismiss();
        } catch (IllegalArgumentException e) {
        }
    }

    protected synchronized void sendActivityResultReceiver() {
        Log.m0d(this.LOG_TAG, "begin sendActivityResultReceiver");
        this.mActivityResultReceiver.setCallback(this.mActivityReceiverCallback);
        if (this.mBound) {
            Log.m0d(this.LOG_TAG, "sending mActivityResultReceiver ...");
            MainService.sendCommandStoreResultReceiver(this.mService, this.mActivityResultReceiver);
            MainService.getServicesStartingOrStopping(this.mService);
        }
        Log.m0d(this.LOG_TAG, "end sendActivityResultReceiver");
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.m0d(this.LOG_TAG, "onCreate");
        getWindow().clearFlags(Flags.FLAG8);
        requestWindowFeature(5);
        MainApplication.setApplication(getApplication());
        this.progressDialog = new ProgressDialog(this);
        this.mActivityReceiverCallback = new ActivityResultReceiverCallback();
        this.mActivityResultReceiver = new DetachableResultReceiver(new Handler(), this.mActivityReceiverCallback);
    }

    protected void onStart() {
        Log.m0d(this.LOG_TAG, "onStart");
        startService(new Intent(getApplicationContext(), MainService.class));
        bindService(new Intent(getApplicationContext(), MainService.class), this.mConnection, 1);
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
    }

    protected void onResume() {
        Log.m0d(this.LOG_TAG, "onResume");
        setProgressBarIndeterminateVisibility(false);
        stopProgress();
        sendActivityResultReceiver();
        updateUI();
        super.onResume();
    }

    protected void onStop() {
        Log.m0d(this.LOG_TAG, "onStop");
        if (this.mBound) {
            unbindService(this.mConnection);
            this.mBound = false;
            this.mActivityResultReceiver.clearCallback();
            Log.m0d(this.LOG_TAG, "Service unbound");
        }
        super.onStop();
        EasyTracker.getInstance().activityStop(this);
    }

    protected void onDestroy() {
        Log.m0d(this.LOG_TAG, "onDestroy");
        if (this.mBound) {
            unbindService(this.mConnection);
            this.mBound = false;
            this.mActivityResultReceiver.clearCallback();
            Log.m0d(this.LOG_TAG, "Service unbound");
        }
        super.onDestroy();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 1) {
            setResult(1);
            finish();
        }
        switch (requestCode) {
            case USER_LOGIN_REQUEST_CODE /*393219*/:
                switch (resultCode) {
                }
                break;
            case EULA_REQUEST_CODE /*393220*/:
                break;
            default:
                return;
        }
        switch (resultCode) {
            case StateMachine.SM_QUIT_CMD /*-1*/:
                showLoginActivity();
            default:
                setResult(1);
                finish();
        }
    }

    protected Dialog onCreateDialog(int id, Bundle args) {
        String msg = ACRAConstants.DEFAULT_STRING_VALUE;
        String title = ACRAConstants.DEFAULT_STRING_VALUE;
        switch (id) {
            case DIALOG_MESSAGE /*393227*/:
                return new Builder(this).setTitle(args.getString(EXTRA_TITLE)).setMessage(args.getString(EXTRA_MESSAGE)).setPositiveButton(C0000R.string.button_ok, new C00022()).create();
            case DIALOG_MESSAGE_EXIT /*393228*/:
                return new Builder(this).setTitle(title).setMessage(args.getString(EXTRA_MESSAGE)).setPositiveButton(C0000R.string.button_ok, new C00033()).create();
            default:
                return super.onCreateDialog(id, args);
        }
    }

    public final boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(C0000R.menu.contextmenu, menu);
        return true;
    }

    public final boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case C0000R.id.menuWithdraw:
                Intent i = new Intent();
                i.setData(Uri.parse(new StringBuilder(Constants.PAYOUT_SERVER_URL).append(DataProvider.getUserAuthenticationToken()).toString()));
                i.setClass(this, WebViewActivity.class);
                startActivity(i);
                return true;
            case C0000R.id.menuPreference:
                startActivityForResult(new Intent(this, MainPreferenceActivity.class), PREFERENCE_REQUEST_CODE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected final boolean startupCheck() {
        if (!MainApplication.getApplication().startupCheckPerformed || MainApplication.getApplication().startupCheckFailed) {
            MainApplication.getApplication().startupCheckPerformed = true;
            EasyTracker.getInstance().setContext(this);
            if (!CoreTask.isNetfilterSupported()) {
                MainApplication.getApplication().accessControlSupported = false;
                MainApplication.getApplication().startupCheckFailed = true;
                EasyTracker.getTracker().sendEvent("device_features", "missing", "netfilter", null);
                showStartupCheckDialog(getString(C0000R.string.missing_feature_netfilter), true);
                return false;
            } else if (!CoreTask.isAccessControlSupported()) {
                MainApplication.getApplication().accessControlSupported = false;
                MainApplication.getApplication().startupCheckFailed = true;
                EasyTracker.getTracker().sendEvent("device_features", "missing", "access_control", null);
                showStartupCheckDialog(getString(C0000R.string.missing_feature_access_control), true);
                return false;
            } else if (CoreTask.hasRootPermission()) {
                Log.m0d(this.LOG_TAG, "onCreate -> installing binaries");
                MainApplication.getApplication().installFiles();
                MainApplication.getApplication().startupCheckFailed = false;
            } else {
                MainApplication.getApplication().startupCheckFailed = true;
                EasyTracker.getTracker().sendEvent("device_features", "missing", "root_permission", null);
                showStartupCheckDialog(getString(C0000R.string.missing_feature_root_permission), true);
                return false;
            }
        }
        if (MainApplication.getApplication().startupCheckFailed) {
            return false;
        }
        return true;
    }

    protected void updateUI() {
        setProgressBarIndeterminateVisibility(false);
    }

    protected void showEula() {
        if (!DataProvider.getEulaAgreed()) {
            startActivityForResult(new Intent(this, EulaActivity.class), EULA_REQUEST_CODE);
        }
    }

    protected final void showLoginActivity() {
        if (!DataProvider.isUserLoggedIn()) {
            startActivityForResult(new Intent(this, UserSignInActivity.class), USER_LOGIN_REQUEST_CODE);
        } else if (!GlobalStates.isServiceStartingOrStarted()) {
            BusinessLogic.processUserTokenValidation(this.mActivityReceiverCallback);
        }
    }

    protected final void showStartupCheckDialog(String msg, boolean exit) {
        if (!msg.isEmpty()) {
            Bundle b = new Bundle();
            b.putString(EXTRA_MESSAGE, msg);
            if (exit) {
                showDialog(DIALOG_MESSAGE_EXIT, b);
            } else {
                showDialog(DIALOG_MESSAGE, b);
            }
        }
    }

    protected final void exitApplication() {
        exitBackgroundServices();
        setResult(1);
        finish();
    }

    public final void startBackgroundServices() {
        Log.m0d(this.LOG_TAG, "startBackgroundServices");
        if (this.mBound) {
            MainService.sendCommandStartBackgroundServices(this.mService);
            EasyTracker.getTracker().sendEvent("ui_action", "button_press", "start_paywall", null);
        }
    }

    public final void stopBackgroundServices() {
        Log.m0d(this.LOG_TAG, "stopBackgroundServices");
        if (this.mBound) {
            MainService.sendCommandStopBackgroundServices(this.mService);
            EasyTracker.getTracker().sendEvent("ui_action", "button_press", "stop_paywall", null);
        }
    }

    public final void exitBackgroundServices() {
        Log.m0d(this.LOG_TAG, "stopBackgroundServices");
        if (this.mBound) {
            MainService.sendCommandExit(this.mService);
        }
    }
}
