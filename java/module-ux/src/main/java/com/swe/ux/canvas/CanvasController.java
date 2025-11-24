package com.swe.ux.canvas;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.swe.canvas.datamodel.canvas.CanvasState;
import com.swe.canvas.datamodel.canvas.ShapeState;
import com.swe.canvas.datamodel.manager.ActionManager;
import com.swe.canvas.datamodel.shape.Point;
import com.swe.canvas.datamodel.shape.Shape;
import com.swe.canvas.datamodel.shape.ShapeFactory;
import com.swe.canvas.datamodel.shape.ShapeId;
import com.swe.canvas.datamodel.shape.ShapeType;
import com.swe.canvas.mvvm.ToolType;
import com.swe.ux.canvas.util.ColorConverter;
import com.swe.ux.canvas.util.GeometryUtils;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

/**
 * JavaFX controller driving the collaborative canvas embedded inside Swing.
 * It converts UI gestures into {@link ActionManager} operations backed by the
 * shared canvas data model from module-canvas.
 */
public class CanvasController {

    /**
     * Factor by which to zoom in/out.
     */
    private static final double ZOOM_FACTOR = 1.1;
    /**
     * Maximum zoom level.
     */
    private static final double MAX_ZOOM = 5.0;
    /**
     * Minimum zoom level.
     */
    private static final double MIN_ZOOM = 0.5;
    /**
     * Default size for shapes.
     */
    private static final double DEFAULT_SIZE = 5.0;
    /**
     * Magic number for sign calculation.
     */
    private static final int MAGIC_ONE = 1;

    // FXML bindings
    /** Select tool button. */
    @FXML private ToggleButton selectBtn;
    /** Freehand drawing tool button. */
    @FXML private ToggleButton freehandBtn;
    /** Rectangle drawing tool button. */
    @FXML private ToggleButton rectBtn;
    /** Ellipse drawing tool button. */
    @FXML private ToggleButton ellipseBtn;
    /** Line drawing tool button. */
    @FXML private ToggleButton lineBtn;
    /** Triangle drawing tool button. */
    @FXML private ToggleButton triangleBtn;
    /** Slider to control shape thickness. */
    @FXML private Slider sizeSlider;
    /** Color picker for shape color. */
    @FXML private ColorPicker colorPicker;
    /** Button to delete selected shape. */
    @FXML private Button deleteBtn;
    /** Button to undo last action. */
    @FXML private Button undoBtn;
    /** Button to redo last undone action. */
    @FXML private Button redoBtn;
    /** The canvas element for drawing. */
    @FXML private Canvas canvas;
    /** Container for the canvas. */
    @FXML private StackPane canvasContainer;
    /** Holder for canvas with transformations. */
    @FXML private StackPane canvasHolder;

    /** Action manager for canvas operations. */
    private ActionManager actionManager;
    /** Renderer for drawing shapes. */
    private CanvasRenderer renderer;
    /** Current state of the canvas. */
    private CanvasState canvasState;
    /** Factory for creating shapes. */
    private final ShapeFactory shapeFactory = new ShapeFactory();
    /** User ID for this canvas instance. */
    private String userId = "user-" + UUID.randomUUID();

    /** Currently active drawing tool. */
    private ToolType activeTool = ToolType.SELECT;
    /** ID of the currently selected shape. */
    private ShapeId selectedShapeId;
    /** Transient shape being drawn. */
    private Shape transientShape;

    // Pan/zoom helpers
    /** Translation transform for panning. */
    private Translate canvasTranslate;
    /** Scale transform for zooming. */
    private Scale canvasScale;
    /** Whether currently panning. */
    private boolean isPanning = false;
    /** Starting X position for pan gesture. */
    private double panStartX;
    /** Starting Y position for pan gesture. */
    private double panStartY;

    /** Flag to prevent recursive updates when setting controls. */
    private boolean updatingSelection = false;

    /**
     * Initialize the model with default user ID.
     * @param manager The action manager
     */
    public void initModel(final ActionManager manager) {
        initModel(manager, "user-" + UUID.randomUUID());
    }

    /**
     * Initialize the model with action manager and user ID.
     * @param manager The action manager
     * @param userIdParam The user ID for this canvas
     */
    public void initModel(final ActionManager manager, final String userIdParam) {
        this.actionManager = manager;
        this.canvasState = manager.getCanvasState();
        if (userIdParam != null) {
            this.userId = userIdParam;
        }
        initializeControls();
    }

    @FXML
    public void initialize() {
        // FXML lifecycle hook – initialization happens in initModel
    }

