package com.fattin.hotspot.apilib;

import com.fattin.hotspot.app.Constants;
import com.fattin.hotspot.helpers.Log;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.acra.ACRAConstants;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.xbill.DNS.KEYRecord;

public class RestClient {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$fattin$hotspot$apilib$RestClient$RequestMethod = null;
    public static final boolean DEFAULT_HTTP_SECURE = true;
    private static final String LOG_TAG = "RestClient";
    private ArrayList<NameValuePair> headers;
    private String message;
    private ArrayList<NameValuePair> params;
    private String response;
    private int responseCode;
    private String url;

    public class EasySSLSocketFactory extends SSLSocketFactory {
        SSLContext sslContext;

        /* renamed from: com.fattin.hotspot.apilib.RestClient.EasySSLSocketFactory.1 */
        class C00151 implements X509TrustManager {
            C00151() {
            }

            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        }

        public EasySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
            super(truststore);
            this.sslContext = SSLContext.getInstance("TLS");
            TrustManager tm = new C00151();
            this.sslContext.init(null, new TrustManager[]{tm}, null);
        }

        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
            return this.sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        public Socket createSocket() throws IOException {
            return this.sslContext.getSocketFactory().createSocket();
        }
    }

    public enum RequestMethod {
        GET,
        POST
    }

    static /* synthetic */ int[] $SWITCH_TABLE$com$fattin$hotspot$apilib$RestClient$RequestMethod() {
        int[] iArr = $SWITCH_TABLE$com$fattin$hotspot$apilib$RestClient$RequestMethod;
        if (iArr == null) {
            iArr = new int[RequestMethod.values().length];
            try {
                iArr[RequestMethod.GET.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[RequestMethod.POST.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            $SWITCH_TABLE$com$fattin$hotspot$apilib$RestClient$RequestMethod = iArr;
        }
        return iArr;
    }

    public String getResponse() {
        return this.response;
    }

    public String getErrorMessage() {
        return this.message;
    }

    public int getResponseCode() {
        return this.responseCode;
    }

    RestClient(String url, ArrayList<NameValuePair> params) {
        this.url = url;
        this.params = params;
        this.headers = new ArrayList();
    }

    public RestClient(String url) {
        this.url = url;
        this.params = new ArrayList();
        this.headers = new ArrayList();
    }

    public void setParams(ArrayList<NameValuePair> params) {
        this.params = params;
    }

    public void AddParam(String name, String value) {
        this.params.add(new BasicNameValuePair(name, value));
    }

    public void AddHeader(String name, String value) {
        this.headers.add(new BasicNameValuePair(name, value));
    }

    public void Execute(RequestMethod method) throws Exception {
        Execute(method, DEFAULT_HTTP_SECURE);
    }

    public void Execute(RequestMethod method, boolean secure) throws Exception {
        Iterator it;
        NameValuePair h;
        switch ($SWITCH_TABLE$com$fattin$hotspot$apilib$RestClient$RequestMethod()[method.ordinal()]) {
            case KEYRecord.PROTOCOL_TLS /*1*/:
                String combinedParams = ACRAConstants.DEFAULT_STRING_VALUE;
                if (!this.params.isEmpty()) {
                    combinedParams = new StringBuilder(String.valueOf(combinedParams)).append("?").toString();
                    it = this.params.iterator();
                    while (it.hasNext()) {
                        NameValuePair p = (NameValuePair) it.next();
                        String paramString = p.getName() + "=" + URLEncoder.encode(p.getValue(), "UTF-8");
                        if (combinedParams.length() > 1) {
                            combinedParams = new StringBuilder(String.valueOf(combinedParams)).append("&").append(paramString).toString();
                        } else {
                            combinedParams = new StringBuilder(String.valueOf(combinedParams)).append(paramString).toString();
                        }
                    }
                }
                Log.m3v(LOG_TAG, "API_Request=" + this.url + combinedParams);
                HttpGet request = new HttpGet(this.url + combinedParams);
                it = this.headers.iterator();
                while (it.hasNext()) {
                    h = (NameValuePair) it.next();
                    request.addHeader(h.getName(), h.getValue());
                }
                Log.m0d(LOG_TAG, "request=" + request.getURI().toString());
                if (secure) {
                    executeSecureRequest(request);
                } else {
                    executeRequest(request);
                }
            case KEYRecord.PROTOCOL_EMAIL /*2*/:
                HttpPost request2 = new HttpPost(this.url);
                it = this.headers.iterator();
                while (it.hasNext()) {
                    h = (NameValuePair) it.next();
                    request2.addHeader(h.getName(), h.getValue());
                }
                if (!this.params.isEmpty()) {
                    request2.setEntity(new UrlEncodedFormEntity(this.params, "UTF-8"));
                }
                if (secure) {
                    executeSecureRequest(request2);
                } else {
                    executeRequest(request2);
                }
            default:
        }
    }

    private void executeSecureRequest(HttpUriRequest request) {
        HttpClient client;
        try {
            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, "UTF-8");
            HttpConnectionParams.setConnectionTimeout(params, Constants.WEBCLIENT_CONNECTION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(params, Constants.WEBCLIENT_SOCKET_TIMEOUT);
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
            client = new DefaultHttpClient(new ThreadSafeClientConnManager(params, schemeRegistry), params);
            Log.m0d(LOG_TAG, "USING HTTPS");
        } catch (Exception e) {
            Log.m5w(LOG_TAG, e);
            client = new DefaultHttpClient();
        }
        try {
            HttpResponse httpResponse = client.execute(request);
            this.responseCode = httpResponse.getStatusLine().getStatusCode();
            this.message = httpResponse.getStatusLine().getReasonPhrase();
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                this.response = convertStreamToString(instream);
                instream.close();
                Log.m0d(LOG_TAG, "response=" + this.response);
            }
        } catch (ClientProtocolException e2) {
            Log.m5w(LOG_TAG, e2);
            client.getConnectionManager().shutdown();
        } catch (IOException e3) {
            Log.m5w(LOG_TAG, e3);
            client.getConnectionManager().shutdown();
        }
    }

    private void executeRequest(HttpUriRequest request) {
        HttpClient client;
        try {
            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, "UTF-8");
            HttpConnectionParams.setConnectionTimeout(params, Constants.WEBCLIENT_CONNECTION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(params, Constants.WEBCLIENT_SOCKET_TIMEOUT);
            client = new DefaultHttpClient(params);
            Log.m0d(LOG_TAG, "USING HTTP");
        } catch (Exception e) {
            Log.m5w(LOG_TAG, e);
            client = new DefaultHttpClient();
        }
        try {
            HttpResponse httpResponse = client.execute(request);
            this.responseCode = httpResponse.getStatusLine().getStatusCode();
            this.message = httpResponse.getStatusLine().getReasonPhrase();
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                this.response = convertStreamToString(instream);
                instream.close();
                Log.m0d(LOG_TAG, "response=" + this.response);
            }
        } catch (ClientProtocolException e2) {
            Log.m5w(LOG_TAG, e2);
            client.getConnectionManager().shutdown();
        } catch (IOException e3) {
            Log.m5w(LOG_TAG, e3);
            client.getConnectionManager().shutdown();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static java.lang.String convertStreamToString(java.io.InputStream r6) {
        /*
        r2 = new java.io.BufferedReader;
        r4 = new java.io.InputStreamReader;
        r4.<init>(r6);
        r2.<init>(r4);
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r1 = 0;
    L_0x0010:
        r1 = r2.readLine();	 Catch:{ IOException -> 0x0035 }
        if (r1 != 0) goto L_0x001e;
    L_0x0016:
        r6.close();	 Catch:{ IOException -> 0x004c }
    L_0x0019:
        r4 = r3.toString();
        return r4;
    L_0x001e:
        r4 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x0035 }
        r5 = java.lang.String.valueOf(r1);	 Catch:{ IOException -> 0x0035 }
        r4.<init>(r5);	 Catch:{ IOException -> 0x0035 }
        r5 = "\n";
        r4 = r4.append(r5);	 Catch:{ IOException -> 0x0035 }
        r4 = r4.toString();	 Catch:{ IOException -> 0x0035 }
        r3.append(r4);	 Catch:{ IOException -> 0x0035 }
        goto L_0x0010;
    L_0x0035:
        r0 = move-exception;
        r0.printStackTrace();	 Catch:{ all -> 0x0042 }
        r6.close();	 Catch:{ IOException -> 0x003d }
        goto L_0x0019;
    L_0x003d:
        r0 = move-exception;
        r0.printStackTrace();
        goto L_0x0019;
    L_0x0042:
        r4 = move-exception;
        r6.close();	 Catch:{ IOException -> 0x0047 }
    L_0x0046:
        throw r4;
    L_0x0047:
        r0 = move-exception;
        r0.printStackTrace();
        goto L_0x0046;
    L_0x004c:
        r0 = move-exception;
        r0.printStackTrace();
        goto L_0x0019;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fattin.hotspot.apilib.RestClient.convertStreamToString(java.io.InputStream):java.lang.String");
    }
}
