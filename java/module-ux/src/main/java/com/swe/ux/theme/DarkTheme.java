package com.swe.ux.theme;

import java.awt.Color;

/**
 * Dark theme implementation with darker color palette.
 */
public class DarkTheme implements Theme {
    /**
     * Background color value.
     */
    private static final int COLOR_BACKGROUND = 0x1B1C1F;
    /**
     * Foreground color value.
     */
    private static final int COLOR_FOREGROUND = 0x2B2B2D;
    /**
     * Text color value.
     */
    private static final int COLOR_TEXT = 0xEDEFF2;
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
        return new Color(COLOR_BACKGROUND); // dark graphite
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
        return true;
    }
}
