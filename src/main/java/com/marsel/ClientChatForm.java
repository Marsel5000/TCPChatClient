package com.marsel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class ClientChatForm extends JFrame implements Runnable {

    ///JSON structure
    private final String JSON_NAME = "<name>";
    private final String JSON_START = "<start>";
    private final String JSON_END = "<end>";

    private final String HOST;
    private final int PORT;
    private boolean registered = true;
    private boolean connected = false;
    private DataOutputStream outputStream;
    private JButton sendButton;
    private JTextField textField;
    private JTextArea textFields;

    public void writeToServer(String message) {
        try {
            if( message.isEmpty() ) return;
            if (!registered) {
                outputStream.writeUTF(JSON_START + message + JSON_END);
            } else {
                outputStream.writeUTF(JSON_NAME + message + JSON_END);
            }
            outputStream.flush();
        } catch (NullPointerException | IOException nullPointerException){
            readMessageFromServer(nullPointerException.getMessage() + "\n");
        }
    }

    private void init() {
        registered = true;
        textField = new JTextField("", 10);
        sendButton = new JButton("Login");
        textFields = new JTextArea(10, 20);
        textFields.setEditable(false);
        textFields.setWrapStyleWord(true);
        textFields.setLineWrap(true);

        JScrollPane jsp = new JScrollPane(textFields, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        JPanel panelChat = new JPanel();

        JPanel panelSend = new JPanel();
        JPanel panelMain = new JPanel();
        GridLayout gridLayoutChat = new GridLayout(1,2, 5, 10);

        panelChat.setLayout( gridLayoutChat );
        panelChat.add( jsp );
        panelSend.setLayout( new BorderLayout( ) );
        panelSend.add( textField, BorderLayout.CENTER );
        panelSend.add( sendButton, BorderLayout.EAST );

        panelMain.setLayout( new BorderLayout( ) );
        panelMain.add( panelChat, BorderLayout.CENTER );
        panelMain.add( panelSend, BorderLayout.SOUTH );
        this.setContentPane( panelMain );
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.pack();
        textField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode()==KeyEvent.VK_ENTER){
                    writeToServer(textField.getText());
                    textField.setText( "" );
                    textField.grabFocus();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
        sendButton.addActionListener(e -> {
            writeToServer(textField.getText());
            textField.setText("");
            textField.grabFocus();
        });
        textField.grabFocus();
    }

    private void readMessageFromServer(String entry) {
        if (entry.startsWith( JSON_NAME ) && entry.endsWith(JSON_END)) {
            entry = entry.replaceFirst(JSON_NAME, "" );
            StringBuilder b = new StringBuilder(entry);
            b.replace(entry.lastIndexOf(JSON_END), entry.lastIndexOf(JSON_END) + 5, "" );
            entry = b.toString();
            if (entry.contains( "ACCESS GRANTED" ) ) {
                registered = false;
                sendButton.setText( "Send" );
            }
            if( entry.contains( "ACCESS DENIED" ) ) {
                sendButton.setText( "This login is busy" );
            }
        }

        String finalMessage = entry;
        SwingUtilities.invokeLater( ( ) -> textFields.append( finalMessage ) );
    }

    ClientChatForm(String host, int port) {
        this.HOST = host;
        this.PORT = port;
        setTitle( "host: " + HOST + " port: " + PORT);
        init();
    }

    public void exe() {
        ExecutorService eService = Executors.newCachedThreadPool();
        eService.execute(this);
    }

    private void connect() {
        try (Socket client = new Socket(HOST,PORT);
             DataInputStream inputStream = new DataInputStream(client.getInputStream())
        ){
            outputStream = new DataOutputStream(client.getOutputStream());
            outputStream.flush();
            connected = true;
            while (!client.isClosed()){
                String entry = inputStream.readUTF();
                readMessageFromServer(entry);
            }
            outputStream.close();
        } catch (UnknownHostException hostException) {
            readMessageFromServer( "Host: " + hostException.getMessage() + " is unknown \n");
        } catch (IOException e) {
            readMessageFromServer(e.getMessage() + "\n");
            reconnect();
        }
    }

    private void reconnect() {
        connected = false;
        registered = true;
        sendButton.setText( "Entry your login" );
        while (!connected) {
            connect();
        }
    }

    @Override
    public void run() {
        connect();
    }
}
