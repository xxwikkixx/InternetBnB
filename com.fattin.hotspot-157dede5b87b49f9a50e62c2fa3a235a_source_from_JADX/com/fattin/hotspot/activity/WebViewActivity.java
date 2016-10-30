package com.fattin.hotspot.activity;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.fattin.hotspot.C0000R;
import com.google.analytics.tracking.android.EasyTracker;

public class WebViewActivity extends Activity {
    private WebView webView;

    /* renamed from: com.fattin.hotspot.activity.WebViewActivity.1 */
    class C00141 extends WebViewClient {
        C00141() {
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0000R.layout.web_auth);
        this.webView = (WebView) findViewById(C0000R.id.auth_WebView);
        this.webView.setWebViewClient(new C00141());
        this.webView.loadUrl(getIntent().getData().toString());
    }

    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
    }

    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance().activityStop(this);
    }
}
