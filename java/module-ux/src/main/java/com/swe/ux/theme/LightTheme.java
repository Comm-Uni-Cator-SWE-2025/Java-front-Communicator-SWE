/*
 * -----------------------------------------------------------------------------
 *  File: LightTheme.java
 *  Owner: Vaibhav Yadav
 *  Roll Number : 142201015
 *  Module : UX
 *
 * -----------------------------------------------------------------------------
 */

package com.swe.ux.theme;

import java.awt.Color;

public class LightTheme implements Theme {

    @Override
    public Color getBackground() { return new Color(0xF7F9FB); } // very light
    @Override
    public Color getForeground() { return new Color(0xFFFFFF); }
    @Override
    public Color getText() { return new Color(0x1E1E1E); }
    @Override
    public Color getPrimary() { return new Color(0x4A86E8); }
    @Override
    public Color getAccent() { return new Color(0xE53935); }
    @Override
    public boolean isDark() { return false; }
}
