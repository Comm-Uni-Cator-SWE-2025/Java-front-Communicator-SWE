package com.swe.ux.theme;

import java.awt.Color;
import java.awt.Font;

public class DarkTheme implements Theme {
    private static final Color PRIMARY = new Color(98, 0, 234);
    private static final Color SECONDARY = new Color(30, 30, 30);
    private static final Color ACCENT = new Color(0, 229, 255);
    private static final Color TEXT_PRIMARY = new Color(255, 255, 255);
    private static final Color PANEL_BACKGROUND = new Color(45, 45, 45);
    private static final Color PANEL_BORDER = new Color(66, 66, 66);
    
    @Override
    public Color getBackground() {
        return SECONDARY;
    }
    
    @Override
    public Color getForeground() {
        return TEXT_PRIMARY;
    }
    
    @Override
    public Color getAccentColor() {
        return ACCENT;
    }
    
    @Override
    public Color getTextColor() {
        return TEXT_PRIMARY;
    }
    
    @Override
    public Color getButtonColor() {
        return PRIMARY;
    }
    
    @Override
    public Color getButtonTextColor() {
        return Color.WHITE;
    }
    
    @Override
    public Color getPanelBackground() {
        return PANEL_BACKGROUND;
    }
    
    @Override
    public Color getPanelBorder() {
        return PANEL_BORDER;
    }
    
    @Override
    public Font getTitleFont() {
        return new Font("Arial", Font.BOLD, 24);
    }
    
    @Override
    public Font getNormalFont() {
        return new Font("Arial", Font.PLAIN, 14);
    }
    
    @Override
    public Font getSmallFont() {
        return new Font("Arial", Font.PLAIN, 12);
    }
    
    @Override
    public Color getPrimary() {
        return PRIMARY;
    }
    
    @Override
    public Color getText() {
        return TEXT_PRIMARY;
    }
    
    @Override
    public Color getAccent() {
        return ACCENT;
    }
    
    @Override
    public boolean isDark() {
        return true;
    }
}