    private void initializeControls() {
        renderer = new CanvasRenderer(canvas);

        canvasTranslate = new Translate();
        canvasScale = new Scale();
        canvasHolder.getTransforms().addAll(canvasTranslate, canvasScale);

        sizeSlider.setValue(DEFAULT_SIZE);
        colorPicker.setValue(Color.BLACK);

        freehandBtn.setUserData(ToolType.FREEHAND);
        selectBtn.setUserData(ToolType.SELECT);
        rectBtn.setUserData(ToolType.RECTANGLE);
        ellipseBtn.setUserData(ToolType.ELLIPSE);
        lineBtn.setUserData(ToolType.LINE);
        triangleBtn.setUserData(ToolType.TRIANGLE);

        sizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (updatingSelection || selectedShapeId == null) {
                return;
            }
            final ShapeState prev = canvasState.getShapeState(selectedShapeId);
            if (prev != null) {
                final Shape modified = prev.getShape().copy();
                modified.setThickness(newVal.doubleValue());
                actionManager.requestModify(prev, modified);
            }
        });

        colorPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (updatingSelection || selectedShapeId == null) {
                return;
            }
            final ShapeState prev = canvasState.getShapeState(selectedShapeId);
            if (prev != null && newVal != null) {
                final Shape modified = prev.getShape().copy();
                modified.setColor(ColorConverter.toAwt(newVal));
                actionManager.requestModify(prev, modified);
            }
        });

        actionManager.setOnUpdate(() -> Platform.runLater(this::redraw));
        canvasState.setOnUpdate(() -> Platform.runLater(this::redraw));

        redraw();
    }

    private void redraw() {
        if (renderer != null) {
            renderer.render(canvasState, transientShape, selectedShapeId, false);
        }
    }

    // === Tool selection ===
    @FXML
    private void onToolSelected(final ActionEvent event) {
        final ToggleButton btn = (ToggleButton) event.getSource();
        activeTool = (ToolType) btn.getUserData();
        if (activeTool != ToolType.SELECT) {
            selectedShapeId = null;
        }
        redraw();
    }

    // === Color button -> handled via listener ===

    // === Pan / Zoom ===
    @FXML
    private void onScroll(final ScrollEvent event) {
        event.consume();
        final double factor;
        if (event.getDeltaY() > 0) {
            factor = ZOOM_FACTOR;
        } else {
            factor = 1.0 / ZOOM_FACTOR;
        }
        double newScale = canvasScale.getX() * factor;
        newScale = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, newScale));
        final Point2D pivot = canvas.sceneToLocal(event.getSceneX(), event.getSceneY());
        canvasScale.setPivotX(pivot.getX());
        canvasScale.setPivotY(pivot.getY());
        canvasScale.setX(newScale);
        canvasScale.setY(newScale);
    }

    @FXML
    private void onViewportMousePressed(final MouseEvent e) {
        if (e.isSecondaryButtonDown()) {
            isPanning = true;
            panStartX = e.getSceneX();
            panStartY = e.getSceneY();
        }
    }

    @FXML
    private void onViewportMouseDragged(final MouseEvent e) {
        if (isPanning && e.isSecondaryButtonDown()) {
            canvasTranslate.setX(canvasTranslate.getX() + (e.getSceneX() - panStartX));
            canvasTranslate.setY(canvasTranslate.getY() + (e.getSceneY() - panStartY));
            panStartX = e.getSceneX();
            panStartY = e.getSceneY();
        }
    }

    @FXML
    private void onViewportMouseReleased(final MouseEvent e) {
        if (e.getButton() == MouseButton.SECONDARY) {
            isPanning = false;
        }
    }

    // === Drawing ===
    @FXML
    private void onCanvasMousePressed(final MouseEvent e) {
        if (!e.isPrimaryButtonDown()) {
            return;
        }
        final Point2D logical = canvas.sceneToLocal(e.getSceneX(), e.getSceneY());

        if (activeTool == ToolType.SELECT) {
            selectShapeAt(logical);
        } else {
            transientShape = createTransientShape(logical);
        }
        redraw();
    }

    @FXML
    private void onCanvasMouseDragged(final MouseEvent e) {
        if (!e.isPrimaryButtonDown() || transientShape == null) {
            return;
        }
        final Point2D logical = canvas.sceneToLocal(e.getSceneX(), e.getSceneY());
        updateTransientShape(logical);
        redraw();
    }

    @FXML
    private void onCanvasMouseReleased(final MouseEvent e) {
        if (!e.isPrimaryButtonDown()) {
            return;
        }
        if (transientShape != null) {
            actionManager.requestCreate(transientShape.copy());
            selectedShapeId = transientShape.getShapeId();
            transientShape = null;
        }
        redraw();
    }

    // === Toolbar actions ===
    @FXML
    private void onDelete() {
        if (selectedShapeId == null) {
            return;
        }
        final ShapeState prev = canvasState.getShapeState(selectedShapeId);
        if (prev != null) {
            actionManager.requestDelete(prev);
            selectedShapeId = null;
        }
        redraw();
    }

    @FXML
    private void onUndo() {
        actionManager.requestUndo();
    }

    @FXML
    private void onRedo() {
        actionManager.requestRedo();
    }

    @FXML
    private void onColorSelected(final ActionEvent event) {
        // Selection listener handles the actual update; this keeps the FXML action happy.
    }

    @FXML
    private void onRegularize() {
        if (selectedShapeId == null) {
            return;
        }
        final ShapeState prev = canvasState.getShapeState(selectedShapeId);
        if (prev == null) {
            return;
        }
        final Shape shape = prev.getShape().copy();
        if (shape.getPoints().size() < 2) {
            return;
        }
        final Point anchor = shape.getPoints().get(0);
        final Point current = shape.getPoints().get(shape.getPoints().size() - 1);
        final double dx = current.getX() - anchor.getX();
        final double dy = current.getY() - anchor.getY();
        final double size = Math.max(Math.abs(dx), Math.abs(dy));
        final double signValueX;
        if (dx == 0) {
            signValueX = MAGIC_ONE;
        } else {
            signValueX = dx;
        }
        final double adjustedX = anchor.getX() + Math.copySign(size, signValueX);
        final double signValueY;
        if (dy == 0) {
            signValueY = MAGIC_ONE;
        } else {
            signValueY = dy;
        }
        final double adjustedY = anchor.getY() + Math.copySign(size, signValueY);

        final List<Point> updated = new ArrayList<>(shape.getPoints());
        updated.set(updated.size() - 1, new Point(adjustedX, adjustedY));
        shape.setPoints(updated);
        actionManager.requestModify(prev, shape);
    }

    // === Helpers ===
    private void selectShapeAt(final Point2D p) {
        selectedShapeId = canvasState.getVisibleShapes().stream()
                .filter(shape -> GeometryUtils.getBounds(shape).contains(p.getX(), p.getY()))
                .map(Shape::getShapeId)
                .findFirst()
                .orElse(null);

        if (selectedShapeId != null) {
            final ShapeState state = canvasState.getShapeState(selectedShapeId);
            if (state != null) {
                updatingSelection = true;
                colorPicker.setValue(ColorConverter.toFx(state.getShape().getColor()));
                sizeSlider.setValue(state.getShape().getThickness());
                updatingSelection = false;
            }
        } else {
            updatingSelection = true;
            colorPicker.setValue(Color.BLACK);
            sizeSlider.setValue(DEFAULT_SIZE);
            updatingSelection = false;
        }
    }

    private Shape createTransientShape(final Point2D start) {
        final ShapeType type = switch (activeTool) {
            case FREEHAND -> ShapeType.FREEHAND;
            case RECTANGLE -> ShapeType.RECTANGLE;
            case ELLIPSE -> ShapeType.ELLIPSE;
            case LINE -> ShapeType.LINE;
            case TRIANGLE -> ShapeType.TRIANGLE;
            case SELECT, REGULARIZE -> ShapeType.FREEHAND;
        };

        final List<Point> points = new ArrayList<>();
        points.add(new Point(start.getX(), start.getY()));
        points.add(new Point(start.getX(), start.getY()));

        return shapeFactory.createShape(
                type,
                ShapeId.randomId(),
                points,
                sizeSlider.getValue(),
                ColorConverter.toAwt(colorPicker.getValue()),
                userId);
    }

    private void updateTransientShape(final Point2D p) {
        final List<Point> points = new ArrayList<>(transientShape.getPoints());
        if (transientShape.getShapeType() == ShapeType.FREEHAND) {
            points.add(new Point(p.getX(), p.getY()));
        } else if (!points.isEmpty()) {
            if (points.size() == 1) {
                points.add(new Point(p.getX(), p.getY()));
            } else {
                points.set(points.size() - 1, new Point(p.getX(), p.getY()));
            }
        } else {
            points.add(new Point(p.getX(), p.getY()));
        }
        transientShape.setPoints(points);
        transientShape.setLastUpdatedBy(userId);
    }
}
