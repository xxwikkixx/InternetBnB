package com.fattin.hotspot.app;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import com.fattin.hotspot.C0000R;
import com.fattin.hotspot.apilib.APIBroadcastReceiver;
import com.fattin.hotspot.apilib.APIs;
import com.fattin.hotspot.apilib.RestResponse;
import com.fattin.hotspot.apilib.RestResponseExceptions.APIUserAuthTokenInvalidException;
import com.fattin.hotspot.app.DetachableResultReceiver.Callback;
import com.fattin.hotspot.helpers.Log;
import com.fattin.hotspot.helpers.Util;
import com.fattin.hotspot.netfilter.AccessListControl;
import com.fattin.hotspot.netfilter.AccessListDataObject;
import com.fattin.hotspot.netfilter.AccessListDatabase;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.acra.ACRAConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xbill.DNS.KEYRecord;

public class BusinessLogic {
    private static final String KEY_DURATION = "duration";
    private static final String KEY_MAC = "mac";
    private static final String KEY_SETTLEMENT = "settlement";
    private static final String KEY_TASK_MESSAGE = "KEY_TASK_MESSAGE";
    private static final String KEY_TASK_RESULT = "KEY_TASK_RESULT";
    private static final String LOG_TAG = "BusinessLogic";
    public static final int RESULT_CODE_HOTSPOT_NOT_REGISTERED = 8760;
    public static final int RESULT_CODE_USER_LOGGED_OUT = 8761;
    private static final int TASK_RESULT_FAILED = 7682;
    private static final int TASK_RESULT_SUCCESS = 7681;

    /* renamed from: com.fattin.hotspot.app.BusinessLogic.1 */
    class C00181 extends AsyncTask<String, Void, Bundle> {
        private final /* synthetic */ DetachableResultReceiver val$receiver;

        C00181(DetachableResultReceiver detachableResultReceiver) {
            this.val$receiver = detachableResultReceiver;
        }

        protected void onPreExecute() {
            DetachableResultReceiver.sendBegin(this.val$receiver, "Signing up", MainApplication.getAppContext().getString(C0000R.string.progress_dialog_please_wait), null);
            super.onPreExecute();
        }

        protected Bundle doInBackground(String... params) {
            Bundle b = new Bundle();
            try {
                RestResponse response = APIs.user_register(params[0], params[1]);
                if (response.isResultSuccess()) {
                    DataProvider.setEmail(params[0]);
                    DataProvider.setPassword(params[1]);
                    b.putInt(BusinessLogic.KEY_TASK_RESULT, BusinessLogic.TASK_RESULT_SUCCESS);
                    b.putString(BusinessLogic.KEY_TASK_MESSAGE, "Sign-in successful.");
                    return b;
                }
                b.putString(BusinessLogic.KEY_TASK_MESSAGE, response.getErrorMessage());
                b.putInt(BusinessLogic.KEY_TASK_RESULT, BusinessLogic.TASK_RESULT_FAILED);
                return b;
            } catch (APIUserAuthTokenInvalidException e) {
                Log.m1e(BusinessLogic.LOG_TAG, "APIUserAuthTokenInvalidException", e);
                b.putInt(BusinessLogic.KEY_TASK_RESULT, BusinessLogic.RESULT_CODE_USER_LOGGED_OUT);
                b.putString(BusinessLogic.KEY_TASK_MESSAGE, "Please Sign-in again.");
            } catch (Exception e2) {
                Log.m1e(BusinessLogic.LOG_TAG, e2.getMessage(), e2);
                b.putString(BusinessLogic.KEY_TASK_MESSAGE, "Couldn't Sign-up. Try later.");
            }
        }

        protected void onPostExecute(Bundle result) {
            BusinessLogic._onPostExecute(this.val$receiver, result);
            super.onPostExecute(result);
        }
    }

    /* renamed from: com.fattin.hotspot.app.BusinessLogic.2 */
    class C00192 extends AsyncTask<String, Void, Bundle> {
        private final /* synthetic */ DetachableResultReceiver val$receiver;

