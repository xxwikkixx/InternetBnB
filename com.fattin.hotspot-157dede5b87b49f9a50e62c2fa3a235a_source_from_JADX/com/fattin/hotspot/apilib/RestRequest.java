package com.fattin.hotspot.apilib;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.ResultReceiver;
import com.fattin.hotspot.apilib.APICallbackResultReceiver.Callback;
import com.fattin.hotspot.apilib.RestClient.RequestMethod;
import com.fattin.hotspot.helpers.Log;
import java.util.ArrayList;
import java.util.Iterator;
import org.acra.ACRAConstants;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class RestRequest implements Parcelable {
    protected static final String API_VERSION_PARAM = "v";
    public static final Creator<RestRequest> CREATOR;
    protected static final String EXTRA_REST_REQUEST = "com.fattin.lib.api.REST_REQUEST";
    private static final String LOG_TAG = "RestRequest";
    private String base_url;
    private ResultReceiver mReceiver;
    private ArrayList<NameValuePair> params;

    /* renamed from: com.fattin.hotspot.apilib.RestRequest.1 */
    class C00161 implements Creator<RestRequest> {
        C00161() {
        }

        public RestRequest createFromParcel(Parcel in) {
            return new RestRequest(null);
        }

        public RestRequest[] newArray(int size) {
            return new RestRequest[size];
        }
    }

    static {
        CREATOR = new C00161();
    }

    private RestRequest(Parcel in) {
        this.base_url = ACRAConstants.DEFAULT_STRING_VALUE;
        this.params = null;
        this.mReceiver = null;
        this.params = new ArrayList();
        readFromParcel(in);
    }

    public RestRequest(String url) {
        this.base_url = ACRAConstants.DEFAULT_STRING_VALUE;
        this.params = null;
        this.mReceiver = null;
        this.params = new ArrayList();
        setBaseURL(url);
        addRestParam(API_VERSION_PARAM, String.valueOf(1));
    }

    public RestRequest(String url, Callback callback) {
        this.base_url = ACRAConstants.DEFAULT_STRING_VALUE;
        this.params = null;
        this.mReceiver = null;
        this.params = new ArrayList();
        setBaseURL(url);
        setRequestCallback(callback);
    }

    public void setBaseURL(String url) {
        this.base_url = url;
    }

    public String getBaseURL() {
        return this.base_url;
    }

    public void addRestParam(String name, String value) {
        this.params.add(new BasicNameValuePair(name, value));
    }

    public ArrayList<NameValuePair> getRestParams() {
        return this.params;
    }

    public void setRequestCallback(Callback callback) {
        this.mReceiver = new APICallbackResultReceiver(new Handler(), callback);
    }

    public void setResultReceiver(APICallbackResultReceiver receiver) {
        this.mReceiver = receiver;
    }

    public ResultReceiver getResultReceiver() {
        return this.mReceiver;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mReceiver, 0);
        dest.writeString(this.base_url);
        Log.m2i("RestRequest Parcelling", "base_url=" + this.base_url);
        dest.writeInt(this.params.size());
        Iterator it = this.params.iterator();
        while (it.hasNext()) {
            NameValuePair pair = (NameValuePair) it.next();
            dest.writeString(pair.getName());
            dest.writeString(pair.getValue());
            Log.m2i("RestRequest Parcelling", "name=" + pair.getName());
            Log.m2i("RestRequest Parcelling", "value=" + pair.getValue());
        }
    }

    public void readFromParcel(Parcel in) {
        this.mReceiver = (ResultReceiver) in.readParcelable(APICallbackResultReceiver.class.getClassLoader());
        this.base_url = in.readString();
        Log.m2i("RestRequest UnParcelling", "base_url=" + this.base_url);
        int paramsSize = in.readInt();
        for (int i = 0; i < paramsSize; i++) {
            String name = in.readString();
            String value = in.readString();
            this.params.add(new BasicNameValuePair(name, value));
            Log.m2i("RestRequest UnParcelling", "name=" + name);
            Log.m2i("RestRequest UnParcelling", "value=" + value);
        }
    }

    public RestResponse execute() throws Exception {
        if (getBaseURL().isEmpty()) {
            Log.m3v(LOG_TAG, "onHandleIntent STATUS_ERROR");
            Log.m0d(LOG_TAG, "execute() ==> Missing BaseURL!");
            throw new Exception("RestRequest Exception: execute() ==> Missing BaseURL!");
        }
        try {
            RestClient client = new RestClient(getBaseURL(), getRestParams());
            client.Execute(RequestMethod.GET);
            Log.m3v(LOG_TAG, "onHandleIntent STATUS_FINISHED");
            return new RestResponse(client.getResponse(), this);
        } catch (Exception e) {
            Log.m3v(LOG_TAG, "onHandleIntent STATUS_ERROR");
            Log.m1e(LOG_TAG, "execute() ==> Error during webservice request!", e);
            throw new Exception("RestRequest Exception: execute() ==> Error while executing request!", e);
        }
    }

    public void execute(Context context) {
        Intent intent = new Intent(context, RestRequestProcessor.class);
        intent.putExtra(EXTRA_REST_REQUEST, this);
        context.startService(intent);
    }
}
