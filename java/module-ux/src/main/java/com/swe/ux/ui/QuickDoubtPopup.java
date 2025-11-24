package com.swe.ux.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.swe.ux.theme.Theme;
import com.swe.ux.theme.ThemeManager;

/**
 * Lightweight floating popup used for quick doubt capture.
 */
public class QuickDoubtPopup extends JPopupMenu {
    private static final long serialVersionUID = 1L;
    /**
     * Card name for input view.
     */
    private static final String CARD_INPUT = "input";
    /**
     * Card name for summary view.
     */
    private static final String CARD_SUMMARY = "summary";
    /**
     * Border size in pixels.
     */
    private static final int BORDER_SIZE = 12;
    /**
     * Number of rows for text area.
     */
    private static final int TEXT_ROWS = 4;
    /**
     * Number of columns for text area.
     */
    private static final int TEXT_COLS = 24;
    /**
     * Layout spacing in pixels.
     */
    private static final int LAYOUT_SPACING = 8;
    /**
     * Small layout spacing in pixels.
     */
    private static final int LAYOUT_SPACING_SMALL = 6;
    /**
     * Y offset in pixels.
     */
    private static final int Y_OFFSET = -8;
    /**
     * Divisor for calculations.
     */
    private static final int DIVISOR = 2;

    /**
     * Card layout for switching between input and summary.
     */
    private final CardLayout cardLayout = new CardLayout();
    /**
     * Main card panel.
     */
    private final JPanel cardPanel = new JPanel(cardLayout);
    /**
     * Text area for input.
     */
    private final JTextArea inputArea = new JTextArea(TEXT_ROWS, TEXT_COLS);
    /**
     * Label showing summary.
     */
    private final JLabel summaryLabel = new JLabel();
    /**
     * Label showing timestamp.
     */
    private final JLabel timestampLabel = new JLabel();
    /**
     * Date time formatter.
     */
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");

    /**
     * Creates a new quick doubt popup.
     */
    public QuickDoubtPopup() {
        setBorder(new EmptyBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE));
        setLightWeightPopupEnabled(true);
        setFocusable(true);
        buildInputCard();
        buildSummaryCard();
        add(cardPanel);
        cardLayout.show(cardPanel, CARD_INPUT);

        addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
                // No action needed
            }

            @Override
            public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
                reset();
            }

            @Override
            public void popupMenuCanceled(final PopupMenuEvent e) {
                reset();
            }
        });
    }

    private void buildInputCard() {
        final JPanel inputCard = new JPanel(new BorderLayout(LAYOUT_SPACING, LAYOUT_SPACING));
        inputCard.setOpaque(false);
        final JLabel title = new JLabel("Quick Doubt");
        title.setFont(title.getFont().deriveFont(title.getFont().getStyle() | Font.BOLD));
        inputCard.add(title, BorderLayout.NORTH);

        inputArea.setWrapStyleWord(true);
        inputArea.setLineWrap(true);
        final JScrollPane scrollPane = new JScrollPane(inputArea);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        inputCard.add(scrollPane, BorderLayout.CENTER);

        final JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, LAYOUT_SPACING, 0));
        actionRow.setOpaque(false);
        final JButton send = new JButton("Send");
        send.addActionListener(e -> handleSend());
        actionRow.add(send);

        inputCard.add(actionRow, BorderLayout.SOUTH);
        cardPanel.add(inputCard, CARD_INPUT);
    }

    private void buildSummaryCard() {
        final JPanel summaryCard = new JPanel(new BorderLayout(LAYOUT_SPACING_SMALL,
                LAYOUT_SPACING_SMALL));
        summaryCard.setOpaque(false);
        summaryLabel.setOpaque(false);
        timestampLabel.setOpaque(false);
        summaryCard.add(summaryLabel, BorderLayout.CENTER);
        summaryCard.add(timestampLabel, BorderLayout.SOUTH);
        cardPanel.add(summaryCard, CARD_SUMMARY);
    }

    private void handleSend() {
        if (inputArea.getText().trim().isEmpty()) {
            return;
        }
        summaryLabel.setText("<html><b>Sent:</b> " + sanitize(inputArea.getText().trim()) + "</html>");
        timestampLabel.setText("at " + formatter.format(LocalDateTime.now()));
        cardLayout.show(cardPanel, CARD_SUMMARY);
    }

    private String sanitize(final String text) {
        return text.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }

    /**
     * Shows the popup above a parent component.
     *
     * @param parent the parent component
     */
    public void showAbove(final Component parent) {
        if (parent == null) {
            return;
        }
        final Dimension size = getPreferredSize();
        final int x = (parent.getWidth() - size.width) / DIVISOR;
        final int y = -size.height + Y_OFFSET;
        show(parent, Math.max(x, -parent.getWidth()), y);
        SwingUtilities.invokeLater(() -> inputArea.requestFocusInWindow());
    }

    /**
     * Resets the popup to initial state.
     */
    public void reset() {
        inputArea.setText("");
        cardLayout.show(cardPanel, CARD_INPUT);
    }

    @Override
    public void updateUI() {
        super.updateUI();
        final Theme theme = ThemeManager.getInstance().getCurrentTheme();
        if (theme != null && cardPanel != null) {
            final Color bg = theme.getPanelBackground();
            cardPanel.setBackground(bg);
            setBackground(bg);
        }
    }
}
