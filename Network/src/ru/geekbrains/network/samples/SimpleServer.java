package ru.geekbrains.network.samples;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleServer {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8189);
             Socket socket = serverSocket.accept()
        ){
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            while (true){
                outputStream.writeUTF("echo: " + inputStream.readUTF());
            }
        }catch (IOException e){
                 e.printStackTrace();
        }
    }
}
