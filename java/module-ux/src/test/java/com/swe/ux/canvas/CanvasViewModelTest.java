package com.swe.ux.canvas;

import com.swe.canvas.datamodel.action.ActionFactory;
import com.swe.canvas.datamodel.canvas.CanvasState;
import com.swe.canvas.datamodel.canvas.ShapeState;
import com.swe.canvas.datamodel.manager.ActionManager;
import com.swe.canvas.datamodel.manager.UndoRedoManager;
import com.swe.canvas.datamodel.shape.Shape;
import com.swe.canvas.datamodel.shape.ShapeFactory;
import com.swe.canvas.datamodel.shape.ShapeId;
import com.swe.canvas.datamodel.shape.ShapeType;
import com.swe.ux.viewmodels.CanvasViewModel;
import com.swe.ux.viewmodels.ToolType;
import javafx.embed.swing.JFXPanel; // init toolkit
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests CanvasViewModel core interactions: create, modify color/thickness,
 * delete, drag.
 * NOTE: Uses a stubbed ActionManager to capture requests without network.
 */
class CanvasViewModelTest {

    static class StubActionManager implements ActionManager {
        final CanvasState canvasState = new CanvasState();
        final ActionFactory factory = new ActionFactory();
        final UndoRedoManager undoRedo = new UndoRedoManager();
        Runnable onUpdate = () -> {
        };
        int createCalls, modifyCalls, deleteCalls;
        int undoCalls, redoCalls;

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
        public void processIncomingMessage(com.swe.canvas.datamodel.collaboration.NetworkMessage message) {
            /* no-op */ }
    }

    private StubActionManager actionManager;
    private CanvasViewModel viewModel;
    private static final ShapeFactory SHAPE_FACTORY = new ShapeFactory();

    @BeforeAll
    static void initFx() {
        new JFXPanel();
    }

    @BeforeEach
    void setUp() {
        actionManager = new StubActionManager();
        viewModel = new CanvasViewModel("u", actionManager);
    }

    @Test
    void createShape_addsShape() {
        viewModel.activeTool.set(ToolType.RECTANGLE);
        viewModel.onMousePressed(10, 10);
        viewModel.onMouseDragged(50, 50);
        viewModel.onMouseReleased(50, 50);
        assertEquals(1, actionManager.createCalls);
        assertEquals(1, actionManager.getCanvasState().getVisibleShapes().size());
    }

    @Test
    void modifySelectedShapeColor() {
        Shape s = SHAPE_FACTORY.createShape(ShapeType.LINE, ShapeId.randomId(), null, 2.0, java.awt.Color.BLACK, "u");
        actionManager.requestCreate(s);
        viewModel.selectedShapeId.set(s.getShapeId());
        viewModel.updateSelectedShapeColor(Color.RED);
        assertEquals(1, actionManager.modifyCalls);
        ShapeState st = actionManager.getCanvasState().getShapeState(s.getShapeId());
        assertNotNull(st);
        assertEquals(java.awt.Color.RED, st.getShape().getColor());
    }

    @Test
    void modifySelectedShapeThickness() {
        Shape s = SHAPE_FACTORY.createShape(ShapeType.RECTANGLE, ShapeId.randomId(), null, 2.0, java.awt.Color.BLACK,
                "u");
        actionManager.requestCreate(s);
        viewModel.selectedShapeId.set(s.getShapeId());
        viewModel.updateSelectedShapeThickness(7.5);
        assertEquals(1, actionManager.modifyCalls);
        ShapeState st = actionManager.getCanvasState().getShapeState(s.getShapeId());
        assertEquals(7.5, st.getShape().getThickness());
    }

    @Test
    void deleteSelectedShape_removesIt() {
        Shape s = SHAPE_FACTORY.createShape(ShapeType.ELLIPSE, ShapeId.randomId(), null, 3.0, java.awt.Color.BLACK,
                "u");
        actionManager.requestCreate(s);
        viewModel.selectedShapeId.set(s.getShapeId());
        viewModel.deleteSelectedShape();
        assertEquals(1, actionManager.deleteCalls);
        ShapeState st = actionManager.getCanvasState().getShapeState(s.getShapeId());
        assertTrue(st.isDeleted());
        assertNull(viewModel.selectedShapeId.get(), "Selection cleared after delete");
    }

    @Test
    void dragSelectedShape_updatesPosition() {
        Shape s = SHAPE_FACTORY.createShape(ShapeType.RECTANGLE, ShapeId.randomId(), null, 2.0, java.awt.Color.BLACK,
                "u");
        actionManager.requestCreate(s);
        viewModel.activeTool.set(ToolType.SELECT);
        viewModel.onMousePressed(15, 15); // miss -> no selection
        viewModel.selectedShapeId.set(s.getShapeId()); // force select
        // Start drag
        viewModel.onMousePressed(0, 0);
        viewModel.isDraggingSelection = true; // emulate hit-test success
        viewModel.onMouseDragged(10, 10);
        viewModel.onMouseReleased(10, 10);
        assertEquals(1, actionManager.modifyCalls, "Drag should invoke modify");
    }
}
