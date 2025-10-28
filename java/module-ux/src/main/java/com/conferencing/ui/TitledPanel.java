package com.conferencing.ui;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.conferencing.theme.Theme;
import com.conferencing.theme.ThemeManager;

public class TitledPanel extends JPanel {

    public TitledPanel() {
        super();
        setOpaque(false);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setLayout(new BorderLayout());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        Theme theme = ThemeManager.getInstance().getTheme();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(theme.getForeground());
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
        g2d.dispose();
    }
    
    @Override
    public void updateUI() {
        super.updateUI();
        repaint();
    }
}

