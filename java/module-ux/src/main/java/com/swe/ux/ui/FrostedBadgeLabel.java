package com.swe.ux.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import com.swe.ux.theme.ThemeManager;

/**
 * Small rounded badge used on login card.
 */
public class FrostedBadgeLabel extends JLabel {

    public FrostedBadgeLabel(String text) {
        super(text);
        setOpaque(false);
        setBorder(new EmptyBorder(6, 12, 6, 12));
        setFont(FontUtil.getJetBrainsMono(12f, Font.PLAIN));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        java.awt.Color bg = ThemeManager.getInstance().getCurrentTheme().isDark() 
                ? new java.awt.Color(255,255,255,18) 
                : new java.awt.Color(0,0,0,8);

        int w = getWidth();
        int h = getHeight();
        int r = h - 2;
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, w, h, r, r);

        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width += 8;
        d.height = Math.max(d.height, 28);
        return d;
    }
}
