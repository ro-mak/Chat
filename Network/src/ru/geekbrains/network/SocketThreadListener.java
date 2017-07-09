package ru.geekbrains.network;

import java.net.Socket;

public interface SocketThreadListener {
    void onStartSocketThread(SocketThread thread);
    void onStopSocketThread(SocketThread thread);

    void onReadySocketThread(SocketThread thread, Socket socket);
    void onReceiveString(SocketThread socketThread, Socket socket,String line);

    void onExceptionSocketThread(SocketThread thread,Socket socket,Exception e);
}
