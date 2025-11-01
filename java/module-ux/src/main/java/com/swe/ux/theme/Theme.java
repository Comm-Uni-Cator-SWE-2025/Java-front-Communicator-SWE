package com.swe.ux.theme;

import java.awt.Color;

public interface Theme {
    // Core colors
    Color getBackground();
    Color getForeground();
    Color getText();
    Color getPrimary();
    Color getAccent();
    boolean isDark();
    
    // Alias methods for better readability
    default Color getBackgroundColor() {
        return getBackground();
    }
    
    default Color getTextColor() {
        return getText();
    }
    
    default Color getPrimaryColor() {
        return getPrimary();
    }
    
    default Color getAccentColor() {
        return getAccent();
    }
    
    // Additional theming support
    default Color getPanelBackground() {
        return isDark() ? getBackground().darker() : getBackground().brighter();
    }
    
    default Color getPanelBorder() {
        return isDark() ? getBackground().brighter() : getBackground().darker();
    }
    
    default Color getInputBackgroundColor() {
        return isDark() ? getBackground().brighter() : Color.WHITE;
    }
}

