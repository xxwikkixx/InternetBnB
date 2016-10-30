package com.fattin.hotspot.app;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import com.fattin.hotspot.helpers.Log;
import org.acra.ACRAConstants;

public class DetachableResultReceiver extends ResultReceiver {
    private static final int BASE = 364544;
    private static final int RESULT_CODE_BEGIN = 364546;
    private static final int RESULT_CODE_FAILURE = 364548;
    private static final int RESULT_CODE_SERVICE_STARTED = 364550;
    private static final int RESULT_CODE_SERVICE_STARTING = 364551;
    private static final int RESULT_CODE_SERVICE_STARTING_FAILURE = 364554;
    private static final int RESULT_CODE_SERVICE_STOPPED = 364552;
    private static final int RESULT_CODE_SERVICE_STOPPING = 364553;
    private static final int RESULT_CODE_SUCCESS = 364547;
    private static final int RESULT_CODE_UNAUTHORIZED = 364549;
    public static final String RESULT_DATA_MESSAGE = "RESULT_DATA_MESSAGE";
    public static final String RESULT_DATA_TITLE = "RESULT_DATA_TITLE";
    private static final String TAG = "DetachableResultReceiver";
    private Callback mCallback;

    public interface Callback {
        void onBegin(String str, String str2, Bundle bundle);

        void onFailure(String str, Bundle bundle);

        void onReceiveResult(int i, Bundle bundle);

        void onServiceStarted(Bundle bundle);

        void onServiceStarting(Bundle bundle);

        void onServiceStartingFailure(String str, Bundle bundle);

        void onServiceStopped(Bundle bundle);

        void onServiceStopping(Bundle bundle);

        void onSuccess(Bundle bundle);

        void onUnauthorized(Bundle bundle);
    }

    public DetachableResultReceiver(Handler handler) {
        super(handler);
    }

    public DetachableResultReceiver(Handler handler, Callback callback) {
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
        if (resultData == null) {
            resultData = new Bundle();
        }
        if (this.mCallback != null) {
            switch (resultCode) {
                case RESULT_CODE_BEGIN /*364546*/:
                    this.mCallback.onBegin(resultData.getString(RESULT_DATA_TITLE) != null ? resultData.getString(RESULT_DATA_TITLE) : ACRAConstants.DEFAULT_STRING_VALUE, resultData.getString(RESULT_DATA_MESSAGE) != null ? resultData.getString(RESULT_DATA_MESSAGE) : ACRAConstants.DEFAULT_STRING_VALUE, resultData);
                    return;
                case RESULT_CODE_SUCCESS /*364547*/:
                    this.mCallback.onSuccess(resultData);
                    return;
                case RESULT_CODE_FAILURE /*364548*/:
                    this.mCallback.onFailure(resultData.getString(RESULT_DATA_MESSAGE) != null ? resultData.getString(RESULT_DATA_MESSAGE) : ACRAConstants.DEFAULT_STRING_VALUE, resultData);
                    return;
                case RESULT_CODE_UNAUTHORIZED /*364549*/:
                    this.mCallback.onUnauthorized(resultData);
                    return;
                case RESULT_CODE_SERVICE_STARTED /*364550*/:
                    this.mCallback.onSuccess(resultData);
                    this.mCallback.onServiceStarted(resultData);
                    return;
                case RESULT_CODE_SERVICE_STARTING /*364551*/:
                    this.mCallback.onSuccess(resultData);
                    this.mCallback.onServiceStarting(resultData);
                    return;
                case RESULT_CODE_SERVICE_STOPPED /*364552*/:
                    this.mCallback.onSuccess(resultData);
                    this.mCallback.onServiceStopped(resultData);
                    return;
                case RESULT_CODE_SERVICE_STOPPING /*364553*/:
                    this.mCallback.onSuccess(resultData);
                    this.mCallback.onServiceStopping(resultData);
                    return;
                case RESULT_CODE_SERVICE_STARTING_FAILURE /*364554*/:
                    this.mCallback.onServiceStartingFailure(resultData.getString(RESULT_DATA_MESSAGE) != null ? resultData.getString(RESULT_DATA_MESSAGE) : ACRAConstants.DEFAULT_STRING_VALUE, resultData);
                    return;
                default:
                    this.mCallback.onReceiveResult(resultCode, resultData);
                    return;
            }
        }
        Log.m0d(TAG, "Dropping result on floor for code " + resultCode + ": " + resultData.toString());
    }

    public static synchronized void sendBegin(ResultReceiver receiver, String title, String message, Bundle data) {
        synchronized (DetachableResultReceiver.class) {
            if (receiver != null) {
                if (data == null) {
                    data = new Bundle();
                }
                data.putString(RESULT_DATA_TITLE, title);
                data.putString(RESULT_DATA_MESSAGE, message);
                receiver.send(RESULT_CODE_BEGIN, data);
            }
        }
    }

    public static synchronized void sendSuccess(ResultReceiver receiver, Bundle data) {
        synchronized (DetachableResultReceiver.class) {
            if (receiver != null) {
                if (data == null) {
                    data = new Bundle();
                }
                receiver.send(RESULT_CODE_SUCCESS, data);
            }
        }
    }

    public static synchronized void sendStartingServices(ResultReceiver receiver, Bundle data) {
        synchronized (DetachableResultReceiver.class) {
            if (receiver != null) {
                if (data == null) {
                    data = new Bundle();
                }
                receiver.send(RESULT_CODE_SERVICE_STARTING, data);
            }
        }
    }

    public static synchronized void sendStoppingServices(ResultReceiver receiver, Bundle data) {
        synchronized (DetachableResultReceiver.class) {
            if (receiver != null) {
                if (data == null) {
                    data = new Bundle();
                }
                receiver.send(RESULT_CODE_SERVICE_STOPPING, data);
            }
        }
    }

    public static synchronized void sendServiceStarted(ResultReceiver receiver, Bundle data) {
        synchronized (DetachableResultReceiver.class) {
            if (receiver != null) {
                if (data == null) {
                    data = new Bundle();
                }
                receiver.send(RESULT_CODE_SERVICE_STARTED, data);
            }
        }
    }

    public static synchronized void sendServiceStopped(ResultReceiver receiver, Bundle data) {
        synchronized (DetachableResultReceiver.class) {
            if (receiver != null) {
                if (data == null) {
                    data = new Bundle();
                }
                receiver.send(RESULT_CODE_SERVICE_STOPPED, data);
            }
        }
    }

    public static synchronized void sendFailure(ResultReceiver receiver, String message, Bundle data) {
        synchronized (DetachableResultReceiver.class) {
            if (receiver != null) {
                if (data == null) {
                    data = new Bundle();
                }
                data.putString(RESULT_DATA_MESSAGE, message);
                receiver.send(RESULT_CODE_FAILURE, data);
            }
        }
    }

    public static synchronized void sendUnauthorized(ResultReceiver receiver, Bundle data) {
        synchronized (DetachableResultReceiver.class) {
            if (receiver != null) {
                if (data == null) {
                    data = new Bundle();
                }
                receiver.send(RESULT_CODE_UNAUTHORIZED, data);
            }
        }
    }

    public static synchronized void sendServiceStartingFailure(ResultReceiver receiver, String error_msg, Bundle data) {
        synchronized (DetachableResultReceiver.class) {
            if (receiver != null) {
                if (data == null) {
                    data = new Bundle();
                }
                data.putString(RESULT_DATA_MESSAGE, error_msg);
                receiver.send(RESULT_CODE_SERVICE_STARTING_FAILURE, data);
            }
        }
    }
}
