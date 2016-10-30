package com.fattin.hotspot.apilib;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import com.fattin.hotspot.apilib.RestClient.RequestMethod;
import com.fattin.hotspot.helpers.Log;

public class RestRequestProcessor extends IntentService {
    private static final String LOG_TAG = "RestRequestProcessor";
    public static final int STATUS_ERROR = 3;
    public static final int STATUS_FINISHED = 2;
    public static final int STATUS_RUNNING = 1;

    public RestRequestProcessor() {
        super("ProcessorService");
    }

    protected void onHandleIntent(Intent intent) {
        Log.m3v(LOG_TAG, "onHandleIntent STATUS_RUNNING");
        RestRequest request = (RestRequest) intent.getParcelableExtra("com.fattin.lib.api.REST_REQUEST");
        ResultReceiver receiver = request.getResultReceiver();
        if (!request.getBaseURL().isEmpty()) {
            try {
                RestClient client = new RestClient(request.getBaseURL(), request.getRestParams());
                client.Execute(RequestMethod.GET);
                RestResponse response = new RestResponse(client.getResponse(), request);
                Log.m3v(LOG_TAG, "onHandleIntent STATUS_FINISHED");
                receiver.send(100, response.getBundled());
            } catch (Exception e) {
                Bundle b = new Bundle();
                b.putString("android.intent.extra.TEXT", e.toString());
                Log.m3v(LOG_TAG, "onHandleIntent STATUS_ERROR");
                receiver.send(RestResponse.RESULT_CODE_EMAIL_ALREADY_IN_USE, b);
            }
        }
        stopSelf();
    }
}
