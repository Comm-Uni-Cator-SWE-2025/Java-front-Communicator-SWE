package com.swe.ux.views;

import com.swe.chat.MessageVM;
import com.swe.ux.viewmodels.ChatViewModel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * The Chat UI View. It is a JPanel that can be embedded in the main application.
 */
public class ChatView extends JPanel {

    // Constants for magic numbers
    /** Standard border padding value. */
    private static final int BORDER_PADDING = 10;
    /** Small padding value. */
    private static final int SMALL_PADDING = 5;
    /** Tiny padding value. */
    private static final int TINY_PADDING = 3;
    /** Value representing minus one. */
    private static final int MINUS_ONE = -1;
    /** Value representing zero. */
    private static final int ZERO = 0;
    /** Value representing one. */
    private static final int ONE = 1;
    /** Value representing two. */
    private static final int TWO = 2;
    /** Value representing four. */
    private static final int FOUR = 4;
    /** Value representing eight. */
    private static final int EIGHT = 8;
    /** Value representing twelve. */
    private static final int TWELVE = 12;
    /** Value representing fourteen. */
    private static final int FOURTEEN = 14;
    /** Value representing sixteen. */
    private static final int SIXTEEN = 16;
    /** Timer delay in milliseconds. */
    private static final int TIMER_DELAY_MS = 1000;
    /** Maximum width for message bubbles. */
    private static final int BUBBLE_MAX_WIDTH = 300;
    /** Maximum width for AI message bubbles. */
    private static final int AI_BUBBLE_MAX_WIDTH = 320;
    /** Maximum dimension value for flexible sizing. */
    private static final int MAX_DIMENSION = 9999;
    /** Bytes per kilobyte. */
    private static final int BYTES_PER_KB = 1024;
    /** Width of quote border. */
    private static final int QUOTE_BORDER_WIDTH = 3;
    /** Width for quote content. */
    private static final int QUOTE_WIDTH = 220;
    /** Width for AI content. */
    private static final int AI_CONTENT_WIDTH = 240;
    /** Font size 10. */
    private static final int FONT_SIZE_10 = 10;
    /** Font size 11. */
    private static final int FONT_SIZE_11 = 11;
    /** Font size 12. */
    private static final int FONT_SIZE_12 = 12;
    /** Font size 13. */
    private static final int FONT_SIZE_13 = 13;
    /** Font size 14. */
    private static final int FONT_SIZE_14 = 14;
    /** Font size 16. */
    private static final int FONT_SIZE_16 = 16;
    /** Background color value. */
    private static final int COLOR_BACKGROUND = 0xE5DDD5;
    /** Border gray color value. */
    private static final int COLOR_BORDER_GRAY = 0xCCCCCC;
    /** Panel background color value. */
    private static final int COLOR_PANEL_BG = 0xEFEFEF;
    /** Highlight color value. */
    private static final int COLOR_HIGHLIGHT = 0xFFFAD2;
    /** AI purple color value. */
    private static final int COLOR_AI_PURPLE = 0x6A0DAD;
    /** AI indigo color value. */
    private static final int COLOR_AI_INDIGO = 0x4B0082;
    /** AI text color value. */
    private static final int COLOR_AI_TEXT = 0x202020;
    /** AI background color value. */
    private static final int COLOR_AI_BG = 0xF5F0FF;
    /** Sent message background color value. */
    private static final int COLOR_SENT_BG = 0xE1F5FE;
    /** Sent message username color value. */
    private static final int COLOR_SENT_USERNAME = 0x005A9E;
    /** Received message background color value. */
    private static final int COLOR_RECEIVED_BG = 0xF1F1F1;
    /** Blue color value. */
    private static final int COLOR_BLUE = 0x007BFF;
    /** Dark blue color value. */
    private static final int COLOR_DARK_BLUE = 0x00008B;
    /** Quote background color value. */
    private static final int COLOR_QUOTE_BG = 0xDDDDDD;
    /** Quote text color value. */
    private static final int COLOR_QUOTE_TEXT = 0x555555;
    /** Send button red component. */
    private static final int COLOR_SEND_BUTTON_R = 0x00;
    /** Send button green component. */
    private static final int COLOR_SEND_BUTTON_G = 0x7B;
    /** Send button blue component. */
    private static final int COLOR_SEND_BUTTON_B = 0xFF;

