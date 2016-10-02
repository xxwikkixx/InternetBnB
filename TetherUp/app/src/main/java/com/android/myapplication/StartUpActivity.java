package com.android.myapplication;

import android.app.ActionBar;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartUpActivity extends AppCompatActivity {

    Button login;
    Button signup;
    Button skip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        skip = (Button)findViewById(R.id.skipButton);
        login = (Button)findViewById(R.id.loginButton);
        signup = (Button)findViewById(R.id.signupButton);

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(StartUpActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(StartUpActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(StartUpActivity.this, SignUpActivity.class);
                startActivity(i);
            }
        });
    }
}
