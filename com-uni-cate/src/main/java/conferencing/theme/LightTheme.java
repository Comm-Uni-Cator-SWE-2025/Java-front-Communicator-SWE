package com.conferencing.theme;

import java.awt.Color;

public class LightTheme implements Theme {
    @Override
    public Color getBackground() {
        return new Color(0xFFFFFF); // White
    }

    @Override
    public Color getForeground() {
        return new Color(0xF2F2F2); // Very light grey
    }

    @Override
    public Color getText() {
        return new Color(0x333333); // Dark grey text
    }

    @Override
    public Color getPrimary() {
        return new Color(0x4A86E8); // Blue
    }
    
    @Override
    public Color getAccent() {
        return new Color(0xE53935); // Red
    }
    
    @Override
    public boolean isDark() {
        return false;
    }
}

