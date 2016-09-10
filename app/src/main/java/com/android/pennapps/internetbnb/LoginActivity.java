package com.android.pennapps.internetbnb;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

public class LoginActivity extends AppCompatActivity {

    EditText emailText;
    EditText passText;
    Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Firebase.setAndroidContext(this);

        emailText = (EditText)findViewById(R.id.editEmailSignup);
        passText = (EditText)findViewById(R.id.editPasswordSignup);
        login = (Button)findViewById(R.id.loginBut);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = emailText.getText().toString();
                String password = passText.getText().toString();

                email = email.trim();
                password = password.trim();

                if(email.isEmpty() || password.isEmpty()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setMessage("Cant leave fields empty")
                            .setTitle("Error")
                            .setPositiveButton("ok", null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }else{
                    Firebase ref = new Firebase("https://wifibnb-53ab1.firebaseio.com");
                    ref.authWithPassword(email, password, new Firebase.AuthResultHandler(){
                        @Override
                        public void onAuthenticated(AuthData authData) {
                            System.out.println("User ID: " + authData.getUid() + ", Provider: " + authData.getProvider());
                            Intent i = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(i);
                        }
                        @Override
                        public void onAuthenticationError(FirebaseError firebaseError) {
                            // there was an error
                            System.out.println("not working");
                        }
                    });
                }
            }
        });

    }


}
