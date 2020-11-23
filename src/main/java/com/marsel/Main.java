package com.marsel;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater( () -> {
            ConnectForm connectForm = new ConnectForm();
            connectForm.setLocation(0, 200);
            connectForm.setVisible(true);
            connectForm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        });
    }
}
