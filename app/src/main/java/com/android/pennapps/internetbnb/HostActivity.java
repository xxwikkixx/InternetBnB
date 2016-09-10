package com.android.pennapps.internetbnb;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.pennapps.internetbnb.wifihotspotutils.WifiApManager;

public class HostActivity extends AppCompatActivity {

    WifiApManager wifiApManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();



    }
}
