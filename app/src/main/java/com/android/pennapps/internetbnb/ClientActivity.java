package com.android.pennapps.internetbnb;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class ClientActivity extends AppCompatActivity {

    String networkSSID = "InternetBnB";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        turnOnWifi();

    }

    public void turnOnWifi(){
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if(wifi.isWifiEnabled()==false){
            Toast.makeText(getApplicationContext(), "Enabling Wifi", Toast.LENGTH_LONG).show();
            wifi.setWifiEnabled(true);

        }else{
            //connects to the open wifi ssid called InternetBnB
            connectWifi();

        }
    }

    public void connectWifi(){
        WifiConfiguration wfc = new WifiConfiguration();
        wfc.SSID = "\"".concat(networkSSID).concat("\"");
        wfc.status = WifiConfiguration.Status.ENABLED; //stackoverflow DISABLED
        wfc.priority = 40;

        wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        wfc.allowedAuthAlgorithms.clear();
        wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

    }
}
