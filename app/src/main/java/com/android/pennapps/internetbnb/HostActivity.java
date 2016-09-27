package com.android.pennapps.internetbnb;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class HostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        //WifiAccessManager.setWifiApState(this, true);

        //system output test for
        System.out.println(android.net.TrafficStats.getMobileRxBytes());
        System.out.println(android.net.TrafficStats.getTotalRxBytes());



    }
}
