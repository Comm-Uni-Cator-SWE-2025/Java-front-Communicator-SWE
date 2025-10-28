package com.swe.ux.views;

import com.swe.ux.App;
import com.swe.ux.theme.ThemeManager;
import com.swe.ux.ui.CustomButton;

import javax.swing.*;
import java.awt.*;

public class MeetingInterface extends JPanel {
    private final App app;
    private final ThemeManager themeManager;
    private JLabel titleLabel;
    private JTextArea chatArea;
    private JTextField messageField;
    private CustomButton sendButton;
    private CustomButton endMeetingButton;

    public MeetingInterface(App app) {
        this.app = app;
        this.themeManager = ThemeManager.getInstance();
        initComponents();
        applyTheme();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setOpaque(false);

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Title
        titleLabel = new JLabel("Meeting");
        titleLabel.setFont(themeManager.getTitleFont());
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // End meeting button
        endMeetingButton = new CustomButton("End Meeting", true);
        endMeetingButton.addActionListener(e -> app.showPage(App.MAIN_PAGE));
        headerPanel.add(endMeetingButton, BorderLayout.EAST);

        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        // Chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(themeManager.getPanelBorder()));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        // Message input panel
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setOpaque(false);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        messageField = new JTextField();
        messageField.setFont(themeManager.getNormalFont());
        messageField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(themeManager.getPanelBorder()),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        sendButton = new CustomButton("Send", false);
        sendButton.setPreferredSize(new Dimension(100, 30));
        sendButton.addActionListener(e -> sendMessage());

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(inputPanel, BorderLayout.SOUTH);

        // Add components to main panel
        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        // Add enter key listener for sending messages
        messageField.addActionListener(e -> sendMessage());
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            // TODO: Implement actual message sending logic
            chatArea.append("You: " + message + "\n");
            messageField.setText("");
        }
    }

    public void applyTheme() {
        if (titleLabel != null) {
            titleLabel.setForeground(themeManager.getTextColor());
        }
        if (chatArea != null) {
            chatArea.setBackground(themeManager.getPanelBackground());
            chatArea.setForeground(themeManager.getTextColor());
            chatArea.setCaretColor(themeManager.getTextColor());
            chatArea.setFont(themeManager.getNormalFont());
        }
        if (messageField != null) {
            messageField.setBackground(themeManager.getPanelBackground());
            messageField.setForeground(themeManager.getTextColor());
            messageField.setCaretColor(themeManager.getTextColor());
        }
        setBackground(themeManager.getBackground());
        repaint();
    }
}
