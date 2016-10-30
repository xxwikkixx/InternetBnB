package com.fattin.hotspot.main;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import com.fattin.hotspot.C0000R;
import com.fattin.hotspot.apilib.APIs;
import com.fattin.hotspot.apilib.RestResponse;
import com.fattin.hotspot.app.MainApplication;
import com.fattin.hotspot.connectivity.TetherManager;
import com.fattin.hotspot.connectivity.WifiAPManager;
import com.fattin.hotspot.helpers.Log;
import com.fattin.hotspot.sm.State;
import com.fattin.hotspot.sm.StateMachine;

public class PrerequisitesStateMachine extends StateMachine {
    private static final int BASE = 425984;
    private static final int CMD_CHECKING_PREREQUISITES = 425985;
    private static final int CMD_CHECK_PREREQUISITES = 425986;
    public static final int EVENT_API_SERVICES_REACHABLE_CHECKED = 425997;
    public static final int EVENT_NETWORK_CONNECTIVITY_CHECKED = 425998;
    public static final int EVENT_PREREQUISITES_CHECKED = 425995;
    public static final int EVENT_WIFIAP_CHECKED = 425996;
    private static final String LOG_TAG = "PrerequisitesStateMachine";
    private CheckedState mCheckedState;
    private CheckingState mCheckingState;
    private Context mContext;
    private DefaultState mDefaultState;
    private MainStateMachine mMainStateMachine;

    private class CheckedState extends State {
        private CheckedState() {
        }

        public void enter() {
            Log.m3v(PrerequisitesStateMachine.LOG_TAG, "CheckedState.enter");
        }

        public boolean processMessage(Message msg) {
            Log.m3v(PrerequisitesStateMachine.LOG_TAG, "CheckedState.processMessage " + msg.what);
            switch (msg.what) {
                case PrerequisitesStateMachine.CMD_CHECKING_PREREQUISITES /*425985*/:
                    break;
                case PrerequisitesStateMachine.CMD_CHECK_PREREQUISITES /*425986*/:
                    PrerequisitesStateMachine.this.deferMessage(msg);
                    PrerequisitesStateMachine.this.transitionTo(PrerequisitesStateMachine.this.mDefaultState);
                    break;
                case PrerequisitesStateMachine.EVENT_PREREQUISITES_CHECKED /*425995*/:
                    Log.m0d(PrerequisitesStateMachine.LOG_TAG, "CheckedState.handleMessage EVENT_PREREQUISITES_CHECKED");
                    PrerequisitesStateMachine.this.mMainStateMachine.sendEventPrerequisitesChecked();
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            Log.m3v(PrerequisitesStateMachine.LOG_TAG, "CheckedState.exit");
        }
    }

    private class CheckingState extends State {
        private CheckingState() {
        }

        public void enter() {
            Log.m3v(PrerequisitesStateMachine.LOG_TAG, "CheckingState.enter");
        }

