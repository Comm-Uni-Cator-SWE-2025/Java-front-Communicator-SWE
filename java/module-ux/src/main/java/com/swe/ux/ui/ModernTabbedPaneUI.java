package com.swe.ux.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import com.swe.ux.theme.ThemeManager;

/**
 * Modern tabbed pane UI - rounded segmented control appearance for tab bars.
 * Theme-aware and uses JetBrains Mono sizing.
 */
public class ModernTabbedPaneUI extends BasicTabbedPaneUI {
    /**
     * Arc radius for pill shape.
     */
    private static final int PILL_ARC = 14;
    /**
     * Minimum tab height.
     */
    private static final int TAB_MIN_HEIGHT = 40;
    /**
     * Tab inset top.
     */
    private static final int TAB_INSET_TOP = 10;
    /**
     * Tab inset left.
     */
    private static final int TAB_INSET_LEFT = 18;
    /**
     * Tab inset bottom.
     */
    private static final int TAB_INSET_BOTTOM = 10;
    /**
     * Tab inset right.
     */
    private static final int TAB_INSET_RIGHT = 18;
    /**
     * Tab area inset.
     */
    private static final int TAB_AREA_INSET = 6;
    /**
     * Font size for tabs.
     */
    private static final float FONT_SIZE = 14f;
    /**
     * Maximum RGB value.
     */
    private static final int RGB_MAX = 255;
    /**
     * Default alpha value.
     */
    private static final int DEFAULT_ALPHA = 18;
    /**
     * Default color value.
     */
    private static final int DEFAULT_COLOR = 36;
    /**
     * Dark selected red component.
     */
    private static final int DARK_SELECTED_R = 72;
    /**
     * Dark selected green component.
     */
    private static final int DARK_SELECTED_G = 84;
    /**
     * Dark selected blue component.
     */
    private static final int DARK_SELECTED_B = 104;
    /**
     * Dark selected alpha value.
     */
    private static final int DARK_SELECTED_ALPHA = 230;
    /**
     * Dark unselected alpha value.
     */
    private static final int DARK_UNSELECTED_ALPHA = 8;
    /**
     * Light selected alpha value.
     */
    private static final int LIGHT_SELECTED_ALPHA = 250;
    /**
     * Light unselected alpha value.
     */
    private static final int LIGHT_UNSELECTED_ALPHA = 12;
    /**
     * Tab inner offset.
     */
    private static final int TAB_INNER_OFFSET = 6;
    /**
     * Tab width extra padding.
     */
    private static final int TAB_WIDTH_EXTRA = 36;
    /**
     * Tab height extra padding.
     */
    private static final int TAB_HEIGHT_EXTRA = 24;
    /**
     * Tab height additional padding.
     */
    private static final int TAB_HEIGHT_ADD = 6;
    /**
     * Scroll button font size.
     */
    private static final float SCROLL_BUTTON_FONT_SIZE = 11f;

    /**
     * Selected tab color.
     */
    private Color tabSelected;
    /**
     * Unselected tab fill color.
     */
    private Color tabUnselectedFill;
    /**
     * Tab text color.
     */
    private Color tabText;
    /**
     * JetBrains Mono font.
     */
    private Font jbFont;

    @Override
    protected void installDefaults() {
        super.installDefaults();
        tabInsets = new Insets(TAB_INSET_TOP, TAB_INSET_LEFT, TAB_INSET_BOTTOM, TAB_INSET_RIGHT);
        tabAreaInsets = new Insets(TAB_AREA_INSET, TAB_AREA_INSET, TAB_AREA_INSET, TAB_AREA_INSET);
        jbFont = FontUtil.getJetBrainsMono(FONT_SIZE, Font.BOLD);
        updateColors();
    }

    private void updateColors() {
        final ThemeManager tm = ThemeManager.getInstance();
        if (tm == null || tm.getCurrentTheme() == null) {
            tabSelected = new Color(RGB_MAX, RGB_MAX, RGB_MAX);
            tabUnselectedFill = new Color(0, 0, 0, DEFAULT_ALPHA);
            tabText = new Color(DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR);
            return;
        }
        final boolean dark = tm.getCurrentTheme().isDark();
        if (dark) {
            tabSelected = new Color(DARK_SELECTED_R, DARK_SELECTED_G, DARK_SELECTED_B,
                    DARK_SELECTED_ALPHA);
            tabUnselectedFill = new Color(RGB_MAX, RGB_MAX, RGB_MAX, DARK_UNSELECTED_ALPHA);
            tabText = tm.getCurrentTheme().getTextColor();
        } else {
            tabSelected = new Color(RGB_MAX, RGB_MAX, RGB_MAX, LIGHT_SELECTED_ALPHA);
            tabUnselectedFill = new Color(0, 0, 0, LIGHT_UNSELECTED_ALPHA);
            tabText = tm.getCurrentTheme().getTextColor();
        }
    }

