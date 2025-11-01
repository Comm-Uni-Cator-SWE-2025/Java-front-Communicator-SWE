package com.swe.ux.ui;

import com.swe.ux.theme.Theme;
import com.swe.ux.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

public class CustomButton extends JButton {
    private boolean isHovered = false;
    private boolean isPressed = false;
    private final boolean isPrimary;
    private final ThemeManager themeManager;

    public CustomButton(String text, boolean isPrimary) {
        super(text);
        this.isPrimary = isPrimary;
        this.themeManager = ThemeManager.getInstance();
        
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setForeground(themeManager.getButtonTextColor());
        setFont(themeManager.getNormalFont());
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                isPressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        Theme theme = themeManager.getTheme();
        int width = getWidth();
        int height = getHeight();
        
        // Draw background
        if (isPrimary) {
            if (isPressed) {
                g2.setColor(theme.getAccentColor().darker());
            } else if (isHovered) {
                g2.setColor(theme.getAccentColor().brighter());
            } else {
                g2.setColor(theme.getButtonColor());
            }
        } else {
            if (isPressed) {
                g2.setColor(theme.getPanelBackground().darker());
            } else if (isHovered) {
                g2.setColor(theme.getPanelBackground().brighter());
            } else {
                g2.setColor(theme.getPanelBackground());
            }
        }
        
        g2.fillRoundRect(0, 0, width, height, 8, 8);
        
        // Draw border
        if (isPrimary) {
            g2.setColor(theme.getAccentColor());
        } else {
            g2.setColor(theme.getPanelBorder());
        }
        g2.drawRoundRect(0, 0, width - 1, height - 1, 8, 8);
        
        // Draw text
        FontMetrics fm = g2.getFontMetrics();
        Rectangle2D rect = fm.getStringBounds(this.getText(), g2);
        int textX = (width - (int) rect.getWidth()) / 2;
        int textY = (height - (int) rect.getHeight()) / 2 + fm.getAscent();
        
        g2.setColor(theme.getTextColor());
        g2.setFont(getFont());
        g2.drawString(getText(), textX, textY);
        
        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        return new Dimension(Math.max(120, size.width + 30), 40);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }
}
