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
    private static final int TAB_EXTRA_H = 8;

    private Color stripBg;
    private Color tabSelected;
    private Color tabUnselectedFill;
    private Color tabText;
    private Color tabOutline;
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
            stripBg = new Color(245, 245, 247);
            tabSelected = new Color(255, 255, 255);
            tabUnselectedFill = new Color(0, 0, 0, 18);
            tabText = new Color(36, 36, 36);
            tabOutline = new Color(0, 0, 0, 24);
            return;
        }
        boolean dark = tm.getCurrentTheme().isDark();
        if (dark) {
            stripBg = new Color(36, 38, 44, 220);
            tabSelected = new Color(72, 84, 104, 230);
            tabUnselectedFill = new Color(255, 255, 255, 8);
            tabText = tm.getCurrentTheme().getTextColor();
            tabOutline = new Color(255, 255, 255, 28);
        } else {
            stripBg = new Color(248, 248, 250);
            tabSelected = new Color(255, 255, 255, 250);
            tabUnselectedFill = new Color(0, 0, 0, 12);
            tabText = tm.getCurrentTheme().getTextColor();
            tabOutline = new Color(0, 0, 0, 28);
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

        int areaW = tabPane.getWidth();
        int areaH = TAB_MIN_HEIGHT + TAB_EXTRA_H;
        // paint background strip
        g2.setColor(stripBg);
        g2.fillRoundRect(0, 0, areaW, areaH, 18, 18);

        super.paintTabArea(g2, tabPlacement, selectedIndex);
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

        if (isSelected) {
            g2.setColor(tabSelected);
            g2.fill(pill);
            // subtle outline
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(tabOutline);
            g2.draw(pill);
        } else {
            // unselected softened fill
            g2.setColor(tabUnselectedFill);
            g2.fill(pill);
        }

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
    public Dimension getPreferredSize(JComponent c) {
        Dimension size = super.getPreferredSize(c);
        if (size == null) size = c != null ? c.getSize() : new Dimension(0, 0);
        size.height += 6;
        return size;
    }
}
