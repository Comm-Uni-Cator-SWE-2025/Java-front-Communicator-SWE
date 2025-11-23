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

    // FXML bindings
    @FXML private ToggleButton selectBtn;
    @FXML private ToggleButton freehandBtn;
    @FXML private ToggleButton rectBtn;
    @FXML private ToggleButton ellipseBtn;
    @FXML private ToggleButton lineBtn;
    @FXML private ToggleButton triangleBtn;
    @FXML private Slider sizeSlider;
    @FXML private ColorPicker colorPicker;
    @FXML private Button deleteBtn;
    @FXML private Button undoBtn;
    @FXML private Button redoBtn;
    @FXML private Canvas canvas;
    @FXML private StackPane canvasContainer;
    @FXML private StackPane canvasHolder;

    private ActionManager actionManager;
    private CanvasRenderer renderer;
    private CanvasState canvasState;
    private final ShapeFactory shapeFactory = new ShapeFactory();
    private String userId = "user-" + UUID.randomUUID();

    private ToolType activeTool = ToolType.SELECT;
    private ShapeId selectedShapeId;
    private Shape transientShape;

    // Pan/zoom helpers
    private Translate canvasTranslate;
    private Scale canvasScale;
    private boolean isPanning = false;
    private double panStartX;
    private double panStartY;
    private static final double ZOOM_FACTOR = 1.1;
    private static final double MAX_ZOOM = 5.0;
    private static final double MIN_ZOOM = 0.5;

    private boolean updatingSelection = false;

    public void initModel(final ActionManager manager) {
        initModel(manager, "user-" + UUID.randomUUID());
    }

    public void initModel(final ActionManager manager, final String userId) {
        this.actionManager = manager;
        this.canvasState = manager.getCanvasState();
        this.userId = userId != null ? userId : this.userId;
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

        sizeSlider.setValue(5);
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
            ShapeState prev = canvasState.getShapeState(selectedShapeId);
            if (prev != null) {
                Shape modified = prev.getShape().copy();
                modified.setThickness(newVal.doubleValue());
                actionManager.requestModify(prev, modified);
            }
        });

        colorPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (updatingSelection || selectedShapeId == null) {
                return;
            }
            ShapeState prev = canvasState.getShapeState(selectedShapeId);
            if (prev != null && newVal != null) {
                Shape modified = prev.getShape().copy();
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
    private void onToolSelected(ActionEvent event) {
        ToggleButton btn = (ToggleButton) event.getSource();
        activeTool = (ToolType) btn.getUserData();
        if (activeTool != ToolType.SELECT) {
            selectedShapeId = null;
        }
        redraw();
    }

    // === Color button -> handled via listener ===

    // === Pan / Zoom ===
    @FXML
    private void onScroll(ScrollEvent event) {
        event.consume();
        double factor = event.getDeltaY() > 0 ? ZOOM_FACTOR : (1.0 / ZOOM_FACTOR);
        double newScale = canvasScale.getX() * factor;
        newScale = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, newScale));
        Point2D pivot = canvas.sceneToLocal(event.getSceneX(), event.getSceneY());
        canvasScale.setPivotX(pivot.getX());
        canvasScale.setPivotY(pivot.getY());
        canvasScale.setX(newScale);
        canvasScale.setY(newScale);
    }

    @FXML
    private void onViewportMousePressed(MouseEvent e) {
        if (e.isSecondaryButtonDown()) {
            isPanning = true;
            panStartX = e.getSceneX();
            panStartY = e.getSceneY();
        }
    }

    @FXML
    private void onViewportMouseDragged(MouseEvent e) {
        if (isPanning && e.isSecondaryButtonDown()) {
            canvasTranslate.setX(canvasTranslate.getX() + (e.getSceneX() - panStartX));
            canvasTranslate.setY(canvasTranslate.getY() + (e.getSceneY() - panStartY));
            panStartX = e.getSceneX();
            panStartY = e.getSceneY();
        }
    }

    @FXML
    private void onViewportMouseReleased(MouseEvent e) {
        if (e.getButton() == MouseButton.SECONDARY) {
            isPanning = false;
        }
    }

    // === Drawing ===
    @FXML
    private void onCanvasMousePressed(MouseEvent e) {
        if (!e.isPrimaryButtonDown()) {
            return;
        }
        Point2D logical = canvas.sceneToLocal(e.getSceneX(), e.getSceneY());

        if (activeTool == ToolType.SELECT) {
            selectShapeAt(logical);
        } else {
            transientShape = createTransientShape(logical);
        }
        redraw();
    }

    @FXML
    private void onCanvasMouseDragged(MouseEvent e) {
        if (!e.isPrimaryButtonDown() || transientShape == null) {
            return;
        }
        Point2D logical = canvas.sceneToLocal(e.getSceneX(), e.getSceneY());
        updateTransientShape(logical);
        redraw();
    }

    @FXML
    private void onCanvasMouseReleased(MouseEvent e) {
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
        ShapeState prev = canvasState.getShapeState(selectedShapeId);
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
    private void onColorSelected(ActionEvent event) {
        // Selection listener handles the actual update; this keeps the FXML action happy.
    }

    @FXML
    private void onRegularize() {
        if (selectedShapeId == null) {
            return;
        }
        ShapeState prev = canvasState.getShapeState(selectedShapeId);
        if (prev == null) {
            return;
        }
        Shape shape = prev.getShape().copy();
        if (shape.getPoints().size() < 2) {
            return;
        }
        Point anchor = shape.getPoints().get(0);
        Point current = shape.getPoints().get(shape.getPoints().size() - 1);
        double dx = current.getX() - anchor.getX();
        double dy = current.getY() - anchor.getY();
        double size = Math.max(Math.abs(dx), Math.abs(dy));
        double adjustedX = anchor.getX() + Math.copySign(size, dx == 0 ? 1 : dx);
        double adjustedY = anchor.getY() + Math.copySign(size, dy == 0 ? 1 : dy);

        List<Point> updated = new ArrayList<>(shape.getPoints());
        updated.set(updated.size() - 1, new Point(adjustedX, adjustedY));
        shape.setPoints(updated);
        actionManager.requestModify(prev, shape);
    }

    // === Helpers ===
    private void selectShapeAt(Point2D p) {
        selectedShapeId = canvasState.getVisibleShapes().stream()
                .filter(shape -> GeometryUtils.getBounds(shape).contains(p.getX(), p.getY()))
                .map(Shape::getShapeId)
                .findFirst()
                .orElse(null);

        if (selectedShapeId != null) {
            ShapeState state = canvasState.getShapeState(selectedShapeId);
            if (state != null) {
                updatingSelection = true;
                colorPicker.setValue(ColorConverter.toFx(state.getShape().getColor()));
                sizeSlider.setValue(state.getShape().getThickness());
                updatingSelection = false;
            }
        } else {
            updatingSelection = true;
            colorPicker.setValue(Color.BLACK);
            sizeSlider.setValue(5);
            updatingSelection = false;
        }
    }

    private Shape createTransientShape(Point2D start) {
        ShapeType type = switch (activeTool) {
            case FREEHAND -> ShapeType.FREEHAND;
            case RECTANGLE -> ShapeType.RECTANGLE;
            case ELLIPSE -> ShapeType.ELLIPSE;
            case LINE -> ShapeType.LINE;
            case TRIANGLE -> ShapeType.TRIANGLE;
            case SELECT, REGULARIZE -> ShapeType.FREEHAND;
        };

        List<Point> points = new ArrayList<>();
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

    private void updateTransientShape(Point2D p) {
        List<Point> points = new ArrayList<>(transientShape.getPoints());
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
