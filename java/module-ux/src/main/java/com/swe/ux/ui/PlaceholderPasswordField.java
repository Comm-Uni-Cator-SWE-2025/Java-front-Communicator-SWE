package com.swe.ux.ui;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;

import com.swe.ux.theme.Theme;
import com.swe.ux.theme.ThemeManager;

/**
 * Password field with placeholder text support.
 */
public class PlaceholderPasswordField extends JPasswordField {
    /**
     * Font size for the password field.
     */
    private static final int FONT_SIZE = 15;
    /**
     * Vertical border padding.
     */
    private static final int BORDER_VERTICAL = 10;
    /**
     * Horizontal border padding.
     */
    private static final int BORDER_HORIZONTAL = 20;
    /**
     * Corner radius for rounded borders.
     */
    private static final int CORNER_RADIUS = 10;
    /**
     * Vertical offset divisor for text positioning.
     */
    private static final int VERTICAL_OFFSET_DIVISOR = 2;

    /**
     * The placeholder text.
     */
    private final String placeholder;

    /**
     * Creates a new placeholder password field.
     *
     * @param placeholderText the placeholder text
     */
    public PlaceholderPasswordField(final String placeholderText) {
        this.placeholder = placeholderText;
        setFont(new Font("SansSerif", Font.PLAIN, FONT_SIZE));
        setBorder(new EmptyBorder(BORDER_VERTICAL, BORDER_HORIZONTAL,
                BORDER_VERTICAL, BORDER_HORIZONTAL));
        setOpaque(false);
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(final FocusEvent e) {
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(final Graphics g) {
        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw rounded background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS);

        super.paintComponent(g);

        if (getPassword().length == 0 && !isFocusOwner()) {
            g2.setColor(getDisabledTextColor());
            g2.setFont(getFont().deriveFont(Font.PLAIN));
            final int y = (getHeight() - g2.getFontMetrics().getHeight()) / VERTICAL_OFFSET_DIVISOR
                    + g2.getFontMetrics().getAscent();
            g2.drawString(placeholder, getInsets().left, y);
        }
        g2.dispose();
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (ThemeManager.getInstance() != null) {
            final Theme theme = ThemeManager.getInstance().getCurrentTheme();
            setDisabledTextColor(theme.getText().darker());
            setBackground(theme.getForeground());
            setForeground(theme.getText());
            setCaretColor(theme.getText());
            repaint();
        }
    }
}
