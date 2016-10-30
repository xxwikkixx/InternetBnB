package com.fattin.hotspot.webserver;

import com.fattin.hotspot.app.Constants;
import com.fattin.hotspot.helpers.Log;
import com.fattin.hotspot.netfilter.AccessListControl;
import com.fattin.hotspot.netfilter.AccessListDataObject;
import com.fattin.hotspot.netfilter.AccessListDatabase;
import com.fattin.hotspot.netfilter.NetFilterManager;
import java.io.IOException;
import java.net.URLEncoder;
import org.acra.ACRAConstants;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

public class CaptivePageHandler implements HttpRequestHandler {
    private static final String LOG_TAG = "CaptivePageHandler";

    public void handle(HttpRequest request, HttpResponse response, HttpContext httpContext) throws HttpException, IOException {
        Log.m0d(LOG_TAG, "handle -> start");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.m1e(LOG_TAG, e.getMessage(), e);
        }
        Log.m2i(LOG_TAG, "host=" + request.getFirstHeader("Host").getValue());
        String hostname = (String) httpContext.getAttribute("hostname");
        String hotspot_token = (String) httpContext.getAttribute("hotspot_token");
        String device_mac_address = AccessListControl.getMacFromArpCache((String) httpContext.getAttribute("client_ip"));
        String hotspot_uri = NetFilterManager.getLocalIpAddress();
        if (hotspot_token == null || hotspot_token == ACRAConstants.DEFAULT_STRING_VALUE) {
            Log.m0d(LOG_TAG, "hotspot_token is empty !");
        }
        if (device_mac_address == null || device_mac_address == ACRAConstants.DEFAULT_STRING_VALUE) {
            Log.m0d(LOG_TAG, "device_mac_address is empty !");
        }
        if (hotspot_uri == null || hotspot_uri == ACRAConstants.DEFAULT_STRING_VALUE) {
            Log.m0d(LOG_TAG, "hotspot_uri is empty !");
        }
        if (AccessListDatabase.getDeviceData(device_mac_address) == null) {
            Log.m2i(LOG_TAG, "adding device " + device_mac_address);
            AccessListDatabase.setDeviceData(new AccessListDataObject(device_mac_address, ACRAConstants.DEFAULT_STRING_VALUE, hostname));
        }
        response.addHeader("Location", "https://www.fattin.com/portal.php?&hotspot_token=" + URLEncoder.encode(hotspot_token, "UTF-8") + "&hotspot_uri=" + hotspot_uri + ":" + Constants.SERVER_PORT_ACCESS_WEBSERVER + "&device_mac_address=" + URLEncoder.encode(device_mac_address, "UTF-8") + "&continue_url=http://" + request.getFirstHeader("Host").getValue() + request.getRequestLine().getUri());
        response.setStatusCode(302);
        response.setEntity(new StringEntity(" ", "UTF-8"));
        Log.m0d(LOG_TAG, "handle -> end");
    }
}
