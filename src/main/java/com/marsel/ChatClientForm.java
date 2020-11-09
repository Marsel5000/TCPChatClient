package com.marsel;
import javax.swing.*;

public class ChatClientForm{

    public static void main(String[] args) {
        SwingUtilities.invokeLater( () -> {
            ClientChat client = new ClientChat();
            client.exe();
            client.setLocation(0, 200);
            client.setVisible(true);
            client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        });
    }
}
