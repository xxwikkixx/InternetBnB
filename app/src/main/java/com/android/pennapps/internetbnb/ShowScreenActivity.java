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
import android.view.Window;
import android.widget.Button;

import java.lang.reflect.Method;

public class ShowScreenActivity extends AppCompatActivity {

    Button login;
    Button signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_screen);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        login = (Button)findViewById(R.id.loginBut);
        signup = (Button)findViewById(R.id.signupButton);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ShowScreenActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ShowScreenActivity.this, SignupActivity.class);
                startActivity(i);
                finish();
            }
        });
    }


}

