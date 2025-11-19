package com.swe.ux.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

import com.swe.ux.theme.ThemeManager;

/**
 * Tiny toggle button to switch between light/dark theme.
 */
public class ThemeToggleButton extends JComponent {

    private boolean hovered = false;

    public ThemeToggleButton() {
        setPreferredSize(new Dimension(56, 28));
        setToolTipText("Toggle theme");
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
            @Override public void mouseClicked(MouseEvent e) { ThemeManager.getInstance().toggleTheme(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean dark = ThemeManager.getInstance().getCurrentTheme().isDark();
        int w = getWidth(), h = getHeight();
        int arc = h;

        // background
        g2.setColor(dark ? new java.awt.Color(255,255,255,14) : new java.awt.Color(0,0,0,10));
        g2.fillRoundRect(0, 0, w, h, arc, arc);

        // knob
        int knobSize = h - 8;
        int x = dark ? w - knobSize - 4 : 4;
        int y = 4;
        g2.setColor(dark ? new java.awt.Color(255,255,255,200) : new java.awt.Color(255,255,255,220));
        g2.fillOval(x, y, knobSize, knobSize);

        g2.dispose();
    }
}
