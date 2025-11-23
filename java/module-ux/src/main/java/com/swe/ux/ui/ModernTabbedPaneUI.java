
/*
 * -----------------------------------------------------------------------------
 *  File: ModernTabbedPaneUI.java
 *  Owner: Aryan Mathur
 *  Roll Number : 122201017
 *  Module : UX
 *
 * -----------------------------------------------------------------------------
 */

package com.swe.ux.ui;

import java.awt.BasicStroke;
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
    private static final int PILL_ARC = 14;
    private static final int TAB_MIN_HEIGHT = 40;

    private Color tabSelected;
    private Color tabUnselectedFill;
    private Color tabText;
    private Font jbFont;

    @Override
    protected void installDefaults() {
        super.installDefaults();
        tabInsets = new Insets(10, 18, 10, 18);
        tabAreaInsets = new Insets(6, 6, 6, 6);
        jbFont = FontUtil.getJetBrainsMono(14f, Font.BOLD);
        updateColors();
    }

    private void updateColors() {
        var tm = ThemeManager.getInstance();
        if (tm == null || tm.getCurrentTheme() == null) {
            tabSelected = new Color(255, 255, 255);
            tabUnselectedFill = new Color(0, 0, 0, 18);
            tabText = new Color(36, 36, 36);
            return;
        }
        boolean dark = tm.getCurrentTheme().isDark();
        if (dark) {
            tabSelected = new Color(72, 84, 104, 230);
            tabUnselectedFill = new Color(255, 255, 255, 8);
            tabText = tm.getCurrentTheme().getTextColor();
        } else {
            tabSelected = new Color(255, 255, 255, 250);
            tabUnselectedFill = new Color(0, 0, 0, 12);
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
    protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
        updateColors();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Customize tab area without delegating to BasicTabbedPaneUI
        int tabCount = tabPane.getTabCount();
        Rectangle iconRect = new Rectangle();
        Rectangle textRect = new Rectangle();
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

    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                      int x, int y, int w, int h, boolean isSelected) {
        updateColors();

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int innerX = x + 6;
        int innerW = w - 12;
        int innerY = y + 6;
        int innerH = Math.max(TAB_MIN_HEIGHT - 12, h - 12);

        RoundRectangle2D pill = new RoundRectangle2D.Double(innerX, innerY, innerW, innerH, PILL_ARC, PILL_ARC);

        g2.setColor(isSelected ? tabSelected : tabUnselectedFill);
        g2.fill(pill);

        g2.dispose();
    }

    @Override
    protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics,
                             int tabIndex, String title, Rectangle textRect, boolean isSelected) {
        updateColors();
        Graphics2D g2 = (Graphics2D) g;
        g2.setFont(jbFont != null ? jbFont : font);
        g2.setColor(tabText);
        FontMetrics fm = g2.getFontMetrics();
        int textX = textRect.x;
        int textY = textRect.y + fm.getAscent();
        g2.drawString(title, textX, textY);
    }

    @Override
    protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
        return Math.max(TAB_MIN_HEIGHT, fontHeight + 24);
    }

    @Override
    protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
        return super.calculateTabWidth(tabPlacement, tabIndex, metrics) + 36;
    }

    @Override
    protected JButton createScrollButton(int direction) {
        JButton b = super.createScrollButton(direction);
        b.setOpaque(false);
        b.setFont(FontUtil.getJetBrainsMono(11f, Font.PLAIN));
        return b;
    }

    @Override
    protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex,
                                       Rectangle iconRect, Rectangle textRect, boolean isSelected) {
        // no focus indicator
    }

    @Override
    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
        // suppress default content border to keep cards clean
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        Dimension size = super.getPreferredSize(c);
        if (size == null) size = c != null ? c.getSize() : new Dimension(0, 0);
        size.height += 6;
        return size;
    }
}
