package com.swe.ux.theme;

import java.awt.Color;

/**
 * Light theme implementation with lighter color palette.
 */
public class LightTheme implements Theme {
    /**
     * Background color value.
     */
    private static final int COLOR_BACKGROUND = 0xF7F9FB;
    /**
     * Foreground color value.
     */
    private static final int COLOR_FOREGROUND = 0xFFFFFF;
    /**
     * Text color value.
     */
    private static final int COLOR_TEXT = 0x1E1E1E;
    /**
     * Primary color value.
     */
    private static final int COLOR_PRIMARY = 0x4A86E8;
    /**
     * Accent color value.
     */
    private static final int COLOR_ACCENT = 0xE53935;

    @Override
    public Color getBackground() {
        return new Color(COLOR_BACKGROUND); // very light
    }

    @Override
    public Color getForeground() {
        return new Color(COLOR_FOREGROUND);
    }

    @Override
    public Color getText() {
        return new Color(COLOR_TEXT);
    }

    @Override
    public Color getPrimary() {
        return new Color(COLOR_PRIMARY);
    }

    @Override
    public Color getAccent() {
        return new Color(COLOR_ACCENT);
    }

    @Override
    public boolean isDark() {
        return false;
    }
}
