package ru.geekbrains.network.samples;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class SimpleClient {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try(Socket socket = new Socket("127.0.0.1",8189)){
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            while (true){
                outputStream.writeUTF(scanner.nextLine());
                System.out.println(inputStream.readUTF());
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
