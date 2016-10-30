package com.fattin.hotspot.netfilter;

import com.fattin.hotspot.helpers.Util;
import org.acra.ACRAConstants;

public class AccessListDataObject {
    private String device_ip_address;
    private String device_mac_address;
    private String device_token;
    private int duration;
    private float earnings;
    private long first_connected_timestamp;
    private String hostname;
    private long last_access_timestamp;
    private long last_heartbeat_timestamp;
    private long session_start_timestamp;

    public AccessListDataObject(String device_mac_address, String device_token, String hostname) {
        this.device_mac_address = ACRAConstants.DEFAULT_STRING_VALUE;
        this.device_ip_address = ACRAConstants.DEFAULT_STRING_VALUE;
        this.device_token = ACRAConstants.DEFAULT_STRING_VALUE;
        this.hostname = ACRAConstants.DEFAULT_STRING_VALUE;
        this.earnings = 0.0f;
        this.duration = 0;
        _setDeviceMacAddress(device_mac_address);
        _setDeviceToken(device_token);
        _setHostName(hostname);
        _setLastAccessTimestamp(Util.getCurrentTimeStamp());
        _setLastHeartbeatTimestamp(Util.getCurrentTimeStamp());
    }

    public String getDeviceMacAddress() {
        return this.device_mac_address;
    }

    public void setDeviceMacAddress(String device_mac_address) {
        _setDeviceMacAddress(device_mac_address);
        AccessListDatabase.sendAccessListChangeBroadcast();
    }

    private void _setDeviceMacAddress(String device_mac_address) {
        this.device_mac_address = device_mac_address;
    }

    public String getDeviceIPAddress() {
        return this.device_ip_address;
    }

    public void setDeviceIPAddress(String device_ip_address) {
        _setDeviceIPAddress(device_ip_address);
        AccessListDatabase.sendAccessListChangeBroadcast();
    }

    private void _setDeviceIPAddress(String device_ip_address) {
        this.device_ip_address = device_ip_address;
    }

    public String getDeviceToken() {
        return this.device_token;
    }

    public void setDeviceToken(String device_token) {
        _setDeviceToken(device_token);
        AccessListDatabase.sendAccessListChangeBroadcast();
    }

    private void _setDeviceToken(String device_token) {
        if (this.device_token.isEmpty() && !device_token.isEmpty()) {
            _setSessionStartTimestamp(Util.getCurrentTimeStamp());
        }
        this.device_token = device_token;
    }

    public String getHostName() {
        return this.hostname;
    }

    public void setHostName(String hostname) {
        _setHostName(hostname);
        AccessListDatabase.sendAccessListChangeBroadcast();
    }

    private void _setHostName(String hostname) {
        this.hostname = hostname;
    }

    public long getFirstConnectedTimestamp() {
        return this.first_connected_timestamp;
    }

    public void setFirstConnectedTimestamp(long first_connected_timestamp) {
        _setFirstConnectedTimestamp(first_connected_timestamp);
        AccessListDatabase.sendAccessListChangeBroadcast();
    }

    private void _setFirstConnectedTimestamp(long first_connected_timestamp) {
        this.first_connected_timestamp = first_connected_timestamp;
    }

    public long getSessionStartTimestamp() {
        return this.session_start_timestamp;
    }

    public void setSessionStartTimestamp(long session_start_timestamp) {
        _setSessionStartTimestamp(session_start_timestamp);
        AccessListDatabase.sendAccessListChangeBroadcast();
    }

    private void _setSessionStartTimestamp(long session_start_timestamp) {
        this.session_start_timestamp = session_start_timestamp;
    }

    public long getLastHeartbeatTimestamp() {
        return this.last_heartbeat_timestamp;
    }

    public void setLastHeartbeatTimestamp(long last_heartbeat_timestamp) {
        _setLastHeartbeatTimestamp(last_heartbeat_timestamp);
        AccessListDatabase.sendAccessListChangeBroadcast();
    }

    private void _setLastHeartbeatTimestamp(long last_heartbeat_timestamp) {
        this.last_heartbeat_timestamp = last_heartbeat_timestamp;
    }

    public long getLastAccessTimestamp() {
        return this.last_access_timestamp;
    }

    public void setLastAccessTimestamp(long last_access_timestamp) {
        _setLastAccessTimestamp(last_access_timestamp);
        AccessListDatabase.sendAccessListChangeBroadcast();
    }

    private void _setLastAccessTimestamp(long last_access_timestamp) {
        this.last_access_timestamp = last_access_timestamp;
    }

    public float getEarnings() {
        return this.earnings;
    }

    public void setEarnings(float earnings) {
        _setEarnings(earnings);
        AccessListDatabase.sendAccessListChangeBroadcast();
    }

    private void _setEarnings(float earnings) {
        this.earnings = earnings;
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int duration) {
        _setDuration(duration);
        AccessListDatabase.sendAccessListChangeBroadcast();
    }

    public void _setDuration(int duration) {
        this.duration = duration;
    }
}
