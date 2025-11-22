package com.swe.ux.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Point;
import java.awt.RenderingHints;
import javax.swing.JPanel;

import com.swe.ux.theme.Theme;
import com.swe.ux.theme.ThemeManager;

/**
 * Root background panel that paints a tasteful gradient + subtle noise tint.
 */
public class FrostedBackgroundPanel extends JPanel {

    public FrostedBackgroundPanel() {
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Theme theme = ThemeManager.getInstance().getCurrentTheme();
        Color base = theme != null ? theme.getBackgroundColor() : new Color(0xF2F2F2);
        Color accent = theme != null ? theme.getPrimaryColor() : new Color(0x4A86E8);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Dimension d = getSize();
        Point p1 = new Point(0, 0);
        Point p2 = new Point(d.width, d.height);
        float[] fractions = {0f, 1f};
        Color start = blend(base, accent, 0.06f);
        Color end = blend(base, accent, 0.02f);
        LinearGradientPaint lg = new LinearGradientPaint(p1, p2, fractions, new Color[]{start, end}, CycleMethod.NO_CYCLE);
        g2.setPaint(lg);
        g2.fillRect(0, 0, d.width, d.height);

        // subtle vignette
        g2.setColor(new Color(0, 0, 0, theme != null && theme.isDark() ? 12 : 6));
        g2.fillRect(0, 0, d.width, d.height);

        g2.dispose();
    }

    private Color blend(Color a, Color b, float ratio) {
        float ir = 1.0f - ratio;
        int r = (int) (a.getRed() * ir + b.getRed() * ratio);
        int g = (int) (a.getGreen() * ir + b.getGreen() * ratio);
        int bl = (int) (a.getBlue() * ir + b.getBlue() * ratio);
        int alpha = (int) (a.getAlpha() * ir + b.getAlpha() * ratio);
        return new Color(r, g, bl, alpha);
    }

    @Override
    public void updateUI() {
        super.updateUI();
        repaint();
    }
}
