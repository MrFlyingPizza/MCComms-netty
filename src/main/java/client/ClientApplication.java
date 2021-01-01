package client;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ClientApplication {
    private JPanel mainPanel;
    private JPanel connectionPanel;
    private JPanel soundPanel;
    private JTextField ipTextField;
    private JTextField portTextField;
    private JTextField connectCodeTextField;
    private JButton connectButton;
    private JLabel ipLabel;
    private JLabel connectCodeLabel;
    private JTextArea statusTextArea;
    private JScrollPane statusScrollPane;
    private JSlider volumeDetectionSlider;
    private JLabel versionCodeLabel;
    private JLabel volumeDetectionLabel;
    public JProgressBar volumeBar;
    private JButton testAudioButton;

    private static ClientApplication app;

    public static ClientApplication getApp() {
        return app;
    }

    public void updateStatus(String status) {
        statusTextArea.setText(status);
    }

    public static int maxLevel = 128;
    public static int code = 0;
    public static boolean feedbackEnabled = false;
    public static boolean mute = false;

    public void disableFields() {
        connectButton.setText("Disconnect");
        ipTextField.setEnabled(false);
        portTextField.setEnabled(false);
        connectCodeTextField.setEnabled(false);
        //testAudioButton.setEnabled(true);
    }

    public void enableFields() {
        connectButton.setText("Connect");
        ipTextField.setEnabled(true);
        portTextField.setEnabled(true);
        connectCodeTextField.setEnabled(true);
        //testAudioButton.setEnabled(false);
    }

    public ClientApplication() {

        soundPanel.setVisible(false);

        connectButton.addActionListener(e -> {

            if (Client.getInstance().isConnected()) {
                Client.getInstance().stop();
                connectButton.setText("Connect");
            } else {

                try {
                    Client.getInstance().start(
                            ipTextField.getText(),
                            Integer.parseInt(portTextField.getText()),
                            code = Integer.parseInt(connectCodeTextField.getText()));

                } catch (InterruptedException | LineUnavailableException interruptedException) {
                    interruptedException.printStackTrace();
                }
                connectButton.setText("Disconnect");
            }

        });

        testAudioButton.addActionListener(e -> {
//            if (feedbackEnabled) {
//                testAudioButton.setText("Enable Mic Feedback");
//                feedbackEnabled = false;
//            } else {
//                testAudioButton.setText("Disable Mic Feedback");
//                feedbackEnabled = true;
//            }
        });

        volumeDetectionSlider.addChangeListener(e -> maxLevel = volumeDetectionSlider.getValue());

    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("MC Comms Client");
        app = new ClientApplication();
        frame.setResizable(false);
        frame.setContentPane(app.mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
