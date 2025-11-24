package com.swe.ux.ui;

import javax.swing.JPanel;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

import com.swe.ux.theme.Theme;
import com.swe.ux.theme.ThemeManager;

/**
 * Lightweight card panel with macOS-style rounded corners.
 * No blur, zero heavy CPU work.
 */
public class SoftCardPanel extends JPanel {
    /**
     * Default corner radius for rounded rectangle.
     */
    private static final int DEFAULT_CORNER_RADIUS = 24;
    /**
     * Default color red component.
     */
    private static final int DEFAULT_COLOR_R = 36;
    /**
     * Default color green component.
     */
    private static final int DEFAULT_COLOR_G = 42;
    /**
     * Default color blue component.
     */
    private static final int DEFAULT_COLOR_B = 56;
    /**
     * Alpha value for light theme.
     */
    private static final int ALPHA_LIGHT = 255;
    /**
     * Alpha value for dark theme.
     */
    private static final int ALPHA_DARK = 245;
    /**
     * Outline alpha for light theme.
     */
    private static final int OUTLINE_ALPHA_LIGHT = 140;
    /**
     * Outline alpha for dark theme.
     */
    private static final int OUTLINE_ALPHA_DARK = 110;
    /**
     * Default outline alpha value.
     */
    private static final int OUTLINE_ALPHA_DEFAULT = 26;
    /**
     * Maximum RGB value.
     */
    private static final int RGB_MAX = 255;

    /**
     * Corner radius for rounded rectangle.
     */
    private int cornerRadius = DEFAULT_CORNER_RADIUS;
    /**
     * Fill color.
     */
    private Color fillColor;
    /**
     * Outline color.
     */
    private Color outlineColor;

    /**
     * Creates a new soft card panel.
     */
    public SoftCardPanel() {
        setOpaque(false);
        setLayout(new BorderLayout());
        updateThemeColors();
    }

    /**
     * Creates a new soft card panel with padding.
     *
     * @param padding the padding amount
     */
    public SoftCardPanel(final int padding) {
        this();
        setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
    }

    /**
     * Sets the corner radius.
     *
     * @param radius the corner radius
     */
    public void setCornerRadius(final int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    private void updateThemeColors() {
        final Theme theme = ThemeManager.getInstance().getCurrentTheme();

        if (theme == null) {
            fillColor = new Color(DEFAULT_COLOR_R, DEFAULT_COLOR_G, DEFAULT_COLOR_B);
            outlineColor = new Color(RGB_MAX, RGB_MAX, RGB_MAX, OUTLINE_ALPHA_DEFAULT);
            return;
        }

        final Color panelBase = theme.getPanelBackground();
        final int alpha;
        if (theme.isDark()) {
            alpha = ALPHA_DARK;
        } else {
            alpha = ALPHA_LIGHT;
        }
        fillColor = new Color(panelBase.getRed(), panelBase.getGreen(),
                panelBase.getBlue(), alpha);

        final Color borderSource = theme.getPanelBorder();
        final int outlineAlpha;
        if (theme.isDark()) {
            outlineAlpha = OUTLINE_ALPHA_DARK;
        } else {
            outlineAlpha = OUTLINE_ALPHA_LIGHT;
        }
        outlineColor = new Color(
                borderSource.getRed(),
                borderSource.getGreen(),
                borderSource.getBlue(),
                outlineAlpha
        );
    }

    @Override
    protected void paintComponent(final Graphics g) {
        updateThemeColors();

        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final int w = getWidth();
        final int h = getHeight();

        final Shape shape = new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius);

        g2.setColor(fillColor);
        g2.fill(shape);

        if (outlineColor != null) {
            g2.setColor(outlineColor);
            g2.draw(shape);
        }

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
