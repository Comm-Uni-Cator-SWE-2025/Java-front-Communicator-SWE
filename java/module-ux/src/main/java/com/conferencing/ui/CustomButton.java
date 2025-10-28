package com.conferencing.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

import com.conferencing.theme.Theme;
import com.conferencing.theme.ThemeManager;

public class CustomButton extends JButton {
    private boolean primary;

    public CustomButton(String text, boolean primary) {
        super(text);
        this.primary = primary;
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorder(new EmptyBorder(10, 25, 10, 25));
        setFont(new Font("SansSerif", Font.BOLD, 14));
        setPreferredSize(new Dimension(150, 45));
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (getModel().isPressed()) {
            g2d.setColor(getBackground().darker());
        } else if (getModel().isRollover()) {
            g2d.setColor(getBackground().brighter());
        } else {
            g2d.setColor(getBackground());
        }
        
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
        
        super.paintComponent(g);
        g2d.dispose();
    }

    private void applyTheme() {
        Theme theme = ThemeManager.getInstance().getTheme();
        if (primary) {
            setBackground(theme.getPrimary());
            setForeground(Color.WHITE);
        } else {
            setBackground(theme.getForeground());
            setForeground(theme.getText());
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();
        // The theme manager will handle updates, but we can apply on creation
        if (ThemeManager.getInstance() != null) {
            applyTheme();
            repaint();
        }
    }
}

