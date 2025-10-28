package com.conferencing.theme;

import java.awt.Color;

public class DarkTheme implements Theme {
    @Override
    public Color getBackground() {
        return new Color(0x2B2B2B); // Very dark grey
    }

    @Override
    public Color getForeground() {
        return new Color(0x3C3F41); // Dark grey
    }

    @Override
    public Color getText() {
        return Color.WHITE; // Pure white for maximum visibility
    }

    @Override
    public Color getPrimary() {
        return new Color(0x4A86E8); // A nice blue
    }
    
    @Override
    public Color getAccent() {
        return new Color(0xE53935); // Red for hang up
    }
    
    @Override
    public boolean isDark() {
        return true;
    }
}

