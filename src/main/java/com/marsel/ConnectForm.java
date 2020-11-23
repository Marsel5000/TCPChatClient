package com.marsel;
import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.NumberFormat;

public class ConnectForm extends JFrame {
    private final JLabel HOST_LABEL = new JLabel("host:");
    private final JLabel PORT_LABEL = new JLabel("port:");
    private final String HOST = "127.0.0.1";
    private static JFormattedTextField portField;
    private static JTextField hostField;

    ConnectForm() {
        init();
    }

    private void init () {
        JPanel panelConnection = new JPanel();
        JPanel panelMain = new JPanel();
        GridLayout gridLayoutConnection = new GridLayout(2, 2, 5, 5);

        NumberFormat format = NumberFormat.getIntegerInstance();
        format.setGroupingUsed(false);
        NumberFormatter numberFormatter = new NumberFormatter(format);
        portField = new JFormattedTextField(numberFormatter);
        hostField = new JTextField(HOST, 20);
        panelConnection.setLayout(gridLayoutConnection);
        panelConnection.add(HOST_LABEL);
        panelConnection.add(hostField);
        panelConnection.add(PORT_LABEL);
        panelConnection.add(portField);

        panelMain.setLayout(new BorderLayout());
        panelMain.add(panelConnection, BorderLayout.NORTH);

        JButton connectButton = new JButton("Connect");
        panelMain.add(connectButton, BorderLayout.CENTER);

        portField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode()==KeyEvent.VK_ENTER){
                    entryConnectButton();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        this.setContentPane(panelMain);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.pack();
        connectButton.addActionListener(e -> entryConnectButton());
    }
    public void entryConnectButton () {
        SwingUtilities.invokeLater(() -> {
            if (portField.getText().isEmpty() || hostField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Uncorrected ip or port", "InfoBox", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            this.setVisible(false);
            ClientChatForm client = new ClientChatForm(hostField.getText(), Integer.parseInt(portField.getText()));
            client.exe();
            client.setLocation(0, 200);
            client.setVisible(true);
            client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        });
    }
}

