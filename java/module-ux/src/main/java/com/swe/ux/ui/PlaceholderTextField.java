package com.swe.ux.ui;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.swe.ux.theme.Theme;
import com.swe.ux.theme.ThemeManager;

/**
 * Text field with placeholder text support.
 */
public class PlaceholderTextField extends JTextField {
    /**
     * Font size for the text field.
     */
    private static final float FONT_SIZE = 15f;
    /**
     * Vertical border padding.
     */
    private static final int BORDER_VERTICAL = 10;
    /**
     * Horizontal border padding.
     */
    private static final int BORDER_HORIZONTAL = 16;
    /**
     * Corner radius for rounded borders.
     */
    private static final int CORNER_RADIUS = 12;
    /**
     * Vertical offset divisor for text positioning.
     */
    private static final int VERTICAL_OFFSET_DIVISOR = 2;

    /**
     * The placeholder text.
     */
    private final String placeholder;

    /**
     * Creates a new placeholder text field.
     *
     * @param placeholderText the placeholder text
     */
    public PlaceholderTextField(final String placeholderText) {
        this.placeholder = placeholderText;

        // Use your project's JetBrains Mono font
        setFont(FontUtil.getJetBrainsMono(FONT_SIZE, Font.PLAIN));

        // Let MainPage set padding if needed
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

        // Frosted-like rounded input background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS);

        super.paintComponent(g);

        // Placeholder logic
        if (getText().isEmpty() && !isFocusOwner()) {
            g2.setColor(getDisabledTextColor()); // macOS style subtle grey
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

        final ThemeManager tm = ThemeManager.getInstance();
        if (tm != null) {
            final Theme theme = tm.getCurrentTheme();

            setDisabledTextColor(theme.getTextColor().darker());
            setBackground(theme.getInputBackgroundColor());
            setForeground(theme.getTextColor());
            setCaretColor(theme.getTextColor());
        }
    }
}
