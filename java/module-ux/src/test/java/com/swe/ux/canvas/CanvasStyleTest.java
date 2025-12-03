package com.swe.ux.canvas;

import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic CSS verification for `canvas-view.css` ensuring critical style class is
 * applied.
 */
@Disabled("JavaFX CSS assertions crash intermittently on headless JDK 24 environments")
class CanvasStyleTest {

    @BeforeAll
    static void initFx() {
        new JFXPanel(); // initialize toolkit
    }

    @Test
    void testToolBarBackgroundColorApplied() {
        ToolBar toolBar = new ToolBar();
        toolBar.getStyleClass().add("tool-bar");
        StackPane root = new StackPane(toolBar);
        Scene scene = new Scene(root, 200, 50);
        String cssPath = getClass().getResource("/css/canvas-view.css").toExternalForm();
        scene.getStylesheets().add(cssPath);
        // Force CSS pass
        root.applyCss();
        toolBar.applyCss();
        assertNotNull(toolBar.getBackground(), "Background should be set by CSS");
        assertFalse(toolBar.getBackground().getFills().isEmpty(), "Background fills should not be empty");
        Color applied = (Color) toolBar.getBackground().getFills().get(0).getFill();
        Color expected = Color.web("#F3F4F6");
        double diff = Math.abs(applied.getRed() - expected.getRed())
                + Math.abs(applied.getGreen() - expected.getGreen())
                + Math.abs(applied.getBlue() - expected.getBlue());
        assertTrue(diff < 0.02, "Applied background color should approximately match #F3F4F6");
    }
}