    /**
     * The view model for this chat view.
     */
    private final ChatViewModel viewModel;

    /**
     * Container panel for all messages.
     */
    private JPanel messageContainer;

    /**
     * Scroll pane containing the message container.
     */
    private JScrollPane scrollPane;

    /**
     * Text field for entering new messages.
     */
    private JTextField messageInputField;

    /**
     * Button to send messages.
     */
    private JButton sendButton;

    /**
     * Panel showing reply quote information.
     */
    private JPanel replyQuotePanel;

    /**
     * Label displaying the reply quote text.
     */
    private JLabel replyQuoteLabel;

    /**
     * Button to attach files.
     */
    private JButton attachButton;

    /**
     * Panel showing attachment information.
     */
    private JPanel attachmentPanel;

    /**
     * Label displaying attachment file name.
     */
    private JLabel attachmentLabel;

    /**
     * Popup menu for suggestions.
     */
    private JPopupMenu suggestionPopup;

    /**
     * Map of message IDs to their UI components.
     */
    private final Map<String, Component> messageComponentMap = new HashMap<>();

    /**
     * Constructor for ChatView.
     *
     * @param chatViewModel the view model for this chat view
     */
    public ChatView(final ChatViewModel chatViewModel) {
        this.viewModel = chatViewModel;

        setupMainPanel();
        setupMessageContainer();
        setupBottomPanel();
        bindViewModelCallbacks();
    }

    /**
     * Sets up the main panel layout and background.
     */
    private void setupMainPanel() {
        setLayout(new BorderLayout(ZERO, ZERO));
        setBackground(new Color(COLOR_BACKGROUND));
    }

    /**
     * Sets up the message container and scroll pane.
     */
    private void setupMessageContainer() {
        messageContainer = new JPanel();
        messageContainer.setLayout(new BoxLayout(messageContainer, BoxLayout.Y_AXIS));
        messageContainer.setBackground(Color.WHITE);

        final JPanel messageContainerWrapper = new JPanel(new BorderLayout());
        messageContainerWrapper.setBackground(Color.WHITE);
        messageContainerWrapper.setBorder(new EmptyBorder(BORDER_PADDING, BORDER_PADDING,
                BORDER_PADDING, BORDER_PADDING));
        messageContainerWrapper.add(messageContainer, BorderLayout.NORTH);

        scrollPane = new JScrollPane(messageContainerWrapper);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createMatteBorder(ZERO, ZERO, ONE, ZERO,
                new Color(COLOR_BORDER_GRAY)));
        scrollPane.getVerticalScrollBar().setUnitIncrement(SIXTEEN);

        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Sets up the bottom panel with input, reply, and attachment sections.
     */
    private void setupBottomPanel() {
        final JPanel bottomVBox = new JPanel();
        bottomVBox.setLayout(new BoxLayout(bottomVBox, BoxLayout.Y_AXIS));

        setupAttachmentPanel(bottomVBox);
        setupReplyPanel(bottomVBox);
        setupInputPanel(bottomVBox);

        add(bottomVBox, BorderLayout.SOUTH);
    }

