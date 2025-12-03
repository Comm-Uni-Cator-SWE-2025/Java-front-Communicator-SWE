package com.swe.ux.canvas;

import com.swe.canvas.datamodel.action.ActionFactory;
import com.swe.canvas.datamodel.canvas.CanvasState;
import com.swe.canvas.datamodel.canvas.ShapeState;
import com.swe.canvas.datamodel.collaboration.NetworkMessage;
import com.swe.canvas.datamodel.collaboration.NetworkService;
import com.swe.canvas.datamodel.manager.ActionManager;
import com.swe.canvas.datamodel.manager.HostActionManager;
import com.swe.canvas.datamodel.manager.UndoRedoManager;
import com.swe.canvas.datamodel.shape.Shape;
import com.swe.canvas.datamodel.shape.ShapeFactory;
import com.swe.canvas.datamodel.shape.ShapeId;
import com.swe.canvas.datamodel.shape.ShapeType;
import javafx.embed.swing.JFXPanel; // Ensures JavaFX toolkit initialized
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests loading of the FXML, controller field injection, and basic event
 * handler behavior.
 */
@Disabled("JavaFX FXML tests are unstable on macOS/JDK24 headless envs; disable for now")
class CanvasControllerFXMLTest {

    // Minimal NetworkService stub for HostActionManager construction.
    static class StubNetworkService implements NetworkService {
        @Override
        public void sendMessageToHost(NetworkMessage message) {
            /* no-op */ }

        @Override
        public void broadcastMessage(NetworkMessage message) {
            /* no-op */ }

        @Override
        public void sendToClient(final NetworkMessage message, final String targetClientId) {
            /* no-op */ }
    }

    /** Simple ActionManager stub (non-host) to count calls. */
    static class StubActionManager implements ActionManager {
        final CanvasState canvasState = new CanvasState();
        final ActionFactory factory = new ActionFactory();
        final UndoRedoManager undoRedo = new UndoRedoManager();
        Runnable onUpdate = () -> {
        };
        int createCalls, modifyCalls, deleteCalls, undoCalls, redoCalls;

        @Override
        public void initialize() {
            /* no-op */ }

        @Override
        public ActionFactory getActionFactory() {
            return factory;
        }

        @Override
        public CanvasState getCanvasState() {
            return canvasState;
        }

        @Override
        public UndoRedoManager getUndoRedoManager() {
            return undoRedo;
        }

        @Override
        public void setOnUpdate(Runnable callback) {
            onUpdate = callback != null ? callback : () -> {
            };
        }

        @Override
        public void requestCreate(Shape newShape) {
            createCalls++;
            canvasState.applyState(newShape.getShapeId(), new ShapeState(newShape, false, System.currentTimeMillis()));
            onUpdate.run();
        }

        @Override
        public void requestModify(ShapeState prevState, Shape modifiedShape) {
            modifyCalls++;
            canvasState.applyState(prevState.getShapeId(),
                    new ShapeState(modifiedShape, false, System.currentTimeMillis()));
            onUpdate.run();
        }

        @Override
        public void requestDelete(ShapeState shapeToDelete) {
            deleteCalls++;
            canvasState.applyState(shapeToDelete.getShapeId(),
                    new ShapeState(shapeToDelete.getShape().copy(), true, System.currentTimeMillis()));
            onUpdate.run();
        }

        @Override
        public void requestUndo() {
            undoCalls++;
        }

        @Override
        public void requestRedo() {
            redoCalls++;
        }

        @Override
        public String saveMap() {
            return "{}";
        }

        @Override
        public void restoreMap(String json) {
            /* no-op */ }

        @Override
        public void processIncomingMessage(NetworkMessage message) {
            /* no-op */ }

        @Override
        public byte[] handleUpdate(final byte[] data) {
            return new byte[0];
        }

        @Override
        public void handleUserJoined(final String userId) {
            /* no-op */ }
    }

    private static final ShapeFactory SHAPE_FACTORY = new ShapeFactory();

    @BeforeAll
    static void initJavaFx() {
        // Initialize JavaFX toolkit once.
        new JFXPanel();
    }

    private CanvasController controller;

    @BeforeEach
    void loadFXML() throws Exception {
        URL fxml = getClass().getResource("/fxml/canvas-view.fxml");
        assertNotNull(fxml, "FXML resource should exist");
        FXMLLoader loader = new FXMLLoader(fxml);
        loader.load();
        controller = loader.getController();
        assertNotNull(controller, "Controller must be instantiated by FXMLLoader");
        // We do not create a Stage here; tests interact with injected nodes directly.
    }

