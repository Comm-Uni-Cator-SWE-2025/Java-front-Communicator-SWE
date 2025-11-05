package com.swe.ux.theme;

import java.awt.Color;
import java.awt.Font;

public class LightTheme implements Theme {
    private static final Color PRIMARY = new Color(63, 81, 181);
    private static final Color SECONDARY = new Color(255, 255, 255);
    private static final Color ACCENT = new Color(255, 64, 129);
    private static final Color TEXT_PRIMARY = new Color(33, 33, 33);
    private static final Color PANEL_BACKGROUND = new Color(245, 245, 245);
    private static final Color PANEL_BORDER = new Color(224, 224, 224);
    
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
        return false;
    }
}
