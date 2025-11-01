package com.swe.ux.ui;

import com.swe.ux.theme.ThemeManager;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class PlaceholderTextField extends JTextField {
    private String placeholder;
    private final ThemeManager themeManager;

    public PlaceholderTextField() {
        this("", 0);
    }

    public PlaceholderTextField(String placeholder) {
        this(placeholder, 0);
    }

    public PlaceholderTextField(int columns) {
        this("", columns);
    }

    public PlaceholderTextField(String placeholder, int columns) {
        super(columns);
        this.themeManager = ThemeManager.getInstance();
        setPlaceholder(placeholder);
        init();
    }

    public PlaceholderTextField(String text, String placeholder) {
        super(text);
        this.themeManager = ThemeManager.getInstance();
        this.placeholder = placeholder;
        init();
    }

    public PlaceholderTextField(int columns, String placeholder) {
        super(columns);
        this.themeManager = ThemeManager.getInstance();
        this.placeholder = placeholder;
        init();
    }

    public PlaceholderTextField(String text, int columns, String placeholder) {
        super(text, columns);
        this.themeManager = ThemeManager.getInstance();
        this.placeholder = placeholder;
        init();
    }

    public PlaceholderTextField(Document doc, String text, int columns, String placeholder) {
        super(doc, text, columns);
        this.themeManager = ThemeManager.getInstance();
        this.placeholder = placeholder;
        init();
    }

    private void init() {
        // Set padding and border
        Border border = getBorder();
        Border empty = new EmptyBorder(5, 10, 5, 10);
        setBorder(new CompoundBorder(border, empty));
        
        // Set colors and font
        setBackground(themeManager.getPanelBackground());
        setForeground(themeManager.getTextColor());
        setCaretColor(themeManager.getTextColor());
        setFont(themeManager.getNormalFont());
        
        // Add focus listener to handle placeholder
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (getText().equals(placeholder)) {
                    setText("");
                    setForeground(themeManager.getTextColor());
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (getText().isEmpty()) {
                    setText(placeholder);
                    setForeground(themeManager.getTextColor().darker());
                }
            }
        });
        
        // Initial state
        if (getText().isEmpty() && placeholder != null) {
            setText(placeholder);
            setForeground(themeManager.getTextColor().darker());
        }
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        if (getText().isEmpty()) {
            setText(placeholder);
            setForeground(themeManager.getTextColor().darker());
        }
    }

    @Override
    public String getText() {
        String text = super.getText();
        return text.equals(placeholder) ? "" : text;
    }

    public void setText(String text, boolean isPlaceholder) {
        if (isPlaceholder) {
            this.placeholder = text;
            setText(text);
            setForeground(themeManager.getTextColor().darker());
        } else {
            setText(text);
        }
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        if (text == null || text.isEmpty()) {
            setForeground(themeManager.getTextColor().darker());
        } else {
            setForeground(themeManager.getTextColor());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (getText().isEmpty() && !(FocusManager.getCurrentKeyboardFocusManager().getFocusOwner() == this)) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(themeManager.getTextColor().darker());
            g2.setFont(getFont().deriveFont(Font.ITALIC));
            
            // Calculate text position
            FontMetrics fm = g2.getFontMetrics();
            int x = getInsets().left;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            
            g2.drawString(placeholder, x, y);
            g2.dispose();
        }
    }
}
