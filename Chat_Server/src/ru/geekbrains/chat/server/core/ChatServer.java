package ru.geekbrains.chat.server.core;


import ru.geekbrains.chat.library.Messages;
import ru.geekbrains.network.ServerSocketThread;
import ru.geekbrains.network.ServerSocketThreadListener;
import ru.geekbrains.network.SocketThread;
import ru.geekbrains.network.SocketThreadListener;

import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Vector;

public class ChatServer implements ServerSocketThreadListener, SocketThreadListener {

    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss ");
    private final ChatServerListener chatServerListener;
    private final SecurityManager securityManager;
    private ServerSocketThread serverSocketThread;
    private final Vector<SocketThread> clients = new Vector<>();

    public ChatServer(ChatServerListener chatServerListener, SecurityManager securityManager){
        this.chatServerListener = chatServerListener;
        this.securityManager = securityManager;
    }

    public void startListening(int port){
        if(serverSocketThread != null && serverSocketThread.isAlive()) {
            putLog("Server is already launched");
            return;
        }
        serverSocketThread = new ServerSocketThread("ServerSocketThread",port,2000,this);
        putLog("Server has been launched.");
        securityManager.init();
    }

    public void dropAllClients(){
        putLog("drop all clients");
    }

    public void stopListening()
    {
        if(serverSocketThread == null || !serverSocketThread.isAlive()){
            putLog("Server is not alive");
            return;
        }
        serverSocketThread.interrupt();
        securityManager.dispose();
    }



//ServerSocketThread
    @Override
    public void onStartServerSocketThread(ServerSocketThread thread) {
        putLog("started...");
    }

    @Override
    public void onStopServerSocketThread(ServerSocketThread thread) {
        putLog("stopped.");
    }

    @Override
    public void onReadyServerSocketThread(ServerSocketThread thread, ServerSocket serverSocket) {
        putLog("ServerSocket is ready");
    }

    @Override
    public void onAcceptedSocket(ServerSocketThread thread, ServerSocket serverSocket, Socket socket) {
        putLog("Client connected: " + socket);
        String threadName = "Socket thread: " + socket.getInetAddress() + ":" + socket.getPort();
        new ChatSocketThread(this,threadName,socket);
    }

    @Override
    public void onTimeOutAccept(ServerSocketThread thread, ServerSocket serverSocket) {
        //putLog("Timeout");
    }

    @Override
    public void onExceptionServerSocketThread(ServerSocketThread thread, Exception e) {
        putLog("Exception happened " + e.getClass().getName() + ": " + e.getMessage());
    }

    private synchronized void putLog(String message){
        String messageLog = dateFormat.format(System.currentTimeMillis())+": " + message;
        if(chatServerListener != null) chatServerListener.onChatServerLog(this,messageLog);

    }

//SocketThread
    @Override
    public synchronized void onStartSocketThread(SocketThread thread) {
        putLog("SocketThread started...");
    }

    @Override
    public synchronized void onStopSocketThread(SocketThread thread) {
        clients.remove(thread);
        putLog("Socket thread stopped");
        ChatSocketThread client = (ChatSocketThread) thread;
        if(client.isAuthorized()){
            sendToAllAuthorizedClients(Messages.getClientDisconnected(client.getNick()));
        }

    }

    @Override
    public synchronized void onReadySocketThread(SocketThread thread, Socket socket) {
        putLog("Socket is Ready");
        clients.add(thread);
    }

    @Override
    public synchronized void onReceiveString(SocketThread socketThread, Socket socket, String line) {
        ChatSocketThread client = (ChatSocketThread) socketThread;
        if(client.isAuthorized()){
            handleAuthorizedClient(client,line);
        }else{

            handleNonAuthorizedClient(client,line);
        }
    }

    private void handleNonAuthorizedClient(ChatSocketThread client, String message){
        String[]tokens = message.split(Messages.DELIMITER);
        if(tokens.length != 3 || !tokens[0].equals(Messages.AUTH_REQUEST)){
            client.messageFormatError(message);
            return;
        }
        String login = tokens[1];
        String password = tokens[2];
        String nickname = securityManager.getNick(login,password);
        if(nickname == null){
            client.authError();
        }else{
            client.authorizeAccept(nickname,getClientsNames());
            putLog(client.getNick() + " connected");
            sendToAllAuthorizedClients(Messages.getClientConnected(client.getNick()));
        }
    }

    private void handleAuthorizedClient(ChatSocketThread client,String message){
      sendToAllAuthorizedClients(Messages.getBroadcast(client.getNick(),message));
    }

    private void sendToAllAuthorizedClients(String message){
        for(int i = 0; i < clients.size(); i++){
            ChatSocketThread client = (ChatSocketThread)clients.get(i);
            if(client.isAuthorized())client.sendMessage(message);
        }
    }

    private synchronized Vector<String> getClientsNames(){
        Vector<String> clientsNames = new Vector<>();
        String nick = "";
        for(int i = 0; i < clients.size(); i++){
            nick = ((ChatSocketThread)clients.get(i)).getNick();
            if(nick!=null&&!(nick.equals("null"))){
                clientsNames.add(nick);
            }
        }
        return clientsNames;
    }

    @Override
    public synchronized void onExceptionSocketThread(SocketThread thread, Socket socket, Exception e) {
        putLog("Exception happened " + e.getClass().getName() + ": " + e.getMessage());
    }
}