        C00192(DetachableResultReceiver detachableResultReceiver) {
            this.val$receiver = detachableResultReceiver;
        }

        protected void onPreExecute() {
            DetachableResultReceiver.sendBegin(this.val$receiver, null, MainApplication.getAppContext().getString(C0000R.string.progress_dialog_please_wait), null);
            super.onPreExecute();
        }

        protected Bundle doInBackground(String... params) {
            Bundle b = new Bundle();
            try {
                RestResponse response = APIs.user_is_authenticated();
                if (response.isResultSuccess()) {
                    if (((Boolean) response.getOutput()).booleanValue()) {
                        b.putInt(BusinessLogic.KEY_TASK_RESULT, BusinessLogic.TASK_RESULT_SUCCESS);
                    } else {
                        b.putInt(BusinessLogic.KEY_TASK_RESULT, BusinessLogic.RESULT_CODE_USER_LOGGED_OUT);
                        b.putString(BusinessLogic.KEY_TASK_MESSAGE, "Please Sign-in.");
                    }
                    return b;
                }
                b.putString(BusinessLogic.KEY_TASK_MESSAGE, response.getErrorMessage());
                b.putInt(BusinessLogic.KEY_TASK_RESULT, BusinessLogic.TASK_RESULT_FAILED);
                return b;
            } catch (APIUserAuthTokenInvalidException e) {
                Log.m1e(BusinessLogic.LOG_TAG, "APIUserAuthTokenInvalidException", e);
                b.putInt(BusinessLogic.KEY_TASK_RESULT, BusinessLogic.RESULT_CODE_USER_LOGGED_OUT);
                b.putString(BusinessLogic.KEY_TASK_MESSAGE, "Please Sign-in again.");
            } catch (Exception e2) {
                Log.m1e(BusinessLogic.LOG_TAG, e2.getMessage(), e2);
                b.putString(BusinessLogic.KEY_TASK_MESSAGE, "Couldn't connect to server. Try later.");
            }
        }

        protected void onPostExecute(Bundle result) {
            BusinessLogic._onPostExecute(this.val$receiver, result);
            super.onPostExecute(result);
        }
    }

    /* renamed from: com.fattin.hotspot.app.BusinessLogic.3 */
    class C00203 extends AsyncTask<String, Void, Bundle> {
        private final /* synthetic */ DetachableResultReceiver val$receiver;

        C00203(DetachableResultReceiver detachableResultReceiver) {
            this.val$receiver = detachableResultReceiver;
        }

        protected void onPreExecute() {
            DetachableResultReceiver.sendBegin(this.val$receiver, "Signing in", MainApplication.getAppContext().getString(C0000R.string.progress_dialog_please_wait), null);
            super.onPreExecute();
        }

