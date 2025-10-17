package com.conferencing.theme;

import java.awt.Color;

public interface Theme {
    Color getBackground();
    Color getForeground();
    Color getText();
    Color getPrimary();
    Color getAccent();
    boolean isDark();
}

