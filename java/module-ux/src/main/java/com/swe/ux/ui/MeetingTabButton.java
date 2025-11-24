package com.swe.ux.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JButton;

import com.swe.ux.theme.Theme;
import com.swe.ux.theme.ThemeManager;

/**
 * Minimal pill-style tab button that integrates with ThemeManager.
 */
public class MeetingTabButton extends JButton {

    private static final long serialVersionUID = 1L;
    private boolean selected;
    private Color accentColor = new Color(82, 140, 255);

    public MeetingTabButton(String text) {
        super(text);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setFont(FontUtil.getJetBrainsMono(13.5f, Font.BOLD));
        setPreferredSize(new Dimension(134, 42));
        setMinimumSize(new Dimension(96, 38));
    }

    public void setSelectedTab(boolean selected) {
        if (this.selected != selected) {
            this.selected = selected;
            repaint();
        }
    }

    public void setAccentColor(Color accent) {
        if (accent != null) {
            this.accentColor = accent;
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Theme theme = ThemeManager.getInstance().getCurrentTheme();
        Color base = mix(theme.getBackgroundColor(), theme.getPanelBorder(), 0.85f);
        Color textColor = theme.getTextColor();

        if (selected) {
            g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 50));
            g2.fillRoundRect(4, 6, getWidth() - 8, getHeight() - 8, 16, 16);
        } else if (getModel().isRollover()) {
            g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 80));
            g2.fillRoundRect(4, 6, getWidth() - 8, getHeight() - 8, 16, 16);
        }

        super.paintComponent(g2);

        g2.dispose();
    }

    private Color mix(Color c1, Color c2, float ratio) {
        float inv = 1f - ratio;
        int r = (int) (c1.getRed() * ratio + c2.getRed() * inv);
        int g = (int) (c1.getGreen() * ratio + c2.getGreen() * inv);
        int b = (int) (c1.getBlue() * ratio + c2.getBlue() * inv);
        return new Color(r, g, b);
    }
}
