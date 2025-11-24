package com.swe.ux.ui;

import java.awt.Font;
import java.io.InputStream;

/**
 * Utility to load JetBrains Mono from resources, or fall back to system fonts.
 *
 * <p>Put JetBrainsMono-Regular.ttf in resources/fonts/ if you want the bundled font.
 */
public class FontUtil {
    /**
     * Cached JetBrains Mono font.
     */
    private static Font jetbrainsMono = null;

    /**
     * Gets the JetBrains Mono font with specified size and style.
     *
     * @param size the font size
     * @param style the font style
     * @return the font
     */
    public static synchronized Font getJetBrainsMono(final float size, final int style) {
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
                final Font f = Font.createFont(Font.TRUETYPE_FONT, is);
                return f;
            }
        } catch (Exception ignored) {
            // Font loading failed, will use fallback
        }
        return null;
    }
}
