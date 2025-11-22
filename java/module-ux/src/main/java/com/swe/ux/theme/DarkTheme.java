package com.swe.ux.theme;

import java.awt.Color;

public class DarkTheme implements Theme {

    @Override
    public Color getBackground() { return new Color(0x1B1C1F); } // dark graphite
    @Override
    public Color getForeground() { return new Color(0x2B2B2D); }
    @Override
    public Color getText() { return new Color(0xEDEFF2); }
    @Override
    public Color getPrimary() { return new Color(0x4A86E8); }
    @Override
    public Color getAccent() { return new Color(0xE53935); }
    @Override
    public boolean isDark() { return true; }
}
