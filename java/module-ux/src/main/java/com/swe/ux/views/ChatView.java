package com.swe.ux.views;

import com.swe.chat.MessageVM;
import com.swe.ux.viewmodels.ChatViewModel;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
    private final JButton attachButton;
    private final JPanel attachmentPanel;
    private final JLabel attachmentLabel;

    private JPopupMenu suggestionPopup;

    private final Map<String, Component> messageComponentMap = new HashMap<>();

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

        // --- Attachment Panel ---
        attachmentPanel = new JPanel(new BorderLayout(5, 5));
        attachmentPanel.setBackground(new Color(0xEFEFEF));
        attachmentPanel.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xCCCCCC)),
                new EmptyBorder(5, 10, 5, 10)
        ));
        attachmentLabel = new JLabel("Attached: file.txt");
        attachmentLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        attachmentLabel.setForeground(Color.GRAY);

        JButton cancelAttachmentButton = new JButton("X");
        cancelAttachmentButton.setMargin(new Insets(2, 4, 2, 4));
        cancelAttachmentButton.addActionListener(e -> viewModel.cancelAttachment());
        bottomVBox.add(attachmentPanel);

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

        attachButton = new JButton("+");
        attachButton.setFont(new Font("Arial", Font.BOLD, 16));
        attachButton.setMargin(new Insets(1, 5, 1, 5));

        JPanel buttonPanel = new JPanel(new BorderLayout(5, 0));
        buttonPanel.add(attachButton, BorderLayout.WEST);
        buttonPanel.add(sendButton, BorderLayout.CENTER);

        // Bind actions to ViewModel
        sendButton.addActionListener(e -> viewModel.send(messageInputField.getText()));
        messageInputField.addActionListener(e -> viewModel.send(messageInputField.getText()));

        inputHBox.add(messageInputField, BorderLayout.CENTER);
        inputHBox.add(buttonPanel, BorderLayout.EAST);
        bottomVBox.add(inputHBox);

        add(bottomVBox, BorderLayout.SOUTH);

        // --- Bind ViewModel Callbacks ---
        viewModel.setOnClearInput(() -> messageInputField.setText(""));

        viewModel.setOnReplyStateChange(quoteText -> {
            replyQuotePanel.setVisible(quoteText != null);
            if (quoteText != null) replyQuoteLabel.setText(quoteText);
            revalidate(); repaint();
        });

        viewModel.setOnAttachmentSet(attachmentName -> {
            attachmentPanel.setVisible(attachmentName != null);
            if (attachmentName != null) attachmentLabel.setText(attachmentName);
            revalidate();
            repaint();
        });

        viewModel.setOnMessageAdded(this::addMessageToView);
        viewModel.setOnMessageRemoved(this::removeMessageFromView);

        // --- Bind actions to ViewModel ---
        sendButton.addActionListener(e -> viewModel.send(messageInputField.getText()));
        messageInputField.addActionListener(e -> viewModel.send(messageInputField.getText()));

        attachButton.addActionListener(e -> showSelectFileDialog());

        // Bind dialog callbacks
        viewModel.setOnShowErrorDialog(message -> {
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        });

        viewModel.setOnShowSuccessDialog(message -> {
            JOptionPane.showMessageDialog(this, message, "File Saved", JOptionPane.INFORMATION_MESSAGE);
        });
    }
    /**
     * This method contains the Swing logic for showing the "Select File" dialog.
     */
    private void showSelectFileDialog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a file to send");

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            // Sanitize path
            String cleanPath = selectedFile.getAbsolutePath().trim();
            if (cleanPath.startsWith("*")) {
                cleanPath = cleanPath.substring(1).trim();
            }
            File finalFile = new File(cleanPath);

            // Notify the ViewModel of the user's choice
            viewModel.userSelectedFileToAttach(finalFile);
        }
    }

    private void scrollToMessage(String targetMessageId) {
        if (targetMessageId == null) return;

        Component targetComponent = messageComponentMap.get(targetMessageId);

        if (targetComponent != null) {
            // 1. SCROLL logic
            // We get the bounds of the component and tell the visible rect to go there
            Rectangle bounds = targetComponent.getBounds();
            messageContainer.scrollRectToVisible(bounds);

            // 2. HIGHLIGHT logic (Flash effect like WhatsApp)
            // We assume targetComponent is the JPanel wrapper we created
            if (targetComponent instanceof JPanel) {
                JPanel panel = (JPanel) targetComponent;
                Color originalColor = panel.getBackground();
                Color highlightColor = new Color(0xFF, 0xFA, 0xD2); // Light Gold/Yellow

                // Change color immediately
                panel.setBackground(highlightColor);
                panel.repaint();

                // Change it back after 1 second using a Swing Timer
                new Timer(1000, e -> {
                    panel.setBackground(originalColor);
                    panel.repaint();
                    ((Timer)e.getSource()).stop(); // Stop the timer
                }).start();
            }
        } else {
            // Optional: Show a small toast if the message isn't loaded/found
            System.out.println("Message " + targetMessageId + " not found in current view.");
        }
    }

    private void removeMessageFromView(String messageId) {
        SwingUtilities.invokeLater(() -> {
            Component componentToRemove = messageComponentMap.get(messageId);
            if (componentToRemove != null) {
                messageContainer.remove(componentToRemove);
                messageComponentMap.remove(messageId);
                messageContainer.revalidate();
                messageContainer.repaint();
            }
        });
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    // Helper to add message on EDT
    private void addMessageToView(MessageVM messageVM) {
        SwingUtilities.invokeLater(() -> {
            // 1. Check if we already have a component for this message ID
            if (messageComponentMap.containsKey(messageVM.getMessageId())) {
                // --- UPDATE LOGIC ---
                Component oldComponent = messageComponentMap.get(messageVM.getMessageId());

                // Find the index of the old component
                int index = -1;
                for (int i = 0; i < messageContainer.getComponentCount(); i++) {
                    if (messageContainer.getComponent(i) == oldComponent) {
                        index = i;
                        break;
                    }
                }

                // If found, replace it
                if (index != -1) {
                    messageContainer.remove(index); // Remove old bubble

                    // Create new bubble (Text bubble with "This message was deleted")
                    Component newComponent = createMessageComponent(messageVM);

                    messageContainer.add(newComponent, index); // Add new at SAME index
                    messageComponentMap.put(messageVM.getMessageId(), newComponent); // Update map

                    messageContainer.revalidate();
                    messageContainer.repaint();
                }
            } else {
                // --- ADD NEW LOGIC (Original) ---
                Component messageComponent = createMessageComponent(messageVM);
                messageComponentMap.put(messageVM.getMessageId(), messageComponent);
                messageContainer.add(messageComponent);

                messageContainer.revalidate();
                messageContainer.repaint();

                // Only scroll to bottom for NEW messages
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            }
        });
    }

    private Component createMessageComponent(MessageVM messageVM) {
        if (messageVM.isFileMessage()) {
            return createFileBubble(messageVM);
        } else {
            // ⭐ CHECK FOR AI MESSAGE
            if (messageVM.getUsername().equals("AI_Bot")) {
                return createAiBubble(messageVM);
            }
            return createTextBubble(messageVM);
        }
    }

    /**
     * ⭐ AI Special Bubble (Distinct Look)
     */
    private Component createAiBubble(MessageVM messageVM) {
        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(new Color(0x6A, 0x0D, 0xAD), 1, true), // Purple Border
                new EmptyBorder(8, 8, 8, 12)
        ));
        bubble.setMaximumSize(new Dimension(320, 9999)); // Slightly wider

        // Header with Robot Icon
        JLabel usernameLabel = new JLabel("✨ AI Assistant");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 13));
        usernameLabel.setForeground(new Color(0x4B, 0x00, 0x82)); // Indigo

        JLabel contentLabel = new JLabel("<html><p style=\"width:240px; font-family:SansSerif;\">"
                + messageVM.getContent() + "</p></html>");
        contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        contentLabel.setForeground(new Color(0x20, 0x20, 0x20));

        JPanel footer = createFooterPanel(messageVM);

        bubble.add(usernameLabel);
        bubble.add(Box.createRigidArea(new Dimension(0, 4)));
        bubble.add(contentLabel);
        bubble.add(Box.createRigidArea(new Dimension(0, 5)));
        bubble.add(footer);

        return wrapBubble(bubble, usernameLabel, false, true); // True = Is AI
    }

    private Component createTextBubble(MessageVM messageVM) {
        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBorder(new EmptyBorder(8, 8, 8, 12));
        bubble.setMaximumSize(new Dimension(300, 9999));

        if (messageVM.hasQuote()) {
            bubble.add(createQuotePanel(messageVM.getQuotedContent(), messageVM.getReplyToId()));
            bubble.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        JLabel usernameLabel = new JLabel(messageVM.getUsername());
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 13));

        // Highlight my questions to AI
        boolean isQuestionToAi = messageVM.getContent().trim().startsWith("@AI");
        if (isQuestionToAi) {
            usernameLabel.setText(messageVM.getUsername() + " (Asking AI)");
            usernameLabel.setForeground(new Color(0x00, 0x00, 0x8B)); // Dark Blue
        }

        JLabel contentLabel = new JLabel("<html><p style=\"width:220px;\">" + messageVM.getContent() + "</p></html>");
        contentLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        // Italicize the @AI part if it's a question
        if (isQuestionToAi) {
            contentLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        }

        contentLabel.setForeground(Color.BLACK);

        JPanel footer = createFooterPanel(messageVM);

        bubble.add(usernameLabel);
        bubble.add(Box.createRigidArea(new Dimension(0, 2)));
        bubble.add(contentLabel);
        bubble.add(Box.createRigidArea(new Dimension(0, 5)));
        bubble.add(footer);

        return wrapBubble(bubble, usernameLabel, messageVM.isSentByMe(), false);
    }

    /**
     * Factory for building a File Message bubble.
     */
    private Component createFileBubble(MessageVM messageVM) {
        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBorder(new EmptyBorder(8, 8, 8, 12));
        bubble.setMaximumSize(new Dimension(300, 9999));

        if (messageVM.hasQuote()) {
            // Pass the ID here
            bubble.add(createQuotePanel(messageVM.getQuotedContent(), messageVM.getReplyToId()));
            bubble.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        JLabel usernameLabel = new JLabel(messageVM.getUsername());
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 13));

        JPanel filePanel = new JPanel(new BorderLayout(10, 0));
        filePanel.setOpaque(false);
        filePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        Icon fileIcon = UIManager.getIcon("FileView.fileIcon");
        JLabel iconLabel = new JLabel(fileIcon);
        iconLabel.setVerticalAlignment(SwingConstants.TOP);
        filePanel.add(iconLabel, BorderLayout.WEST);

        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel fileNameLabel = new JLabel(messageVM.getFileName());
        fileNameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        fileNameLabel.setForeground(Color.BLACK);
        infoPanel.add(fileNameLabel);

        // ⭐ Display compressed size from metadata
        String sizeDisplay = messageVM.getCompressedFileSize() > 0
                ? formatFileSize(messageVM.getCompressedFileSize()) + " (compressed)"
                : "Size unknown";
        JLabel sizeLabel = new JLabel(sizeDisplay);
        sizeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        sizeLabel.setForeground(Color.GRAY);
        infoPanel.add(sizeLabel);

        filePanel.add(infoPanel, BorderLayout.CENTER);

        // Save button for received files only
        if (!messageVM.isSentByMe()) {
            infoPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            JButton saveButton = new JButton("Save File");
            saveButton.setFont(new Font("Arial", Font.PLAIN, 12));
            saveButton.setMargin(new Insets(2, 5, 2, 5));
            saveButton.setAlignmentX(Component.LEFT_ALIGNMENT);

            saveButton.addActionListener(e -> {
                // ⭐ Request backend to decompress and save
                viewModel.downloadFile(messageVM);
            });
            infoPanel.add(saveButton);
        }

        JPanel footer = createFooterPanel(messageVM);

        bubble.add(usernameLabel);
        bubble.add(Box.createRigidArea(new Dimension(0, 5)));
        bubble.add(filePanel);

        if (messageVM.getContent() != null && !messageVM.getContent().trim().isEmpty()) {
            bubble.add(Box.createRigidArea(new Dimension(0, 8)));
            JLabel captionLabel = new JLabel(
                    "<html><p style=\"width:220px;\">" + messageVM.getContent() + "</p></html>"
            );
            captionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            captionLabel.setForeground(Color.BLACK);
            captionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            bubble.add(captionLabel);
        }

        bubble.add(Box.createRigidArea(new Dimension(0, 5)));
        bubble.add(footer);

        return wrapBubble(bubble, usernameLabel, messageVM.isSentByMe(), false);
    }

    /**
     * Reusable UI Helper - Quote Panel
     */
    // Change signature to accept replyToId
    private Component createQuotePanel(String quotedContent, String replyToId) {
        JPanel quotePanel = new JPanel(new BorderLayout());
        quotePanel.setBackground(new Color(0xDDDDDD));
        quotePanel.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, new Color(0x007BFF)),
                new EmptyBorder(3, 5, 3, 5)
        ));

        JLabel quoteLabel = new JLabel(quotedContent);
        quoteLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        quoteLabel.setForeground(new Color(0x555555));
        quotePanel.add(quoteLabel, BorderLayout.CENTER);
        quotePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // --- NEW: MAKE IT CLICKABLE ---
        if (replyToId != null) {
            quotePanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            quotePanel.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    scrollToMessage(replyToId);
                }
            });
            // Optional: Add tooltip
            quotePanel.setToolTipText("Click to jump to original message");
        }

        return quotePanel;
    }

    /**
     * Reusable UI Helper - Footer Panel (with Reply/Delete buttons)
     */
    // ✅ FIXED: Use MessageVM instead of ChatViewModel.MessageVM
    /**
     * Reusable UI Helper - Footer Panel (with Reply/Delete buttons)
     */
    private JPanel createFooterPanel(MessageVM messageVM) {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 1. Always show the timestamp
        JLabel timeLabel = new JLabel(messageVM.getTimestamp());
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        timeLabel.setForeground(Color.GRAY);
        footer.add(timeLabel, BorderLayout.WEST);

        // 2. CHECK IF MESSAGE IS DELETED
        // We check if the content contains the specific text we set in the ViewModel
        boolean isDeleted = messageVM.getContent() != null &&
                messageVM.getContent().contains("This message was deleted");

        // 3. ONLY ADD BUTTONS IF THE MESSAGE IS NOT DELETED
        if (!isDeleted) {
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            buttonPanel.setOpaque(false);

            // --- Reply Button ---
            JButton replyBtn = new JButton("Reply");
            replyBtn.setFont(new Font("Arial", Font.PLAIN, 10));
            replyBtn.setBorderPainted(false);
            replyBtn.setContentAreaFilled(false);
            replyBtn.setForeground(new Color(0x007BFF));
            replyBtn.setMargin(new Insets(0, 0, 0, 0));
            replyBtn.addActionListener(e -> {
                viewModel.startReply(messageVM);
                messageInputField.requestFocus(); // Focus input after clicking reply
            });
            buttonPanel.add(replyBtn);

            // --- Delete Button (Only if sent by me) ---
            if (messageVM.isSentByMe()) {
                JButton deleteBtn = new JButton("Delete");
                deleteBtn.setFont(new Font("Arial", Font.PLAIN, 10));
                deleteBtn.setBorderPainted(false);
                deleteBtn.setContentAreaFilled(false);
                deleteBtn.setForeground(Color.RED);
                deleteBtn.setMargin(new Insets(0, 5, 0, 0));
                deleteBtn.addActionListener(e -> {
                    int choice = JOptionPane.showConfirmDialog(this,
                            "Are you sure you want to delete this message?", "Delete Message",
                            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (choice == JOptionPane.YES_OPTION) {
                        viewModel.deleteMessage(messageVM);
                    }
                });
                buttonPanel.add(deleteBtn);
            }

            // Add the button panel to the footer
            footer.add(buttonPanel, BorderLayout.EAST);
        }

        return footer;
    }

    /**
     * Reusable UI Helper - Wrap Bubble (align left/right based on sender)
     */
    private Component wrapBubble(JPanel bubble, JLabel usernameLabel, boolean isSentByMe, boolean isAi) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new EmptyBorder(3, 0, 3, 0));

        if (isAi) {
            // ⭐ AI Messages: Centered or Left, Distinct Color
            bubble.setBackground(new Color(0xF5, 0xF0, 0xFF)); // Light Purple
            wrapper.add(bubble, BorderLayout.WEST);
        } else if (isSentByMe) {
            bubble.setBackground(new Color(0xE1, 0xF5, 0xFE)); // Light Blue
            usernameLabel.setForeground(new Color(0x00, 0x5A, 0x9E));
            wrapper.add(bubble, BorderLayout.EAST);
        } else {
            bubble.setBackground(new Color(0xF1, 0xF1, 0xF1)); // Gray
            usernameLabel.setForeground(new Color(0x00, 0x7B, 0xFF));
            wrapper.add(bubble, BorderLayout.WEST);
        }
        return wrapper;
    }

}