    @Override
    protected void installComponents() {
        super.installComponents();
        if (tabPane != null) {
            tabPane.setOpaque(false);
            tabPane.setFont(jbFont);
        }
    }

    @Override
    protected void paintTabArea(final Graphics g, final int tabPlacement,
                                final int selectedIndex) {
        updateColors();
        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Customize tab area without delegating to BasicTabbedPaneUI
        final int tabCount = tabPane.getTabCount();
        final Rectangle iconRect = new Rectangle();
        final Rectangle textRect = new Rectangle();
        for (int i = 0; i < tabCount; i++) {
            if (i != selectedIndex) {
                paintTab(g2, tabPlacement, rects, i, iconRect, textRect);
            }
        }

        if (selectedIndex >= 0) {
            paintTab(g2, tabPlacement, rects, selectedIndex, iconRect, textRect);
        }

        g2.dispose();
    }

    // CHECKSTYLE:OFF: ParameterNumber - Override method must match parent signature
    @Override
    protected void paintTabBackground(final Graphics g, final int tabPlacement,
                                       final int tabIndex, final int x, final int y,
                                       final int w, final int h, final boolean isSelected) {
        // CHECKSTYLE:ON: ParameterNumber
        updateColors();

        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final int innerX = x + TAB_INNER_OFFSET;
        final int innerW = w - TAB_INNER_OFFSET * 2;
        final int innerY = y + TAB_INNER_OFFSET;
        final int innerH = Math.max(TAB_MIN_HEIGHT - TAB_INNER_OFFSET * 2,
                h - TAB_INNER_OFFSET * 2);

        final RoundRectangle2D pill = new RoundRectangle2D.Double(innerX, innerY, innerW, innerH,
                PILL_ARC, PILL_ARC);

        final Color fillColor;
        if (isSelected) {
            fillColor = tabSelected;
        } else {
            fillColor = tabUnselectedFill;
        }
        g2.setColor(fillColor);
        g2.fill(pill);

        g2.dispose();
    }

    // CHECKSTYLE:OFF: ParameterNumber - Override method must match parent signature
    @Override
    protected void paintText(final Graphics g, final int tabPlacement, final Font font,
                             final FontMetrics metrics, final int tabIndex, final String title,
                             final Rectangle textRect, final boolean isSelected) {
        // CHECKSTYLE:ON: ParameterNumber
        updateColors();
        final Graphics2D g2 = (Graphics2D) g;
        final Font textFont;
        if (jbFont != null) {
            textFont = jbFont;
        } else {
            textFont = font;
        }
        g2.setFont(textFont);
        g2.setColor(tabText);
        final FontMetrics fm = g2.getFontMetrics();
        final int textX = textRect.x;
        final int textY = textRect.y + fm.getAscent();
        g2.drawString(title, textX, textY);
    }

    @Override
    protected int calculateTabHeight(final int tabPlacement, final int tabIndex,
                                     final int fontHeight) {
        return Math.max(TAB_MIN_HEIGHT, fontHeight + TAB_HEIGHT_EXTRA);
    }

    @Override
    protected int calculateTabWidth(final int tabPlacement, final int tabIndex,
                                    final FontMetrics metrics) {
        return super.calculateTabWidth(tabPlacement, tabIndex, metrics) + TAB_WIDTH_EXTRA;
    }

    @Override
    protected JButton createScrollButton(final int direction) {
        final JButton b = super.createScrollButton(direction);
        b.setOpaque(false);
        b.setFont(FontUtil.getJetBrainsMono(SCROLL_BUTTON_FONT_SIZE, Font.PLAIN));
        return b;
    }

    @Override
    protected void paintFocusIndicator(final Graphics g, final int tabPlacement,
                                       final Rectangle[] rects, final int tabIndex,
                                       final Rectangle iconRect, final Rectangle textRect,
                                       final boolean isSelected) {
        // no focus indicator
    }

    @Override
    protected void paintContentBorder(final Graphics g, final int tabPlacement,
                                      final int selectedIndex) {
        // suppress default content border to keep cards clean
    }

    @Override
    public Dimension getPreferredSize(final JComponent c) {
        Dimension size = super.getPreferredSize(c);
        if (size == null) {
            if (c != null) {
                size = c.getSize();
            } else {
                size = new Dimension(0, 0);
            }
        }
        size.height += TAB_HEIGHT_ADD;
        return size;
    }
}
