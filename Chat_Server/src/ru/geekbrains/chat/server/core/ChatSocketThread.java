package ru.geekbrains.chat.server.core;

import ru.geekbrains.chat.library.Messages;
import ru.geekbrains.network.SocketThread;
import ru.geekbrains.network.SocketThreadListener;

import java.net.Socket;
import java.util.Vector;

class ChatSocketThread extends SocketThread {

     private boolean isAuthorized;
     private String nick;

    public ChatSocketThread(SocketThreadListener eventListener, String name, Socket socket) {
        super(eventListener, name, socket);
    }

     boolean isAuthorized(){
         return isAuthorized;
     }

     public String getNick() {
         return nick;
     }

     void authError(){
         sendMessage(Messages.getAuthError());
         close();
     }

     void authorizeAccept(String nick, Vector<String> clients){
         this.isAuthorized = true;
         this.nick = nick;
         StringBuilder message = new StringBuilder();
         message.append(Messages.getAuthAccept(nick));
         for(int i = 0; i < clients.size(); i++){
             message.append(Messages.DELIMITER);
             message.append(clients.get(i));
         }
         sendMessage(message.toString());
     }

    public void messageFormatError(String message){
        sendMessage(Messages.getMsgFormatError(message));
        close();
     }
 }
