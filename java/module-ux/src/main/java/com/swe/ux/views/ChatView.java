package com.swe.ux.views;

import com.swe.chat.MessageVM;
import com.swe.ux.theme.Theme;
import com.swe.ux.theme.ThemeManager;
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
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
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
    /** Highlight color value. */
    private static final int COLOR_HIGHLIGHT = 0xFFFAD2;
    /** Default sent bubble color. */
    private static final Color DEFAULT_SENT_COLOR = new Color(0xE1F5FE);
    /** Default received bubble color. */
    private static final Color DEFAULT_RECEIVED_COLOR = new Color(0xF1F1F1);
    /** Default AI bubble color. */
    private static final Color DEFAULT_AI_COLOR = new Color(0xF5F0FF);
    /** Default accent color. */
    private static final Color DEFAULT_ACCENT = new Color(0x4A86E8);
    /** Default muted text color. */
    private static final Color DEFAULT_MUTED = Color.GRAY;
    /** Default normal text color. */
    private static final Color DEFAULT_TEXT = Color.DARK_GRAY;
    /** Default danger color for destructive actions. */
    private static final Color DEFAULT_DANGER = new Color(0xE53935);
    /** Bubble rounding radius. */
    private static final int BUBBLE_CORNER_RADIUS = 22;
    /** Card rounding radius. */
    private static final int CARD_CORNER_RADIUS = 18;
    /** Input rounding radius. */
    private static final int INPUT_CORNER_RADIUS = 26;
    /** Client property key for bubble role. */
    private static final String CHAT_ROLE_KEY = "chatRole";
    /** Client property key for username label. */
    private static final String USERNAME_LABEL_KEY = "usernameLabel";
    /** Client property key for quote panels. */
    private static final String QUOTE_PANEL_KEY = "quotePanel";
    /** Client property key for muted labels. */
    private static final String MUTED_LABEL_KEY = "mutedLabel";
    /** Client property key for wrapped bubble panels. */
    private static final String BUBBLE_PANEL_KEY = "bubblePanel";
    /** Client property key to skip label theming. */
    private static final String SKIP_LABEL_THEME_KEY = "skipLabelTheme";
    /** Client property key to mark AI question labels. */
    private static final String AI_QUESTION_LABEL_KEY = "aiQuestion";
    /** Bubble roles for styling. */
    private enum BubbleRole { SENT, RECEIVED, AI }

    /**
     * The view model for this chat view.
     */
    private final ChatViewModel viewModel;

    /**
     * Container panel for all messages.
     */
    private JPanel messageContainer;
    /**
     * Stacked column holding preview + message list.
     */
    private JPanel conversationColumn;
    /**
     * Wrapper panel to allow background changes.
     */
    private JPanel messageContainerWrapper;
    /**
     * Panel that shows a pending upload preview.
     */
    private RoundedPanel uploadPreviewPanel;
    /**
     * Preview label for pending uploads.
     */
    private JLabel uploadPreviewLabel;
    /**
     * Cancel button for preview.
     */
    private JButton uploadPreviewCancelButton;

    /**
     * Scroll pane containing the message container.
     */
    private JScrollPane scrollPane;

    /**
     * Text field for entering new messages.
     */
    private JTextField messageInputField;
    /**
     * Rounded wrapper for the input field.
     */
    private RoundedPanel messageInputWrapper;

    /**
     * Button to send messages.
     */
    private JButton sendButton;
    /**
     * Input panel reference for theming.
     */
    private JPanel inputPanel;

    /**
     * Panel showing reply quote information.
     */
    private RoundedPanel replyQuotePanel;

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
    private RoundedPanel attachmentPanel;

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
    /** Cached theme colors. */
    private Color backgroundColor;
    private Color panelBackgroundColor;
    private Color borderColor;
    private Color textColor;
    private Color accentColor;
    private Color inputBackgroundColor;
    private Color mutedTextColor;
    private Color sentBubbleColor;
    private Color receivedBubbleColor;
    private Color aiBubbleColor;
    private Color quoteBackgroundColor;
    private Color quoteBorderColor;
    private Color sentUsernameColor;
    private Color receivedUsernameColor;

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

        ThemeManager.getInstance().addThemeChangeListener(() ->
                SwingUtilities.invokeLater(this::applyTheme));
        applyTheme();
    }

    /**
     * Sets up the main panel layout and background.
     */
    private void setupMainPanel() {
        setLayout(new BorderLayout(ZERO, ZERO));
        setBackground(Color.WHITE);
    }

    /**
     * Sets up the message container and scroll pane.
     */
    private void setupMessageContainer() {
        messageContainer = new JPanel();
        messageContainer.setLayout(new BoxLayout(messageContainer, BoxLayout.Y_AXIS));
        messageContainer.setOpaque(false);
        messageContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        conversationColumn = new JPanel();
        conversationColumn.setLayout(new BoxLayout(conversationColumn, BoxLayout.Y_AXIS));
        conversationColumn.setOpaque(false);
        conversationColumn.setAlignmentX(Component.LEFT_ALIGNMENT);

        uploadPreviewPanel = buildUploadPreviewPanel();
        conversationColumn.add(uploadPreviewPanel);
        conversationColumn.add(Box.createRigidArea(new Dimension(ZERO, SMALL_PADDING)));
        conversationColumn.add(messageContainer);

        messageContainerWrapper = new JPanel(new BorderLayout());
        messageContainerWrapper.setBorder(new EmptyBorder(BORDER_PADDING, BORDER_PADDING,
                BORDER_PADDING, BORDER_PADDING));
        messageContainerWrapper.add(conversationColumn, BorderLayout.NORTH);

        scrollPane = new JScrollPane(messageContainerWrapper);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(SIXTEEN);

        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Builds the upload preview banner shown above the conversation.
     * @return configured preview panel
     */
    private RoundedPanel buildUploadPreviewPanel() {
        final RoundedPanel panel = new RoundedPanel(CARD_CORNER_RADIUS);
        panel.setLayout(new BorderLayout(SMALL_PADDING, ZERO));
        panel.setBorder(new EmptyBorder(SMALL_PADDING, BORDER_PADDING, SMALL_PADDING, BORDER_PADDING));
        panel.setVisible(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        uploadPreviewLabel = new JLabel();
        uploadPreviewLabel.setFont(new Font("Arial", Font.PLAIN, FONT_SIZE_12));
        uploadPreviewLabel.putClientProperty(MUTED_LABEL_KEY, Boolean.TRUE);
        applyLabelTheme(uploadPreviewLabel);

        uploadPreviewCancelButton = new JButton("Cancel");
        uploadPreviewCancelButton.setMargin(new Insets(TWO, TWELVE, TWO, TWELVE));
        uploadPreviewCancelButton.setFocusPainted(false);
        uploadPreviewCancelButton.addActionListener(e -> viewModel.cancelAttachment());

        panel.add(uploadPreviewLabel, BorderLayout.CENTER);
        panel.add(uploadPreviewCancelButton, BorderLayout.EAST);
        return panel;
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
        attachmentPanel = new RoundedPanel(CARD_CORNER_RADIUS);
        attachmentPanel.setLayout(new BorderLayout(SMALL_PADDING, SMALL_PADDING));
        attachmentPanel.setBorder(new EmptyBorder(SMALL_PADDING, BORDER_PADDING,
                SMALL_PADDING, BORDER_PADDING));
        attachmentLabel = new JLabel("");
        attachmentLabel.setFont(new Font("Arial", Font.ITALIC, TWELVE));
        attachmentLabel.putClientProperty(MUTED_LABEL_KEY, Boolean.TRUE);
        applyLabelTheme(attachmentLabel);

        final JButton cancelAttachmentButton = new JButton("X");
        cancelAttachmentButton.setMargin(new Insets(TWO, FOUR, TWO, FOUR));
        cancelAttachmentButton.setFocusPainted(false);
        cancelAttachmentButton.addActionListener(e -> viewModel.cancelAttachment());
        attachmentPanel.add(attachmentLabel, BorderLayout.CENTER);
        attachmentPanel.add(cancelAttachmentButton, BorderLayout.EAST);
        attachmentPanel.setVisible(false);
        parentPanel.add(attachmentPanel);
    }

    /**
     * Sets up the reply panel.
     *
     * @param parentPanel the parent panel to add the reply panel to
     */
    private void setupReplyPanel(final JPanel parentPanel) {
        replyQuotePanel = new RoundedPanel(CARD_CORNER_RADIUS);
        replyQuotePanel.setLayout(new BorderLayout(SMALL_PADDING, SMALL_PADDING));
        replyQuotePanel.setBorder(new EmptyBorder(SMALL_PADDING, BORDER_PADDING,
                SMALL_PADDING, BORDER_PADDING));
        replyQuoteLabel = new JLabel("");
        replyQuoteLabel.setFont(new Font("Arial", Font.ITALIC, TWELVE));
        replyQuoteLabel.putClientProperty(MUTED_LABEL_KEY, Boolean.TRUE);
        applyLabelTheme(replyQuoteLabel);

        final JButton cancelReplyButton = new JButton("X");
        cancelReplyButton.setMargin(new Insets(TWO, FOUR, TWO, FOUR));
        cancelReplyButton.setFocusPainted(false);
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
        inputPanel = new JPanel(new BorderLayout(BORDER_PADDING, BORDER_PADDING));
        final JPanel inputHBox = inputPanel;
        inputHBox.setBorder(new EmptyBorder(BORDER_PADDING, BORDER_PADDING,
                BORDER_PADDING, BORDER_PADDING));

        messageInputField = new JTextField();
        messageInputField.setFont(new Font("Arial", Font.PLAIN, FOURTEEN));
        messageInputField.setBorder(BorderFactory.createEmptyBorder(SMALL_PADDING, SMALL_PADDING,
                SMALL_PADDING, SMALL_PADDING));
        messageInputField.setOpaque(false);

        sendButton = new JButton("Send");
        sendButton.setFocusPainted(false);
        sendButton.setFont(new Font("Arial", Font.BOLD, TWELVE));

        attachButton = new JButton("+");
        attachButton.setFont(new Font("Arial", Font.BOLD, FONT_SIZE_16));
        attachButton.setMargin(new Insets(ONE, SMALL_PADDING, ONE, SMALL_PADDING));
        attachButton.setFocusPainted(false);

        messageInputWrapper = new RoundedPanel(INPUT_CORNER_RADIUS);
        messageInputWrapper.setLayout(new BorderLayout());
        messageInputWrapper.setBorder(new EmptyBorder(TWO, SMALL_PADDING, TWO, SMALL_PADDING));
        messageInputWrapper.add(messageInputField, BorderLayout.CENTER);

        final JPanel buttonPanel = new JPanel(new BorderLayout(SMALL_PADDING, ZERO));
        buttonPanel.setOpaque(false);
        buttonPanel.add(attachButton, BorderLayout.WEST);
        buttonPanel.add(sendButton, BorderLayout.CENTER);

        sendButton.addActionListener(e -> viewModel.send(messageInputField.getText()));
        messageInputField.addActionListener(e -> viewModel.send(messageInputField.getText()));

        inputHBox.add(messageInputWrapper, BorderLayout.CENTER);
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

        viewModel.setOnAttachmentSet(attachmentName ->
                SwingUtilities.invokeLater(() -> handleAttachmentStateChanged(attachmentName)));

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
     * Handles attachment panel visibility + preview banner.
     * @param attachmentName descriptive label from the view model
     */
    private void handleAttachmentStateChanged(final String attachmentName) {
        if (attachmentPanel == null || attachmentLabel == null) {
            return;
        }

        if (attachmentName != null) {
            final String normalized = normalizeAttachmentName(attachmentName);
            attachmentPanel.setVisible(true);
            attachmentLabel.setText("Attached: " + normalized);
            showUploadPreview(normalized);
        } else {
            attachmentPanel.setVisible(false);
            showUploadPreview(null);
        }
        revalidate();
        repaint();
    }

    /**
     * Normalizes the attachment label text.
     * @param attachmentName raw label
     * @return formatted label
     */
    private String normalizeAttachmentName(final String attachmentName) {
        if (attachmentName == null) {
            return "";
        }
        final String trimmed = attachmentName.replace("Attached:", "").trim();
        if (trimmed.isEmpty()) {
            return attachmentName.trim();
        }
        return trimmed;
    }

    /**
     * Shows/ hides the inline upload preview banner.
     * @param displayText preview text
     */
    private void showUploadPreview(final String displayText) {
        if (uploadPreviewPanel == null || uploadPreviewLabel == null) {
            return;
        }
        if (displayText == null || displayText.isBlank()) {
            uploadPreviewPanel.setVisible(false);
        } else {
            uploadPreviewPanel.setVisible(true);
            uploadPreviewLabel.setText("[File] " + displayText + " - ready to send");
        }
        if (conversationColumn != null) {
            conversationColumn.revalidate();
            conversationColumn.repaint();
        }
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
     * Builds a rounded bubble container with shared padding.
     * @return rounded panel
     */
    private RoundedPanel createBubblePanel() {
        final RoundedPanel bubble = new RoundedPanel(BUBBLE_CORNER_RADIUS);
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBorder(new EmptyBorder(EIGHT, EIGHT, EIGHT, TWELVE));
        bubble.setOpaque(false);
        bubble.setAlignmentX(Component.LEFT_ALIGNMENT);
        return bubble;
    }

    /**
     * ⭐ AI Special Bubble (Distinct Look).
     *
     * @param messageVM the message view model
     * @return the created AI bubble component
     */
    private Component createAiBubble(final MessageVM messageVM) {
        final RoundedPanel bubble = createBubblePanel();
        bubble.setMaximumSize(new Dimension(AI_BUBBLE_MAX_WIDTH, MAX_DIMENSION));

        // Header with Robot Icon
        final JLabel usernameLabel = new JLabel("✨ AI Assistant");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, FONT_SIZE_13));
        usernameLabel.putClientProperty(SKIP_LABEL_THEME_KEY, Boolean.TRUE);

        final JLabel contentLabel = new JLabel("<html><p style=\"width:" + AI_CONTENT_WIDTH
                + "px; font-family:SansSerif;\">" + messageVM.getContent() + "</p></html>");
        contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, FOURTEEN));
        applyLabelTheme(contentLabel);

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
        final RoundedPanel bubble = createBubblePanel();
        bubble.setMaximumSize(new Dimension(BUBBLE_MAX_WIDTH, MAX_DIMENSION));

        if (messageVM.hasQuote()) {
            bubble.add(createQuotePanel(messageVM.getQuotedContent(), messageVM.getReplyToId()));
            bubble.add(Box.createRigidArea(new Dimension(ZERO, SMALL_PADDING)));
        }

        final JLabel usernameLabel = new JLabel(messageVM.getUsername());
        usernameLabel.setFont(new Font("Arial", Font.BOLD, FONT_SIZE_13));
        usernameLabel.putClientProperty(SKIP_LABEL_THEME_KEY, Boolean.TRUE);

        // Highlight my questions to AI
        final boolean isQuestionToAi = messageVM.getContent().trim().startsWith("@AI");
        if (isQuestionToAi) {
            usernameLabel.setText(messageVM.getUsername() + " (Asking AI)");
            usernameLabel.putClientProperty(AI_QUESTION_LABEL_KEY, Boolean.TRUE);
        } else {
            usernameLabel.putClientProperty(AI_QUESTION_LABEL_KEY, null);
        }

        final JLabel contentLabel = new JLabel("<html><p style=\"width:" + QUOTE_WIDTH
                + "px;\">" + messageVM.getContent() + "</p></html>");
        contentLabel.setFont(new Font("Arial", isQuestionToAi ? Font.ITALIC : Font.PLAIN, FOURTEEN));
        applyLabelTheme(contentLabel);

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
        final RoundedPanel bubble = createBubblePanel();
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
        applyLabelTheme(fileNameLabel);
        infoPanel.add(fileNameLabel);

        final String sizeDisplay;
        if (messageVM.getCompressedFileSize() > ZERO) {
            sizeDisplay = formatFileSize(messageVM.getCompressedFileSize()) + " (compressed)";
        } else {
            sizeDisplay = "Size unknown";
        }
        final JLabel sizeLabel = new JLabel(sizeDisplay);
        sizeLabel.setFont(new Font("Arial", Font.PLAIN, TWELVE));
        sizeLabel.putClientProperty(MUTED_LABEL_KEY, Boolean.TRUE);
        applyLabelTheme(sizeLabel);
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
            applyLabelTheme(captionLabel);
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
        final RoundedPanel quotePanel = new RoundedPanel(BUBBLE_CORNER_RADIUS - 6);
        quotePanel.setLayout(new BorderLayout());
        quotePanel.putClientProperty(QUOTE_PANEL_KEY, Boolean.TRUE);
        styleQuotePanel(quotePanel);

        final JLabel quoteLabel = new JLabel(quotedContent);
        quoteLabel.setFont(new Font("Arial", Font.ITALIC, FONT_SIZE_11));
        quoteLabel.putClientProperty(MUTED_LABEL_KEY, Boolean.TRUE);
        applyLabelTheme(quoteLabel);
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
        timeLabel.putClientProperty(MUTED_LABEL_KEY, Boolean.TRUE);
        applyLabelTheme(timeLabel);
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
            styleLinkButton(replyBtn, safeAccentColor());
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
                styleLinkButton(deleteBtn, safeDangerColor());
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
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(TINY_PADDING, ZERO, TINY_PADDING, ZERO));

        final BubbleRole role;
        if (isAi) {
            role = BubbleRole.AI;
        } else if (isSentByMe) {
            role = BubbleRole.SENT;
        } else {
            role = BubbleRole.RECEIVED;
        }

        usernameLabel.putClientProperty(SKIP_LABEL_THEME_KEY, Boolean.TRUE);
        bubble.putClientProperty(CHAT_ROLE_KEY, role);
        bubble.putClientProperty(USERNAME_LABEL_KEY, usernameLabel);
        wrapper.putClientProperty(BUBBLE_PANEL_KEY, bubble);
        applyBubbleTheme(bubble, role);

        if (role == BubbleRole.SENT) {
            wrapper.add(bubble, BorderLayout.EAST);
        } else {
            wrapper.add(bubble, BorderLayout.WEST);
        }
        return wrapper;
    }


    /**
     * Applies the active theme to the chat view.
     */
    private void applyTheme() {
        final Theme theme = ThemeManager.getInstance().getCurrentTheme();

        backgroundColor = theme.getBackgroundColor();
        panelBackgroundColor = theme.getPanelBackground();
        borderColor = theme.getPanelBorder();
        textColor = theme.getTextColor();
        accentColor = theme.getPrimaryColor();
        inputBackgroundColor = theme.getInputBackgroundColor();
        mutedTextColor = blendColors(textColor, backgroundColor, theme.isDark() ? 0.5 : 0.7);
        sentBubbleColor = blendColors(accentColor, backgroundColor, theme.isDark() ? 0.35 : 0.75);
        receivedBubbleColor = blendColors(panelBackgroundColor, backgroundColor,
                theme.isDark() ? 0.2 : 0.45);
        aiBubbleColor = blendColors(accentColor, panelBackgroundColor, 0.85);
        quoteBackgroundColor = blendColors(panelBackgroundColor, backgroundColor,
                theme.isDark() ? 0.15 : 0.4);
        quoteBorderColor = safeAccentColor();
        sentUsernameColor = blendColors(accentColor, textColor, 0.25);
        receivedUsernameColor = blendColors(textColor, accentColor, 0.85);

        setBackground(backgroundColor);

        if (messageContainerWrapper != null) {
            messageContainerWrapper.setBackground(panelBackgroundColor);
            messageContainerWrapper.setOpaque(true);
        }
        if (conversationColumn != null) {
            conversationColumn.setBackground(panelBackgroundColor);
        }
        if (messageContainer != null) {
            messageContainer.setBackground(panelBackgroundColor);
        }

        styleScrollPane();
        styleSurfacePanel(attachmentPanel);
        styleSurfacePanel(replyQuotePanel);
        styleUploadPreviewPanel();
        styleInputPanel();

        applyLabelTheme(attachmentLabel);
        applyLabelTheme(replyQuoteLabel);
        applyLabelTheme(uploadPreviewLabel);

        refreshAllBubbleThemes();
        retintLabels(messageContainerWrapper);
        refreshQuotePanels(messageContainerWrapper);

        revalidate();
        repaint();
    }

    private void styleScrollPane() {
        if (scrollPane == null) {
            return;
        }
        final Color border = borderColor != null ? borderColor : DEFAULT_MUTED;
        scrollPane.setBorder(BorderFactory.createMatteBorder(ZERO, ZERO, ONE, ZERO, border));
        if (scrollPane.getViewport() != null) {
            scrollPane.getViewport()
                    .setBackground(panelBackgroundColor != null ? panelBackgroundColor : Color.WHITE);
        }
        scrollPane.setBackground(panelBackgroundColor != null ? panelBackgroundColor : Color.WHITE);
    }

    private void styleSurfacePanel(final JPanel panel) {
        if (panel == null) {
            return;
        }
        final Color fill = panelBackgroundColor != null ? panelBackgroundColor : Color.WHITE;
        final Color borderCol = blendColors(fallbackColor(borderColor, DEFAULT_MUTED), fill, 0.65);
        if (panel instanceof RoundedPanel) {
            final RoundedPanel rp = (RoundedPanel) panel;
            rp.setGradient(null, null);
            rp.setFillColor(fill);
            rp.setBorderColor(borderCol);
        } else {
            panel.setOpaque(true);
            panel.setBackground(fill);
        }
    }

    private void styleUploadPreviewPanel() {
        if (uploadPreviewPanel == null) {
            return;
        }
        styleSurfacePanel(uploadPreviewPanel);
        final RoundedPanel rounded = uploadPreviewPanel;
        final Color accent = safeAccentColor();
        rounded.setBorderColor(blendColors(accent, panelBackgroundColor, 0.2));
        rounded.setGradient(
                blendColors(accent, Color.WHITE, 0.4),
                blendColors(accent, panelBackgroundColor, 0.4));
        applyLabelTheme(uploadPreviewLabel);
        styleSecondaryButton(uploadPreviewCancelButton);
    }

    private void styleInputPanel() {
        if (inputPanel == null) {
            return;
        }
        final Color bg = panelBackgroundColor != null ? panelBackgroundColor : Color.WHITE;
        inputPanel.setBackground(bg);
        inputPanel.setBorder(new EmptyBorder(BORDER_PADDING, BORDER_PADDING,
                BORDER_PADDING, BORDER_PADDING));

        if (messageInputField != null) {
            messageInputField.setForeground(fallbackColor(textColor, DEFAULT_TEXT));
            messageInputField.setCaretColor(fallbackColor(textColor, DEFAULT_TEXT));
            messageInputField.setBackground(new Color(0, 0, 0, 0));
        }

        if (messageInputWrapper != null) {
            final Color inputBg = blendColors(inputBackgroundColor, panelBackgroundColor, 0.5);
            messageInputWrapper.setGradient(null, null);
            messageInputWrapper.setFillColor(fallbackColor(inputBg, Color.WHITE));
            messageInputWrapper.setBorderColor(new Color(0, 0, 0, 0));
        }

        stylePrimaryButton(sendButton);
        styleSecondaryButton(attachButton);
    }

    private void refreshAllBubbleThemes() {
        if (messageContainer == null) {
            return;
        }
        for (Component component : messageContainer.getComponents()) {
            if (!(component instanceof JPanel)) {
                continue;
            }
            final JPanel wrapper = (JPanel) component;
            final Object bubbleObj = wrapper.getClientProperty(BUBBLE_PANEL_KEY);
            if (bubbleObj instanceof JPanel) {
                final JPanel bubblePanel = (JPanel) bubbleObj;
                final Object roleObj = bubblePanel.getClientProperty(CHAT_ROLE_KEY);
                if (roleObj instanceof BubbleRole) {
                    applyBubbleTheme(bubblePanel, (BubbleRole) roleObj);
                }
            }
        }
        messageContainer.revalidate();
        messageContainer.repaint();
    }

    private void retintLabels(final Component component) {
        if (component == null) {
            return;
        }
        if (component instanceof JLabel) {
            applyLabelTheme((JLabel) component);
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                retintLabels(child);
            }
        }
    }

    private void refreshQuotePanels(final Component component) {
        if (component == null) {
            return;
        }
        if (component instanceof JPanel) {
            final JPanel panel = (JPanel) component;
            if (Boolean.TRUE.equals(panel.getClientProperty(QUOTE_PANEL_KEY))) {
                styleQuotePanel(panel);
            }
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                refreshQuotePanels(child);
            }
        }
    }

    private void applyBubbleTheme(final JPanel bubble, final BubbleRole role) {
        if (bubble == null || role == null) {
            return;
        }
        final Color fillColor;
        final Color usernameColor;
        switch (role) {
            case SENT:
                fillColor = fallbackColor(sentBubbleColor, DEFAULT_SENT_COLOR);
                usernameColor = fallbackColor(sentUsernameColor, safeAccentColor());
                break;
            case AI:
                fillColor = fallbackColor(aiBubbleColor, DEFAULT_AI_COLOR);
                usernameColor = safeAccentColor();
                break;
            default:
                fillColor = fallbackColor(receivedBubbleColor, DEFAULT_RECEIVED_COLOR);
                usernameColor = fallbackColor(receivedUsernameColor, DEFAULT_TEXT);
                break;
        }

        if (bubble instanceof RoundedPanel) {
            final RoundedPanel rounded = (RoundedPanel) bubble;
            rounded.setGradient(null, null);
            rounded.setFillColor(fillColor);
            rounded.setBorderColor(new Color(0, 0, 0, 0));
        } else {
            bubble.setOpaque(true);
            bubble.setBackground(fillColor);
        }

        final Object usernameObj = bubble.getClientProperty(USERNAME_LABEL_KEY);
        if (usernameObj instanceof JLabel) {
            final JLabel usernameLabel = (JLabel) usernameObj;
            Color finalColor = usernameColor;
            if (Boolean.TRUE.equals(usernameLabel.getClientProperty(AI_QUESTION_LABEL_KEY))) {
                finalColor = safeAccentColor();
            }
            usernameLabel.setForeground(finalColor != null ? finalColor : DEFAULT_TEXT);
        }
    }

    private void styleQuotePanel(final JPanel quotePanel) {
        if (quotePanel == null) {
            return;
        }
        final Color fill = fallbackColor(quoteBackgroundColor, DEFAULT_RECEIVED_COLOR);
        final Color border = blendColors(fallbackColor(quoteBorderColor, safeAccentColor()), fill, 0.4);
        quotePanel.setBorder(new EmptyBorder(SMALL_PADDING, SMALL_PADDING, SMALL_PADDING, SMALL_PADDING));
        if (quotePanel instanceof RoundedPanel) {
            final RoundedPanel rounded = (RoundedPanel) quotePanel;
            rounded.setFillColor(fill);
            rounded.setGradient(null, null);
            rounded.setBorderColor(border);
        } else {
            quotePanel.setOpaque(true);
            quotePanel.setBackground(fill);
        }
    }

    private void applyLabelTheme(final JLabel label) {
        if (label == null || Boolean.TRUE.equals(label.getClientProperty(SKIP_LABEL_THEME_KEY))) {
            return;
        }
        final boolean muted = Boolean.TRUE.equals(label.getClientProperty(MUTED_LABEL_KEY));
        if (muted) {
            label.setForeground(fallbackColor(mutedTextColor, DEFAULT_MUTED));
        } else {
            label.setForeground(fallbackColor(textColor, DEFAULT_TEXT));
        }
    }

    private void stylePrimaryButton(final JButton button) {
        if (button == null) {
            return;
        }
        button.setOpaque(true);
        button.setBackground(safeAccentColor());
        button.setForeground(Color.WHITE);
        button.setBorder(new EmptyBorder(SMALL_PADDING, TWELVE, SMALL_PADDING, TWELVE));
        button.setFocusPainted(false);
    }

    private void styleSecondaryButton(final JButton button) {
        if (button == null) {
            return;
        }
        final Color accent = safeAccentColor();
        button.setOpaque(true);
        button.setBackground(panelBackgroundColor != null ? panelBackgroundColor : Color.WHITE);
        button.setForeground(accent);
        button.setBorder(BorderFactory.createLineBorder(accent));
        button.setFocusPainted(false);
    }

    private void styleLinkButton(final JButton button, final Color color) {
        if (button == null) {
            return;
        }
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setForeground(color != null ? color : safeAccentColor());
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
    }

    private Color safeAccentColor() {
        return accentColor != null ? accentColor : DEFAULT_ACCENT;
    }

    private Color safeDangerColor() {
        return DEFAULT_DANGER;
    }

    private Color blendColors(final Color base, final Color mix, final double ratioParam) {
        if (base == null && mix == null) {
            return null;
        }
        if (base == null) {
            return mix;
        }
        if (mix == null) {
            return base;
        }
        final double ratio = Math.max(0, Math.min(1, ratioParam));
        final double inverse = 1 - ratio;
        final int r = (int) Math.round(base.getRed() * inverse + mix.getRed() * ratio);
        final int g = (int) Math.round(base.getGreen() * inverse + mix.getGreen() * ratio);
        final int b = (int) Math.round(base.getBlue() * inverse + mix.getBlue() * ratio);
        return new Color(r, g, b);
    }

    private Color fallbackColor(final Color candidate, final Color fallback) {
        return candidate != null ? candidate : fallback;
    }

    /**
     * Simple rounded panel with optional gradient support.
     */
    private static final class RoundedPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private final int cornerRadius;
        private Color fillColor;
        private Color borderColor;
        private Color gradientStart;
        private Color gradientEnd;

        RoundedPanel(final int radius) {
            this.cornerRadius = radius;
            setOpaque(false);
        }

        void setFillColor(final Color color) {
            this.fillColor = color;
            repaint();
        }

        void setBorderColor(final Color color) {
            this.borderColor = color;
            repaint();
        }

        void setGradient(final Color start, final Color end) {
            this.gradientStart = start;
            this.gradientEnd = end;
            repaint();
        }

        @Override
        protected void paintComponent(final Graphics g) {
            final Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            final int width = getWidth();
            final int height = getHeight();
            if (gradientStart != null && gradientEnd != null) {
                g2.setPaint(new GradientPaint(0, 0, gradientStart, width, height, gradientEnd));
            } else if (fillColor != null) {
                g2.setColor(fillColor);
            } else {
                g2.setColor(getBackground());
            }
            g2.fillRoundRect(0, 0, Math.max(0, width - 1), Math.max(0, height - 1),
                    cornerRadius, cornerRadius);

            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.drawRoundRect(0, 0, Math.max(0, width - 1), Math.max(0, height - 1),
                        cornerRadius, cornerRadius);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
