package com.marsel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;

public class ClientChat extends JFrame implements Runnable {
    private Socket client;
    private Boolean isRegister = true;
    private Boolean isConnected = false;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private JButton sendButton;
    private JTextField textField;
    private JTextArea textFields;
    private Future<Boolean> future;

    public void writeToServer(String message) {
        try {
            if( message.isEmpty() ) return;
            if (!isRegister) {
                outputStream.writeUTF("<start>" + message + "<end>");
            } else {
                outputStream.writeUTF("<name>" + message + "<end>");
            }
            outputStream.flush();
        } catch (NullPointerException | IOException nullPointerException){
            readMessageFromServer(nullPointerException.getMessage() + "\n");
        }
    }

    private void init() {
        isRegister = true;
        textField = new JTextField("", 10);
        sendButton = new JButton("Login");
        textFields = new JTextArea(10, 20);
        textFields.setEditable(false);
        textFields.setWrapStyleWord(true);
        textFields.setLineWrap(true);
        JScrollPane jsp = new JScrollPane(textFields, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(jsp, BorderLayout.CENTER );
        panel.add(textField, BorderLayout.SOUTH );
        panel.add(sendButton, BorderLayout.EAST );
        this.setContentPane(panel);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.pack();
        sendButton.addActionListener(e -> writeToServer(textField.getText()));
    }

    private void sendButtonActionPerformed(ActionEvent e) {
        writeToServer(textField.getText());
    }

    private void readMessageFromServer(String message) {
        if (message.contains("<name>") && message.contains("<end>")) {
            message = message.replace("<name>", "");
            message = message.replace("<end>", "");
            if (message.contains("ACCESS GRANTED")) {
                isRegister = false;
                sendButton.setText( "Send" );
            }
            if( message.contains("ACCESS DENIED") ){
                sendButton.setText( "This login is busy" );
            }
        }
        textField.setText("");
        String finalMessage = message;
        SwingUtilities.invokeLater(() -> textFields.append(/*"\n"+*/ finalMessage));
    }

    ClientChat() {
        init();
    }

    public void exe() {
        ExecutorService eService = Executors.newCachedThreadPool();
        eService.execute(this);
    }

    private void connect() {
        try {
            int port = 80;
            String host = "127.0.0.1";
            client = new Socket(host, port);
            inputStream = new DataInputStream(client.getInputStream());
            outputStream = new DataOutputStream(client.getOutputStream());
            outputStream.flush();
            isConnected = true;
            ExecutorService service = Executors.newFixedThreadPool(1);
            future = service.submit(() -> {
                String text = null;
                try {
                    while (true) {
                        text = inputStream.readUTF();
                        readMessageFromServer(text);
                    }
                } catch (IOException e) {
                    readMessageFromServer(e.getMessage() + "\n");
                    reconnect();
                }
                return true;
            });
        } catch (UnknownHostException hostException) {
            readMessageFromServer( hostException.getMessage() + "\n");
        } catch (IOException e) {
            try {
                if (e instanceof SocketTimeoutException) {
                    throw new SocketTimeoutException();
                } else {
                    readMessageFromServer(e.getMessage() + "\n");
                    reconnect();
                }
            } catch (SocketTimeoutException ste) {
                readMessageFromServer(ste.getMessage() + "\n");
                reconnect();
            }
        } finally {
            try {
                if (future != null && future.get()) {
                    if (!client.isClosed()) {
                        inputStream.close();
                        outputStream.close();
                        client.close();
                    }
                }
            } catch (IOException | InterruptedException | ExecutionException exception) {
                readMessageFromServer( exception.getMessage() + "\n");
            }
        }
    }

    private void reconnect() {
        isConnected = false;
        isRegister = true;
        sendButton.setText( "Entry your login" );
        while (!isConnected) {
            connect();
        }
    }

    @Override
    public void run() {
        connect();
    }
}
