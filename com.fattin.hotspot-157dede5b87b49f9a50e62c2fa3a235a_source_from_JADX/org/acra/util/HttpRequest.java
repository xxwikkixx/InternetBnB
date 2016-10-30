package org.acra.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.log.ACRALog;
import org.acra.log.AndroidLogDelegate;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.xbill.DNS.KEYRecord.Flags;

public final class HttpRequest {
    private static ACRALog log;
    private int connectionTimeOut;
    private String login;
    private int maxNrRetries;
    private String password;
    private int socketTimeOut;

    private static class SocketTimeOutRetryHandler implements HttpRequestRetryHandler {
        private final HttpParams httpParams;
        private final int maxNrRetries;

        private SocketTimeOutRetryHandler(HttpParams httpParams, int maxNrRetries) {
            this.httpParams = httpParams;
            this.maxNrRetries = maxNrRetries;
        }

        public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
            if (exception instanceof SocketTimeoutException) {
                if (executionCount <= this.maxNrRetries) {
                    if (this.httpParams != null) {
                        int newSocketTimeOut = HttpConnectionParams.getSoTimeout(this.httpParams) * 2;
                        HttpConnectionParams.setSoTimeout(this.httpParams, newSocketTimeOut);
                        HttpRequest.log.d(ACRA.LOG_TAG, "SocketTimeOut - increasing time out to " + newSocketTimeOut + " millis and trying again");
                    } else {
                        HttpRequest.log.d(ACRA.LOG_TAG, "SocketTimeOut - no HttpParams, cannot increase time out. Trying again with current settings");
                    }
                    return true;
                }
                HttpRequest.log.d(ACRA.LOG_TAG, "SocketTimeOut but exceeded max number of retries : " + this.maxNrRetries);
            }
            return false;
        }
    }

    public HttpRequest() {
        this.connectionTimeOut = ACRAConstants.DEFAULT_CONNECTION_TIMEOUT;
        this.socketTimeOut = ACRAConstants.DEFAULT_CONNECTION_TIMEOUT;
        this.maxNrRetries = 3;
    }

    static {
        log = new AndroidLogDelegate();
    }

    static void setLog(ACRALog log) {
        log = log;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setConnectionTimeOut(int connectionTimeOut) {
        this.connectionTimeOut = connectionTimeOut;
    }

    public void setSocketTimeOut(int socketTimeOut) {
        this.socketTimeOut = socketTimeOut;
    }

    public void setMaxNrRetries(int maxNrRetries) {
        this.maxNrRetries = maxNrRetries;
    }

    public void sendPost(URL url, Map<?, ?> parameters) throws IOException {
        HttpClient httpClient = getHttpClient();
        HttpPost httpPost = getHttpPost(url, parameters);
        log.d(ACRA.LOG_TAG, "Sending request to " + url);
        Iterator i$ = parameters.keySet().iterator();
        while (i$.hasNext()) {
            i$.next();
        }
        HttpResponse response = httpClient.execute(httpPost, new BasicHttpContext());
        if (response != null) {
            if (response.getStatusLine() != null) {
                String statusCode = Integer.toString(response.getStatusLine().getStatusCode());
                if (statusCode.startsWith("4") || statusCode.startsWith("5")) {
                    throw new IOException("Host returned error code " + statusCode);
                }
            }
            EntityUtils.toString(response.getEntity());
        }
    }

    private HttpClient getHttpClient() {
        HttpParams httpParams = new BasicHttpParams();
        httpParams.setParameter("http.protocol.cookie-policy", "rfc2109");
        HttpConnectionParams.setConnectionTimeout(httpParams, this.connectionTimeOut);
        HttpConnectionParams.setSoTimeout(httpParams, this.socketTimeOut);
        HttpConnectionParams.setSocketBufferSize(httpParams, Flags.FLAG2);
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", new PlainSocketFactory(), 80));
        registry.register(new Scheme("https", new FakeSocketFactory(), 443));
        DefaultHttpClient httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(httpParams, registry), httpParams);
        httpClient.setHttpRequestRetryHandler(new SocketTimeOutRetryHandler(this.maxNrRetries, null));
        return httpClient;
    }

    private UsernamePasswordCredentials getCredentials() {
        if (this.login == null && this.password == null) {
            return null;
        }
        return new UsernamePasswordCredentials(this.login, this.password);
    }

    private HttpPost getHttpPost(URL url, Map<?, ?> parameters) throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(url.toString());
        UsernamePasswordCredentials creds = getCredentials();
        if (creds != null) {
            httpPost.addHeader(BasicScheme.authenticate(creds, "UTF-8", false));
        }
        httpPost.setHeader("User-Agent", "Android");
        httpPost.setHeader("Accept", "text/html,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setEntity(new StringEntity(getParamsAsString(parameters), "UTF-8"));
        return httpPost;
    }

    private String getParamsAsString(Map<?, ?> parameters) throws UnsupportedEncodingException {
        StringBuilder dataBfr = new StringBuilder();
        for (Object key : parameters.keySet()) {
            Object value;
            if (dataBfr.length() != 0) {
                dataBfr.append('&');
            }
            Object preliminaryValue = parameters.get(key);
            if (preliminaryValue == null) {
                value = ACRAConstants.DEFAULT_STRING_VALUE;
            } else {
                value = preliminaryValue;
            }
            dataBfr.append(URLEncoder.encode(key.toString(), "UTF-8"));
            dataBfr.append('=');
            dataBfr.append(URLEncoder.encode(value.toString(), "UTF-8"));
        }
        return dataBfr.toString();
    }
}
