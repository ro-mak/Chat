package ru.geekbrains.chat.server.gui;

import ru.geekbrains.chat.server.core.ChatServer;
import ru.geekbrains.chat.server.core.ChatServerListener;
import ru.geekbrains.chat.server.core.SQLSecurityManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatServerGUI extends JFrame implements ActionListener, ChatServerListener,Thread.UncaughtExceptionHandler {
    public static final int WIDTH  = 800;
    public static final int HEIGHT = 400;
    public static final int positionX = 400;
    public static final int positionY = 400;

    public static final String TITLE = "Chat Server";
    public static final String START_LISTENING = "Start listening";
    public static final String STOP_LISTENING = "Stop listening";
    public static final String DROP_ALL_CLIENTS = "Drop all clients";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatServerGUI();
            }
        });
    }
    private final ChatServer chatServer = new ChatServer(this,  new SQLSecurityManager());
    private final JButton buttonStartListening = new JButton(START_LISTENING);
    private final JButton buttonStopListening = new JButton(STOP_LISTENING);
    private final JButton buttonDropAllClients = new JButton(DROP_ALL_CLIENTS);
    private final JTextArea log = new JTextArea();

    public ChatServerGUI(){
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(positionX,positionY,WIDTH,HEIGHT);
        setTitle(TITLE);

        buttonStartListening.addActionListener(this);
        buttonStopListening.addActionListener(this);
        buttonDropAllClients.addActionListener(this);

        JPanel upperPanel = new JPanel(new GridLayout());
        upperPanel.add(buttonStartListening);
        upperPanel.add(buttonDropAllClients);
        upperPanel.add(buttonStopListening);
        add(upperPanel, BorderLayout.NORTH);

        log.setEditable(false);
        JScrollPane scrollLog = new JScrollPane(log);
        add(scrollLog,BorderLayout.CENTER);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if(source == buttonStartListening){
            chatServer.startListening(8189);
        }else if(source == buttonDropAllClients){
            chatServer.dropAllClients();
        }else if(source == buttonStopListening){
            chatServer.stopListening();
        }else{
            throw new RuntimeException("Unknown source =" + source);
        }
    }

    @Override
    public void onChatServerLog(ChatServer chatServer, String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(message + "\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        String message;
        if(stackTraceElements.length == 0){
            message = "StackTraceElements has no elements";
        }else{
            message = e.getClass().getCanonicalName() + ": " + e.getMessage() +"\n"+stackTraceElements[0];
        }
        JOptionPane.showMessageDialog(null,message,"Exception: ",JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
}
