package com.fattin.hotspot.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.fattin.hotspot.C0000R;
import com.fattin.hotspot.app.BusinessLogic;
import org.acra.ACRAConstants;

public class RegHotspotActivity extends AbstractActivity {
    private TextView message_text;
    private EditText price_edit;
    private ActivityResultReceiverCallback processHotspotRegistrationCallback;

    /* renamed from: com.fattin.hotspot.activity.RegHotspotActivity.2 */
    class C00072 implements OnClickListener {
        C00072() {
        }

        public void onClick(View v) {
            boolean valid_price;
            RegHotspotActivity.this.message_text.setText(ACRAConstants.DEFAULT_STRING_VALUE);
            try {
                valid_price = Float.parseFloat(RegHotspotActivity.this.price_edit.getText().toString()) > 0.0f;
            } catch (Exception e) {
                valid_price = false;
            }
            if (valid_price) {
                BusinessLogic.processHotspotRegistration(RegHotspotActivity.this.price_edit.getText().toString(), RegHotspotActivity.this.processHotspotRegistrationCallback);
            } else {
                RegHotspotActivity.this.message_text.setText("Price is not valid!");
            }
        }
    }

    /* renamed from: com.fattin.hotspot.activity.RegHotspotActivity.3 */
    class C00083 implements OnClickListener {
        C00083() {
        }

        public void onClick(View v) {
            RegHotspotActivity.this.setResult(0);
            RegHotspotActivity.this.finish();
        }
    }

    class 1 extends ActivityResultReceiverCallback {
        1(AbstractActivity abstractActivity) {
            super();
        }

        public void onSuccess(Bundle resultData) {
            super.onSuccess(resultData);
            RegHotspotActivity.this.setResult(-1);
            RegHotspotActivity.this.finish();
        }

        public void onFailure(String message, Bundle resultData) {
            RegHotspotActivity.this.message_text.setText("Hotspot Registration Failed!");
            super.onFailure(message, resultData);
        }
    }

    public RegHotspotActivity() {
        this.processHotspotRegistrationCallback = new 1(this);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.LOG_TAG = "RegHotspotActivity";
        setContentView(C0000R.layout.hotspot_reg);
        this.price_edit = (EditText) findViewById(C0000R.id.hotspot_reg_price_EditText);
        this.message_text = (TextView) findViewById(C0000R.id.hotspot_reg_message_TextView);
        this.message_text.setText(ACRAConstants.DEFAULT_STRING_VALUE);
        ((Button) findViewById(C0000R.id.hotspot_reg_register_Button)).setOnClickListener(new C00072());
        ((Button) findViewById(C0000R.id.hotspot_reg_cancel_Button)).setOnClickListener(new C00083());
    }
}
