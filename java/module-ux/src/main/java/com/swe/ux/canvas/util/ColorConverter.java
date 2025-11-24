package com.swe.ux.canvas.util;

/**
 * Converts between java.awt.Color (Data Model) and javafx.scene.paint.Color (UI).
 */
public final class ColorConverter {

    /**
     * Alpha channel scale factor (0-255 to 0.0-1.0).
     */
    private static final double ALPHA_SCALE = 255.0;

    private ColorConverter() {
        // Utility class should not be instantiated
    }

    /**
     * Convert to javafx.
     * @param awtColor Input awt color
     * @return javafx.scene.paint.Color
     */
    public static javafx.scene.paint.Color toFx(final java.awt.Color awtColor) {
        return javafx.scene.paint.Color.rgb(
                awtColor.getRed(),
                awtColor.getGreen(),
                awtColor.getBlue(),
                awtColor.getAlpha() / ALPHA_SCALE);
    }

    /**
     * Convert to awt.
     * @param fxColor Input jfx color
     * @return java.awt.Color
     */
    public static java.awt.Color toAwt(final javafx.scene.paint.Color fxColor) {
        return new java.awt.Color(
                (float) fxColor.getRed(),
                (float) fxColor.getGreen(),
                (float) fxColor.getBlue(),
                (float) fxColor.getOpacity());
    }
}


