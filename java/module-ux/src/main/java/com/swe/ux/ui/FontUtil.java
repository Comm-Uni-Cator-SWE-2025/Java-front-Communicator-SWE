
/*
 * -----------------------------------------------------------------------------
 *  File: FontUtil.java
 *  Owner: Aryan Mathur
 *  Roll Number : 122201017
 *  Module : UX
 *
 * -----------------------------------------------------------------------------
 */
package com.swe.ux.ui;

import java.awt.Font;
import java.io.InputStream;

/**
 * Utility to load JetBrains Mono from resources, or fall back to system fonts.
 *
 * Put JetBrainsMono-Regular.ttf in resources/fonts/ if you want the bundled font.
 */
public class FontUtil {

    private static Font jetbrainsMono = null;

    public static synchronized Font getJetBrainsMono(float size, int style) {
        if (jetbrainsMono == null) {
            jetbrainsMono = tryLoadFromResources();
            if (jetbrainsMono == null) {
                // fallback to system monospaced or SansSerif if mono not found
                try {
                    jetbrainsMono = new Font("JetBrains Mono", style, (int) size);
                } catch (Exception e) {
                    jetbrainsMono = new Font("Monospaced", style, (int) size);
                }
            }
        }
        return jetbrainsMono.deriveFont(style, size);
    }

    private static Font tryLoadFromResources() {
        try (InputStream is = FontUtil.class.getResourceAsStream("/fonts/JetBrainsMono-Regular.ttf")) {
            if (is != null) {
                Font f = Font.createFont(Font.TRUETYPE_FONT, is);
                return f;
            }
        } catch (Exception ignored) {}
        return null;
    }
}
