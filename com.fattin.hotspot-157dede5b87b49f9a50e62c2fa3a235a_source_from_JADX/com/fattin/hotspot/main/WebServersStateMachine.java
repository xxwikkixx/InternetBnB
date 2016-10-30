package com.fattin.hotspot.main;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.fattin.hotspot.helpers.Log;
import com.fattin.hotspot.sm.State;
import com.fattin.hotspot.sm.StateMachine;
import com.fattin.hotspot.webserver.AccessWebServer;
import com.fattin.hotspot.webserver.CaptiveWebServer;
import com.fattin.hotspot.webserver.SecureCaptiveWebServer;
import com.fattin.hotspot.webserver.SecureWebServer;
import com.fattin.hotspot.webserver.WebServer;

public class WebServersStateMachine extends StateMachine {
    private static final int BASE = 409600;
    private static final int CMD_STARTING_WEBSERVERS = 409601;
    private static final int CMD_START_WEBSERVERS = 409602;
    private static final int CMD_STOPPING_WEBSERVERS = 409603;
    private static final int CMD_STOP_WEBSERVERS = 409604;
    private static final String LOG_TAG = "WebServerStateMachine";
    public static WebServer accessWebServer;
    public static WebServer captiveWebServer;
    public static SecureWebServer secureCaptiveWebServer;
    private DefaultState mDefaultState;
    private MainStateMachine mMainStateMachine;
    private StartedState mStartedState;
    private StoppedState mStoppedState;

    private class DefaultState extends State {
        private DefaultState() {
        }

        public void enter() {
            Log.m3v(WebServersStateMachine.LOG_TAG, "DefaultState.enter");
        }

        public boolean processMessage(Message msg) {
            Log.m3v(WebServersStateMachine.LOG_TAG, "DefaultState.processMessage " + msg.what);
            switch (msg.what) {
                case WebServersStateMachine.CMD_START_WEBSERVERS /*409602*/:
                    WebServersStateMachine.this.sendMessage((int) WebServersStateMachine.CMD_STARTING_WEBSERVERS);
                    WebServersStateMachine.this.transitionTo(WebServersStateMachine.this.mStartedState);
                    break;
                case WebServersStateMachine.CMD_STOP_WEBSERVERS /*409604*/:
                    WebServersStateMachine.this.sendMessage((int) WebServersStateMachine.CMD_STOPPING_WEBSERVERS);
                    WebServersStateMachine.this.transitionTo(WebServersStateMachine.this.mStoppedState);
                    break;
            }
            return true;
        }

        public void exit() {
            Log.m3v(WebServersStateMachine.LOG_TAG, "DefaultState.exit");
        }
    }

    private class StartedState extends State {
        private StartedState() {
        }

        public void enter() {
            Log.m3v(WebServersStateMachine.LOG_TAG, "StartedState.enter");
        }

        public boolean processMessage(Message msg) {
            Log.m3v(WebServersStateMachine.LOG_TAG, "StartedState.processMessage " + msg.what);
            switch (msg.what) {
                case WebServersStateMachine.CMD_STARTING_WEBSERVERS /*409601*/:
                    WebServersStateMachine.this.startWebServers();
                    WebServersStateMachine.this.mMainStateMachine.sendEventWebServersStarted();
                    break;
                case WebServersStateMachine.CMD_START_WEBSERVERS /*409602*/:
                    WebServersStateMachine.this.mMainStateMachine.sendEventWebServersStarted();
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            Log.m3v(WebServersStateMachine.LOG_TAG, "StartedState.exit");
        }
    }

    private class StoppedState extends State {
        private StoppedState() {
        }

        public void enter() {
            Log.m3v(WebServersStateMachine.LOG_TAG, "StoppedState.enter");
        }

        public boolean processMessage(Message msg) {
            Log.m3v(WebServersStateMachine.LOG_TAG, "StoppedState.processMessage " + msg.what);
            switch (msg.what) {
                case WebServersStateMachine.CMD_STOPPING_WEBSERVERS /*409603*/:
                case WebServersStateMachine.CMD_STOP_WEBSERVERS /*409604*/:
                    WebServersStateMachine.this.stopWebServers();
                    WebServersStateMachine.this.mMainStateMachine.sendEventWebServersStopped();
                    return true;
                default:
                    return false;
            }
        }

        public void exit() {
            Log.m3v(WebServersStateMachine.LOG_TAG, "StoppedState.exit");
        }
    }

    static {
        captiveWebServer = null;
        secureCaptiveWebServer = null;
        accessWebServer = null;
    }

    public WebServersStateMachine(Context context, MainStateMachine msm, Handler target) {
        super(LOG_TAG, target.getLooper());
        this.mDefaultState = new DefaultState();
        this.mStartedState = new StartedState();
        this.mStoppedState = new StoppedState();
        this.mMainStateMachine = msm;
        addState(this.mDefaultState);
        addState(this.mStartedState, this.mDefaultState);
        addState(this.mStoppedState, this.mDefaultState);
        setInitialState(this.mStoppedState);
        start();
    }

    public void startWebServers() {
        if (captiveWebServer == null) {
            captiveWebServer = new CaptiveWebServer();
        }
        if (secureCaptiveWebServer == null) {
            secureCaptiveWebServer = new SecureCaptiveWebServer();
        }
        if (accessWebServer == null) {
            accessWebServer = new AccessWebServer();
        }
        captiveWebServer.startThread();
        secureCaptiveWebServer.startThread();
        accessWebServer.startThread();
    }

    public void stopWebServers() {
        if (captiveWebServer != null && captiveWebServer.isAlive()) {
            Log.m0d(LOG_TAG, "CaptiveWebServer thread is still alive !");
            captiveWebServer.stopThread();
            captiveWebServer = null;
        }
        if (secureCaptiveWebServer != null && secureCaptiveWebServer.isAlive()) {
            Log.m0d(LOG_TAG, "CaptiveWebServer thread is still alive !");
            secureCaptiveWebServer.stopThread();
            secureCaptiveWebServer = null;
        }
        if (accessWebServer != null && accessWebServer.isAlive()) {
            Log.m0d(LOG_TAG, "AccessWebServer thread is still alive !");
            accessWebServer.stopThread();
            accessWebServer = null;
        }
    }

    public boolean sendCommandStartWebServers() {
        Log.m3v(LOG_TAG, "sendCommandStartWebServers");
        sendMessage((int) CMD_START_WEBSERVERS);
        return true;
    }

    public boolean sendCommandStopWebServers() {
        Log.m3v(LOG_TAG, "sendCommandStopWebServers");
        sendMessage((int) CMD_STOP_WEBSERVERS);
        return true;
    }
}
