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

public class RegUserActivity extends AbstractActivity {
    private EditText confirm_password_edit;
    private TextView message_text;
    private EditText password_edit;
    private ActivityResultReceiverCallback processUserRegistrationCallback;
    private EditText username_edit;

    /* renamed from: com.fattin.hotspot.activity.RegUserActivity.2 */
    class C00092 implements OnClickListener {
        C00092() {
        }

        public void onClick(View v) {
            RegUserActivity.this.message_text.setText(ACRAConstants.DEFAULT_STRING_VALUE);
            String username = RegUserActivity.this.username_edit.getText().toString();
            String password = RegUserActivity.this.password_edit.getText().toString();
            if (username.isEmpty() || password.isEmpty()) {
                RegUserActivity.this.message_text.setText("Username or password is missing!");
            } else if (RegUserActivity.this.confirm_password_edit.getText().toString().equals(RegUserActivity.this.password_edit.getText().toString())) {
                BusinessLogic.processUserRegistration(username, password, RegUserActivity.this.processUserRegistrationCallback);
            } else {
                RegUserActivity.this.message_text.setText("Passwords don't match!");
            }
        }
    }

    /* renamed from: com.fattin.hotspot.activity.RegUserActivity.3 */
    class C00103 implements OnClickListener {
        C00103() {
        }

        public void onClick(View v) {
            RegUserActivity.this.setResult(0);
            RegUserActivity.this.finish();
        }
    }

    class 1 extends ActivityResultReceiverCallback {
        1(AbstractActivity abstractActivity) {
            super();
        }

        public void onSuccess(Bundle resultData) {
            super.onSuccess(resultData);
            RegUserActivity.this.setResult(-1);
            RegUserActivity.this.finish();
        }

        public void onFailure(String message, Bundle resultData) {
            RegUserActivity.this.message_text.setText("User Registration Failed!");
            super.onFailure(message, resultData);
        }
    }

    public RegUserActivity() {
        this.processUserRegistrationCallback = new 1(this);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.LOG_TAG = "RegUserActivity";
        setContentView(C0000R.layout.user_reg);
        this.username_edit = (EditText) findViewById(C0000R.id.user_reg_username_EditText);
        this.password_edit = (EditText) findViewById(C0000R.id.user_reg_password_EditText);
        this.confirm_password_edit = (EditText) findViewById(C0000R.id.user_reg_confirm_password_EditText);
        this.message_text = (TextView) findViewById(C0000R.id.user_reg_message_TextView);
        this.username_edit.setText(ACRAConstants.DEFAULT_STRING_VALUE);
        this.message_text.setText(ACRAConstants.DEFAULT_STRING_VALUE);
        ((Button) findViewById(C0000R.id.user_reg_register_Button)).setOnClickListener(new C00092());
        ((Button) findViewById(C0000R.id.user_reg_cancel_Button)).setOnClickListener(new C00103());
    }
}
