package com.swe.ux.theme;

import java.awt.Color;

/**
 * Interface defining theme colors and properties.
 */
public interface Theme {

    Color getBackground();

    Color getForeground();

    Color getText();

    Color getPrimary();

    Color getAccent();

    boolean isDark();

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

    /**
     * Gets the panel background color.
     *
     * @return the panel background color
     */
    default Color getPanelBackground() {
        if (isDark()) {
            return getBackground().darker();
        } else {
            return getBackground().brighter();
        }
    }

    /**
     * Gets the panel border color.
     *
     * @return the panel border color
     */
    default Color getPanelBorder() {
        if (isDark()) {
            return getBackground().brighter();
        } else {
            return getBackground().darker();
        }
    }

    /**
     * Gets the input background color.
     *
     * @return the input background color
     */
    default Color getInputBackgroundColor() {
        if (isDark()) {
            return getBackground().brighter();
        } else {
            return Color.WHITE;
        }
    }
}
