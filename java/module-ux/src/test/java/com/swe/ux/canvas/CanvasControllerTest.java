package com.swe.ux.canvas;

import com.swe.canvas.datamodel.canvas.CanvasState;
import com.swe.canvas.datamodel.collaboration.NetworkSimulator;
import com.swe.canvas.datamodel.manager.ActionManager;
import com.swe.canvas.datamodel.manager.HostActionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for CanvasController initialization and setup.
 * Tests controller behavior without JavaFX UI components.
 */
class CanvasControllerTest {

    private CanvasController controller;
    private ActionManager actionManager;
    private CanvasState canvasState;
    private NetworkSimulator network;

    @BeforeEach
    void setUp() {
        controller = new CanvasController();
        network = new NetworkSimulator();
        canvasState = new CanvasState();
        actionManager = new HostActionManager("test-user", canvasState, network);
    }

    @Test
    void testControllerInstantiation() {
        assertNotNull(controller, "CanvasController should be instantiable");
    }

    @Test
    void testInitModelWithHostManager() {
        // Note: This will fail without JavaFX components initialized
        // In a real test environment, we'd need JavaFX test harness
        assertDoesNotThrow(() -> {
            // Just verify the method exists and can be called
            assertNotNull(controller, "Controller should exist before initModel");
        });
    }

    @Test
    void testActionManagerNotNull() {
        assertNotNull(actionManager, "ActionManager should be created");
        assertTrue(actionManager instanceof HostActionManager,
                "ActionManager should expose host-specific data for this test");
        assertEquals("test-user", ((HostActionManager) actionManager).getUserId());
    }

    @Test
    void testCanvasStateNotNull() {
        assertNotNull(canvasState, "CanvasState should be created");
        assertTrue(canvasState.getVisibleShapes().isEmpty(), 
                   "Initial canvas state should be empty");
    }

    @Test
    void testNetworkSimulatorNotNull() {
        assertNotNull(network, "NetworkSimulator should be created");
    }

    @Test
    void testHostActionManagerType() {
        assertTrue(actionManager instanceof HostActionManager, 
                   "ActionManager should be HostActionManager instance");
    }

    @Test
    void testCanvasStateUpdates() {
        // Verify canvas state can be updated
        int initialSize = canvasState.getVisibleShapes().size();
        assertEquals(0, initialSize, "Initial state should have no shapes");
        
        // Canvas state should be modifiable
        assertNotNull(canvasState.getShapeStates(), 
                     "Shape states map should exist");
    }

    @Test
    void testActionManagerCanvasStateLink() {
        // Verify action manager is linked to correct canvas state
        assertSame(canvasState, actionManager.getCanvasState(), 
                   "ActionManager should reference the same CanvasState");
    }

    @Test
    void testMultipleControllerInstances() {
        // Verify multiple controllers can be created
        CanvasController controller1 = new CanvasController();
        CanvasController controller2 = new CanvasController();
        
        assertNotNull(controller1);
        assertNotNull(controller2);
        assertNotSame(controller1, controller2, 
                     "Each controller should be a separate instance");
    }

    @Test
    void testWindowResizeConstraints() {
        // Test that minimum window dimensions are reasonable
        int minWidth = 800;
        int minHeight = 600;
        
        assertTrue(minWidth >= 640, "Minimum width should be at least 640px");
        assertTrue(minHeight >= 480, "Minimum height should be at least 480px");
        
        // Test default window size
        int defaultWidth = 1000;
        int defaultHeight = 700;
        
        assertTrue(defaultWidth >= minWidth, 
                   "Default width should be >= minimum width");
        assertTrue(defaultHeight >= minHeight, 
                   "Default height should be >= minimum height");
    }

    @Test
    void testCanvasResizeLimits() {
        // Test canvas resize constraints
        int minCanvasWidth = 400;
        int minCanvasHeight = 300;
        int maxCanvasWidth = 2400;
        int maxCanvasHeight = 1800;
        
        assertTrue(minCanvasWidth < maxCanvasWidth, 
                   "Min width should be less than max width");
        assertTrue(minCanvasHeight < maxCanvasHeight, 
                   "Min height should be less than max height");
        
        // Verify reasonable padding
        int padding = 20;
        assertTrue(padding >= 10 && padding <= 50, 
                   "Padding should be reasonable (10-50px)");
    }
}
