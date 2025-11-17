package com.swe.ux.view;

import com.swe.ux.viewmodel.ChatViewModel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The Chat UI View. It is a JPanel that can be embedded in the main application.
 */
public class ChatView extends JPanel {

    private final ChatViewModel viewModel;

    // UI Components
    private final JPanel messageContainer;
    private final JScrollPane scrollPane;
    private final JTextField messageInputField;
    private final JButton sendButton;
    private final JPanel replyQuotePanel;
    private final JLabel replyQuoteLabel;

    public ChatView(ChatViewModel viewModel) {
        this.viewModel = viewModel;

        // --- Setup Main Panel ---
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(0xE5DDD5)); // Optional: Set a background color for the whole chat area

        // --- Message List (Center) ---
        messageContainer = new JPanel();
        messageContainer.setLayout(new BoxLayout(messageContainer, BoxLayout.Y_AXIS));
        messageContainer.setBackground(Color.WHITE);

        JPanel messageContainerWrapper = new JPanel(new BorderLayout());
        messageContainerWrapper.setBackground(Color.WHITE);
        messageContainerWrapper.setBorder(new EmptyBorder(10, 10, 10, 10));
        messageContainerWrapper.add(messageContainer, BorderLayout.NORTH);

        scrollPane = new JScrollPane(messageContainerWrapper);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xCCCCCC)));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        // --- Bottom Panel (Input + Reply) ---
        JPanel bottomVBox = new JPanel();
        bottomVBox.setLayout(new BoxLayout(bottomVBox, BoxLayout.Y_AXIS));

        // Reply Panel (Hidden by default)
        replyQuotePanel = new JPanel(new BorderLayout(5, 5));
        replyQuotePanel.setBackground(new Color(0xEFEFEF));
        replyQuotePanel.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xCCCCCC)),
                new EmptyBorder(5, 10, 5, 10)
        ));
        replyQuoteLabel = new JLabel("Replying to...");
        replyQuoteLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        replyQuoteLabel.setForeground(Color.GRAY); // Force dark text for visibility

        JButton cancelReplyButton = new JButton("X");
        cancelReplyButton.setMargin(new Insets(2, 4, 2, 4));
        // Bind action to ViewModel
        cancelReplyButton.addActionListener(e -> viewModel.cancelReply());

        replyQuotePanel.add(replyQuoteLabel, BorderLayout.CENTER);
        replyQuotePanel.add(cancelReplyButton, BorderLayout.EAST);
        replyQuotePanel.setVisible(false);
        bottomVBox.add(replyQuotePanel);

        // Input Panel
        JPanel inputHBox = new JPanel(new BorderLayout(10, 10));
        inputHBox.setBorder(new EmptyBorder(10, 10, 10, 10));

        messageInputField = new JTextField();
        messageInputField.setFont(new Font("Arial", Font.PLAIN, 14));

        sendButton = new JButton("Send");
        sendButton.setBackground(new Color(0x00, 0x7B, 0xFF));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setFont(new Font("Arial", Font.BOLD, 12));

        // Bind actions to ViewModel
        sendButton.addActionListener(e -> viewModel.sendMessage(messageInputField.getText()));
        messageInputField.addActionListener(e -> viewModel.sendMessage(messageInputField.getText()));

        inputHBox.add(messageInputField, BorderLayout.CENTER);
        inputHBox.add(sendButton, BorderLayout.EAST);
        bottomVBox.add(inputHBox);

        add(bottomVBox, BorderLayout.SOUTH);

        // --- Bind ViewModel Callbacks ---
        viewModel.setOnClearInput(() -> messageInputField.setText(""));

        viewModel.setOnReplyStateChange(quoteText -> {
            replyQuotePanel.setVisible(quoteText != null);
            if (quoteText != null) replyQuoteLabel.setText(quoteText);
            revalidate(); repaint();
        });

        viewModel.setOnMessageAdded(this::addMessageToView);
    }

    // Helper to add message on EDT
    private void addMessageToView(ChatViewModel.MessageVM messageVM) {
        SwingUtilities.invokeLater(() -> {
            messageContainer.add(createMessageComponent(messageVM));
            messageContainer.revalidate();
            messageContainer.repaint();
            // Auto-scroll to bottom
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    /**
     * Builds the custom chat bubble component.
     * Contains fixes for Dark Mode and Alignment.
     */
    private Component createMessageComponent(ChatViewModel.MessageVM messageVM) {
        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));

        // FIX 1: ALIGNMENT. Changed left padding from 12 to 8 to match quote border.
        bubble.setBorder(new EmptyBorder(8, 8, 8, 12));
        bubble.setMaximumSize(new Dimension(300, 9999));

        // --- Quote ---
        if (messageVM.hasQuote()) {
            JPanel quotePanel = new JPanel(new BorderLayout());
            quotePanel.setBackground(new Color(0xDDDDDD));
            // Border that creates the 3px blue line and 5px left padding
            quotePanel.setBorder(new CompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 0, 0, new Color(0x007BFF)),
                    new EmptyBorder(3, 5, 3, 5)
            ));
            JLabel quoteLabel = new JLabel(messageVM.quotedContent);
            quoteLabel.setFont(new Font("Arial", Font.ITALIC, 11));
            quoteLabel.setForeground(new Color(0x555555)); // Force dark color

            quotePanel.add(quoteLabel, BorderLayout.CENTER);
            bubble.add(quotePanel);
            bubble.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        // --- Username ---
        JLabel usernameLabel = new JLabel(messageVM.username);
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 13));

        // --- Content ---
        JLabel contentLabel = new JLabel("<html><p style=\"width:220px;\">" + messageVM.content + "</p></html>");
        contentLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        // FIX 2: DARK MODE. Force text to be black so it's readable on light bubbles.
        contentLabel.setForeground(Color.BLACK);

        // --- Footer (Time + Reply) ---
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        JLabel timeLabel = new JLabel(messageVM.timestamp);
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        timeLabel.setForeground(Color.GRAY);

        JButton replyBtn = new JButton("Reply");
        replyBtn.setFont(new Font("Arial", Font.PLAIN, 10));
        replyBtn.setBorderPainted(false);
        replyBtn.setContentAreaFilled(false);
        replyBtn.setForeground(new Color(0x007BFF));
        replyBtn.setMargin(new Insets(0, 0, 0, 0));
        // Bind reply action
        replyBtn.addActionListener(e -> {
            viewModel.startReply(messageVM);
            messageInputField.requestFocus();
        });

        footer.add(timeLabel, BorderLayout.WEST);
        footer.add(replyBtn, BorderLayout.EAST);

        // Assemble Bubble
        bubble.add(usernameLabel);
        bubble.add(Box.createRigidArea(new Dimension(0, 2)));
        bubble.add(contentLabel);
        bubble.add(Box.createRigidArea(new Dimension(0, 5)));
        bubble.add(footer);

        // --- Wrapper for Alignment ---
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new EmptyBorder(3, 0, 3, 0));

        if (messageVM.isSentByMe) {
            bubble.setBackground(new Color(0xE1, 0xF5, 0xFE)); // Light Blue
            usernameLabel.setForeground(new Color(0x00, 0x5A, 0x9E));
            wrapper.add(bubble, BorderLayout.EAST);
        } else {
            bubble.setBackground(new Color(0xF1, 0xF1, 0xF1)); // Light Gray
            usernameLabel.setForeground(new Color(0x00, 0x7B, 0xFF));
            wrapper.add(bubble, BorderLayout.WEST);
        }

        return wrapper;
    }
}