        public boolean processMessage(Message msg) {
            Log.m3v(PrerequisitesStateMachine.LOG_TAG, "CheckingState.processMessage " + msg.what);
            switch (msg.what) {
                case PrerequisitesStateMachine.CMD_CHECKING_PREREQUISITES /*425985*/:
                    Log.m0d(PrerequisitesStateMachine.LOG_TAG, "CheckingState.handleMessage CMD_CHECKING_PREREQUISITES");
                    if (PrerequisitesStateMachine.this.checkAndStartWifiAP()) {
                        Log.m0d(PrerequisitesStateMachine.LOG_TAG, "CheckingState.handleMessage checkAndStartWifiAP result=true");
                        PrerequisitesStateMachine.this.sendMessage((int) PrerequisitesStateMachine.EVENT_WIFIAP_CHECKED);
                        return true;
                    }
                    Log.m0d(PrerequisitesStateMachine.LOG_TAG, "CheckingState.handleMessage checkAndStartWifiAP result=false");
                    PrerequisitesStateMachine.this.mMainStateMachine.sendEventPrerequisitesCheckFailure(PrerequisitesStateMachine.this.mContext.getString(C0000R.string.prerequisites_message_wifi_ap_check_failed));
                    PrerequisitesStateMachine.this.transitionTo(PrerequisitesStateMachine.this.mDefaultState);
                    return true;
                case PrerequisitesStateMachine.CMD_CHECK_PREREQUISITES /*425986*/:
                    return true;
                case PrerequisitesStateMachine.EVENT_WIFIAP_CHECKED /*425996*/:
                    Log.m0d(PrerequisitesStateMachine.LOG_TAG, "CheckingState.handleMessage EVENT_WIFIAP_CHECKED");
                    if (!TetherManager.getSingleton().isMobileDataEnabled()) {
                        Log.m0d(PrerequisitesStateMachine.LOG_TAG, "CheckingState.handleMessage isMobileDataEnabled result=false");
                        PrerequisitesStateMachine.this.mMainStateMachine.sendEventPrerequisitesCheckFailure(PrerequisitesStateMachine.this.mContext.getString(C0000R.string.prerequisites_message_mobile_data_enabled_check_failed));
                        PrerequisitesStateMachine.this.transitionTo(PrerequisitesStateMachine.this.mDefaultState);
                        return true;
                    } else if (PrerequisitesStateMachine.this.isNetworkAvailable()) {
                        Log.m0d(PrerequisitesStateMachine.LOG_TAG, "CheckingState.handleMessage isNetworkAvailable result=true");
                        PrerequisitesStateMachine.this.sendMessage((int) PrerequisitesStateMachine.EVENT_NETWORK_CONNECTIVITY_CHECKED);
                        return true;
                    } else {
                        Log.m0d(PrerequisitesStateMachine.LOG_TAG, "CheckingState.handleMessage isNetworkAvailable result=false");
                        PrerequisitesStateMachine.this.mMainStateMachine.sendEventPrerequisitesCheckFailure(PrerequisitesStateMachine.this.mContext.getString(C0000R.string.prerequisites_message_mobile_data_connected_check_failed));
                        PrerequisitesStateMachine.this.transitionTo(PrerequisitesStateMachine.this.mDefaultState);
                        return true;
                    }
                case PrerequisitesStateMachine.EVENT_API_SERVICES_REACHABLE_CHECKED /*425997*/:
                    Log.m0d(PrerequisitesStateMachine.LOG_TAG, "CheckingState.handleMessage EVENT_API_SERVICES_REACHABLE_CHECKED");
                    PrerequisitesStateMachine.this.sendMessage((int) PrerequisitesStateMachine.EVENT_PREREQUISITES_CHECKED);
                    PrerequisitesStateMachine.this.transitionTo(PrerequisitesStateMachine.this.mCheckedState);
                    return true;
                case PrerequisitesStateMachine.EVENT_NETWORK_CONNECTIVITY_CHECKED /*425998*/:
                    Log.m0d(PrerequisitesStateMachine.LOG_TAG, "CheckingState.handleMessage EVENT_NETWORK_CONNECTIVITY_CHECKED");
                    try {
                        RestResponse response = APIs.user_is_authenticated();
                        if (response == null) {
                            Log.m0d(PrerequisitesStateMachine.LOG_TAG, "CheckingState.handleMessage user_is_authenticated response is null");
                        } else if (response.isResultSuccess()) {
                            Log.m0d(PrerequisitesStateMachine.LOG_TAG, "CheckingState.handleMessage user_is_authenticated result=RESULT_CODE_SUCCESS");
                            PrerequisitesStateMachine.this.sendMessage((int) PrerequisitesStateMachine.EVENT_API_SERVICES_REACHABLE_CHECKED);
                            return true;
                        } else {
                            Log.m0d(PrerequisitesStateMachine.LOG_TAG, "CheckingState.handleMessage user_is_authenticated result_code=" + response.getResultCode());
                        }
                    } catch (Exception e) {
                        Log.m1e(PrerequisitesStateMachine.LOG_TAG, e.getMessage(), e);
                    }
                    PrerequisitesStateMachine.this.mMainStateMachine.sendEventPrerequisitesCheckFailure(PrerequisitesStateMachine.this.mContext.getString(C0000R.string.prerequisites_message_server_reachable_check_failed));
                    PrerequisitesStateMachine.this.transitionTo(PrerequisitesStateMachine.this.mDefaultState);
                    return true;
                default:
                    return false;
            }
        }

