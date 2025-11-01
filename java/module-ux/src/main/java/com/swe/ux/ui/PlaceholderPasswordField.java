package com.swe.ux.ui;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;

import com.swe.ux.theme.Theme;
import com.swe.ux.theme.ThemeManager;

public class PlaceholderPasswordField extends JPasswordField {

    private final String placeholder;

    public PlaceholderPasswordField(String placeholder) {
        this.placeholder = placeholder;
        setFont(new Font("SansSerif", Font.PLAIN, 15));
        setBorder(new EmptyBorder(10, 20, 10, 20));
        setOpaque(false);
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }
            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw rounded background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        
        super.paintComponent(g);

        if (getPassword().length == 0 && !isFocusOwner()) {
            g2.setColor(getDisabledTextColor());
            g2.setFont(getFont().deriveFont(Font.PLAIN));
            int y = (getHeight() - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
            g2.drawString(placeholder, getInsets().left, y);
        }
        g2.dispose();
    }
    
    @Override
    public void updateUI() {
        super.updateUI();
        if (ThemeManager.getInstance() != null) {
            Theme theme = ThemeManager.getInstance().getTheme();
            setDisabledTextColor(theme.getText().darker());
            setBackground(theme.getForeground());
            setForeground(theme.getText());
            setCaretColor(theme.getText());
            repaint();
        }
    }
}