        protected Bundle doInBackground(String... params) {
            Bundle b = new Bundle();
            try {
                RestResponse responseUserAuth = APIs.user_get_authentication_token(params[0], params[1]);
                if (responseUserAuth.isResultSuccess()) {
                    DataProvider.setUserAuthenticationToken((String) responseUserAuth.getOutput());
                    RestResponse responseHotspotIsReg = APIs.hotspot_is_registered();
                    if (!responseHotspotIsReg.isResultSuccess()) {
                        b.putInt(BusinessLogic.KEY_TASK_RESULT, BusinessLogic.RESULT_CODE_HOTSPOT_NOT_REGISTERED);
                        b.putString(BusinessLogic.KEY_TASK_MESSAGE, responseHotspotIsReg.getErrorMessage());
                    } else if (((Boolean) responseHotspotIsReg.getOutput()).booleanValue()) {
                        RestResponse responseHotspotAuth = APIs.hotspot_get_authentication_token();
                        if (responseHotspotAuth.isResultSuccess()) {
                            DataProvider.setHotspotAuthToken((String) responseHotspotAuth.getOutput());
                            RestResponse responseHotspotGetPrice = APIs.hotspot_get_price();
                            if (responseHotspotGetPrice.isResultSuccess()) {
                                DataProvider.setHotspotPrice((String) responseHotspotGetPrice.getOutput());
                                b.putInt(BusinessLogic.KEY_TASK_RESULT, BusinessLogic.TASK_RESULT_SUCCESS);
                                b.putString(BusinessLogic.KEY_TASK_MESSAGE, "Sign-in successful");
                            } else {
                                b.putInt(BusinessLogic.KEY_TASK_RESULT, BusinessLogic.RESULT_CODE_HOTSPOT_NOT_REGISTERED);
                                b.putString(BusinessLogic.KEY_TASK_MESSAGE, responseHotspotGetPrice.getErrorMessage());
                            }
                        } else {
                            b.putInt(BusinessLogic.KEY_TASK_RESULT, BusinessLogic.RESULT_CODE_HOTSPOT_NOT_REGISTERED);
                            b.putString(BusinessLogic.KEY_TASK_MESSAGE, responseHotspotAuth.getErrorMessage());
                        }
                    } else {
                        b.putInt(BusinessLogic.KEY_TASK_RESULT, BusinessLogic.RESULT_CODE_HOTSPOT_NOT_REGISTERED);
                        b.putString(BusinessLogic.KEY_TASK_MESSAGE, "Hotspot not registered for current user.");
                    }
                } else {
                    b.putInt(BusinessLogic.KEY_TASK_RESULT, BusinessLogic.TASK_RESULT_FAILED);
                    b.putString(BusinessLogic.KEY_TASK_MESSAGE, responseUserAuth.getErrorMessage());
                }
            } catch (APIUserAuthTokenInvalidException e) {
                Log.m1e(BusinessLogic.LOG_TAG, "APIUserAuthTokenInvalidException", e);
                b.putInt(BusinessLogic.KEY_TASK_RESULT, BusinessLogic.RESULT_CODE_USER_LOGGED_OUT);
                b.putString(BusinessLogic.KEY_TASK_MESSAGE, "Please Sign-in again.");
            } catch (Exception e2) {
                Log.m1e(BusinessLogic.LOG_TAG, e2.getMessage(), e2);
                b.putString(BusinessLogic.KEY_TASK_MESSAGE, "Couldn't Sign in. Try later.");
                b.putInt(BusinessLogic.KEY_TASK_RESULT, BusinessLogic.TASK_RESULT_FAILED);
            }
            return b;
        }

        protected void onPostExecute(Bundle result) {
            BusinessLogic._onPostExecute(this.val$receiver, result);
            super.onPostExecute(result);
        }
    }

    /* renamed from: com.fattin.hotspot.app.BusinessLogic.4 */
    class C00214 extends AsyncTask<String, Void, Bundle> {
        private final /* synthetic */ DetachableResultReceiver val$receiver;

        C00214(DetachableResultReceiver detachableResultReceiver) {
            this.val$receiver = detachableResultReceiver;
        }

        protected void onPreExecute() {
            DetachableResultReceiver.sendBegin(this.val$receiver, "Signing out", MainApplication.getAppContext().getString(C0000R.string.progress_dialog_please_wait), null);
            super.onPreExecute();
        }

        protected Bundle doInBackground(String... params) {
            Bundle b = new Bundle();
            try {
                RestResponse response = APIs.user_delete_authentication_token();
                if (response.isResultSuccess()) {
                    b.putString(BusinessLogic.KEY_TASK_MESSAGE, "Sign-out successful.");
                    Log.m2i(BusinessLogic.LOG_TAG, "Logout API successful.");
                } else {
                    Log.m2i(BusinessLogic.LOG_TAG, "Logout API failed.");
                    b.putString(BusinessLogic.KEY_TASK_MESSAGE, response.getErrorMessage());
                }
                b.putInt(BusinessLogic.KEY_TASK_RESULT, BusinessLogic.RESULT_CODE_USER_LOGGED_OUT);
            } catch (APIUserAuthTokenInvalidException e) {
                Log.m1e(BusinessLogic.LOG_TAG, "APIUserAuthTokenInvalidException", e);
                b.putInt(BusinessLogic.KEY_TASK_RESULT, BusinessLogic.RESULT_CODE_USER_LOGGED_OUT);
                b.putString(BusinessLogic.KEY_TASK_MESSAGE, "Sign-out successful");
            } catch (Exception e2) {
                Log.m1e(BusinessLogic.LOG_TAG, e2.getMessage(), e2);
                b.putString(BusinessLogic.KEY_TASK_MESSAGE, "Couldn't Sign out. Try later.");
                b.putInt(BusinessLogic.KEY_TASK_RESULT, BusinessLogic.TASK_RESULT_FAILED);
            }
            return b;
        }