    /**
     * Sets up the attachment panel.
     *
     * @param parentPanel the parent panel to add the attachment panel to
     */
    private void setupAttachmentPanel(final JPanel parentPanel) {
        attachmentPanel = new JPanel(new BorderLayout(SMALL_PADDING, SMALL_PADDING));
        attachmentPanel.setBackground(new Color(COLOR_PANEL_BG));
        attachmentPanel.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(ONE, ZERO, ZERO, ZERO,
                        new Color(COLOR_BORDER_GRAY)),
                new EmptyBorder(SMALL_PADDING, BORDER_PADDING, SMALL_PADDING, BORDER_PADDING)
        ));
        attachmentLabel = new JLabel("Attached: file.txt");
        attachmentLabel.setFont(new Font("Arial", Font.ITALIC, TWELVE));
        attachmentLabel.setForeground(Color.GRAY);

        final JButton cancelAttachmentButton = new JButton("X");
        cancelAttachmentButton.setMargin(new Insets(TWO, FOUR, TWO, FOUR));
        cancelAttachmentButton.addActionListener(e -> viewModel.cancelAttachment());
        parentPanel.add(attachmentPanel);
    }

    /**
     * Sets up the reply panel.
     *
     * @param parentPanel the parent panel to add the reply panel to
     */
    private void setupReplyPanel(final JPanel parentPanel) {
        replyQuotePanel = new JPanel(new BorderLayout(SMALL_PADDING, SMALL_PADDING));
        replyQuotePanel.setBackground(new Color(COLOR_PANEL_BG));
        replyQuotePanel.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(ONE, ZERO, ZERO, ZERO,
                        new Color(COLOR_BORDER_GRAY)),
                new EmptyBorder(SMALL_PADDING, BORDER_PADDING, SMALL_PADDING, BORDER_PADDING)
        ));
        replyQuoteLabel = new JLabel("Replying to...");
        replyQuoteLabel.setFont(new Font("Arial", Font.ITALIC, TWELVE));
        replyQuoteLabel.setForeground(Color.GRAY);

        final JButton cancelReplyButton = new JButton("X");
        cancelReplyButton.setMargin(new Insets(TWO, FOUR, TWO, FOUR));
        cancelReplyButton.addActionListener(e -> viewModel.cancelReply());

        replyQuotePanel.add(replyQuoteLabel, BorderLayout.CENTER);
        replyQuotePanel.add(cancelReplyButton, BorderLayout.EAST);
        replyQuotePanel.setVisible(false);
        parentPanel.add(replyQuotePanel);
    }

    /**
     * Sets up the input panel with text field and buttons.
     *
     * @param parentPanel the parent panel to add the input panel to
     */
    private void setupInputPanel(final JPanel parentPanel) {
        final JPanel inputHBox = new JPanel(new BorderLayout(BORDER_PADDING, BORDER_PADDING));
        inputHBox.setBorder(new EmptyBorder(BORDER_PADDING, BORDER_PADDING,
                BORDER_PADDING, BORDER_PADDING));

        messageInputField = new JTextField();
        messageInputField.setFont(new Font("Arial", Font.PLAIN, FOURTEEN));

        sendButton = new JButton("Send");
        sendButton.setBackground(new Color(COLOR_SEND_BUTTON_R, COLOR_SEND_BUTTON_G,
                COLOR_SEND_BUTTON_B));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setFont(new Font("Arial", Font.BOLD, TWELVE));

        attachButton = new JButton("+");
        attachButton.setFont(new Font("Arial", Font.BOLD, FONT_SIZE_16));
        attachButton.setMargin(new Insets(ONE, SMALL_PADDING, ONE, SMALL_PADDING));

        final JPanel buttonPanel = new JPanel(new BorderLayout(SMALL_PADDING, ZERO));
        buttonPanel.add(attachButton, BorderLayout.WEST);
        buttonPanel.add(sendButton, BorderLayout.CENTER);

        sendButton.addActionListener(e -> viewModel.send(messageInputField.getText()));
        messageInputField.addActionListener(e -> viewModel.send(messageInputField.getText()));

        inputHBox.add(messageInputField, BorderLayout.CENTER);
        inputHBox.add(buttonPanel, BorderLayout.EAST);
        parentPanel.add(inputHBox);
    }

    /**
     * Binds all ViewModel callbacks and actions.
     */
    private void bindViewModelCallbacks() {
        viewModel.setOnClearInput(() -> messageInputField.setText(""));

        viewModel.setOnReplyStateChange(quoteText -> {
            replyQuotePanel.setVisible(quoteText != null);
            if (quoteText != null) {
                replyQuoteLabel.setText(quoteText);
            }
            revalidate();
            repaint();
        });

        viewModel.setOnAttachmentSet(attachmentName -> {
            attachmentPanel.setVisible(attachmentName != null);
            if (attachmentName != null) {
                attachmentLabel.setText(attachmentName);
            }
            revalidate();
            repaint();
        });

        viewModel.setOnMessageAdded(this::addMessageToView);
        viewModel.setOnMessageRemoved(this::removeMessageFromView);

        attachButton.addActionListener(e -> showSelectFileDialog());

        viewModel.setOnShowErrorDialog(message -> {
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        });

        viewModel.setOnShowSuccessDialog(message -> {
            JOptionPane.showMessageDialog(this, message, "File Saved",
                    JOptionPane.INFORMATION_MESSAGE);
        });
    }

    /**
     * This method contains the Swing logic for showing the "Select File" dialog.
     */
    private void showSelectFileDialog() {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a file to send");

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            final File selectedFile = fileChooser.getSelectedFile();

            // Sanitize path
            String cleanPath = selectedFile.getAbsolutePath().trim();
            if (cleanPath.startsWith("*")) {
                cleanPath = cleanPath.substring(ONE).trim();
            }
            final File finalFile = new File(cleanPath);

            // Notify the ViewModel of the user's choice
            viewModel.userSelectedFileToAttach(finalFile);
        }
    }

    /**
     * Scrolls to a specific message and highlights it.
     *
     * @param targetMessageId the ID of the message to scroll to
     */
    private void scrollToMessage(final String targetMessageId) {
        if (targetMessageId == null) {
            return;
        }

        final Component targetComponent = messageComponentMap.get(targetMessageId);

        if (targetComponent != null) {
            // 1. SCROLL logic
            // We get the bounds of the component and tell the visible rect to go there
            final Rectangle bounds = targetComponent.getBounds();
            messageContainer.scrollRectToVisible(bounds);

            // 2. HIGHLIGHT logic (Flash effect like WhatsApp)
            // We assume targetComponent is the JPanel wrapper we created
            if (targetComponent instanceof JPanel) {
                final JPanel panel = (JPanel) targetComponent;
                final Color originalColor = panel.getBackground();
                final Color highlightColor = new Color(COLOR_HIGHLIGHT);

                // Change color immediately
                panel.setBackground(highlightColor);
                panel.repaint();

                // Change it back after 1 second using a Swing Timer
                new Timer(TIMER_DELAY_MS, e -> {
                    panel.setBackground(originalColor);
                    panel.repaint();
                    ((Timer) e.getSource()).stop(); // Stop the timer
                }).start();
            }
        } else {
            // Optional: Show a small toast if the message isn't loaded/found
            System.out.println("Message " + targetMessageId + " not found in current view.");
        }
    }

    /**
     * Removes a message from the view.
     *
     * @param messageId the ID of the message to remove
     */
    private void removeMessageFromView(final String messageId) {
        SwingUtilities.invokeLater(() -> {
            final Component componentToRemove = messageComponentMap.get(messageId);
            if (componentToRemove != null) {
                messageContainer.remove(componentToRemove);
                messageComponentMap.remove(messageId);
                messageContainer.revalidate();
                messageContainer.repaint();
            }
        });
    }

    /**
     * Formats file size in human-readable format.
     *
     * @param bytes the file size in bytes
     * @return formatted file size string
     */
    private String formatFileSize(final long bytes) {
        if (bytes < BYTES_PER_KB) {
            return bytes + " B";
        }
        final int exp = (int) (Math.log(bytes) / Math.log(BYTES_PER_KB));
        final String pre = "KMGTPE".charAt(exp - ONE) + "";
        return String.format("%.1f %sB", bytes / Math.pow(BYTES_PER_KB, exp), pre);
    }

    /**
     * Helper to add message on EDT.
     *
     * @param messageVM the message view model to add
     */
    private void addMessageToView(final MessageVM messageVM) {
        SwingUtilities.invokeLater(() -> {
            // 1. Check if we already have a component for this message ID
            if (messageComponentMap.containsKey(messageVM.getMessageId())) {
                // --- UPDATE LOGIC ---
                final Component oldComponent = messageComponentMap.get(messageVM.getMessageId());

                // Find the index of the old component
                int index = MINUS_ONE;
                for (int i = ZERO; i < messageContainer.getComponentCount(); i++) {
                    if (messageContainer.getComponent(i) == oldComponent) {
                        index = i;
                        break;
                    }
                }

                // If found, replace it
                if (index != MINUS_ONE) {
                    messageContainer.remove(index); // Remove old bubble

                    // Create new bubble (Text bubble with "This message was deleted")
                    final Component newComponent = createMessageComponent(messageVM);

                    messageContainer.add(newComponent, index); // Add new at SAME index
                    messageComponentMap.put(messageVM.getMessageId(), newComponent); // Update map

                    messageContainer.revalidate();
                    messageContainer.repaint();
                }
            } else {
                // --- ADD NEW LOGIC (Original) ---
                final Component messageComponent = createMessageComponent(messageVM);
                messageComponentMap.put(messageVM.getMessageId(), messageComponent);
                messageContainer.add(messageComponent);

                messageContainer.revalidate();
                messageContainer.repaint();

                // Only scroll to bottom for NEW messages
                final JScrollBar vertical = scrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            }
        });
    }

    /**
     * Creates a message component based on message type.
     *
     * @param messageVM the message view model
     * @return the created component
     */
    private Component createMessageComponent(final MessageVM messageVM) {
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
     * ⭐ AI Special Bubble (Distinct Look).
     *
     * @param messageVM the message view model
     * @return the created AI bubble component
     */
    private Component createAiBubble(final MessageVM messageVM) {
        final JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(new Color(COLOR_AI_PURPLE), ONE, true),
                new EmptyBorder(EIGHT, EIGHT, EIGHT, TWELVE)
        ));
        bubble.setMaximumSize(new Dimension(AI_BUBBLE_MAX_WIDTH, MAX_DIMENSION));

        // Header with Robot Icon
        final JLabel usernameLabel = new JLabel("✨ AI Assistant");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, FONT_SIZE_13));
        usernameLabel.setForeground(new Color(COLOR_AI_INDIGO));

        final JLabel contentLabel = new JLabel("<html><p style=\"width:" + AI_CONTENT_WIDTH
                + "px; font-family:SansSerif;\">" + messageVM.getContent() + "</p></html>");
        contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, FOURTEEN));
        contentLabel.setForeground(new Color(COLOR_AI_TEXT));

        final JPanel footer = createFooterPanel(messageVM);

        bubble.add(usernameLabel);
        bubble.add(Box.createRigidArea(new Dimension(ZERO, FOUR)));
        bubble.add(contentLabel);
        bubble.add(Box.createRigidArea(new Dimension(ZERO, SMALL_PADDING)));
        bubble.add(footer);

        return wrapBubble(bubble, usernameLabel, false, true); // True = Is AI
    }

    /**
     * Creates a text message bubble.
     *
     * @param messageVM the message view model
     * @return the created text bubble component
     */
    private Component createTextBubble(final MessageVM messageVM) {
        final JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBorder(new EmptyBorder(EIGHT, EIGHT, EIGHT, TWELVE));
        bubble.setMaximumSize(new Dimension(BUBBLE_MAX_WIDTH, MAX_DIMENSION));

        if (messageVM.hasQuote()) {
            bubble.add(createQuotePanel(messageVM.getQuotedContent(), messageVM.getReplyToId()));
            bubble.add(Box.createRigidArea(new Dimension(ZERO, SMALL_PADDING)));
        }

        final JLabel usernameLabel = new JLabel(messageVM.getUsername());
        usernameLabel.setFont(new Font("Arial", Font.BOLD, FONT_SIZE_13));

        // Highlight my questions to AI
        final boolean isQuestionToAi = messageVM.getContent().trim().startsWith("@AI");
        if (isQuestionToAi) {
            usernameLabel.setText(messageVM.getUsername() + " (Asking AI)");
            usernameLabel.setForeground(new Color(COLOR_DARK_BLUE));
        }

        final JLabel contentLabel = new JLabel("<html><p style=\"width:" + QUOTE_WIDTH
                + "px;\">" + messageVM.getContent() + "</p></html>");
        contentLabel.setFont(new Font("Arial", Font.PLAIN, FOURTEEN));

        // Italicize the @AI part if it's a question
        if (isQuestionToAi) {
            contentLabel.setFont(new Font("Arial", Font.ITALIC, FOURTEEN));
        }

        contentLabel.setForeground(Color.BLACK);

        final JPanel footer = createFooterPanel(messageVM);

        bubble.add(usernameLabel);
        bubble.add(Box.createRigidArea(new Dimension(ZERO, TWO)));
        bubble.add(contentLabel);
        bubble.add(Box.createRigidArea(new Dimension(ZERO, SMALL_PADDING)));
        bubble.add(footer);

        return wrapBubble(bubble, usernameLabel, messageVM.isSentByMe(), false);
    }

    /**
     * Factory for building a File Message bubble.
     *
     * @param messageVM the message view model
     * @return the created file bubble component
     */
    private Component createFileBubble(final MessageVM messageVM) {
        final JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBorder(new EmptyBorder(EIGHT, EIGHT, EIGHT, TWELVE));
        bubble.setMaximumSize(new Dimension(BUBBLE_MAX_WIDTH, MAX_DIMENSION));

        if (messageVM.hasQuote()) {
            bubble.add(createQuotePanel(messageVM.getQuotedContent(), messageVM.getReplyToId()));
            bubble.add(Box.createRigidArea(new Dimension(ZERO, SMALL_PADDING)));
        }

        final JLabel usernameLabel = new JLabel(messageVM.getUsername());
        usernameLabel.setFont(new Font("Arial", Font.BOLD, FONT_SIZE_13));

        final JPanel filePanel = createFilePanel(messageVM);
        final JPanel footer = createFooterPanel(messageVM);

        bubble.add(usernameLabel);
        bubble.add(Box.createRigidArea(new Dimension(ZERO, SMALL_PADDING)));
        bubble.add(filePanel);

        addCaptionIfPresent(bubble, messageVM);

        bubble.add(Box.createRigidArea(new Dimension(ZERO, SMALL_PADDING)));
        bubble.add(footer);

        return wrapBubble(bubble, usernameLabel, messageVM.isSentByMe(), false);
    }

    /**
     * Creates the file panel with icon and file information.
     *
     * @param messageVM the message view model
     * @return the created file panel
     */
    private JPanel createFilePanel(final MessageVM messageVM) {
        final JPanel filePanel = new JPanel(new BorderLayout(BORDER_PADDING, ZERO));
        filePanel.setOpaque(false);
        filePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        final Icon fileIcon = UIManager.getIcon("FileView.fileIcon");
        final JLabel iconLabel = new JLabel(fileIcon);
        iconLabel.setVerticalAlignment(SwingConstants.TOP);
        filePanel.add(iconLabel, BorderLayout.WEST);

        final JPanel infoPanel = createFileInfoPanel(messageVM);
        filePanel.add(infoPanel, BorderLayout.CENTER);

        return filePanel;
    }

    /**
     * Creates the file info panel with file name, size, and save button.
     *
     * @param messageVM the message view model
     * @return the created info panel
     */
    private JPanel createFileInfoPanel(final MessageVM messageVM) {
        final JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        final JLabel fileNameLabel = new JLabel(messageVM.getFileName());
        fileNameLabel.setFont(new Font("Arial", Font.BOLD, FOURTEEN));
        fileNameLabel.setForeground(Color.BLACK);
        infoPanel.add(fileNameLabel);

        final String sizeDisplay;
        if (messageVM.getCompressedFileSize() > ZERO) {
            sizeDisplay = formatFileSize(messageVM.getCompressedFileSize()) + " (compressed)";
        } else {
            sizeDisplay = "Size unknown";
        }
        final JLabel sizeLabel = new JLabel(sizeDisplay);
        sizeLabel.setFont(new Font("Arial", Font.PLAIN, TWELVE));
        sizeLabel.setForeground(Color.GRAY);
        infoPanel.add(sizeLabel);

        if (!messageVM.isSentByMe()) {
            addSaveButton(infoPanel, messageVM);
        }

        return infoPanel;
    }

    /**
     * Adds a save button to the info panel.
     *
     * @param infoPanel the info panel to add the button to
     * @param messageVM the message view model
     */
    private void addSaveButton(final JPanel infoPanel, final MessageVM messageVM) {
        infoPanel.add(Box.createRigidArea(new Dimension(ZERO, EIGHT)));
        final JButton saveButton = new JButton("Save File");
        saveButton.setFont(new Font("Arial", Font.PLAIN, TWELVE));
        saveButton.setMargin(new Insets(TWO, SMALL_PADDING, TWO, SMALL_PADDING));
        saveButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        saveButton.addActionListener(e -> {
            viewModel.downloadFile(messageVM);
        });
        infoPanel.add(saveButton);
    }

    /**
     * Adds caption to bubble if message has content.
     *
     * @param bubble the bubble panel
     * @param messageVM the message view model
     */
    private void addCaptionIfPresent(final JPanel bubble, final MessageVM messageVM) {
        if (messageVM.getContent() != null && !messageVM.getContent().trim().isEmpty()) {
            bubble.add(Box.createRigidArea(new Dimension(ZERO, EIGHT)));
            final JLabel captionLabel = new JLabel(
                    "<html><p style=\"width:" + QUOTE_WIDTH + "px;\">"
                            + messageVM.getContent() + "</p></html>"
            );
            captionLabel.setFont(new Font("Arial", Font.PLAIN, FOURTEEN));
            captionLabel.setForeground(Color.BLACK);
            captionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            bubble.add(captionLabel);
        }
    }

    /**
     * Reusable UI Helper - Quote Panel.
     *
     * @param quotedContent the quoted message content
     * @param replyToId the ID of the message being replied to
     * @return the created quote panel component
     */
    private Component createQuotePanel(final String quotedContent, final String replyToId) {
        final JPanel quotePanel = new JPanel(new BorderLayout());
        quotePanel.setBackground(new Color(COLOR_QUOTE_BG));
        quotePanel.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(ZERO, QUOTE_BORDER_WIDTH, ZERO, ZERO,
                        new Color(COLOR_BLUE)),
                new EmptyBorder(TINY_PADDING, SMALL_PADDING, TINY_PADDING, SMALL_PADDING)
        ));

        final JLabel quoteLabel = new JLabel(quotedContent);
        quoteLabel.setFont(new Font("Arial", Font.ITALIC, FONT_SIZE_11));
        quoteLabel.setForeground(new Color(COLOR_QUOTE_TEXT));
        quotePanel.add(quoteLabel, BorderLayout.CENTER);
        quotePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // --- NEW: MAKE IT CLICKABLE ---
        if (replyToId != null) {
            quotePanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            quotePanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    scrollToMessage(replyToId);
                }
            });
            // Optional: Add tooltip
            quotePanel.setToolTipText("Click to jump to original message");
        }

        return quotePanel;
    }

    /**
     * Reusable UI Helper - Footer Panel (with Reply/Delete buttons).
     *
     * @param messageVM the message view model
     * @return the created footer panel
     */
    private JPanel createFooterPanel(final MessageVM messageVM) {
        final JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 1. Always show the timestamp
        final JLabel timeLabel = new JLabel(messageVM.getTimestamp());
        timeLabel.setFont(new Font("Arial", Font.PLAIN, FONT_SIZE_10));
        timeLabel.setForeground(Color.GRAY);
        footer.add(timeLabel, BorderLayout.WEST);

        // 2. CHECK IF MESSAGE IS DELETED
        // We check if the content contains the specific text we set in the ViewModel
        final boolean isDeleted = messageVM.getContent() != null
                && messageVM.getContent().contains("This message was deleted");

        // 3. ONLY ADD BUTTONS IF THE MESSAGE IS NOT DELETED
        if (!isDeleted) {
            final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, ZERO, ZERO));
            buttonPanel.setOpaque(false);

            // --- Reply Button ---
            final JButton replyBtn = new JButton("Reply");
            replyBtn.setFont(new Font("Arial", Font.PLAIN, FONT_SIZE_10));
            replyBtn.setBorderPainted(false);
            replyBtn.setContentAreaFilled(false);
            replyBtn.setForeground(new Color(COLOR_BLUE));
            replyBtn.setMargin(new Insets(ZERO, ZERO, ZERO, ZERO));
            replyBtn.addActionListener(e -> {
                viewModel.startReply(messageVM);
                messageInputField.requestFocus(); // Focus input after clicking reply
            });
            buttonPanel.add(replyBtn);

            // --- Delete Button (Only if sent by me) ---
            if (messageVM.isSentByMe()) {
                final JButton deleteBtn = new JButton("Delete");
                deleteBtn.setFont(new Font("Arial", Font.PLAIN, FONT_SIZE_10));
                deleteBtn.setBorderPainted(false);
                deleteBtn.setContentAreaFilled(false);
                deleteBtn.setForeground(Color.RED);
                deleteBtn.setMargin(new Insets(ZERO, SMALL_PADDING, ZERO, ZERO));
                deleteBtn.addActionListener(e -> {
                    final int choice = JOptionPane.showConfirmDialog(this,
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
     * Reusable UI Helper - Wrap Bubble (align left/right based on sender).
     *
     * @param bubble the bubble panel to wrap
     * @param usernameLabel the username label
     * @param isSentByMe whether the message was sent by the current user
     * @param isAi whether this is an AI message
     * @return the wrapped bubble component
     */
    private Component wrapBubble(final JPanel bubble, final JLabel usernameLabel,
                                  final boolean isSentByMe, final boolean isAi) {
        final JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new EmptyBorder(TINY_PADDING, ZERO, TINY_PADDING, ZERO));

        if (isAi) {
            // ⭐ AI Messages: Centered or Left, Distinct Color
            bubble.setBackground(new Color(COLOR_AI_BG));
            wrapper.add(bubble, BorderLayout.WEST);
        } else if (isSentByMe) {
            bubble.setBackground(new Color(COLOR_SENT_BG));
            usernameLabel.setForeground(new Color(COLOR_SENT_USERNAME));
            wrapper.add(bubble, BorderLayout.EAST);
        } else {
            bubble.setBackground(new Color(COLOR_RECEIVED_BG));
            usernameLabel.setForeground(new Color(COLOR_BLUE));
            wrapper.add(bubble, BorderLayout.WEST);
        }
        return wrapper;
    }

}