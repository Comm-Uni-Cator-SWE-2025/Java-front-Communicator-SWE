package com.swe.ux.canvas;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for CanvasApp FXML and CSS resource loading.
 * Tests verify that the application can properly load required resources.
 */
class CanvasAppTest {

    @Test
    void testFxmlResourceExists() {
        // Test that FXML resource is accessible on classpath
        java.net.URL fxmlUrl = CanvasApp.class.getResource("/fxml/canvas-view.fxml");
        assertNotNull(fxmlUrl, "FXML resource /fxml/canvas-view.fxml should exist on classpath");
    }

    @Test
    void testCssResourceExists() {
        // Test that CSS resource is accessible on classpath
        java.net.URL cssUrl = CanvasApp.class.getResource("/css/canvas-view.css");
        assertNotNull(cssUrl, "CSS resource /css/canvas-view.css should exist on classpath");
    }

    @Test
    void testFxmlResourceWithClassLoader() {
        // Test alternative classloader approach
        java.net.URL fxmlUrl = ClassLoader.getSystemClassLoader().getResource("fxml/canvas-view.fxml");
        if (fxmlUrl == null) {
            fxmlUrl = Thread.currentThread().getContextClassLoader().getResource("fxml/canvas-view.fxml");
        }
        assertNotNull(fxmlUrl, "FXML should be loadable via classloader");
    }

    @Test
    void testCssResourceWithClassLoader() {
        // Test alternative classloader approach
        java.net.URL cssUrl = ClassLoader.getSystemClassLoader().getResource("css/canvas-view.css");
        if (cssUrl == null) {
            cssUrl = Thread.currentThread().getContextClassLoader().getResource("css/canvas-view.css");
        }
        assertNotNull(cssUrl, "CSS should be loadable via classloader");
    }

    @Test
    void testFxmlResourceIsReadable() throws Exception {
        // Test that FXML file can be opened and read
        java.net.URL fxmlUrl = CanvasApp.class.getResource("/fxml/canvas-view.fxml");
        assertNotNull(fxmlUrl);
        
        try (java.io.InputStream stream = fxmlUrl.openStream()) {
            assertNotNull(stream, "FXML resource should be readable");
            assertTrue(stream.available() > 0, "FXML file should have content");
        }
    }

    @Test
    void testCssResourceIsReadable() throws Exception {
        // Test that CSS file can be opened and read
        java.net.URL cssUrl = CanvasApp.class.getResource("/css/canvas-view.css");
        assertNotNull(cssUrl);
        
        try (java.io.InputStream stream = cssUrl.openStream()) {
            assertNotNull(stream, "CSS resource should be readable");
            assertTrue(stream.available() > 0, "CSS file should have content");
        }
    }

    @Test
    void testFxmlContainsBorderPane() throws Exception {
        // Verify FXML contains expected root element
        java.net.URL fxmlUrl = CanvasApp.class.getResource("/fxml/canvas-view.fxml");
        assertNotNull(fxmlUrl);
        
        String content = new String(fxmlUrl.openStream().readAllBytes());
        assertTrue(content.contains("BorderPane"), "FXML should contain BorderPane root element");
        assertTrue(content.contains("fx:controller"), "FXML should specify controller");
    }

    @Test
    void testFxmlContainsCanvasController() throws Exception {
        // Verify FXML references correct controller class
        java.net.URL fxmlUrl = CanvasApp.class.getResource("/fxml/canvas-view.fxml");
        assertNotNull(fxmlUrl);
        
        String content = new String(fxmlUrl.openStream().readAllBytes());
        assertTrue(content.contains("CanvasController"), "FXML should reference CanvasController");
    }

    @Test
    void testCssContainsToolBarStyles() throws Exception {
        // Verify CSS contains expected style definitions
        java.net.URL cssUrl = CanvasApp.class.getResource("/css/canvas-view.css");
        assertNotNull(cssUrl);
        
        String content = new String(cssUrl.openStream().readAllBytes());
        assertTrue(content.contains("tool-bar") || content.contains(".tool"), 
                   "CSS should contain toolbar styling");
    }

    @Test
    void testResourcePathsAreAbsolute() {
        // Verify resources use absolute paths (starting with /)
        java.net.URL fxmlUrl = CanvasApp.class.getResource("/fxml/canvas-view.fxml");
        java.net.URL cssUrl = CanvasApp.class.getResource("/css/canvas-view.css");
        
        assertNotNull(fxmlUrl, "Absolute FXML path should work");
        assertNotNull(cssUrl, "Absolute CSS path should work");
        
        // Verify the URLs have proper structure
        assertTrue(fxmlUrl.toString().endsWith("canvas-view.fxml"), 
                   "FXML URL should end with correct filename");
        assertTrue(cssUrl.toString().endsWith("canvas-view.css"), 
                   "CSS URL should end with correct filename");
    }
}