        protected void onPostExecute(Bundle result) {
            BusinessLogic._onPostExecute(this.val$receiver, result);
            super.onPostExecute(result);
        }
    }

    /* renamed from: com.fattin.hotspot.app.BusinessLogic.5 */
    class C00225 extends AsyncTask<String, Void, Bundle> {
        private final /* synthetic */ DetachableResultReceiver val$receiver;

        C00225(DetachableResultReceiver detachableResultReceiver) {
            this.val$receiver = detachableResultReceiver;
        }

        protected void onPreExecute() {
            DetachableResultReceiver.sendBegin(this.val$receiver, "Registering hotspot", MainApplication.getAppContext().getString(C0000R.string.progress_dialog_please_wait), null);
            super.onPreExecute();
        }

        protected Bundle doInBackground(String... params) {
            Bundle b = new Bundle();
            try {
                RestResponse response = APIs.hotspot_register(params[0]);
                if (!response.isResultSuccess()) {
                    b.putString(BusinessLogic.KEY_TASK_MESSAGE, response.getErrorMessage());
                } else if (((Boolean) response.getOutput()).booleanValue()) {
                    DataProvider.setHotspotPrice(params[0]);
                    RestResponse responseHotspotAuth = APIs.hotspot_get_authentication_token();
                    if (responseHotspotAuth.isResultSuccess()) {
                        DataProvider.setHotspotAuthToken((String) responseHotspotAuth.getOutput());
                        b.putInt(BusinessLogic.KEY_TASK_RESULT, BusinessLogic.TASK_RESULT_SUCCESS);
                        b.putString(BusinessLogic.KEY_TASK_MESSAGE, "Hotspot Registered");
                        return b;
                    }
                    b.putString(BusinessLogic.KEY_TASK_MESSAGE, responseHotspotAuth.getErrorMessage());
                }
            } catch (APIUserAuthTokenInvalidException e) {
                Log.m1e(BusinessLogic.LOG_TAG, "APIUserAuthTokenInvalidException", e);
                b.putInt(BusinessLogic.KEY_TASK_RESULT, BusinessLogic.RESULT_CODE_USER_LOGGED_OUT);
                b.putString(BusinessLogic.KEY_TASK_MESSAGE, "Please Sign-in again.");
            } catch (Exception e2) {
                Log.m1e(BusinessLogic.LOG_TAG, e2.getMessage(), e2);
                b.putString(BusinessLogic.KEY_TASK_MESSAGE, "Hotspot registration failed");
            }
            b.putInt(BusinessLogic.KEY_TASK_RESULT, BusinessLogic.TASK_RESULT_FAILED);
            return b;
        }

        protected void onPostExecute(Bundle result) {
            BusinessLogic._onPostExecute(this.val$receiver, result);
            super.onPostExecute(result);
        }
    }

    /* renamed from: com.fattin.hotspot.app.BusinessLogic.6 */
    class C00236 extends AsyncTask<String, Void, Bundle> {
        private final /* synthetic */ DetachableResultReceiver val$receiver;

        C00236(DetachableResultReceiver detachableResultReceiver) {
            this.val$receiver = detachableResultReceiver;
        }

        protected void onPreExecute() {
            DetachableResultReceiver.sendBegin(this.val$receiver, "Updating Hotspot Price", MainApplication.getAppContext().getString(C0000R.string.progress_dialog_please_wait), null);
            super.onPreExecute();
        }

