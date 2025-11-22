package com.swe.canvas.mvvm;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import com.swe.canvas.datamodel.action.ActionFactory;
import com.swe.canvas.datamodel.canvas.CanvasState;
import com.swe.canvas.datamodel.canvas.ShapeState;
import com.swe.canvas.datamodel.manager.ActionManager;
import com.swe.canvas.datamodel.shape.Point;
import com.swe.canvas.datamodel.shape.Shape;
import com.swe.canvas.datamodel.shape.ShapeFactory;
import com.swe.canvas.datamodel.shape.ShapeId;
import com.swe.canvas.datamodel.shape.ShapeType;
import com.swe.canvas.ui.util.ColorConverter;
import com.swe.canvas.ui.util.GeometryUtils;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

/**
 * View model for the canvas.
 * This class is now a "thin client" as per the prompt.
 * It does not modify its own state.
 * It sends requests to the IActionManager and displays a "ghost shape" (transientShape).
 */
public class CanvasViewModel {

    private final ActionManager actionManager;
    private final CanvasState canvasState; // This is now a local mirror
    private final ActionFactory actionFactory;
    private final ShapeFactory shapeFactory;
    private final String userId; // This VM's user ID

    public final ObjectProperty<ToolType> activeTool = new SimpleObjectProperty<>(ToolType.FREEHAND);
    public final ObjectProperty<Color> activeColor = new SimpleObjectProperty<>(Color.BLACK);
    public final DoubleProperty activeStrokeWidth = new SimpleDoubleProperty(2.0);
    public final ObjectProperty<ShapeId> selectedShapeId = new SimpleObjectProperty<>(null);

    private final List<Point> currentPoints = new ArrayList<>();
    
    // "Ghost Shape" for 2-second timeout
    private Shape transientShape = null; 
    private Timer ghostTimer = new Timer(true); // Daemon timer

    private double lastDragX;
    private double lastDragY;
    public boolean isDraggingSelection = false;
    private ShapeState originalShapeForDrag = null; // Store the original state for drag

    /**
     * This ViewModel is created by the Main app and injected with the
     * appropriate action manager (Host or Client).
     */
    public CanvasViewModel(String userId, ActionManager actionManager) {
        this.userId = userId;
        this.actionManager = actionManager;
        this.canvasState = actionManager.getCanvasState(); // Get state from manager
        this.actionFactory = actionManager.getActionFactory();
        this.shapeFactory = new ShapeFactory();
    }

    public CanvasState getCanvasState() {
        return canvasState;
    }

    /**
     * Returns the "ghost shape" for rendering.
     */
    public Shape getTransientShape() {
        return transientShape;
    }

    public void setOnCanvasUpdate(final Runnable r) {
        actionManager.setOnUpdate(r);
    }

    // --- Property Updates (Now send requests) ---
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
                
                // Send MODIFY request
                actionManager.requestModify(currentState, modifiedShape);
                
                // Show ghost shape
                showGhostShape(modifiedShape);
            }
        }
    }

    // --- Input Handling (Client Logic) ---

    public void onMousePressed(final double x, final double y) {
        lastDragX = x;
        lastDragY = y;
        transientShape = null; // Clear any previous ghost

        if (activeTool.get() == ToolType.SELECT) {
            final ShapeId hitShapeId = findHitShape(x, y);
            selectedShapeId.set(hitShapeId);

            if (hitShapeId != null) {
                final ShapeState ss = canvasState.getShapeState(hitShapeId);
                if (ss != null && !ss.isDeleted() && ss.getShape() != null) {
                    isDraggingSelection = true;
                    originalShapeForDrag = ss; // Store original state
                    transientShape = ss.getShape().copy(); // Start ghosting
                } else {
                    isDraggingSelection = false;
                    originalShapeForDrag = null;
                }
            } else {
                isDraggingSelection = false;
                originalShapeForDrag = null;
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

    public void onMouseDragged(final double x, final double y) {
        if (activeTool.get() == ToolType.SELECT) {
            if (isDraggingSelection && transientShape != null && originalShapeForDrag != null) {
                // Apply delta to the ghost shape
                final double dx = x - lastDragX;
                final double dy = y - lastDragY;
                transientShape.translate(dx, dy);
                
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

    public void onMouseReleased(final double x, final double y) {
        if (activeTool.get() == ToolType.SELECT) {
            // Commit the drag/move
            if (isDraggingSelection && transientShape != null && originalShapeForDrag != null) {
                // We must send the *original* state as prevState
                actionManager.requestModify(originalShapeForDrag, transientShape);
                // transientShape is already set, just start the timer
                startGhostTimer();
            }
            isDraggingSelection = false;
            originalShapeForDrag = null;
            
        } else if (transientShape != null) {
            // Commit the new drawing
            actionManager.requestCreate(transientShape);
            startGhostTimer(); // Show ghost until confirmed
        }
        
        currentPoints.clear();
    }
    
    // --- Ghost Shape Logic ---
    
    private void showGhostShape(Shape shape) {
        transientShape = shape;
        startGhostTimer();
    }
    
    private void startGhostTimer() {
        ghostTimer.cancel(); // Cancel any existing timer
        ghostTimer = new Timer(true);
        ghostTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                transientShape = null;
                // We must redraw on the UI thread
                Platform.runLater(() -> {
                    if (actionManager != null) {
                        // This forces a redraw by notifying the manager
                        actionManager.getCanvasState().notifyUpdate();
                    }
                });
            }
        }, 2000); // 2 second timeout
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
            case RECTANGLE: type = ShapeType.RECTANGLE; break;
            case ELLIPSE: type = ShapeType.ELLIPSE; break;
            case TRIANGLE: type = ShapeType.TRIANGLE; break;
            case LINE: type = ShapeType.LINE; break;
            case FREEHAND: type = ShapeType.FREEHAND; break;
            default: type = ShapeType.FREEHAND; break;
        }
        
        // Create the shape with the client's user ID
        transientShape = shapeFactory.createShape(
                type, ShapeId.randomId(), new ArrayList<>(currentPoints),
                activeStrokeWidth.get(), ColorConverter.toAwt(activeColor.get()), userId);
    }

    public void deleteSelectedShape() {
        final ShapeId id = selectedShapeId.get();
        if (id != null) {
            final ShapeState stateToDelete = canvasState.getShapeState(id);
            if (stateToDelete != null && !stateToDelete.isDeleted()) {
                actionManager.requestDelete(stateToDelete);
                
                // Show deleted shape as a ghost
                Shape ghost = stateToDelete.getShape().copy();
                showGhostShape(ghost); // Show ghost
                selectedShapeId.set(null);
            }
        }
    }

    public void undo() {
        actionManager.requestUndo();
    }

    public void redo() {
        actionManager.requestRedo();
    }

    public void handleValidatedUpdate() {
        if (transientShape == null) return;

        final ShapeId tid = transientShape.getShapeId();
        if (tid == null) return;

        final ShapeState st = canvasState.getShapeState(tid);

        // DELETE fix:
        // Clear ghost when:
        // 1) server removed the shape (st == null)
        // 2) server marked it deleted   (st.isDeleted())
        if (st == null || st.isDeleted()) {
            transientShape = null;
            try { ghostTimer.cancel(); } catch (Exception ignored) {}
            return;
        }

        // CREATE / MODIFY confirmation
        transientShape = null;
        try { ghostTimer.cancel(); } catch (Exception ignored) {}
    }
}