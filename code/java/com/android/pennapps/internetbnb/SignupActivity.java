package com.android.pennapps.internetbnb;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    EditText email;
    EditText pass;
    Button signup;

    public WifiManager wifiManager;
    public Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        Firebase.setAndroidContext(this);

        email = (EditText)findViewById(R.id.editEmailSignup);
        pass = (EditText)findViewById(R.id.editPasswordSignup);

        signup = (Button)findViewById(R.id.signupBut);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String Email = email.getText().toString();
                String Password = pass.getText().toString();

                Email = Email.trim();
                Password = Password.trim();

                Firebase ref = new Firebase("https://wifibnb-53ab1.firebaseio.com");
                ref.createUser(Email, Password, new Firebase.ValueResultHandler<Map<String, Object>>() {
                    @Override
                    public void onSuccess(Map<String, Object> result) {
                        System.out.println("Successfully created user account with uid: " + result.get("uid"));
                        Intent i = new Intent(SignupActivity.this, LoginActivity.class);
                        startActivity(i);
                    }

                    @Override
                    public void onError(FirebaseError firebaseError) {
                        // there was an error
                        System.out.println("not working!");
                    }
                });
            }
        });
    }
}
