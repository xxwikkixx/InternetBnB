package com.fattin.hotspot.apilib;

import com.fattin.hotspot.app.DataProvider;

public class APIs {
    protected static final String BASE_URL = "https://www.fattin.com/api.php";
    protected static final String CALLBACK_NAME_PARAM = "c";
    private static final String HOTSPOT_GET_AUTHENTICATION = "hotspot.get_authentication_token";
    private static final String HOTSPOT_GET_PRICE = "hotspot.get_price";
    private static final String HOTSPOT_HEARTBEAT = "hotspot.heartbeat";
    private static final String HOTSPOT_IS_REGISTERED = "hotspot.is_registered";
    private static final String HOTSPOT_REGISTER = "hotspot.register";
    private static final String HOTSPOT_SET_PRICE = "hotspot.set_price";
    private static final String LOG_TAG = "APIs";
    protected static final String SERVICE_NAME_PARAM = "r";
    private static final String SYSTEM_GET_ALLOWED_IPS = "system.get_allowed_ips";
    private static final String USER_DELETE_AUTHENTICATION = "user.delete_authentication_token";
    private static final String USER_GET_AUTHENTICATION = "user.get_authentication_token";
    private static final String USER_IS_AUTHENTICATED = "user.is_authenticated";
    private static final String USER_REGISTERATION = "user.register";

    public static RestResponse system_get_allowed_ips() throws Exception {
        RestRequest request = new RestRequest(BASE_URL);
        request.addRestParam(SERVICE_NAME_PARAM, SYSTEM_GET_ALLOWED_IPS);
        return request.execute();
    }

    public static RestResponse user_register(String email, String password) throws Exception {
        RestRequest request = new RestRequest(BASE_URL);
        request.addRestParam(SERVICE_NAME_PARAM, USER_REGISTERATION);
        request.addRestParam("email", email);
        request.addRestParam("password", password);
        return request.execute();
    }

    public static RestResponse user_get_authentication_token(String email, String password) throws Exception {
        RestRequest request = new RestRequest(BASE_URL);
        request.addRestParam(SERVICE_NAME_PARAM, USER_GET_AUTHENTICATION);
        request.addRestParam("email", email);
        request.addRestParam("password", password);
        return request.execute();
    }

    public static RestResponse user_is_authenticated() throws Exception {
        RestRequest request = new RestRequest(BASE_URL);
        request.addRestParam(SERVICE_NAME_PARAM, USER_IS_AUTHENTICATED);
        request.addRestParam("token", DataProvider.getUserAuthenticationToken());
        return request.execute();
    }

    public static RestResponse user_delete_authentication_token() throws Exception {
        RestRequest request = new RestRequest(BASE_URL);
        request.addRestParam(SERVICE_NAME_PARAM, USER_DELETE_AUTHENTICATION);
        request.addRestParam("token", DataProvider.getUserAuthenticationToken());
        return request.execute();
    }

    public static RestResponse hotspot_is_registered() throws Exception {
        RestRequest request = new RestRequest(BASE_URL);
        request.addRestParam(SERVICE_NAME_PARAM, HOTSPOT_IS_REGISTERED);
        request.addRestParam("a", DataProvider.getUserAuthenticationToken());
        request.addRestParam("hotspot_uuid", DataProvider.getHotspotUUID());
        return request.execute();
    }

    public static RestResponse hotspot_register(String price_per_hour) throws Exception {
        RestRequest request = new RestRequest(BASE_URL);
        request.addRestParam(SERVICE_NAME_PARAM, HOTSPOT_REGISTER);
        request.addRestParam("a", DataProvider.getUserAuthenticationToken());
        request.addRestParam("hotspot_uuid", DataProvider.getHotspotUUID());
        request.addRestParam("price_per_hour", price_per_hour);
        return request.execute();
    }

    public static RestResponse hotspot_get_authentication_token() throws Exception {
        RestRequest request = new RestRequest(BASE_URL);
        request.addRestParam(SERVICE_NAME_PARAM, HOTSPOT_GET_AUTHENTICATION);
        request.addRestParam("a", DataProvider.getUserAuthenticationToken());
        request.addRestParam("hotspot_uuid", DataProvider.getHotspotUUID());
        return request.execute();
    }

    public static RestResponse hotspot_get_price() throws Exception {
        RestRequest request = new RestRequest(BASE_URL);
        request.addRestParam(SERVICE_NAME_PARAM, HOTSPOT_GET_PRICE);
        request.addRestParam("a", DataProvider.getUserAuthenticationToken());
        request.addRestParam("hotspot_token", DataProvider.getHotspotAuthToken());
        return request.execute();
    }

    public static RestResponse hotspot_set_price(String price_per_hour) throws Exception {
        RestRequest request = new RestRequest(BASE_URL);
        request.addRestParam(SERVICE_NAME_PARAM, HOTSPOT_SET_PRICE);
        request.addRestParam("a", DataProvider.getUserAuthenticationToken());
        request.addRestParam("hotspot_token", DataProvider.getHotspotAuthToken());
        request.addRestParam("price_per_hour", price_per_hour);
        return request.execute();
    }

    public static RestResponse hotspot_heartbeat(String device_tokens) throws Exception {
        RestRequest request = new RestRequest(BASE_URL);
        request.addRestParam(SERVICE_NAME_PARAM, HOTSPOT_HEARTBEAT);
        request.addRestParam("a", DataProvider.getUserAuthenticationToken());
        request.addRestParam("hotspot_token", DataProvider.getHotspotAuthToken());
        request.addRestParam("device_tokens", device_tokens);
        return request.execute();
    }
}
