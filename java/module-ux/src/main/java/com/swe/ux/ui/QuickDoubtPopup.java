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
    private static final String CARD_INPUT = "input";
    private static final String CARD_SUMMARY = "summary";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);
    private final JTextArea inputArea = new JTextArea(4, 24);
    private final JLabel summaryLabel = new JLabel();
    private final JLabel timestampLabel = new JLabel();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");

    public QuickDoubtPopup() {
        setBorder(new EmptyBorder(12, 12, 12, 12));
        setLightWeightPopupEnabled(true);
        setFocusable(true);
        buildInputCard();
        buildSummaryCard();
        add(cardPanel);
        cardLayout.show(cardPanel, CARD_INPUT);

        addPopupMenuListener(new PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { reset(); }
            @Override public void popupMenuCanceled(PopupMenuEvent e) { reset(); }
        });
    }

    private void buildInputCard() {
        JPanel inputCard = new JPanel(new BorderLayout(8, 8));
        inputCard.setOpaque(false);
        JLabel title = new JLabel("Quick Doubt");
        title.setFont(title.getFont().deriveFont(title.getFont().getStyle() | Font.BOLD));
        inputCard.add(title, BorderLayout.NORTH);

        inputArea.setWrapStyleWord(true);
        inputArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(inputArea);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        inputCard.add(scrollPane, BorderLayout.CENTER);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionRow.setOpaque(false);
        JButton send = new JButton("Send");
        send.addActionListener(e -> handleSend());
        actionRow.add(send);

        inputCard.add(actionRow, BorderLayout.SOUTH);
        cardPanel.add(inputCard, CARD_INPUT);
    }

    private void buildSummaryCard() {
        JPanel summaryCard = new JPanel(new BorderLayout(6, 6));
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

    private String sanitize(String text) {
        return text.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }

    public void showAbove(Component parent) {
        if (parent == null) {
            return;
        }
        Dimension size = getPreferredSize();
        int x = (parent.getWidth() - size.width) / 2;
        int y = -size.height - 8;
        show(parent, Math.max(x, -parent.getWidth()), y);
        SwingUtilities.invokeLater(() -> inputArea.requestFocusInWindow());
    }

    public void reset() {
        inputArea.setText("");
        cardLayout.show(cardPanel, CARD_INPUT);
    }

    @Override
    public void updateUI() {
        super.updateUI();
        Theme theme = ThemeManager.getInstance().getCurrentTheme();
        if (theme != null && cardPanel != null) {
            Color bg = theme.getPanelBackground();
            cardPanel.setBackground(bg);
            setBackground(bg);
        }
    }
}
