package com.swe.ux.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import com.swe.ux.theme.Theme;
import com.swe.ux.theme.ThemeManager;

/**
 * Lightweight card panel with macOS-style rounded corners.
 * No blur, zero heavy CPU work.
 */
public class SoftCardPanel extends JPanel {

    private int cornerRadius = 24;
    private Color fillColor;
    private Color outlineColor;

    public SoftCardPanel() {
        setOpaque(false);
        setLayout(new BorderLayout());
        updateThemeColors();
    }

    public SoftCardPanel(int padding) {
        this();
        setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    private void updateThemeColors() {
        Theme theme = ThemeManager.getInstance().getCurrentTheme();

        if (theme.isDark()) {
            fillColor = new Color(255, 255, 255, 25);   // subtle translucent white
            outlineColor = new Color(255, 255, 255, 40);
        } else {
            fillColor = new Color(255, 255, 255, 210);  // nice macOS card white
            outlineColor = new Color(0, 0, 0, 30);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        updateThemeColors();

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        Shape shape = new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius);

        g2.setColor(fillColor);
        g2.fill(shape);

        g2.setColor(outlineColor);
        g2.setStroke(new BasicStroke(1.4f));
        g2.draw(shape);

        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public void updateUI() {
        super.updateUI();
        updateThemeColors();
        repaint();
    }
}
