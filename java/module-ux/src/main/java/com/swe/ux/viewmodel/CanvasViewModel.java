package com.swe.ux.viewmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.swe.canvas.datamodel.action.Action;
import com.swe.canvas.datamodel.action.ActionFactory;
import com.swe.canvas.datamodel.canvas.CanvasState;
import com.swe.canvas.datamodel.canvas.ShapeState;
import com.swe.canvas.datamodel.serialization.NetActionSerializer;
import com.swe.canvas.datamodel.serialization.ShapeSerializer;
import com.swe.canvas.datamodel.shape.Point;
import com.swe.canvas.datamodel.shape.Shape;
import com.swe.canvas.datamodel.shape.ShapeFactory;
import com.swe.canvas.datamodel.shape.ShapeId;
import com.swe.canvas.datamodel.shape.ShapeType;
import com.swe.canvas.services.AiService;
import com.swe.canvas.ui.util.ColorConverter;
import com.swe.canvas.ui.util.GeometryUtils;
import com.swe.networking.AbstractNetworking;
import com.swe.networking.ClientNode;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

/**
 * Canvas ViewModel (merged / updated).
 *
 * This implementation is the "integrated" view model updated to match the
 * features in the newer mvvm version: networking, incoming action processing,
 * AI regularization, and serialization of shapes/actions.
 *
 * Package: com.swe.ux.viewmodel
 *
 * @author Merged by ChatGPT for user
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

    // Networking (may be null if running standalone)
    private AbstractNetworking network;

    public void setNetworking(final AbstractNetworking networking) {
        this.network = networking;
    }

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

                // Optionally broadcast the modify action over network
                if (network != null && action != null) {
                    try {
                        final String actionJson = NetActionSerializer.serializeAction(action);
                        network.sendData(actionJson.getBytes(), generateClientNodes(), 2, 0);
                    } catch (Exception ignored) {
                        // swallow network exceptions — network is optional
                    }
                }
            }
        }
    }

    /**
     * Process incoming serialized data (expected to be an Action in JSON form).
     *
     * This method deserializes the incoming bytes into an Action and then
     * applies corresponding local actions so the canvas state updates locally.
     *
     * @param data incoming bytes representing a serialized Action JSON
     */
    public void processIncomingData(final byte[] data) {
        if (data == null || data.length == 0) return;

        try {
            final String json = new String(data);
            final Action action = NetActionSerializer.deserializeAction(json);

            if (action == null || action.getActionType() == null) return;

            switch (action.getActionType()) {
                case CREATE -> {
                    // For create we expect action.getNewState() to contain the shape
                    if (action.getNewState() != null && action.getNewState().getShape() != null) {
                        final Action createAction = actionFactory.createCreateAction(action.getNewState().getShape(), action.getUserId());
                        actionManager.requestLocalAction(createAction);
                    } else {
                        // Fallback: if incoming action only contains shape bytes externally,
                        // the higher-level code should call processIncomingData with serialized Action.
                    }
                }
                case MODIFY -> {
                    if (action.getShapeId() != null && action.getNewState() != null) {
                        final Action modifyAction = actionFactory.createModifyAction(
                                canvasState, action.getShapeId(), action.getNewState().getShape(), action.getUserId());
                        actionManager.requestLocalAction(modifyAction);
                    }
                }
                case DELETE -> {
                    if (action.getShapeId() != null) {
                        final Action deleteAction = actionFactory.createDeleteAction(canvasState, action.getShapeId(), action.getUserId());
                        actionManager.requestLocalAction(deleteAction);
                    }
                }
                default -> {
                    // no-op for unknown types
                }
            }
        } catch (Exception e) {
            // Don't crash on malformed incoming data; log if you have a logger.
            e.printStackTrace();
        }
    }

    // --- Input Handling ---

    /**
     * Handles mouse press event.
     *
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
                final ShapeState ss = canvasState.getShapeState(hitShapeId);
                if (ss != null && !ss.isDeleted() && ss.getShape() != null) {
                    isDraggingSelection = true;
                    // Create the ghost shape immediately so we can see it move
                    ghostShape = ss.getShape().copy();
                } else {
                    // Shape no longer exists (race) — treat as no hit
                    isDraggingSelection = false;
                    ghostShape = null;
                    selectedShapeId.set(null);
                }
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
     * Handles mouse drag event.
     *
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
     *
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

                // Broadcast modify
                if (network != null) {
                    try {
                        final String actionJson = NetActionSerializer.serializeAction(modifyAction);
                        network.sendData(actionJson.getBytes(), generateClientNodes(), 2, 0);
                    } catch (Exception ignored) {
                    }
                }
            }
            isDraggingSelection = false;
        } else if (ghostShape != null) {
            // Broadcast shape bytes (shape-only) to clients if network available
            if (network != null) {
                try {
                    final String shapeJson = ShapeSerializer.testSerializeShapeOnly(ghostShape);
                    final byte[] shapeData = shapeJson.getBytes();
                    network.sendData(shapeData, generateClientNodes(), 2, 0);
                } catch (Exception ignored) {
                }
            }

            // Commit newly drawn shape locally
            final Action createAction = actionFactory.createCreateAction(ghostShape, userId);
            actionManager.requestLocalAction(createAction);

            // Broadcast create action
            if (network != null) {
                try {
                    final String actionJson = NetActionSerializer.serializeAction(createAction);
                    byte[] actionBytes = actionJson.getBytes();
                    
                    network.broadcast(actionBytes, 2, 0);
                } catch (Exception ignored) {
                }
            }
        }
        ghostShape = null;
        currentPoints.clear();
    }

    private ClientNode[] generateClientNodes() {
        // NOTE: placeholder stub. Replace with real client discovery / list in production.
        String ip = "";
        int port = 0;
        final ClientNode client1 = new ClientNode(ip, port);
        final ClientNode[] clients = {client1};
        return clients;
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

            if (network != null) {
                try {
                    final String actionJson = NetActionSerializer.serializeAction(deleteAction);
                    network.sendData(actionJson.getBytes(), generateClientNodes(), 2, 0);
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * Regularize the currently selected shape (only applies to FREEHAND shapes).
     *
     * This method delegates to AiService to compute a regularized polygon.
     * If AiService returns a modified shape, a modify action is applied and
     * broadcast.
     *
     * @param sides number of polygon sides (semantic hint for AI; may be ignored)
     */
    public void regularizeSelectedShape() {
        final ShapeId id = selectedShapeId.get();
        if (id == null) {
            return; // nothing selected
        }

        final ShapeState current = canvasState.getShapeState(id);
        if (current == null || current.isDeleted() || current.getShape() == null) {
            return;
        }

        final Shape original = current.getShape();
        if (original.getShapeType() != ShapeType.FREEHAND) {
            return; // only applies to freehand
        }

        final Shape modified = AiService.regularizeFreehandShape(original);
        if (modified == null || modified.getPoints() == null || modified.getPoints().isEmpty()) {
            return;
        }

        final Action modifyAction = actionFactory.createModifyAction(canvasState, id, modified, userId);
        actionManager.requestLocalAction(modifyAction);

        if (network != null) {
            try {
                final String actionJson = NetActionSerializer.serializeAction(modifyAction);
                network.sendData(actionJson.getBytes(), generateClientNodes(), 2, 0);
            } catch (Exception ignored) {
            }
        }
    }

    public void undo() {
        actionManager.performUndo();
    }

    public void redo() {
        actionManager.performRedo();
    }
}
