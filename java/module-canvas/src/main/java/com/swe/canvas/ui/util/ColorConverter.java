package com.swe.canvas.ui.util;

/**
 * Converts between java.awt.Color (Data Model) and javafx.scene.paint.Color (UI).
 */
public class ColorConverter {
    public static javafx.scene.paint.Color toFx(java.awt.Color awtColor) {
        return javafx.scene.paint.Color.rgb(
                awtColor.getRed(),
                awtColor.getGreen(),
                awtColor.getBlue(),
                awtColor.getAlpha() / 255.0);
    }

    public static java.awt.Color toAwt(javafx.scene.paint.Color fxColor) {
        return new java.awt.Color(
                (float) fxColor.getRed(),
                (float) fxColor.getGreen(),
                (float) fxColor.getBlue(),
                (float) fxColor.getOpacity());
    }
}