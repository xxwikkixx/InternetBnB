package com.fattin.hotspot.apilib;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import com.fattin.hotspot.helpers.Log;

public class APICallbackResultReceiver extends ResultReceiver {
    public static final String EXTRA_CALLBACK_RESULT_RECEIVER = "com.fattin.lib.api.CALLBACK_RESULT_RECEIVER";
    private static final String LOG_TAG = "CallbackResultReceiver";
    static final int RESULT_CODE_FAILURE = 200;
    public static final int RESULT_CODE_SUCCESS = 100;
    private static final String TAG = "CallbackResultReceiver";
    private Callback mCallback;

    public interface Callback {
        void onReceiveResult(int i, RestResponse restResponse);
    }

    public APICallbackResultReceiver(Handler handler, Callback callback) {
        super(handler);
        setCallback(callback);
    }

    public void clearCallback() {
        this.mCallback = null;
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (this.mCallback != null) {
            Log.m0d(TAG, "resultCode=" + resultCode);
            resultData.setClassLoader(RestResponse.class.getClassLoader());
            this.mCallback.onReceiveResult(resultCode, (RestResponse) resultData.getParcelable(RestResponse.EXTRA_REST_RESPONSE));
            return;
        }
        Log.m0d(TAG, "Dropping result on floor for code " + resultCode + ": " + resultData.toString());
    }
}