        public void exit() {
            Log.m3v(PrerequisitesStateMachine.LOG_TAG, "CheckingState.exit");
        }
    }

    private class DefaultState extends State {
        private DefaultState() {
        }

        public void enter() {
            Log.m3v(PrerequisitesStateMachine.LOG_TAG, "DefaultState.enter");
        }

        public boolean processMessage(Message msg) {
            Log.m3v(PrerequisitesStateMachine.LOG_TAG, "DefaultState.processMessage " + msg.what);
            switch (msg.what) {
                case PrerequisitesStateMachine.CMD_CHECK_PREREQUISITES /*425986*/:
                    PrerequisitesStateMachine.this.sendMessage((int) PrerequisitesStateMachine.CMD_CHECKING_PREREQUISITES);
                    PrerequisitesStateMachine.this.transitionTo(PrerequisitesStateMachine.this.mCheckingState);
                    break;
            }
            return true;
        }

        public void exit() {
            Log.m3v(PrerequisitesStateMachine.LOG_TAG, "DefaultState.exit");
        }
    }

    public PrerequisitesStateMachine(Context context, MainStateMachine msm, Handler target) {
        super(LOG_TAG, target.getLooper());
        this.mDefaultState = new DefaultState();
        this.mCheckingState = new CheckingState();
        this.mCheckedState = new CheckedState();
        this.mContext = context;
        this.mMainStateMachine = msm;
        addState(this.mDefaultState);
        addState(this.mCheckingState, this.mDefaultState);
        addState(this.mCheckedState, this.mCheckingState);
        setInitialState(this.mDefaultState);
        start();
    }

    private boolean checkAndStartWifiAP() {
        if (!WifiAPManager.getInstance(MainApplication.getAppContext()).isWiFiApValid()) {
            WifiAPManager.getInstance(MainApplication.getAppContext()).reconfigWifiAp();
        }
        if (!WifiAPManager.getInstance(MainApplication.getAppContext()).isWifiApEnablingOrEnabled()) {
            WifiAPManager.getInstance(MainApplication.getAppContext()).startWifiAp();
        }
        return WifiAPManager.getInstance(MainApplication.getAppContext()).isWifiApEnabled();
    }

    private boolean isNetworkAvailable() {
        boolean available = false;
        ConnectivityManager connectivity = null;
        int loopMax = 60;
        while (loopMax > 0 && !available) {
            loopMax--;
            if (connectivity == null) {
                try {
                    connectivity = (ConnectivityManager) this.mContext.getSystemService("connectivity");
                } catch (Exception e) {
                    Log.m1e(LOG_TAG, e.getMessage(), e);
                }
            }
            try {
                Thread.sleep(300);
            } catch (Exception e2) {
                Log.m1e(LOG_TAG, e2.getMessage(), e2);
            }
            if (connectivity == null) {
                Log.m0d(LOG_TAG, "(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE) is null");
                break;
            }
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (NetworkInfo state : info) {
                    if (state.getState() == NetworkInfo.State.CONNECTED) {
                        available = true;
                    }
                }
            }
            if (!available) {
                NetworkInfo wiMax = connectivity.getNetworkInfo(6);
                if (wiMax != null && wiMax.isConnected()) {
                    available = true;
                }
            }
            Log.m0d(LOG_TAG, "isNetworkAvailable available=" + available);
        }
        return available;
    }

    public boolean sendCommandCheckPrerequisites() {
        Log.m3v(LOG_TAG, "sendCommandCheckPrerequisites");
        sendMessage((int) CMD_CHECK_PREREQUISITES);
        return true;
    }
}
