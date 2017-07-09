package ru.geekbrains.chat.client;

import ru.geekbrains.chat.library.Messages;
import ru.geekbrains.network.SocketThread;
import ru.geekbrains.network.SocketThreadListener;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Vector;

public class ChatClientGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler, KeyListener, SocketThreadListener {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatClientGUI();
            }
        });
    }

    private static final int WIDTH = 800;
    private static final int HEIGHT = 300;
    private static final String TITLE = "Chat";

    private final JPanel upperPanel = new JPanel(new GridLayout(2, 3));
    //private final JTextField fieldIPAddress = new JTextField("89.222.249.131");
    private final JTextField fieldIPAddress = new JTextField("127.0.0.1");
    private final JTextField fieldPort = new JTextField("8189");
    private final JCheckBox checkAlwaysOnTop = new JCheckBox("Always on top");
    private final JTextField fieldLogin = new JTextField("roma");
    private final JPasswordField fieldPass = new JPasswordField("12345");
    private final JButton buttonLogin = new JButton("Login");

    private final JTextArea log = new JTextArea();

    private JScrollPane scrollUsers;
    private JList<String> userList = new JList<>();
    private Vector<String> listVector = new Vector<>();

    private final JPanel bottomPanel = new JPanel(new BorderLayout());
    private final JButton buttonDisconnect = new JButton("Disconnect");
    private final JTextField fieldInput = new JTextField();
    private final JButton buttonSend = new JButton("Send");

    private boolean isAuthorized;

    private ChatClientGUI() {
        URL url = null;
            url = getClass().getResource("Без названия (1).png");

         ImageIcon background = new ImageIcon(url);
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setSize(WIDTH, HEIGHT);
        setTitle(TITLE);
        setBackground(Color.green);
        upperPanel.add(fieldIPAddress);
        upperPanel.add(fieldPort);
        upperPanel.add(checkAlwaysOnTop);
        upperPanel.add(fieldLogin);
        upperPanel.add(fieldPass);
        upperPanel.add(buttonLogin);
        add(upperPanel, BorderLayout.NORTH);

        log.setEditable(false);
        log.setLineWrap(true);
        JScrollPane scrollLog = new JScrollPane(log);
        add(scrollLog, BorderLayout.CENTER);

        scrollUsers = new JScrollPane(userList);
        scrollUsers.setPreferredSize(new Dimension(150, 0));
        add(scrollUsers, BorderLayout.EAST);

        bottomPanel.add(buttonDisconnect, BorderLayout.WEST);
        bottomPanel.add(fieldInput, BorderLayout.CENTER);
        bottomPanel.add(buttonSend, BorderLayout.EAST);
        bottomPanel.setVisible(false);
        add(bottomPanel, BorderLayout.SOUTH);

        fieldPort.addActionListener(this);
        fieldLogin.addActionListener(this);
        fieldIPAddress.addActionListener(this);
        fieldPass.addActionListener(this);
        checkAlwaysOnTop.addActionListener(this);
        fieldInput.addKeyListener(this);
        buttonSend.addActionListener(this);
        buttonLogin.addActionListener(this);
        buttonDisconnect.addActionListener(this);
        setIconImage(background.getImage());
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == fieldIPAddress ||
                source == fieldLogin ||
                source == fieldPass ||
                source == fieldPort ||
                source == buttonLogin) {
            connect();
        } else if (source == checkAlwaysOnTop) {
            setAlwaysOnTop(checkAlwaysOnTop.isSelected());
        } else if (source == buttonDisconnect) {
            disconnect();
        } else if (source == buttonSend) {
            sendMessage();
        } else {
            throw new RuntimeException("Unknown source = " + source);
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        String message;
        if (stackTraceElements.length == 0) {
            message = "StackTraceElements has no elements";
        } else {
            message = e.getClass().getCanonicalName() + ": " + e.getMessage() + "\n" + stackTraceElements[0];
        }
        JOptionPane.showMessageDialog(null, message, "Exception: ", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    private SocketThread socketThread;

    private void connect() {

        try {
            Socket socket = new Socket(fieldIPAddress.getText(), Integer.parseInt(fieldPort.getText()));
            socketThread = new SocketThread(this, "SocketThread", socket);
        } catch (IOException e) {
            e.printStackTrace();
            log.append("Exception: " + e.getMessage() + "\n");
            log.setCaretPosition(log.getDocument().getLength());
        }
    }

    private void disconnect() {
        socketThread.close();
    }

    private void sendMessage() {
        String message = fieldInput.getText();
        if (message.equals("")) return;
        fieldInput.setText(null);
        socketThread.sendMessage(message);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER && fieldInput.hasFocus()) {
            sendMessage();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void onStartSocketThread(SocketThread thread) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.setText("Connected" + "\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }

    @Override
    public void onStopSocketThread(SocketThread thread) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (isAuthorized) {
                    log.setText("Disconnected");
                    log.setCaretPosition(log.getDocument().getLength());
                }
                upperPanel.setVisible(true);
                bottomPanel.setVisible(false);
                listVector.clear();
                updateUsersList();
            }
        });
    }

    @Override
    public void onReadySocketThread(SocketThread thread, Socket socket) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                upperPanel.setVisible(false);
                bottomPanel.setVisible(true);
                String login = fieldLogin.getText();
                String password = new String(fieldPass.getPassword());
                socketThread.sendMessage(Messages.getAuthRequest(login, password));
            }
        });

    }

    @Override
    public void onReceiveString(SocketThread socketThread, Socket socket, String line) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                handleMessages(line);
            }
        });

    }

    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss ");

    private void handleMessages(String line) {
        String[] parsedLine = line.split(Messages.DELIMITER);
        String result;

        if (line.startsWith(Messages.AUTH_ACCEPT)) {
            isAuthorized = true;
            if (parsedLine.length > 2) {
                for (int i = 2; i < parsedLine.length; i++) {
                    listVector.add(parsedLine[i]);
                }
                updateUsersList();
            }
            return;
        } else if (line.startsWith(Messages.AUTH_ERROR)) {
            isAuthorized = false;
            log.setText("Wrong login or password. Please, try again.\n");
            log.setCaretPosition(log.getDocument().getLength());
            return;
        }

        String timeInMillis = parsedLine[1];
        String name = parsedLine[2];

        if (line.startsWith(Messages.BROADCAST)) {

            result = transformMillisInTime(timeInMillis) + " "
                    + name + ": " + parsedLine[3];
            log.append(result + "\n");
            log.setCaretPosition(log.getDocument().getLength());

        } else if (line.startsWith(Messages.CLIENT_CONNECTED)) {

            log.append(transformMillisInTime(timeInMillis) + " " + name + parsedLine[3] + "\n");
            log.setCaretPosition(log.getDocument().getLength());

            listVector.add(name);
            updateUsersList();

        } else if (line.startsWith(Messages.CLIENT_DISCONNECTED)) {

            log.append(transformMillisInTime(timeInMillis) + " " + name + parsedLine[3] + "\n");
            log.setCaretPosition(log.getDocument().getLength());

            listVector.removeElement(name);
            updateUsersList();
        }
    }

    private void updateUsersList() {
        remove(scrollUsers);
        userList = new JList<>(listVector);
        scrollUsers = new JScrollPane(userList);
        scrollUsers.setPreferredSize(new Dimension(150, 0));
        add(scrollUsers, BorderLayout.EAST);
        revalidate();
        repaint();
    }

    private String transformMillisInTime(String time) {
        return dateFormat.format(Long.parseLong(time));
    }

    @Override
    public void onExceptionSocketThread(SocketThread thread, Socket socket, Exception e) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (e != null && e.getMessage() != null && !(e.getMessage().equals("Socket closed"))) {
                    e.printStackTrace();
                    log.append("Exception: " + e.getMessage() + "\n");
                    log.setCaretPosition(log.getDocument().getLength());
                }
            }
        });
    }
}
