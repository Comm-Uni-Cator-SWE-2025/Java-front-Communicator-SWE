package com.swe.canvas.mvvm;

import com.swe.canvas.datamodel.action.Action;
import com.swe.canvas.datamodel.action.ActionFactory;
import com.swe.canvas.datamodel.canvas.CanvasState;
import com.swe.canvas.datamodel.canvas.ShapeState;
import com.swe.canvas.datamodel.shape.ShapeFactory;
import com.swe.canvas.datamodel.shape.Point;
import com.swe.canvas.datamodel.shape.Shape;
import com.swe.canvas.datamodel.shape.ShapeType;
import com.swe.canvas.datamodel.shape.ShapeId;

import com.swe.canvas.ui.util.ColorConverter;
import com.swe.canvas.ui.util.GeometryUtils;
import javafx.beans.property.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

import java.util.function.Consumer;

/**
 * View model for the canvas
 * @author Bhogaraju Shanmukha Sri Krishna
 */
public class CanvasViewModel {

    private final CanvasState canvasState;
    private final StandaloneActionManager actionManager;
    private final ActionFactory actionFactory;
    private final ShapeFactory shapeFactory;
    private final String userId = "local-user";

    public final ObjectProperty<ToolType> activeTool = new SimpleObjectProperty<>(ToolType.FREEHAND);
    public final ObjectProperty<Color> activeColor = new SimpleObjectProperty<>(Color.BLACK);
    public final DoubleProperty activeStrokeWidth = new SimpleDoubleProperty(2.0);
    public final ObjectProperty<ShapeId> selectedShapeId = new SimpleObjectProperty<>(null);

    private final List<Point> currentPoints = new ArrayList<>();
    private Shape ghostShape = null;

    private double lastDragX;
    private double lastDragY;
    // Exposed so Renderer knows if we are currently moving something
    public boolean isDraggingSelection = false;

    public CanvasViewModel(final CanvasState state) {
        this.canvasState = state;
        this.actionFactory = new ActionFactory();
        this.shapeFactory = new ShapeFactory();
        this.actionManager = new StandaloneActionManager(canvasState, actionFactory, userId);
    }

    public CanvasState getCanvasState() {
        return canvasState;
    }

    public Shape getGhostShape() {
        return ghostShape;
    }

    public void setOnCanvasUpdate(final Runnable r) {
        actionManager.setOnUpdate(r);
    }

    // --- Property Updates ---
    public void updateSelectedShapeColor(final Color newFxColor) {
        updateShapeProperty(s -> s.setColor(ColorConverter.toAwt(newFxColor)));
    }

    public void updateSelectedShapeThickness(final double newThickness) {
        updateShapeProperty(s -> s.setThickness(newThickness));
    }

    private void updateShapeProperty(final Consumer<Shape> modifier) {
        final ShapeId id = selectedShapeId.get();
        if (id != null) {
            final ShapeState currentState = canvasState.getShapeState(id);
            if (currentState != null && !currentState.isDeleted()) {
                final Shape modifiedShape = currentState.getShape().copy();
                modifier.accept(modifiedShape);
                final Action action = actionFactory.createModifyAction(canvasState, id, modifiedShape, userId);
                actionManager.requestLocalAction(action);
            }
        }
    }

    // --- Input Handling ---

    /**
     * Handles mouse press event
     * @param x x coordinate
     * @param y y coordinate
     */
    public void onMousePressed(final double x, final double y) {
        lastDragX = x;
        lastDragY = y;

        if (activeTool.get() == ToolType.SELECT) {
            // 1. Find what we clicked on
            final ShapeId hitShapeId = findHitShape(x, y);

            // 2. Update selection
            selectedShapeId.set(hitShapeId);

            // 3. If we clicked a shape, prepare for immediate dragging
            if (hitShapeId != null) {
                isDraggingSelection = true;
                // Create the ghost shape immediately so we can see it move
                ghostShape = canvasState.getShapeState(hitShapeId).getShape().copy();
            } else {
                isDraggingSelection = false;
                ghostShape = null;
            }
        } else {
            // Drawing mode
            selectedShapeId.set(null);
            currentPoints.clear();
            currentPoints.add(new Point(x, y));
            currentPoints.add(new Point(x, y));
            updateGhostShape();
        }
    }

    /**
     * Handles mouse drag event
     * @param x x coordinate
     * @param y y coordinate
     */
    public void onMouseDragged(final double x, final double y) {
        if (activeTool.get() == ToolType.SELECT) {
            if (isDraggingSelection && ghostShape != null) {
                final double dx = x - lastDragX;
                final double dy = y - lastDragY;
                ghostShape.translate(dx, dy);
                lastDragX = x;
                lastDragY = y;
            }
        } else {
            if (activeTool.get() == ToolType.FREEHAND) {
                currentPoints.add(new Point(x, y));
            } else {
                currentPoints.set(currentPoints.size() - 1, new Point(x, y));
            }
            updateGhostShape();
        }
    }

    /**
     * Handles mouse release events
     * @param x x coordinate
     * @param y y coordinate
     */
    public void onMouseReleased(final double x, final double y) {
        if (activeTool.get() == ToolType.SELECT) {
            // If we were dragging, commit the move now
            if (isDraggingSelection && ghostShape != null && selectedShapeId.get() != null) {
                final Action modifyAction = actionFactory.createModifyAction(
                        canvasState, selectedShapeId.get(), ghostShape, userId);
                actionManager.requestLocalAction(modifyAction);
            }
            isDraggingSelection = false;
        } else if (ghostShape != null) {
            // Commit newly drawn shape
            final Action createAction = actionFactory.createCreateAction(ghostShape, userId);
            actionManager.requestLocalAction(createAction);
        }
        ghostShape = null;
        currentPoints.clear();
    }

    private ShapeId findHitShape(final double x, final double y) {
        final List<Shape> shapes = new ArrayList<>(canvasState.getVisibleShapes());
        // Iterate backwards to select top-most shapes first
        for (int i = shapes.size() - 1; i >= 0; i--) {
            if (GeometryUtils.hitTest(shapes.get(i), x, y)) {
                return shapes.get(i).getShapeId();
            }
        }
        return null;
    }

    private void updateGhostShape() {
        final ShapeType type;
        switch (activeTool.get()) {
            case RECTANGLE:
                type = ShapeType.RECTANGLE;
                break;
            case ELLIPSE:
                type = ShapeType.ELLIPSE;
                break;
            case TRIANGLE:
                type = ShapeType.TRIANGLE;
                break;
            case LINE:
                type = ShapeType.LINE;
                break;
            case FREEHAND:
                type = ShapeType.FREEHAND;
                break;
            default:
                type = ShapeType.FREEHAND;
                break;
        }

        ghostShape = shapeFactory.createShape(
                type, ShapeId.randomId(), new ArrayList<>(currentPoints),
                activeStrokeWidth.get(), ColorConverter.toAwt(activeColor.get()), userId);
    }

    public void deleteSelectedShape() {
        final ShapeId id = selectedShapeId.get();
        if (id != null) {
            final Action deleteAction = actionFactory.createDeleteAction(canvasState, id, userId);
            actionManager.requestLocalAction(deleteAction);
            selectedShapeId.set(null); // Clear selection after delete
        }
    }

    public void undo() {
        actionManager.performUndo();
    }

    public void redo() {
        actionManager.performRedo();
    }
}