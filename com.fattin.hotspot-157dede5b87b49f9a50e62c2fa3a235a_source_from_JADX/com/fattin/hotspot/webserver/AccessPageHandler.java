package com.fattin.hotspot.webserver;

import com.fattin.hotspot.apilib.RestResponseExceptions.APIUserAuthTokenInvalidException;
import com.fattin.hotspot.app.BusinessLogic;
import com.fattin.hotspot.helpers.Log;
import com.fattin.hotspot.netfilter.AccessListControl;
import com.fattin.hotspot.netfilter.AccessListDataObject;
import com.fattin.hotspot.netfilter.AccessListDatabase;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.acra.ACRAConstants;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

public class AccessPageHandler implements HttpRequestHandler {
    private static final String LOG_TAG = "AccessPageHandler";

    public void handle(HttpRequest request, HttpResponse response, HttpContext httpContext) throws HttpException, IOException {
        int i;
        Log.m0d(LOG_TAG, "handle -> start");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.m1e(LOG_TAG, e.getMessage(), e);
        }
        URI uri = null;
        try {
            uri = new URI(request.getRequestLine().getUri());
        } catch (URISyntaxException e2) {
            Log.m1e(LOG_TAG, e2.getMessage(), e2);
        }
        List<NameValuePair> queryPairs = URLEncodedUtils.parse(uri, "UTF-8");
        String device_token = ACRAConstants.DEFAULT_STRING_VALUE;
        String device_mac_address = ACRAConstants.DEFAULT_STRING_VALUE;
        String continue_url = ACRAConstants.DEFAULT_STRING_VALUE;
        for (i = 0; i < queryPairs.size(); i++) {
            if (((NameValuePair) queryPairs.get(i)).getName().equals("device_token")) {
                device_token = ((NameValuePair) queryPairs.get(i)).getValue();
            } else if (((NameValuePair) queryPairs.get(i)).getName().equals("device_mac_address")) {
                device_mac_address = ((NameValuePair) queryPairs.get(i)).getValue();
            } else if (((NameValuePair) queryPairs.get(i)).getName().equals("continue_url")) {
                continue_url = ((NameValuePair) queryPairs.get(i)).getValue();
            }
        }
        if (device_token == null || device_token == ACRAConstants.DEFAULT_STRING_VALUE || device_mac_address == null || device_mac_address == ACRAConstants.DEFAULT_STRING_VALUE || continue_url == ACRAConstants.DEFAULT_STRING_VALUE || continue_url == ACRAConstants.DEFAULT_STRING_VALUE) {
            Log.m0d(LOG_TAG, "(device_token || device_mac_address || continue_url) is null or empty !");
        }
        AccessListDatabase.setDeviceData(new AccessListDataObject(device_mac_address, device_token, (String) httpContext.getAttribute("hostname")));
        try {
            BusinessLogic.heartbeatDevice(device_mac_address);
        } catch (APIUserAuthTokenInvalidException e3) {
            Log.m1e(LOG_TAG, e3.getMessage(), e3);
        }
        for (i = 0; i < 50; i++) {
            if (AccessListControl.isAccessAllowedForMAC(device_mac_address)) {
                Log.m0d(LOG_TAG, new StringBuilder(String.valueOf(device_mac_address)).append(" is allowed in netfilter").toString());
                response.setStatusCode(302);
                response.addHeader("Location", continue_url);
                response.setEntity(new StringEntity(" ", "UTF-8"));
                Log.m0d(LOG_TAG, "handle -> end");
                return;
            }
            Log.m0d(LOG_TAG, new StringBuilder(String.valueOf(device_mac_address)).append(" still NOT allowed in netfilter").toString());
            try {
                Thread.sleep(200);
            } catch (InterruptedException e4) {
                Log.m1e(LOG_TAG, e4.getMessage(), e4);
            }
        }
        Log.m0d(LOG_TAG, new StringBuilder(String.valueOf(device_mac_address)).append(" Device was NOT allowed in netfilter").toString());
        response.setStatusCode(302);
        response.addHeader("Location", "https://www.fattin.com/error.php?MESSAGE_TYPE=Error: Device was not allowed in netfilter");
        response.setEntity(new StringEntity(" ", "UTF-8"));
        Log.m0d(LOG_TAG, "handle -> end");
    }
}
