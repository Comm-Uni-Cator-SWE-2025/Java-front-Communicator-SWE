/*
 * -----------------------------------------------------------------------------
 *  File: PlaceHolderTextField.java
 *  Owner: Vaibhav Yadav
 *  Roll Number : 142201015
 *  Module : UX
 *
 * -----------------------------------------------------------------------------
 */


package com.swe.ux.ui;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.swe.ux.theme.Theme;
import com.swe.ux.theme.ThemeManager;

public class PlaceholderTextField extends JTextField {

    private final String placeholder;

    public PlaceholderTextField(String placeholder) {
        this.placeholder = placeholder;

        // Use your project’s JetBrains Mono font
        setFont(FontUtil.getJetBrainsMono(15f, Font.PLAIN));

        // Let MainPage set padding if needed
        setBorder(new EmptyBorder(10, 16, 10, 16));
        setOpaque(false);

        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) { repaint(); }
            @Override
            public void focusLost(FocusEvent e) { repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Frosted-like rounded input background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

        super.paintComponent(g);

        // Placeholder logic
        if (getText().isEmpty() && !isFocusOwner()) {
            g2.setColor(getDisabledTextColor()); // macOS style subtle grey
            g2.setFont(getFont().deriveFont(Font.PLAIN));

            int y = (getHeight() - g2.getFontMetrics().getHeight()) / 2 
                  + g2.getFontMetrics().getAscent();

            g2.drawString(placeholder, getInsets().left, y);
        }

        g2.dispose();
    }

    @Override
    public void updateUI() {
        super.updateUI();

        ThemeManager tm = ThemeManager.getInstance();
        if (tm != null) {
            Theme theme = tm.getCurrentTheme();

            setDisabledTextColor(theme.getTextColor().darker());
            setBackground(theme.getInputBackgroundColor());
            setForeground(theme.getTextColor());
            setCaretColor(theme.getTextColor());
        }
    }
}