    @Test
    void testFxmlFieldInjection() throws Exception {
        // Use reflection to verify a few critical @FXML fields are non-null.
        ToggleButton freehandBtn = getPrivateField(controller, "freehandBtn", ToggleButton.class);
        Slider sizeSlider = getPrivateField(controller, "sizeSlider", Slider.class);
        ColorPicker colorPicker = getPrivateField(controller, "colorPicker", ColorPicker.class);
        Button deleteBtn = getPrivateField(controller, "deleteBtn", Button.class);
        assertNotNull(freehandBtn);
        assertNotNull(sizeSlider);
        assertNotNull(colorPicker);
        assertNotNull(deleteBtn);
        // Freehand button marked selected in FXML
        assertTrue(freehandBtn.isSelected(), "Freehand should be initially selected per FXML");
    }

    @Test
    void testHostButtonsVisibilityHostVsClient() {
        Button saveBtn = getPrivateField(controller, "saveBtn", Button.class);
        Button restoreBtn = getPrivateField(controller, "restoreBtn", Button.class);
        // Before initModel they are invisible (FXML sets visible="false")
        assertFalse(saveBtn.isVisible());
        assertFalse(restoreBtn.isVisible());

        // Non-host manager should keep them hidden
        StubActionManager clientManager = new StubActionManager();
        controller.initModel(clientManager);
        assertFalse(saveBtn.isVisible(), "Client should not see save button");
        assertFalse(restoreBtn.isVisible(), "Client should not see restore button");

        // Host manager toggles visibility
        HostActionManager hostManager = new HostActionManager("host", new CanvasState(), new StubNetworkService());
        controller.initModel(hostManager);
        assertTrue(saveBtn.isVisible(), "Host should see save button");
        assertTrue(restoreBtn.isVisible(), "Host should see restore button");
    }

    @Test
    void testToolSelectionClearsSelectionWhenNonSelect() {
        StubActionManager manager = new StubActionManager();
        controller.initModel(manager);
        ToggleButton rectBtn = getPrivateField(controller, "rectBtn", ToggleButton.class);
        // Simulate a selected shape existing
        Shape shape = SHAPE_FACTORY.createShape(ShapeType.RECTANGLE, ShapeId.randomId(), SHAPE_FACTORY
                .createShape(ShapeType.RECTANGLE, ShapeId.randomId(), null, 2.0, java.awt.Color.BLACK, "u").getPoints(),
                2.0, java.awt.Color.BLACK, "u");
        manager.requestCreate(shape);
        // Set selectedShapeId inside viewModel via reflection
        CanvasController canvasController = controller;
        Object viewModel = getPrivateField(canvasController, "viewModel", Object.class);
        try {
            var selectedShapeIdField = viewModel.getClass().getField("selectedShapeId");
            @SuppressWarnings("unchecked")
            var prop = (javafx.beans.property.ObjectProperty<ShapeId>) selectedShapeIdField.get(viewModel);
            prop.set(shape.getShapeId());
            assertNotNull(prop.get());
            // Fire tool selected event on RECTANGLE (non-select)
            rectBtn.fire(); // triggers onAction -> onToolSelected
            assertNull(prop.get(), "Selecting non-select tool should clear selection");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Reflection failure: " + e.getMessage());
        }
    }

    @Test
    void testDeleteButtonInvokesActionManagerDelete() {
        StubActionManager manager = new StubActionManager();
        controller.initModel(manager);
        // Create shape and select it
        Shape shape = SHAPE_FACTORY.createShape(ShapeType.LINE, ShapeId.randomId(), SHAPE_FACTORY
                .createShape(ShapeType.LINE, ShapeId.randomId(), null, 1.0, java.awt.Color.BLUE, "u").getPoints(), 1.0,
                java.awt.Color.BLUE, "u");
        manager.requestCreate(shape);
        Object vm = getPrivateField(controller, "viewModel", Object.class);
        try {
            var selectedShapeIdField = vm.getClass().getField("selectedShapeId");
            @SuppressWarnings("unchecked")
            var prop = (javafx.beans.property.ObjectProperty<ShapeId>) selectedShapeIdField.get(vm);
            prop.set(shape.getShapeId());
            Button deleteBtn = getPrivateField(controller, "deleteBtn", Button.class);
            deleteBtn.fire(); // invokes onDelete
            assertEquals(1, manager.deleteCalls, "Delete request should be forwarded to ActionManager");
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void testUndoRedoButtonsInvokeActionManager() {
        StubActionManager manager = new StubActionManager();
        controller.initModel(manager);
        Button undoBtn = getPrivateField(controller, "undoBtn", Button.class);
        Button redoBtn = getPrivateField(controller, "redoBtn", Button.class);
        undoBtn.fire();
        redoBtn.fire();
        assertEquals(1, manager.undoCalls);
        assertEquals(1, manager.redoCalls);
    }

    // Utility to access private @FXML fields injected by FXMLLoader
    private <T> T getPrivateField(Object target, String name, Class<T> type) {
        try {
            var f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            Object val = f.get(target);
            return type.cast(val);
        } catch (Exception e) {
            fail("Cannot access field " + name + ": " + e.getMessage());
            return null;
        }
    }
}
