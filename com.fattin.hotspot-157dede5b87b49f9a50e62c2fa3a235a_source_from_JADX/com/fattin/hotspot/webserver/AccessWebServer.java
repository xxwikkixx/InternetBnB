package com.fattin.hotspot.webserver;

import com.fattin.hotspot.app.Constants;
import java.net.Socket;

public class AccessWebServer extends WebServer {
    public AccessWebServer() {
        this.LOG_TAG = "AccessWebServer";
    }

    protected Runnable getNewClientConnectionThread(Socket socket) {
        return new ClientConnectionThread(socket, new AccessPageHandler());
    }

    protected int getServerPort() {
        return Constants.SERVER_PORT_ACCESS_WEBSERVER;
    }
}
