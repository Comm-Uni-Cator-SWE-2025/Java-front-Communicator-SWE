package com.swe.ux.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JButton;

import com.swe.ux.theme.Theme;
import com.swe.ux.theme.ThemeManager;

/**
 * Minimal pill-style tab button that integrates with ThemeManager.
 */
public class MeetingTabButton extends JButton {
    private static final long serialVersionUID = 1L;
    /**
     * Font size for tab button text.
     */
    private static final float FONT_SIZE = 13.5f;
    /**
     * Preferred width for tab button.
     */
    private static final int PREFERRED_WIDTH = 134;
    /**
     * Preferred height for tab button.
     */
    private static final int PREFERRED_HEIGHT = 42;
    /**
     * Minimum width for tab button.
     */
    private static final int MIN_WIDTH = 96;
    /**
     * Minimum height for tab button.
     */
    private static final int MIN_HEIGHT = 38;
    /**
     * Default color red component.
     */
    private static final int DEFAULT_COLOR_R = 82;
    /**
     * Default color green component.
     */
    private static final int DEFAULT_COLOR_G = 140;
    /**
     * Default color blue component.
     */
    private static final int DEFAULT_COLOR_B = 255;
    /**
     * Color mixing ratio.
     */
    private static final float MIX_RATIO = 0.85f;
    /**
     * Alpha value for selected state.
     */
    private static final int ALPHA_SELECTED = 50;
    /**
     * Alpha value for hover state.
     */
    private static final int ALPHA_HOVER = 80;
    /**
     * Rectangle inset value.
     */
    private static final int RECT_INSET = 4;
    /**
     * Rectangle offset value.
     */
    private static final int RECT_OFFSET = 6;
    /**
     * Rectangle reduction value.
     */
    private static final int RECT_REDUCTION = 8;
    /**
     * Corner radius for rounded buttons.
     */
    private static final int CORNER_RADIUS = 16;

    /**
     * Whether this tab is selected.
     */
    private boolean selected;
    /**
     * Accent color for the tab.
     */
    private Color accentColor = new Color(DEFAULT_COLOR_R, DEFAULT_COLOR_G, DEFAULT_COLOR_B);

    /**
     * Creates a new meeting tab button.
     *
     * @param text the button text
     */
    public MeetingTabButton(final String text) {
        super(text);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setFont(FontUtil.getJetBrainsMono(FONT_SIZE, Font.BOLD));
        setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
    }

    /**
     * Sets the selected state of the tab.
     *
     * @param isSelected true if selected
     */
    public void setSelectedTab(final boolean isSelected) {
        if (this.selected != isSelected) {
            this.selected = isSelected;
            repaint();
        }
    }

    /**
     * Sets the accent color.
     *
     * @param accent the accent color
     */
    public void setAccentColor(final Color accent) {
        if (accent != null) {
            this.accentColor = accent;
            repaint();
        }
    }

    @Override
    protected void paintComponent(final Graphics g) {
        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final Theme theme = ThemeManager.getInstance().getCurrentTheme();
        final Color base = mix(theme.getBackgroundColor(), theme.getPanelBorder(), MIX_RATIO);

        if (selected) {
            g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(),
                    accentColor.getBlue(), ALPHA_SELECTED));
            g2.fillRoundRect(RECT_INSET, RECT_OFFSET,
                    getWidth() - RECT_REDUCTION, getHeight() - RECT_REDUCTION,
                    CORNER_RADIUS, CORNER_RADIUS);
        } else if (getModel().isRollover()) {
            g2.setColor(new Color(base.getRed(), base.getGreen(),
                    base.getBlue(), ALPHA_HOVER));
            g2.fillRoundRect(RECT_INSET, RECT_OFFSET,
                    getWidth() - RECT_REDUCTION, getHeight() - RECT_REDUCTION,
                    CORNER_RADIUS, CORNER_RADIUS);
        }

        super.paintComponent(g2);

        g2.dispose();
    }

    private Color mix(final Color c1, final Color c2, final float ratio) {
        final float inv = 1f - ratio;
        final int r = (int) (c1.getRed() * ratio + c2.getRed() * inv);
        final int g = (int) (c1.getGreen() * ratio + c2.getGreen() * inv);
        final int b = (int) (c1.getBlue() * ratio + c2.getBlue() * inv);
        return new Color(r, g, b);
    }
}
