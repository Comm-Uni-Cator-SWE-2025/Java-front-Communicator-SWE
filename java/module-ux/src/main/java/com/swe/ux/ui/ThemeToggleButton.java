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
        setPreferredSize(new Dimension(44, 24));
        setMinimumSize(new Dimension(44, 24));
        setMaximumSize(new Dimension(44, 24));
        setToolTipText("Toggle theme");
        // Add some internal padding/margin to prevent clipping
        setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
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
        
        // Account for border padding - draw slightly inset to prevent clipping
        int inset = 1; // Small inset to ensure nothing gets clipped
        int drawX = inset;
        int drawY = inset;
        int drawW = w - (inset * 2);
        int drawH = h - (inset * 2);
        
        // Background: solid enough to be seen, but translucent
        g2.setColor(dark ? new java.awt.Color(255, 255, 255, 30) : new java.awt.Color(0, 0, 0, 20));
        g2.fillRoundRect(drawX, drawY, drawW, drawH, drawH, drawH);

        // Knob with proper padding from edges
        int padding = 3;
        int knobSize = drawH - (padding * 2);
        int x = dark ? (drawX + drawW - knobSize - padding) : (drawX + padding);
        int y = drawY + padding;
        
        // Knob color - solid white for visibility
        g2.setColor(dark ? new java.awt.Color(255, 255, 255, 240) : java.awt.Color.WHITE);
        g2.fillOval(x, y, knobSize, knobSize);

        g2.dispose();
    }
}
