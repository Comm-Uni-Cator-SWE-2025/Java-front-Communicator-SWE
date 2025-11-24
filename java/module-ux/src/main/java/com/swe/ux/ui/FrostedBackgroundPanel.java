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
    /**
     * Default light color.
     */
    private static final int DEFAULT_COLOR_LIGHT = 0xF2F2F2;
    /**
     * Default accent color.
     */
    private static final int DEFAULT_COLOR_ACCENT = 0x4A86E8;
    /**
     * Blend start ratio.
     */
    private static final float BLEND_START = 0.06f;
    /**
     * Blend end ratio.
     */
    private static final float BLEND_END = 0.02f;
    /**
     * Vignette alpha for dark theme.
     */
    private static final int VIGNETTE_ALPHA_DARK = 12;
    /**
     * Vignette alpha for light theme.
     */
    private static final int VIGNETTE_ALPHA_LIGHT = 6;

    /**
     * Creates a new frosted background panel.
     */
    public FrostedBackgroundPanel() {
        setOpaque(true);
    }

    @Override
    protected void paintComponent(final Graphics g) {
        final Theme theme = ThemeManager.getInstance().getCurrentTheme();
        final Color base;
        if (theme != null) {
            base = theme.getBackgroundColor();
        } else {
            base = new Color(DEFAULT_COLOR_LIGHT);
        }
        final Color accent;
        if (theme != null) {
            accent = theme.getPrimaryColor();
        } else {
            accent = new Color(DEFAULT_COLOR_ACCENT);
        }

        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final Dimension d = getSize();
        final Point p1 = new Point(0, 0);
        final Point p2 = new Point(d.width, d.height);
        final float[] fractions = {0f, 1f};
        final Color start = blend(base, accent, BLEND_START);
        final Color end = blend(base, accent, BLEND_END);
        final LinearGradientPaint lg = new LinearGradientPaint(p1, p2, fractions,
                new Color[]{start, end}, CycleMethod.NO_CYCLE);
        g2.setPaint(lg);
        g2.fillRect(0, 0, d.width, d.height);

        // subtle vignette
        final int vignetteAlpha;
        if (theme != null && theme.isDark()) {
            vignetteAlpha = VIGNETTE_ALPHA_DARK;
        } else {
            vignetteAlpha = VIGNETTE_ALPHA_LIGHT;
        }
        g2.setColor(new Color(0, 0, 0, vignetteAlpha));
        g2.fillRect(0, 0, d.width, d.height);

        g2.dispose();
    }

    private Color blend(final Color a, final Color b, final float ratio) {
        final float ir = 1.0f - ratio;
        final int r = (int) (a.getRed() * ir + b.getRed() * ratio);
        final int g = (int) (a.getGreen() * ir + b.getGreen() * ratio);
        final int bl = (int) (a.getBlue() * ir + b.getBlue() * ratio);
        final int alpha = (int) (a.getAlpha() * ir + b.getAlpha() * ratio);
        return new Color(r, g, bl, alpha);
    }

    @Override
    public void updateUI() {
        super.updateUI();
        repaint();
    }
}