        protected Bundle doInBackground(String... params) {
            Bundle b = new Bundle();
            try {
                RestResponse response = APIs.hotspot_set_price(params[0]);
                if (!response.isResultSuccess()) {
                    b.putString(BusinessLogic.KEY_TASK_MESSAGE, response.getErrorMessage());
                } else if (((Boolean) response.getOutput()).booleanValue()) {
                    DataProvider.setHotspotPrice(params[0]);
                    b.putInt(BusinessLogic.KEY_TASK_RESULT, BusinessLogic.TASK_RESULT_SUCCESS);
                    b.putString(BusinessLogic.KEY_TASK_MESSAGE, "Price Updated");
                    return b;
                }
            } catch (APIUserAuthTokenInvalidException e) {
                Log.m1e(BusinessLogic.LOG_TAG, "APIUserAuthTokenInvalidException", e);
                b.putInt(BusinessLogic.KEY_TASK_RESULT, BusinessLogic.RESULT_CODE_USER_LOGGED_OUT);
                b.putString(BusinessLogic.KEY_TASK_MESSAGE, "Please Sign-in again.");
            } catch (Exception e2) {
                Log.m1e(BusinessLogic.LOG_TAG, e2.getMessage(), e2);
                b.putString(BusinessLogic.KEY_TASK_MESSAGE, "Couldn't update Price. Try later.");
            }
            b.putInt(BusinessLogic.KEY_TASK_RESULT, BusinessLogic.TASK_RESULT_FAILED);
            return b;
        }

        protected void onPostExecute(Bundle result) {
            BusinessLogic._onPostExecute(this.val$receiver, result);
            super.onPostExecute(result);
        }
    }

    private static void _onPostExecute(DetachableResultReceiver receiver, Bundle result) {
        Log.m3v(LOG_TAG, "_onPostExecute");
        String task_result_message = result.getString(KEY_TASK_MESSAGE);
        if (task_result_message != null) {
            MainApplication.getApplication().displayToastMessage(task_result_message);
        }
        int task_result = result.getInt(KEY_TASK_RESULT);
        switch (task_result) {
            case TASK_RESULT_SUCCESS /*7681*/:
                DetachableResultReceiver.sendSuccess(receiver, null);
            case TASK_RESULT_FAILED /*7682*/:
                DetachableResultReceiver.sendFailure(receiver, null, null);
            case RESULT_CODE_USER_LOGGED_OUT /*8761*/:
                APIBroadcastReceiver.sendBroadcastAPIUserUnauthorized(MainApplication.getAppContext());
            default:
                receiver.send(task_result, null);
        }
    }

    public static synchronized void processUserRegistration(String username, String password, Callback callback) {
        synchronized (BusinessLogic.class) {
            Log.m3v(LOG_TAG, "processUserRegistration");
            new C00181(new DetachableResultReceiver(new Handler(), callback)).execute(new String[]{username, password});
        }
    }

    public static synchronized void processUserTokenValidation(Callback callback) {
        synchronized (BusinessLogic.class) {
            Log.m3v(LOG_TAG, "processUserTokenValidation");
            new C00192(new DetachableResultReceiver(new Handler(), callback)).execute(new String[0]);
        }
    }

    public static synchronized void processUserLogin(String username, String password, Callback callback) {
        synchronized (BusinessLogic.class) {
            Log.m3v(LOG_TAG, "processUserLogin");
            new C00203(new DetachableResultReceiver(new Handler(), callback)).execute(new String[]{username, password});
        }
    }

    public static synchronized void processUserLogout(Callback callback) {
        synchronized (BusinessLogic.class) {
            Log.m3v(LOG_TAG, "processUserLogout");
            new C00214(new DetachableResultReceiver(new Handler(), callback)).execute(new String[0]);
        }
    }

    public static synchronized void processHotspotRegistration(String price_per_hour, Callback callback) {
        synchronized (BusinessLogic.class) {
            Log.m3v(LOG_TAG, "processHotspotRegistration");
            new C00225(new DetachableResultReceiver(new Handler(), callback)).execute(new String[]{price_per_hour});
        }
    }

    public static synchronized void processHotspotSetPrice(String price_per_hour, Callback callback) {
        synchronized (BusinessLogic.class) {
            Log.m3v(LOG_TAG, "processHotspotSetPrice");
            new C00236(new DetachableResultReceiver(new Handler(), callback)).execute(new String[]{price_per_hour});
        }
    }

