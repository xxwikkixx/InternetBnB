package com.fattin.hotspot.activity;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import com.fattin.hotspot.C0000R;
import com.fattin.hotspot.app.BusinessLogic;
import com.fattin.hotspot.app.Constants;
import com.fattin.hotspot.app.DataProvider;
import com.fattin.hotspot.app.DetachableResultReceiver;
import com.fattin.hotspot.app.DetachableResultReceiver.Callback;
import com.fattin.hotspot.app.MainApplication;
import com.fattin.hotspot.helpers.Log;
import com.fattin.hotspot.main.MainService;
import com.google.analytics.tracking.android.EasyTracker;
import org.acra.ACRAConstants;

public class MainPreferenceActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    protected final String LOG_TAG;
    private PreferenceScreen login_preference;
    private Preference logout_preference;
    private ActivityResultReceiverCallback mActivityReceiverCallback;
    private DetachableResultReceiver mActivityResultReceiver;
    protected boolean mBound;
    private BusinessLogicApiCallback mBusinessLogicApiCallback;
    protected final ServiceConnection mConnection;
    protected Messenger mService;
    private String previous_price;
    private EditTextPreference price_preference;
    private ProgressDialog progressDialog;

    /* renamed from: com.fattin.hotspot.activity.MainPreferenceActivity.1 */
    class C00061 implements ServiceConnection {
        C00061() {
        }

        public void onServiceConnected(ComponentName className, IBinder service) {
            MainPreferenceActivity.this.mService = new Messenger(service);
            MainPreferenceActivity.this.mBound = true;
            Log.m0d("MainPreferenceActivity", "Service bound");
            MainPreferenceActivity.this.sendActivityResultReceiver();
        }

        public void onServiceDisconnected(ComponentName className) {
            MainPreferenceActivity.this.mActivityResultReceiver.clearCallback();
            MainPreferenceActivity.this.mService = null;
            MainPreferenceActivity.this.mBound = false;
            Log.m0d("MainPreferenceActivity", "Service unbound");
        }
    }

    class ActivityResultReceiverCallback implements Callback {
        ActivityResultReceiverCallback() {
        }

        public void onBegin(String title, String message, Bundle resultData) {
            Log.m0d("MainPreferenceActivity", "ActivityResultReceiverCallback.onBegin");
            MainPreferenceActivity.this.setProgressBarIndeterminateVisibility(true);
            MainPreferenceActivity.this.progressDialog.setTitle(title);
            MainPreferenceActivity.this.progressDialog.setMessage(message);
            try {
                MainPreferenceActivity.this.progressDialog.show();
            } catch (Exception e) {
                Log.m1e("MainPreferenceActivity", e.getMessage(), e);
            }
        }

        public void onSuccess(Bundle resultData) {
            Log.m0d("MainPreferenceActivity", "ActivityResultReceiverCallback.onSuccess");
            MainPreferenceActivity.this.stopProgress();
            MainPreferenceActivity.this.updateLoginPref();
        }

        public void onServiceStarting(Bundle resultData) {
            Log.m0d("MainPreferenceActivity", "ActivityResultReceiverCallback.onServiceStarting");
            onBegin("Starting services...", "...please wait a moment.", resultData);
        }

        public void onServiceStarted(Bundle resultData) {
            Log.m0d("MainPreferenceActivity", "ActivityResultReceiverCallback.onServiceStarted");
            onSuccess(resultData);
        }

        public void onServiceStartingFailure(String error_msg, Bundle resultData) {
            Log.m0d("MainPreferenceActivity", "ActivityResultReceiverCallback.onServiceStartingFailure");
            onFailure(error_msg, resultData);
        }

        public void onServiceStopping(Bundle resultData) {
            Log.m0d("MainPreferenceActivity", "ActivityResultReceiverCallback.onServiceStopping");
            onBegin("Stopping services...", "...please wait a moment.", resultData);
        }

        public void onServiceStopped(Bundle resultData) {
            Log.m0d("MainPreferenceActivity", "ActivityResultReceiverCallback.onServiceStopped");
            onSuccess(resultData);
        }

        public void onFailure(String message, Bundle resultData) {
            Log.m0d("MainPreferenceActivity", "ActivityResultReceiverCallback.onFailure");
            MainPreferenceActivity.this.stopProgress();
            MainPreferenceActivity.this.updateLoginPref();
            if (message != null && !message.isEmpty()) {
                resultData.putString(AbstractActivity.EXTRA_MESSAGE, message);
                MainPreferenceActivity.this.showDialog(393227, resultData);
            }
        }

        public void onUnauthorized(Bundle resultData) {
            Log.m0d("MainPreferenceActivity", "ActivityResultReceiverCallback.onUnauthorized");
            MainPreferenceActivity.this.stopProgress();
            MainPreferenceActivity.this.stopBackgroundServices();
            MainPreferenceActivity.this.updateLoginPref();
        }

        public void onReceiveResult(int resultCode, Bundle resultData) {
        }
    }

    class BusinessLogicApiCallback implements Callback {
        BusinessLogicApiCallback() {
        }

        public void onBegin(String title, String message, Bundle resultData) {
            Log.m0d("MainPreferenceActivity", "ActivityResultReceiverCallback.onBegin");
            MainPreferenceActivity.this.setProgressBarIndeterminateVisibility(true);
            MainPreferenceActivity.this.progressDialog.setTitle(title);
            MainPreferenceActivity.this.progressDialog.setMessage(message);
            try {
                MainPreferenceActivity.this.progressDialog.show();
            } catch (Exception e) {
                Log.m1e("MainPreferenceActivity", e.getMessage(), e);
            }
        }

        public void onSuccess(Bundle resultData) {
            Log.m0d("MainPreferenceActivity", "ActivityResultReceiverCallback.onSuccess");
            MainPreferenceActivity.this.stopProgress();
            try {
                if (Float.parseFloat(MainPreferenceActivity.this.price_preference.getText()) > 0.0f) {
                    MainPreferenceActivity.this.previous_price = MainPreferenceActivity.this.price_preference.getText().toString();
                }
            } catch (NumberFormatException e) {
                Log.m1e("MainPreferenceActivity", e.getMessage(), e);
            }
            MainPreferenceActivity.this.updateLoginPref();
        }

        public void onServiceStarting(Bundle resultData) {
        }

        public void onServiceStarted(Bundle resultData) {
        }

        public void onServiceStartingFailure(String error_msg, Bundle resultData) {
        }

        public void onServiceStopping(Bundle resultData) {
        }

        public void onServiceStopped(Bundle resultData) {
        }

        public void onFailure(String message, Bundle resultData) {
            Log.m0d("MainPreferenceActivity", "ActivityResultReceiverCallback.onFailure");
            MainPreferenceActivity.this.stopProgress();
            MainPreferenceActivity.this.price_preference.setText(MainPreferenceActivity.this.previous_price);
            if (!(message == null || message.isEmpty())) {
                resultData.putString(AbstractActivity.EXTRA_MESSAGE, message);
                MainPreferenceActivity.this.showDialog(393227, resultData);
            }
            MainPreferenceActivity.this.updateLoginPref();
        }

        public void onReceiveResult(int resultCode, Bundle resultData) {
        }

        public void onUnauthorized(Bundle resultData) {
            Log.m0d("MainPreferenceActivity", "ActivityResultReceiverCallback.onUnauthorized");
            MainPreferenceActivity.this.stopProgress();
            MainPreferenceActivity.this.stopBackgroundServices();
            MainPreferenceActivity.this.updateLoginPref();
        }
    }

    public MainPreferenceActivity() {
        this.LOG_TAG = "MainPreferenceActivity";
        this.mActivityReceiverCallback = null;
        this.mActivityResultReceiver = null;
        this.mBusinessLogicApiCallback = null;
        this.mService = null;
        this.mConnection = new C00061();
    }

    protected void stopProgress() {
        setProgressBarIndeterminateVisibility(false);
        try {
            this.progressDialog.dismiss();
        } catch (IllegalArgumentException e) {
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(5);
        super.onCreate(savedInstanceState);
        this.progressDialog = new ProgressDialog(this);
        this.mActivityReceiverCallback = new ActivityResultReceiverCallback();
        this.mActivityResultReceiver = new DetachableResultReceiver(new Handler(), this.mActivityReceiverCallback);
        this.mBusinessLogicApiCallback = new BusinessLogicApiCallback();
        addPreferencesFromResource(C0000R.xml.preference);
    }

    protected void onStart() {
        Log.m0d("MainPreferenceActivity", "onStart");
        startService(new Intent(getApplicationContext(), MainService.class));
        bindService(new Intent(getApplicationContext(), MainService.class), this.mConnection, 1);
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
    }

    protected void onResume() {
        super.onResume();
        setProgressBarIndeterminateVisibility(false);
        stopProgress();
        sendActivityResultReceiver();
        this.login_preference = (PreferenceScreen) getPreferenceScreen().findPreference(Constants.PREF_KEY_LOGIN);
        this.logout_preference = getPreferenceScreen().findPreference(Constants.PREF_KEY_LOGOUT);
        this.price_preference = (EditTextPreference) getPreferenceScreen().findPreference(Constants.PREF_KEY_HOTSPOT_PRICE);
        updateLoginPref();
        if (this.price_preference.getText() != null) {
            this.previous_price = this.price_preference.getText().toString();
        }
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    protected void onStop() {
        Log.m0d("MainPreferenceActivity", "onStop");
        if (this.mBound) {
            unbindService(this.mConnection);
            this.mBound = false;
            this.mActivityResultReceiver.clearCallback();
            Log.m0d("MainPreferenceActivity", "Service unbound");
        }
        super.onStop();
        EasyTracker.getInstance().activityStop(this);
    }

    protected void updateLoginPref() {
        boolean isLoggedOut;
        boolean z;
        boolean z2 = false;
        if (DataProvider.isUserLoggedIn()) {
            isLoggedOut = false;
        } else {
            isLoggedOut = true;
        }
        this.login_preference.setSummary(DataProvider.getEmail());
        this.price_preference.setSummary(this.price_preference.getText());
        this.login_preference.setEnabled(isLoggedOut);
        this.login_preference.setSelectable(isLoggedOut);
        Preference preference = this.logout_preference;
        if (isLoggedOut) {
            z = false;
        } else {
            z = true;
        }
        preference.setEnabled(z);
        preference = this.logout_preference;
        if (isLoggedOut) {
            z = false;
        } else {
            z = true;
        }
        preference.setSelectable(z);
        EditTextPreference editTextPreference = this.price_preference;
        if (isLoggedOut) {
            z = false;
        } else {
            z = true;
        }
        editTextPreference.setEnabled(z);
        EditTextPreference editTextPreference2 = this.price_preference;
        if (!isLoggedOut) {
            z2 = true;
        }
        editTextPreference2.setSelectable(z2);
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == this.logout_preference) {
            BusinessLogic.processUserLogout(this.mBusinessLogicApiCallback);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.m0d("MainPreferenceActivity", "onSharedPreferenceChanged");
        if (key.equals(Constants.PREF_KEY_HOTSPOT_PRICE) && DataProvider.isUserLoggedIn()) {
            String price_per_hour = sharedPreferences.getString(key, ACRAConstants.DEFAULT_STRING_VALUE);
            try {
                Log.m0d("MainPreferenceActivity", "onSharedPreferenceChanged; price_per_hour=" + price_per_hour);
                Log.m0d("MainPreferenceActivity", "onSharedPreferenceChanged; previous_price=" + Float.parseFloat(this.previous_price));
                if (Float.parseFloat(price_per_hour) != Float.parseFloat(this.previous_price)) {
                    if (Float.parseFloat(price_per_hour) > 0.0f) {
                        Log.m0d("MainPreferenceActivity", "onSharedPreferenceChanged; before BusinessLogic.processHotspotSetPrice() call");
                        BusinessLogic.processHotspotSetPrice(price_per_hour, this.mBusinessLogicApiCallback);
                    } else {
                        Log.m0d("MainPreferenceActivity", "onSharedPreferenceChanged; BusinessLogic.processHotspotSetPrice() not called due to invalid price");
                        MainApplication.getApplication().displayToastMessage("Price not valid (" + Float.parseFloat(price_per_hour) + ")");
                        this.price_preference.setText(this.previous_price);
                    }
                }
            } catch (NumberFormatException e) {
                Log.m1e("MainPreferenceActivity", e.getMessage(), e);
                this.price_preference.setText(this.previous_price);
                MainApplication.getApplication().displayToastMessage("Invalid Price(" + price_per_hour + ")");
            } catch (Exception e2) {
                Log.m1e("MainPreferenceActivity", e2.getMessage(), e2);
                this.price_preference.setText(this.previous_price);
            }
            this.price_preference.setSummary(this.price_preference.getText());
        }
    }

    protected synchronized void sendActivityResultReceiver() {
        Log.m0d("MainPreferenceActivity", "begin sendActivityResultReceiver");
        this.mActivityResultReceiver.setCallback(this.mActivityReceiverCallback);
        if (this.mBound) {
            Log.m0d("MainPreferenceActivity", "sending mActivityResultReceiver ...");
            MainService.sendCommandStoreResultReceiver(this.mService, this.mActivityResultReceiver);
            MainService.getServicesStartingOrStopping(this.mService);
        }
        Log.m0d("MainPreferenceActivity", "end sendActivityResultReceiver");
    }

    public final void stopBackgroundServices() {
        Log.m0d("MainPreferenceActivity", "stopBackgroundServices");
        if (this.mBound) {
            MainService.sendCommandStopBackgroundServices(this.mService);
        }
    }
}
