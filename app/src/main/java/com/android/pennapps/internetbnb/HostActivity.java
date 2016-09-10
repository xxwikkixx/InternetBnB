package com.android.pennapps.internetbnb;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.pennapps.internetbnb.wifihotspotutils.WifiApManager;

import java.lang.reflect.Method;

public class HostActivity extends AppCompatActivity {

    ToggleButton startHotSpot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        WifiAccessManager.setWifiApState(this, true);


    }



}
