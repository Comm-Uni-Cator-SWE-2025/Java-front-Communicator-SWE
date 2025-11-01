package com.swe.ux.theme;

import java.awt.Color;
import java.awt.Font;

public interface Theme {
    Color getBackground();
    Color getForeground();
    Color getAccentColor();
    Color getTextColor();
    Color getButtonColor();
    Color getButtonTextColor();
    Color getPanelBackground();
    Color getPanelBorder();
    Font getTitleFont();
    Font getNormalFont();
    Font getSmallFont();
    Color getPrimary();
    Color getText();
    Color getAccent();
    boolean isDark();
}
