package com.fattin.hotspot.webserver;

import com.fattin.hotspot.app.GlobalStates;
import com.fattin.hotspot.helpers.Log;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public abstract class WebServer extends Thread {
    protected String LOG_TAG;
    private ThreadGroup connectionsThreadGroup;
    private volatile boolean isGoingToShutdown;
    private ServerSocket serverSocket;

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

    public WebServer() {
        this.LOG_TAG = "WebServer";
        this.isGoingToShutdown = false;
    }

    public void run() {
        super.run();
        this.serverSocket = new ServerSocket(getServerPort());
        this.serverSocket.setReuseAddress(true);
        Log.m0d(this.LOG_TAG, "run -> before while loop");
        while (!this.isGoingToShutdown && this.serverSocket != null && !this.serverSocket.isClosed() && GlobalStates.isServiceStartingOrStarted()) {
            try {
                Log.m0d(this.LOG_TAG, "run -> waiting for a connection");
                Socket socket = this.serverSocket.accept();
                Log.m0d(this.LOG_TAG, "run -> a connection received");
                if (!this.isGoingToShutdown) {
                    new Thread(this.connectionsThreadGroup, getNewClientConnectionThread(socket)).start();
                    Log.m0d(this.LOG_TAG, "run -> ClientConnectionThread created");
                }
            } catch (IOException e) {
                try {
                    Log.m1e(this.LOG_TAG, e.getMessage(), e);
                } catch (IOException e2) {
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
        } catch (IOException e22) {
            Log.m1e(this.LOG_TAG, e22.getMessage(), e22);
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
