package com.swe.ux.ui;

import com.swe.ux.theme.ThemeManager;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class PlaceholderPasswordField extends JPasswordField {
    private String placeholder;
    private final ThemeManager themeManager;
    private boolean isPasswordVisible = false;
    private JToggleButton toggleButton;

    public PlaceholderPasswordField() {
        this("");
    }

    public PlaceholderPasswordField(String placeholder) {
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
        
        // Create toggle button for showing/hiding password
        toggleButton = new JToggleButton();
        toggleButton.setPreferredSize(new Dimension(30, 20));
        toggleButton.setFocusable(false);
        toggleButton.setBorderPainted(false);
        toggleButton.setContentAreaFilled(false);
        toggleButton.setOpaque(false);
        updateToggleButtonIcon();
        
        toggleButton.addActionListener(e -> {
            isPasswordVisible = toggleButton.isSelected();
            updateToggleButtonIcon();
            setEchoChar(isPasswordVisible ? (char) 0 : '•');
        });
        
        // Add focus listener to handle placeholder
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (String.valueOf(getPassword()).equals(placeholder)) {
                    setText("");
                    setEchoChar('•');
                    setForeground(themeManager.getTextColor());
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (getPassword().length == 0) {
                    setText(placeholder);
                    setEchoChar((char) 0);
                    setForeground(themeManager.getTextColor().darker());
                }
            }
        });
        
        // Initial state
        if (getPassword().length == 0 && placeholder != null) {
            setText(placeholder);
            setEchoChar((char) 0);
            setForeground(themeManager.getTextColor().darker());
        } else {
            setEchoChar('•');
        }
    }

    private void updateToggleButtonIcon() {
        if (isPasswordVisible) {
            toggleButton.setIcon(UIManager.getIcon("FileChooser.detailsViewIcon"));
            toggleButton.setToolTipText("Hide password");
        } else {
            toggleButton.setIcon(UIManager.getIcon("FileChooser.homeFolderIcon"));
            toggleButton.setToolTipText("Show password");
        }
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        if (getPassword().length == 0) {
            setText(placeholder);
            setEchoChar((char) 0);
            setForeground(themeManager.getTextColor().darker());
        }
    }

    @Override
    public char[] getPassword() {
        char[] password = super.getPassword();
        return String.valueOf(password).equals(placeholder) ? new char[0] : password;
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
        
        if (getPassword().length == 0 && !(FocusManager.getCurrentKeyboardFocusManager().getFocusOwner() == this)) {
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
