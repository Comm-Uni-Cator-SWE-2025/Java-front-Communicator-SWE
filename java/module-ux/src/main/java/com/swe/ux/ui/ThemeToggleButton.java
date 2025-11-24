package com.swe.ux.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

import com.swe.ux.theme.ThemeManager;

/**
 * Tiny toggle button to switch between light/dark theme.
 */
public class ThemeToggleButton extends JComponent {
    /**
     * Button width.
     */
    private static final int BUTTON_WIDTH = 44;
    /**
     * Button height.
     */
    private static final int BUTTON_HEIGHT = 24;
    /**
     * Border padding.
     */
    private static final int BORDER_PADDING = 2;
    /**
     * Inset value.
     */
    private static final int INSET = 1;
    /**
     * Double inset value.
     */
    private static final int INSET_DOUBLE = 2;
    /**
     * Padding for knob.
     */
    private static final int KNOB_PADDING = 3;
    /**
     * Alpha value for light background.
     */
    private static final int ALPHA_LIGHT_BG = 20;
    /**
     * Alpha value for dark background.
     */
    private static final int ALPHA_DARK_BG = 30;
    /**
     * Alpha value for knob.
     */
    private static final int ALPHA_KNOB = 240;
    /**
     * Maximum RGB value.
     */
    private static final int RGB_MAX = 255;

    /**
     * Whether mouse is hovering over the button.
     */
    private boolean hovered = false;

    /**
     * Creates a new theme toggle button.
     */
    public ThemeToggleButton() {
        setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        setMinimumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        setMaximumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        setToolTipText("Toggle theme");
        // Add some internal padding/margin to prevent clipping
        setBorder(javax.swing.BorderFactory.createEmptyBorder(BORDER_PADDING,
                BORDER_PADDING, BORDER_PADDING, BORDER_PADDING));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(final MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(final MouseEvent e) {
                hovered = false;
                repaint();
            }

            @Override
            public void mouseClicked(final MouseEvent e) {
                ThemeManager.getInstance().toggleTheme();
            }
        });
    }

    @Override
    protected void paintComponent(final Graphics g) {
        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final boolean dark = ThemeManager.getInstance().getCurrentTheme().isDark();
        final int w = getWidth();
        final int h = getHeight();

        // Account for border padding - draw slightly inset to prevent clipping
        final int drawX = INSET;
        final int drawY = INSET;
        final int drawW = w - (INSET * INSET_DOUBLE);
        final int drawH = h - (INSET * INSET_DOUBLE);

        // Background: solid enough to be seen, but translucent
        if (dark) {
            g2.setColor(new java.awt.Color(RGB_MAX, RGB_MAX, RGB_MAX, ALPHA_DARK_BG));
        } else {
            g2.setColor(new java.awt.Color(0, 0, 0, ALPHA_LIGHT_BG));
        }
        g2.fillRoundRect(drawX, drawY, drawW, drawH, drawH, drawH);

        // Knob with proper padding from edges
        final int knobSize = drawH - (KNOB_PADDING * INSET_DOUBLE);
        final int x;
        if (dark) {
            x = drawX + drawW - knobSize - KNOB_PADDING;
        } else {
            x = drawX + KNOB_PADDING;
        }
        final int y = drawY + KNOB_PADDING;

        // Knob color - solid white for visibility
        if (dark) {
            g2.setColor(new java.awt.Color(RGB_MAX, RGB_MAX, RGB_MAX, ALPHA_KNOB));
        } else {
            g2.setColor(java.awt.Color.WHITE);
        }
        g2.fillOval(x, y, knobSize, knobSize);

        g2.dispose();
    }
}
