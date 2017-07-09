package ru.geekbrains.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SocketThread extends Thread {

    private final SocketThreadListener eventListener;
    private final Socket socket;
    private DataOutputStream dataOutputStream;


    public SocketThread(SocketThreadListener eventListener, String name, Socket socket){
        super(name);
        this.eventListener = eventListener;
        this.socket = socket;
        start();
    }


    @Override
    public void run() {
        eventListener.onStartSocketThread(this);
        try{
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream datainputStream = new DataInputStream(socket.getInputStream());
            eventListener.onReadySocketThread(this,socket);
            while (!isInterrupted()){
                String message = datainputStream.readUTF();
                eventListener.onReceiveString(this,socket,message);
            }
        }catch (IOException e){
            eventListener.onExceptionSocketThread(this,socket,e);
        }finally {
            try {
                socket.close();
            }catch (IOException e){
                eventListener.onExceptionSocketThread(this,socket,e);
            }
            eventListener.onStopSocketThread(this);
        }
    }

    public synchronized void sendMessage(String message){
        try {
            dataOutputStream.writeUTF(message);
            dataOutputStream.flush();
        } catch (IOException e) {
            eventListener.onExceptionSocketThread(this,socket,e);
            close();
        }
    }

    public synchronized void close(){
        interrupt();
        try{
            socket.close();
        }catch (IOException e){
            eventListener.onExceptionSocketThread(this,socket,e);
        }
    }
}
