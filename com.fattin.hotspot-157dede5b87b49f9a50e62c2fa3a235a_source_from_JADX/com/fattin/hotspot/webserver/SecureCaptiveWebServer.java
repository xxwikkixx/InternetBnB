package com.fattin.hotspot.webserver;

import com.fattin.hotspot.app.Constants;
import java.net.Socket;

public class SecureCaptiveWebServer extends SecureWebServer {
    public SecureCaptiveWebServer() {
        this.LOG_TAG = "SecureCaptiveWebServer";
    }

    protected Runnable getNewClientConnectionThread(Socket socket) {
        return new ClientConnectionThread(socket, new CaptivePageHandler());
    }

    protected int getServerPort() {
        return Constants.SECURE_SERVER_PORT_CAPTIVE_WEBSERVER;
    }
}
