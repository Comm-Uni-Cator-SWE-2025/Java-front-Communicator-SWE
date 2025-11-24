package com.swe.ux.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

import com.swe.ux.theme.Theme;
import com.swe.ux.theme.ThemeManager;

/**
 * Custom styled button with primary/secondary variants.
 */
public class CustomButton extends JButton {
    /**
     * Vertical border padding.
     */
    private static final int BORDER_VERTICAL = 10;
    /**
     * Horizontal border padding.
     */
    private static final int BORDER_HORIZONTAL = 25;
    /**
     * Font size for button text.
     */
    private static final int FONT_SIZE = 14;
    /**
     * Preferred button width.
     */
    private static final int BUTTON_WIDTH = 150;
    /**
     * Preferred button height.
     */
    private static final int BUTTON_HEIGHT = 45;
    /**
     * Corner radius for rounded buttons.
     */
    private static final int CORNER_RADIUS = 15;

    /**
     * Whether this is a primary button.
     */
    private boolean primary;

    /**
     * Sets the primary style.
     *
     * @param isPrimary true for primary style
     */
    public void setPrimary(final boolean isPrimary) {
        this.primary = isPrimary;
        applyTheme();
    }

    /**
     * Creates a new custom button.
     *
     * @param text the button text
     * @param isPrimary true for primary style
     */
    public CustomButton(final String text, final boolean isPrimary) {
        super(text);
        this.primary = isPrimary;
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorder(new EmptyBorder(BORDER_VERTICAL, BORDER_HORIZONTAL,
                BORDER_VERTICAL, BORDER_HORIZONTAL));
        setFont(new Font("SansSerif", Font.BOLD, FONT_SIZE));
        setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(final Graphics g) {
        final Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (getModel().isPressed()) {
            g2d.setColor(getBackground().darker());
        } else if (getModel().isRollover()) {
            g2d.setColor(getBackground().brighter());
        } else {
            g2d.setColor(getBackground());
        }

        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS);

        super.paintComponent(g);
        g2d.dispose();
    }

    private void applyTheme() {
        final Theme theme = ThemeManager.getInstance().getCurrentTheme();
        if (primary) {
            setBackground(theme.getPrimary());
            setForeground(Color.WHITE);
        } else {
            setBackground(theme.getForeground());
            setForeground(theme.getText());
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();
        // The theme manager will handle updates, but we can apply on creation
        if (ThemeManager.getInstance() != null) {
            applyTheme();
            repaint();
        }
    }
}
