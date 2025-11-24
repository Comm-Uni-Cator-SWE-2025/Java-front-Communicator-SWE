package com.swe.ux.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import com.swe.ux.theme.ThemeManager;

/**
 * Small rounded badge used on login card.
 */
public class FrostedBadgeLabel extends JLabel {
    /**
     * Font size.
     */
    private static final float FONT_SIZE = 12f;
    /**
     * Vertical border padding.
     */
    private static final int BORDER_VERTICAL = 6;
    /**
     * Horizontal border padding.
     */
    private static final int BORDER_HORIZONTAL = 12;
    /**
     * Width padding.
     */
    private static final int WIDTH_PADDING = 8;
    /**
     * Minimum height.
     */
    private static final int MIN_HEIGHT = 28;
    /**
     * Alpha for light theme.
     */
    private static final int ALPHA_LIGHT = 18;
    /**
     * Alpha for dark theme.
     */
    private static final int ALPHA_DARK = 8;
    /**
     * Border offset.
     */
    private static final int BORDER_OFFSET = 2;
    /**
     * Maximum RGB value.
     */
    private static final int RGB_MAX = 255;

    /**
     * Creates a new frosted badge label.
     *
     * @param text the label text
     */
    public FrostedBadgeLabel(final String text) {
        super(text);
        setOpaque(false);
        setBorder(new EmptyBorder(BORDER_VERTICAL, BORDER_HORIZONTAL,
                BORDER_VERTICAL, BORDER_HORIZONTAL));
        setFont(FontUtil.getJetBrainsMono(FONT_SIZE, Font.PLAIN));
    }

    @Override
    protected void paintComponent(final Graphics g) {
        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final java.awt.Color bg;
        if (ThemeManager.getInstance().getCurrentTheme().isDark()) {
            bg = new java.awt.Color(RGB_MAX, RGB_MAX, RGB_MAX, ALPHA_LIGHT);
        } else {
            bg = new java.awt.Color(0, 0, 0, ALPHA_DARK);
        }

        final int w = getWidth();
        final int h = getHeight();
        final int r = h - BORDER_OFFSET;
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, w, h, r, r);

        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public Dimension getPreferredSize() {
        final Dimension d = super.getPreferredSize();
        d.width += WIDTH_PADDING;
        d.height = Math.max(d.height, MIN_HEIGHT);
        return d;
    }
}
