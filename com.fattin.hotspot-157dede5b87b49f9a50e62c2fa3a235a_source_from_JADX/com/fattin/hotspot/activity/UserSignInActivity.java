package com.fattin.hotspot.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.fattin.hotspot.C0000R;
import com.fattin.hotspot.app.BusinessLogic;
import com.fattin.hotspot.app.DataProvider;
import com.fattin.hotspot.sm.StateMachine;
import org.acra.ACRAConstants;
import org.xbill.DNS.KEYRecord;

public class UserSignInActivity extends AbstractActivity {
    private static final int HOTSPOT_REGISTER_REQUEST_CODE = 1002;
    private static final int USER_REGISTER_REQUEST_CODE = 1001;
    private TextView message_text;
    private EditText password_edit;
    private ActivityResultReceiverCallback processUserLoginCallback;
    private EditText username_edit;

    /* renamed from: com.fattin.hotspot.activity.UserSignInActivity.2 */
    class C00112 implements OnClickListener {
        C00112() {
        }

        public void onClick(View v) {
            UserSignInActivity.this.onClickSignInAction();
        }
    }

    /* renamed from: com.fattin.hotspot.activity.UserSignInActivity.3 */
    class C00123 implements OnClickListener {
        C00123() {
        }

        public void onClick(View v) {
            UserSignInActivity.this.startActivityForResult(new Intent(UserSignInActivity.this, RegUserActivity.class), UserSignInActivity.USER_REGISTER_REQUEST_CODE);
        }
    }

    /* renamed from: com.fattin.hotspot.activity.UserSignInActivity.4 */
    class C00134 implements OnClickListener {
        C00134() {
        }

        public void onClick(View v) {
            UserSignInActivity.this.setResult(0);
            UserSignInActivity.this.finish();
        }
    }

    class 1 extends ActivityResultReceiverCallback {
        1(AbstractActivity abstractActivity) {
            super();
        }

        public void onSuccess(Bundle resultData) {
            super.onSuccess(resultData);
            UserSignInActivity.this.message_text.setText("Sign in successful!");
            DataProvider.setUserLoggedIn(true);
            UserSignInActivity.this.setResult(-1);
            UserSignInActivity.this.finish();
        }

        public void onFailure(String message, Bundle resultData) {
            super.onFailure(message, resultData);
            UserSignInActivity.this.message_text.setText("Sign in failed!");
        }

        public void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            switch (resultCode) {
                case BusinessLogic.RESULT_CODE_HOTSPOT_NOT_REGISTERED /*8760*/:
                    UserSignInActivity.this.stopProgress();
                    UserSignInActivity.this.startActivityForResult(new Intent(UserSignInActivity.this, RegHotspotActivity.class), UserSignInActivity.HOTSPOT_REGISTER_REQUEST_CODE);
                default:
            }
        }
    }

    public UserSignInActivity() {
        this.processUserLoginCallback = new 1(this);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.LOG_TAG = "UserSignInActivity";
        setContentView(C0000R.layout.user_signin);
        this.username_edit = (EditText) findViewById(C0000R.id.user_signin_username_EditText);
        this.password_edit = (EditText) findViewById(C0000R.id.user_signin_password_EditText);
        this.message_text = (TextView) findViewById(C0000R.id.user_signin_message_TextView);
        this.username_edit.setText(DataProvider.getEmail());
        this.password_edit.setText(DataProvider.getPassword());
        this.message_text.setText(ACRAConstants.DEFAULT_STRING_VALUE);
        ((Button) findViewById(C0000R.id.user_signin_login_Button)).setOnClickListener(new C00112());
        ((Button) findViewById(C0000R.id.user_signin_signup_Button)).setOnClickListener(new C00123());
        ((Button) findViewById(C0000R.id.user_signin_cancel_Button)).setOnClickListener(new C00134());
    }

    private void onClickSignInAction() {
        this.message_text.setText(ACRAConstants.DEFAULT_STRING_VALUE);
        String username = this.username_edit.getText().toString();
        String password = this.password_edit.getText().toString();
        if (username.isEmpty() || password.isEmpty()) {
            this.message_text.setText("Username or password is missing!");
            return;
        }
        DataProvider.setEmail(username);
        DataProvider.setPassword(password);
        BusinessLogic.processUserLogin(username, password, this.processUserLoginCallback);
    }

    protected void onResume() {
        this.username_edit.setText(DataProvider.getEmail());
        this.password_edit.setText(DataProvider.getPassword());
        this.message_text.setText(ACRAConstants.DEFAULT_STRING_VALUE);
        super.onResume();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case USER_REGISTER_REQUEST_CODE /*1001*/:
                switch (resultCode) {
                    case StateMachine.SM_QUIT_CMD /*-1*/:
                        BusinessLogic.processUserLogin(DataProvider.getEmail(), DataProvider.getPassword(), this.processUserLoginCallback);
                    case KEYRecord.OWNER_USER /*0*/:
                    default:
                }
            case HOTSPOT_REGISTER_REQUEST_CODE /*1002*/:
                switch (resultCode) {
                    case StateMachine.SM_QUIT_CMD /*-1*/:
                        BusinessLogic.processUserLogin(DataProvider.getEmail(), DataProvider.getPassword(), this.processUserLoginCallback);
                    case KEYRecord.OWNER_USER /*0*/:
                    default:
                }
            default:
        }
    }
}
