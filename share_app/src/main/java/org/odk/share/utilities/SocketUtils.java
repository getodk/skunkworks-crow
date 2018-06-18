package org.odk.share.utilities;

import java.io.IOException;
import java.net.ServerSocket;

import timber.log.Timber;

public final class SocketUtils {

    private SocketUtils() {

    }

    /**
     * @return port number that can be used for socket communication
     */
    public static int getPort() {
        int port = -1;
        try {
            ServerSocket serverSocket = new ServerSocket(0);
            port = serverSocket.getLocalPort();
            serverSocket.close();
        } catch (IOException e) {
            Timber.e(e);
        }
        return port;
    }

}
