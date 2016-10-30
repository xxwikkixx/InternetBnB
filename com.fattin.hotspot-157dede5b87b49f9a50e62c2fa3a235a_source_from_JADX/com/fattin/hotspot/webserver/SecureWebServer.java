package com.fattin.hotspot.webserver;

import com.fattin.hotspot.app.GlobalStates;
import com.fattin.hotspot.helpers.CoreTask;
import com.fattin.hotspot.helpers.Log;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

public abstract class SecureWebServer extends Thread {
    private final String KEYSTORE;
    private final String KEYTYPE;
    protected String LOG_TAG;
    private final char[] PASSWORD;
    private final String PROTOCOL;
    private ThreadGroup connectionsThreadGroup;
    private volatile boolean isGoingToShutdown;
    private KeyManager[] keyManager;
    private KeyManagerFactory keyManagerFactory;
    private KeyStore keyStore;
    protected ServerSocket serverSocket;
    private SSLContext sslContext;
    protected SSLServerSocketFactory sslServerSocketFactory;

    private static class ShutdownServerTask implements Runnable {
        String LOG_TAG;
        ServerSocket serverSocket;

        ShutdownServerTask(ServerSocket serverSocket) {
            this.LOG_TAG = "ShutdownServerTask";
            this.serverSocket = serverSocket;
        }

        public void run() {
            Log.m0d(this.LOG_TAG, "stopThread -> shutting down server...............");
            try {
                new Socket(this.serverSocket.getInetAddress(), this.serverSocket.getLocalPort()).close();
            } catch (IOException e) {
                Log.m1e(this.LOG_TAG, e.getMessage(), e);
            }
            Log.m0d(this.LOG_TAG, "stopThread -> server shutdown.");
        }
    }

    protected abstract Runnable getNewClientConnectionThread(Socket socket);

    protected abstract int getServerPort();

    public SecureWebServer() {
        this.LOG_TAG = "SecureWebServer";
        this.isGoingToShutdown = false;
        this.PROTOCOL = "SSLv3";
        this.KEYTYPE = "PKCS12";
        this.KEYSTORE = CoreTask.DATA_FILE_PATH + "/bin/ssl.key";
        this.PASSWORD = "P@ssw0rd!".toCharArray();
    }

    public void run() {
        super.run();
        this.sslContext = SSLContext.getInstance("SSLv3");
        this.keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        this.keyStore = KeyStore.getInstance("PKCS12");
        Log.m0d(this.LOG_TAG, "sslContext.getProtocol()=" + this.sslContext.getProtocol());
        Log.m0d(this.LOG_TAG, "sslContext.getProvider().getName()=" + this.sslContext.getProvider().getName());
        this.keyStore.load(new FileInputStream(this.KEYSTORE), this.PASSWORD);
        this.keyManagerFactory.init(this.keyStore, this.PASSWORD);
        this.keyManager = this.keyManagerFactory.getKeyManagers();
        this.sslContext.init(this.keyManager, null, new SecureRandom());
        this.sslServerSocketFactory = this.sslContext.getServerSocketFactory();
        this.serverSocket = this.sslServerSocketFactory.createServerSocket(getServerPort());
        this.serverSocket.setReuseAddress(true);
        Log.m0d(this.LOG_TAG, "run -> before while loop");
        while (!this.isGoingToShutdown && this.serverSocket != null && !this.serverSocket.isClosed() && GlobalStates.isServiceStartingOrStarted()) {
            try {
                Log.m0d(this.LOG_TAG, "run -> waiting for a connection");
                Socket socket = this.serverSocket.accept();
                Log.m0d(this.LOG_TAG, "run -> a connection received");
                if (!this.isGoingToShutdown) {
                    if (socket != null) {
                        new Thread(this.connectionsThreadGroup, getNewClientConnectionThread(socket)).start();
                        Log.m0d(this.LOG_TAG, "run -> ClientConnectionThread created");
                    }
                }
            } catch (IOException e) {
                try {
                    Log.m1e(this.LOG_TAG, e.getMessage(), e);
                } catch (Exception e2) {
                    Log.m1e(this.LOG_TAG, e2.getMessage(), e2);
                    return;
                }
            }
        }
        try {
            break;
            if (!(this.serverSocket == null || this.serverSocket.isClosed())) {
                this.serverSocket.close();
            }
        } catch (IOException e3) {
            Log.m1e(this.LOG_TAG, e3.getMessage(), e3);
        }
        Log.m0d(this.LOG_TAG, "run -> after while loop ( end of thread )");
    }

    public synchronized void startThread() {
        Log.m0d(this.LOG_TAG, "startThread -> begin");
        if (this.connectionsThreadGroup == null) {
            this.isGoingToShutdown = false;
            this.connectionsThreadGroup = new ThreadGroup("connectionsThreadGroup");
            super.start();
        }
        Log.m0d(this.LOG_TAG, "startThread -> end");
    }

    public synchronized void stopThread() {
        Log.m0d(this.LOG_TAG, "stopThread -> begin");
        this.isGoingToShutdown = true;
        new Thread(new ShutdownServerTask(this.serverSocket)).start();
        try {
            this.connectionsThreadGroup.interrupt();
            this.connectionsThreadGroup.destroy();
        } catch (IllegalThreadStateException e) {
            Log.m1e(this.LOG_TAG, e.getMessage(), e);
        } catch (Exception e2) {
            Log.m1e(this.LOG_TAG, e2.getMessage(), e2);
        }
        Thread.currentThread().interrupt();
        this.connectionsThreadGroup = null;
        Log.m0d(this.LOG_TAG, "stopThread -> end");
    }
}
