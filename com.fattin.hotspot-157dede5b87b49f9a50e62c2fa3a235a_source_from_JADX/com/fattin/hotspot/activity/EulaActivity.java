package com.fattin.hotspot.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import com.fattin.hotspot.C0000R;
import com.fattin.hotspot.app.DataProvider;
import com.google.analytics.tracking.android.EasyTracker;

public class EulaActivity extends Activity {
    private static final String LOG_TAG = "EulaActivity";

    /* renamed from: com.fattin.hotspot.activity.EulaActivity.1 */
    class C00051 implements OnClickListener {
        C00051() {
        }

        public void onClick(View v) {
            DataProvider.setSendCrashReport(((CheckBox) EulaActivity.this.findViewById(C0000R.id.eula_agree_CheckBox)).isChecked());
            DataProvider.setEulaAgreed(true);
            EulaActivity.this.setResult(-1);
            EulaActivity.this.finish();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0000R.layout.eula);
        ((Button) findViewById(C0000R.id.eula_agree_Button)).setOnClickListener(new C00051());
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
