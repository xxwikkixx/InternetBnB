package com.fattin.hotspot.webserver;

import com.fattin.hotspot.app.Constants;
import java.net.Socket;

public class CaptiveWebServer extends WebServer {
    public CaptiveWebServer() {
        this.LOG_TAG = "CaptiveWebServer";
    }

    protected Runnable getNewClientConnectionThread(Socket socket) {
        return new ClientConnectionThread(socket, new CaptivePageHandler());
    }

    protected int getServerPort() {
        return Constants.SERVER_PORT_CAPTIVE_WEBSERVER;
    }
}
