package com.swe.ux.ui;

import com.swe.ux.theme.ThemeManager;
import javax.swing.JCheckBox;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ThemeToggleButton extends JCheckBox {

    public ThemeToggleButton() {
        super();
        setPreferredSize(new Dimension(50, 25));
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setSelected(ThemeManager.getInstance().getTheme().isDark());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                ThemeManager.getInstance().toggleTheme();
                setSelected(ThemeManager.getInstance().getTheme().isDark());
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        int arc = height;
        
        // Background
        if (isSelected()) {
            g2.setColor(ThemeManager.getInstance().getTheme().getPrimary());
        } else {
            g2.setColor(Color.GRAY);
        }
        g2.fillRoundRect(0, 0, width, height, arc, arc);

        // Switch
        int switchSize = height - 4;
        g2.setColor(Color.WHITE);
        if (isSelected()) {
            g2.fillOval(width - switchSize - 2, 2, switchSize, switchSize);
        } else {
            g2.fillOval(2, 2, switchSize, switchSize);
        }

        g2.dispose();
    }
    
    @Override
    public void updateUI() {
        // Override to prevent standard look and feel from painting over our custom one
        setUI(null);
    }
}