    public static synchronized boolean heartbeatAll() throws APIUserAuthTokenInvalidException {
        boolean z;
        synchronized (BusinessLogic.class) {
            Log.m3v(LOG_TAG, "heartbeatAll");
            try {
                AccessListControl.updateAccessListDatabaseFromArpCache(true, 300);
                long currentTimeStamp = Util.getCurrentTimeStamp();
                String device_tokens = ACRAConstants.DEFAULT_STRING_VALUE;
                Collection<AccessListDataObject> list = AccessListDatabase.getAllDevicesData();
                synchronized (list) {
                    for (AccessListDataObject deviceData : list) {
                        if (!deviceData.getDeviceToken().trim().isEmpty()) {
                            device_tokens = new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(device_tokens)).append(device_tokens.length() > 1 ? "," : ACRAConstants.DEFAULT_STRING_VALUE).toString())).append(deviceData.getDeviceToken()).toString();
                        }
                    }
                }
                if (device_tokens.isEmpty()) {
                    Log.m0d(LOG_TAG, "heartbeatAll -> devices_mac_addresses variable is null");
                } else {
                    RestResponse response = APIs.hotspot_heartbeat(device_tokens);
                    _process_heartbeat_response(response);
                    if (response.isResultSuccess() && response.getOutput() != null) {
                        synchronized (list) {
                            for (AccessListDataObject deviceData2 : list) {
                                long deviceLastHeartbeatTimeStamp = deviceData2.getLastHeartbeatTimestamp();
                                if (deviceLastHeartbeatTimeStamp < currentTimeStamp) {
                                    deviceData2.setLastHeartbeatTimestamp(deviceLastHeartbeatTimeStamp - Constants.HEARTBEAT_DEVICE_TIMEOUT);
                                }
                            }
                        }
                    }
                }
                synchronized (list) {
                    for (AccessListDataObject deviceData22 : list) {
                        if (AccessListControl.isReachable(AccessListControl.getIPFromArpCache(deviceData22.getDeviceMacAddress()))) {
                            Log.m0d(LOG_TAG, "heartbeatAll -> still connected: device_mac_address=" + deviceData22.getDeviceMacAddress());
                            deviceData22.setLastAccessTimestamp(currentTimeStamp);
                        } else {
                            Log.m0d(LOG_TAG, "heartbeatAll -> not connected: device_mac_address=" + deviceData22.getDeviceMacAddress());
                        }
                        if (!deviceData22.getDeviceToken().isEmpty()) {
                            if (currentTimeStamp - deviceData22.getLastHeartbeatTimestamp() > Constants.HEARTBEAT_DEVICE_TIMEOUT) {
                                Log.m0d(LOG_TAG, "heartbeatAll -> Access Denied for device_mac_address=" + deviceData22.getDeviceMacAddress());
                                AccessListControl.disallowAccessForMAC(deviceData22.getDeviceMacAddress());
                                deviceData22.setDeviceToken(ACRAConstants.DEFAULT_STRING_VALUE);
                                deviceData22.setHostName(MainApplication.getAppContext().getString(C0000R.string.business_message_hearbeat_timeout));
                            } else if (currentTimeStamp - deviceData22.getLastAccessTimestamp() > Constants.ACCESS_LIST_DEVICE_ACCESS_TIMEOUT) {
                                Log.m0d(LOG_TAG, "heartbeatAll -> Access Expired for device_mac_address=" + deviceData22.getDeviceMacAddress());
                                AccessListControl.disallowAccessForMAC(deviceData22.getDeviceMacAddress());
                                deviceData22.setDeviceToken(ACRAConstants.DEFAULT_STRING_VALUE);
                                deviceData22.setHostName(MainApplication.getAppContext().getString(C0000R.string.business_message_access_timeout));
                            }
                        }
                    }
                }
                z = true;
            } catch (APIUserAuthTokenInvalidException e) {
                APIBroadcastReceiver.sendBroadcastAPIUserUnauthorized(MainApplication.getAppContext());
                throw e;
            } catch (Exception e2) {
                Log.m1e(LOG_TAG, e2.getMessage(), e2);
                z = false;
            }
        }
        return z;
    }

    public static synchronized boolean heartbeatDevice(String device_mac_address) throws APIUserAuthTokenInvalidException {
        boolean z;
        synchronized (BusinessLogic.class) {
            Log.m0d(LOG_TAG, "heartbeatDevice");
            Log.m0d(LOG_TAG, "heartbeatDevice -> device_mac_address=" + device_mac_address);
            AccessListDataObject deviceData = AccessListDatabase.getDeviceData(device_mac_address);
            if (deviceData != null) {
                Log.m0d(LOG_TAG, "heartbeatDevice -> device_token=" + deviceData.getDeviceToken());
                if (!deviceData.getDeviceToken().isEmpty()) {
                    try {
                        _process_heartbeat_response(APIs.hotspot_heartbeat(deviceData.getDeviceToken()));
                        Log.m0d(LOG_TAG, "heartbeatDevice -> end True");
                        z = true;
                    } catch (APIUserAuthTokenInvalidException e) {
                        APIBroadcastReceiver.sendBroadcastAPIUserUnauthorized(MainApplication.getAppContext());
                        throw e;
                    } catch (Exception e2) {
                        Log.m1e(LOG_TAG, e2.getMessage(), e2);
                    }
                }
            }
            Log.m0d(LOG_TAG, "heartbeatDevice -> end False");
            z = false;
        }
        return z;
    }

    private static void _process_heartbeat_response(RestResponse response) throws Exception {
        long currentTimeStamp = Util.getCurrentTimeStamp();
        if (response.isResultSuccess() && response.getOutput() != null) {
            String mac_address;
            switch (1) {
                case KEYRecord.PROTOCOL_TLS /*1*/:
                    ArrayList<String> devices_mac_addresses = convertJSONArrayToArrayList((JSONArray) response.getOutput());
                    Log.m0d(LOG_TAG, "_process_heartbeat_response -> devices_mac_addresses=" + devices_mac_addresses.toString());
                    Iterator it = devices_mac_addresses.iterator();
                    while (it.hasNext()) {
                        mac_address = (String) it.next();
                        AccessListDatabase.getDeviceData(mac_address).setLastHeartbeatTimestamp(currentTimeStamp);
                        if (!AccessListControl.isAccessAllowedForMAC(mac_address)) {
                            AccessListControl.allowAccessForMAC(mac_address);
                        }
                    }
                case KEYRecord.PROTOCOL_EMAIL /*2*/:
                    JSONArray devices_data_array = (JSONArray) response.getOutput();
                    Log.m0d(LOG_TAG, "_process_heartbeat_response -> devices_data=" + devices_data_array.toString());
                    for (int i = 0; i < devices_data_array.length(); i++) {
                        JSONObject device_data_object = devices_data_array.getJSONObject(i);
                        mac_address = device_data_object.getString(KEY_MAC);
                        float settlement = (float) device_data_object.getDouble(KEY_SETTLEMENT);
                        int duration = device_data_object.getInt(KEY_DURATION);
                        AccessListDatabase.getDeviceData(mac_address).setLastHeartbeatTimestamp(currentTimeStamp);
                        if (!AccessListControl.isAccessAllowedForMAC(mac_address)) {
                            AccessListControl.allowAccessForMAC(mac_address);
                        }
                        AccessListDatabase.getDeviceData(mac_address).setEarnings(settlement);
                        AccessListDatabase.getDeviceData(mac_address).setDuration(duration);
                    }
                default:
                    throw new Exception("API Version not supported. (v=1)");
            }
        }
    }

    public static ArrayList<String> convertJSONArrayToArrayList(JSONArray jsonArray) {
        ArrayList<String> arrayList = new ArrayList();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                arrayList.add(jsonArray.getString(i));
            } catch (JSONException e) {
                Log.m1e(LOG_TAG, e.getMessage(), e);
            }
        }
        return arrayList;
    }
}
