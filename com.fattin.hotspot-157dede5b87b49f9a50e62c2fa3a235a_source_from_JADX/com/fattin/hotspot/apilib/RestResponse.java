package com.fattin.hotspot.apilib;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.fattin.hotspot.apilib.RestResponseExceptions.APIUserAuthTokenInvalidException;
import org.acra.ACRAConstants;
import org.json.JSONException;
import org.json.JSONObject;

public class RestResponse extends JSONObject implements Parcelable {
    public static final Creator<RestResponse> CREATOR;
    public static final String EXTRA_REST_RESPONSE = "com.fattin.lib.api.REST_RESPONSE";
    private static final String KEY_EXE_TIME = "exe_time";
    private static final String KEY_RESULT = "result";
    private static final String KEY_RESULT_CODE = "result_code";
    private static final String KEY_TIMESTAMP = "timestamp";
    public static final int RESULT_CODE_AUTHORIZATION_INVALID = 113;
    public static final int RESULT_CODE_AUTHORIZATION_REQUIRED = 112;
    public static final int RESULT_CODE_EMAIL_ALREADY_IN_USE = 200;
    public static final int RESULT_CODE_HOTSPOT_ALREADY_REGISTERED_WITH_USER = 205;
    public static final int RESULT_CODE_HOTSPOT_AUTHENTICATION_TOKEN_NOT_FOUND = 209;
    public static final int RESULT_CODE_INVALID_AUTHENTICATION_TOKEN = 102;
    public static final int RESULT_CODE_INVALID_CREDENTIALS = 201;
    public static final int RESULT_CODE_INVALID_PARAMETERS = 114;
    public static final int RESULT_CODE_MISSING_PARAMETERS = 109;
    public static final int RESULT_CODE_SUCCESS = 0;
    public static final int RESULT_CODE_USER_AUTHENTICATION_TOKEN_NOT_FOUND = 202;
    private RestRequest mRequest;

    /* renamed from: com.fattin.hotspot.apilib.RestResponse.1 */
    class C00171 implements Creator<RestResponse> {
        C00171() {
        }

        public RestResponse createFromParcel(Parcel in) {
            try {
                return new RestResponse(in.readString(), (RestRequest) in.readParcelable(RestRequest.class.getClassLoader()));
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        public RestResponse[] newArray(int size) {
            return new RestResponse[size];
        }
    }

    static {
        CREATOR = new C00171();
    }

    public RestResponse(String json, RestRequest request) throws JSONException {
        super(json);
        setRestRequest(request);
    }

    public void setRestRequest(RestRequest request) {
        this.mRequest = request;
    }

    public void setResultCode(int resultCode) {
        try {
            put(KEY_RESULT_CODE, resultCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getResultCode() {
        try {
            return getInt(KEY_RESULT_CODE);
        } catch (JSONException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public <T> T getOutput() {
        try {
            if (getResultCode() == 0) {
                return get(KEY_RESULT);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T> T getOutput(T defaults) {
        try {
            if (getResultCode() == 0) {
                defaults = get(KEY_RESULT);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return defaults;
    }

    public String getErrorMessage() {
        try {
            if (getResultCode() != 0) {
                return getString(KEY_RESULT);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ACRAConstants.DEFAULT_STRING_VALUE;
    }

    public long getTimestamp() {
        try {
            return getLong(KEY_TIMESTAMP);
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public double getExeTime() {
        try {
            return getDouble(KEY_EXE_TIME);
        } catch (JSONException e) {
            e.printStackTrace();
            return 0.0d;
        }
    }

    public int describeContents() {
        return RESULT_CODE_SUCCESS;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mRequest, RESULT_CODE_SUCCESS);
        dest.writeString(toString());
    }

    public Bundle getBundled() {
        Bundle b = new Bundle();
        b.putParcelable(EXTRA_REST_RESPONSE, this);
        return b;
    }

    public RestRequest getRestRequest() {
        return this.mRequest;
    }

    public boolean isResultSuccess() throws APIUserAuthTokenInvalidException {
        switch (getResultCode()) {
            case RESULT_CODE_SUCCESS /*0*/:
                return true;
            case RESULT_CODE_INVALID_AUTHENTICATION_TOKEN /*102*/:
            case RESULT_CODE_AUTHORIZATION_REQUIRED /*112*/:
            case RESULT_CODE_AUTHORIZATION_INVALID /*113*/:
                throw new APIUserAuthTokenInvalidException();
            default:
                return false;
        }
    }
}
