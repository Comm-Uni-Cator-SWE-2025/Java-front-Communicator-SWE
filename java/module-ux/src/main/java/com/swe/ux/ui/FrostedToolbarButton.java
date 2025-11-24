package com.swe.ux.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JButton;

import com.swe.ux.theme.ThemeManager;

/**
 * Toolbar button with frosted glass effect.
 */
public class FrostedToolbarButton extends JButton {
    /**
     * Font size.
     */
    private static final float FONT_SIZE = 15f;
    /**
     * Default alpha value.
     */
    private static final float ALPHA_DEFAULT = 0.94f;
    /**
     * Maximum alpha value.
     */
    private static final float ALPHA_MAX = 1f;
    /**
     * Minimum alpha value.
     */
    private static final float ALPHA_MIN = 0.5f;
    /**
     * Arc radius for rounded corners.
     */
    private static final int ARC_RADIUS = 14;
    /**
     * Maximum RGB value.
     */
    private static final int RGB_MAX = 255;
    /**
     * Default alpha for dark theme.
     */
    private static final int ALPHA_DARK_DEFAULT = 20;
    /**
     * Default alpha for light theme.
     */
    private static final int ALPHA_LIGHT_DEFAULT = 12;

    /**
     * Current alpha value for fade effect.
     */
    private float alpha = ALPHA_DEFAULT;
    /**
     * Custom fill color.
     */
    private Color customFill = null;
    /**
     * Default fill for light theme.
     */
    private final Color defaultFillLight = new Color(0, 0, 0, ALPHA_LIGHT_DEFAULT);
    /**
     * Default fill for dark theme.
     */
    private final Color defaultFillDark = new Color(RGB_MAX, RGB_MAX, RGB_MAX, ALPHA_DARK_DEFAULT);

    /**
     * Creates a new frosted toolbar button.
     *
     * @param text the button text
     */
    public FrostedToolbarButton(final String text) {
        super(text);

        setOpaque(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(FontUtil.getJetBrainsMono(FONT_SIZE, Font.BOLD));

        // hover fade
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(final java.awt.event.MouseEvent e) {
                setAlpha(ALPHA_MAX);
            }

            @Override
            public void mouseExited(final java.awt.event.MouseEvent e) {
                setAlpha(ALPHA_DEFAULT);
            }
        });
    }

    /**
     * Sets custom fill color.
     *
     * @param c the fill color
     */
    public void setCustomFill(final Color c) {
        this.customFill = c;
        repaint();
    }

    /**
     * Gets the custom fill color.
     *
     * @return the custom fill color
     */
    public Color getCustomFill() {
        return customFill;
    }

    /**
     * Sets the alpha for fade effect.
     *
     * @param a the alpha value
     */
    public void setAlpha(final float a) {
        this.alpha = Math.max(ALPHA_MIN, Math.min(ALPHA_MAX, a));
        repaint();
    }

    @Override
    protected void paintComponent(final Graphics g) {
        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final boolean dark = ThemeManager.getInstance().getCurrentTheme().isDark();

        // choose fill
        final Color fill;
        if (customFill != null) {
            fill = customFill;
        } else {
            if (dark) {
                fill = defaultFillDark;
            } else {
                fill = defaultFillLight;
            }
        }

        // draw frosted rect
        g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC_RADIUS, ARC_RADIUS);

        g2.dispose();

        // draw text
        super.paintComponent(g);
    }
}
