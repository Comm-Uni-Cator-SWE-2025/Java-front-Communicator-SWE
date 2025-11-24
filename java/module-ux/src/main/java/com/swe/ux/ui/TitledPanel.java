package com.swe.ux.ui;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.swe.ux.theme.Theme;
import com.swe.ux.theme.ThemeManager;

/**
 * A titled panel with rounded borders.
 */
public class TitledPanel extends JPanel {
    /**
     * Border padding in pixels.
     */
    private static final int BORDER_PADDING = 20;
    /**
     * Border radius in pixels.
     */
    private static final int BORDER_RADIUS = 25;

    /**
     * Creates a new titled panel.
     */
    public TitledPanel() {
        super();
        setOpaque(false);
        setBorder(new EmptyBorder(BORDER_PADDING, BORDER_PADDING, BORDER_PADDING, BORDER_PADDING));
        setLayout(new BorderLayout());
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2d = (Graphics2D) g.create();
        final Theme theme = ThemeManager.getInstance().getCurrentTheme();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(theme.getForeground());
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), BORDER_RADIUS, BORDER_RADIUS);
        g2d.dispose();
    }

    @Override
    public void updateUI() {
        super.updateUI();
        repaint();
    }
}
