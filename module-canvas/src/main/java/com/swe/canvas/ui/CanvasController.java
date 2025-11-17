package com.swe.canvas.ui;

import com.swe.canvas.datamodel.canvas.CanvasState;
import com.swe.canvas.datamodel.canvas.ShapeState;
import com.swe.canvas.mvvm.CanvasViewModel;
import com.swe.canvas.mvvm.ToolType;
import com.swe.canvas.ui.util.ColorConverter;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

/**
 * Controller for the fxml view.
 * Now includes Pan and Zoom logic, mirroring the C# MainWindow.xaml.cs.
 */
public class CanvasController {

    // --- FXML Control References ---
    @FXML private ToggleButton selectBtn;
    @FXML private ToggleButton freehandBtn;
    @FXML private ToggleButton rectBtn;
    @FXML private ToggleButton ellipseBtn;
    @FXML private ToggleButton lineBtn;
    @FXML private ToggleButton triangleBtn;

    @FXML private Slider sizeSlider;
    @FXML private ColorPicker colorPicker;

    @FXML private Button deleteBtn;
    @FXML private Button regularizeBtn;
    @FXML private Button undoBtn;
    @FXML private Button redoBtn;

    @FXML private Canvas canvas;
    
    // The "viewport" pane that contains the canvas. Used for pan/zoom events.
    @FXML private StackPane canvasContainer; 

    // FIX: Add reference for the "white box" StackPane
    @FXML private StackPane canvasHolder; 

    private CanvasViewModel viewModel;
    private CanvasRenderer renderer;
    private boolean isUpdatingUI = false;
    private final Label sizeValueLabel = new Label();

    // --- Pan and Zoom State ---
    private Translate canvasTranslate;
    private Scale canvasScale;
    private boolean isPanning = false;
    private double panStartX, panStartY;
    
    private static final double ZOOM_FACTOR = 1.1;
    private static final double MAX_ZOOM = 5.0;
    private static final double MIN_ZOOM = 0.5;

    /**
     * Initializes the view/GUI
     */
    public void initialize() {
        viewModel = new CanvasViewModel(new CanvasState());
        renderer = new CanvasRenderer(canvas);

        // --- Setup Transforms for Pan and Zoom ---
        canvasTranslate = new Translate();
        canvasScale = new Scale();
        
        // FIX: Apply transforms to the "canvasHolder" (the white box)
        // instead of the canvas itself.
        canvasHolder.getTransforms().addAll(canvasTranslate, canvasScale);

        // --- Initialize Controls ---
        sizeSlider.setValue(viewModel.activeStrokeWidth.get());
        colorPicker.setValue(viewModel.activeColor.get());

        // --- Bindings & Listeners ---
        sizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (isUpdatingUI) return;
            final double thickness = newVal.doubleValue();
            viewModel.activeStrokeWidth.set(thickness);
            if (viewModel.selectedShapeId.get() != null) {
                viewModel.updateSelectedShapeThickness(thickness);
            }
        });

        deleteBtn.disableProperty().bind(viewModel.selectedShapeId.isNull());
        regularizeBtn.disableProperty().bind(viewModel.selectedShapeId.isNull());

        viewModel.selectedShapeId.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                ShapeState state = viewModel.getCanvasState().getShapeState(newVal);
                if (state != null && !state.isDeleted()) {
                    isUpdatingUI = true;
                    try {
                        colorPicker.setValue(ColorConverter.toFx(state.getShape().getColor()));
                        sizeSlider.setValue(state.getShape().getThickness());
                        viewModel.activeColor.set(colorPicker.getValue());
                        viewModel.activeStrokeWidth.set(sizeSlider.getValue());
                    } finally {
                        isUpdatingUI = false;
                    }
                }
            }
            redraw();
        });

        // --- Standard Setup ---
        canvas.setFocusTraversable(true);
        // Focus canvas to receive key events (we'll attach to container instead)
        canvasContainer.setOnMouseClicked(e -> canvas.requestFocus());
        // Handle DELETE key on the container
        canvasContainer.setOnKeyPressed(this::onKeyPressed);

        viewModel.setOnCanvasUpdate(this::redraw);

        // Set UserData for tool selection
        freehandBtn.setUserData(ToolType.FREEHAND);
        selectBtn.setUserData(ToolType.SELECT);
        rectBtn.setUserData(ToolType.RECTANGLE);
        ellipseBtn.setUserData(ToolType.ELLIPSE);
        lineBtn.setUserData(ToolType.LINE);
        triangleBtn.setUserData(ToolType.TRIANGLE);
        
        redraw();
    }

    public CanvasViewModel getViewModel() { return viewModel; }

    private void redraw() {
        renderer.render(viewModel.getCanvasState(), viewModel.getGhostShape(), viewModel.selectedShapeId.get(), viewModel.isDraggingSelection);
    }

    // =========================================================================
    // --- Pan and Zoom Event Handlers (Attached to canvasContainer) ---
    // =========================================================================

    /**
     * Handles Mouse Wheel events on the viewport (canvasContainer) for zooming.
     */
    @FXML
    private void onScroll(ScrollEvent event) {
        event.consume(); // Don't let parent containers scroll

        double delta = event.getDeltaY();
        if (delta == 0) return;

        double zoomFactor = (delta > 0) ? ZOOM_FACTOR : (1.0 / ZOOM_FACTOR);
        double newScale = canvasScale.getX() * zoomFactor;
        
        // Clamp scale
        newScale = Math.max(MIN_ZOOM, Math.min(newScale, MAX_ZOOM));
        
        // Get mouse position relative to the canvas (logical coordinates)
        // We use sceneToLocal to convert screen coordinates to the canvas's untransformed space
        // This still works because the canvas is a child of the scaled node.
        Point2D mouseLogicalCoords = canvas.sceneToLocal(event.getSceneX(), event.getSceneY());
        
        // Set scale pivot to the mouse cursor's logical position
        canvasScale.setPivotX(mouseLogicalCoords.getX());
        canvasScale.setPivotY(mouseLogicalCoords.getY());
        
        // Apply new scale
        canvasScale.setX(newScale);
        canvasScale.setY(newScale);
    }
    
    /**
     * Handles Mouse Pressed events on the viewport (canvasContainer).
     * This is used to INITIATE panning (right-click).
     */
    @FXML
    private void onViewportMousePressed(MouseEvent event) {
        // Start panning on right-click (SECONDARY button)
        if (event.isSecondaryButtonDown()) {
            isPanning = true;
            // We use scene coordinates for panning to avoid jitter
            panStartX = event.getSceneX();
            panStartY = event.getSceneY();
            event.consume();
        }
    }

    /**
     * Handles Mouse Dragged events on the viewport (canvasContainer).
     * This is used to UPDATE panning.
     */
    @FXML
    private void onViewportMouseDragged(MouseEvent event) {
        if (isPanning && event.isSecondaryButtonDown()) {
            double dx = event.getSceneX() - panStartX;
            double dy = event.getSceneY() - panStartY;
            
            // Add delta to the existing translation
            canvasTranslate.setX(canvasTranslate.getX() + dx);
            canvasTranslate.setY(canvasTranslate.getY() + dy);
            
            // Update start position for next drag event
            panStartX = event.getSceneX();
            panStartY = event.getSceneY();
            
            event.consume();
        }
    }

    /**
     * Handles Mouse Released events on the viewport (canvasContainer).
     * This is used to STOP panning.
     */
    @FXML
    private void onViewportMouseReleased(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            isPanning = false;
            event.consume();
        }
    }

    // =========================================================================
    // --- Drawing Event Handlers (Attached to Canvas) ---
    // =========================================================================

    /**
     * Converts MouseEvent coordinates from screen space to the canvas's
     * logical (un-scaled, un-translated) coordinate space.
     */
    private Point2D getLogicalCoords(MouseEvent event) {
        // canvas.sceneToLocal transforms scene (window) coordinates
        // into the local coordinates of the canvas, correctly
        // accounting for all transforms (pan and zoom) on its parent.
        return canvas.sceneToLocal(event.getSceneX(), event.getSceneY());
    }

    /**
     * Handles Mouse Pressed events *on the canvas* for drawing (left-click).
     */
    @FXML 
    private void onCanvasMousePressed(final MouseEvent e) {
        // Only draw/select on PRIMARY button click
        if (e.isPrimaryButtonDown()) {
            Point2D logicalCoords = getLogicalCoords(e);
            viewModel.onMousePressed(logicalCoords.getX(), logicalCoords.getY()); 
            redraw();
            e.consume();
        }
    }

    /**
     * Handles Mouse Dragged events *on the canvas* for drawing (left-click).
     */
    @FXML 
    private void onCanvasMouseDragged(final MouseEvent e) {
        // Only draw/drag on PRIMARY button click
        if (e.isPrimaryButtonDown()) {
            Point2D logicalCoords = getLogicalCoords(e);
            viewModel.onMouseDragged(logicalCoords.getX(), logicalCoords.getY()); 
            redraw();
            e.consume();
        }
    }

    /**
     * Handles Mouse Released events *on the canvas* for drawing (left-click).
     */
    @FXML 
    private void onCanvasMouseReleased(final MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY) {
            Point2D logicalCoords = getLogicalCoords(e);
            viewModel.onMouseReleased(logicalCoords.getX(), logicalCoords.getY()); 
            redraw();
            e.consume();
        }
    }

    // =========================================================================
    // --- Toolbar Button Handlers (Unchanged) ---
    // =========================================================================

    @FXML
    private void onColorSelected(final ActionEvent event) {
        if (isUpdatingUI) return;
        
        final Color selectedColor = colorPicker.getValue();
        viewModel.activeColor.set(selectedColor);
        if (viewModel.selectedShapeId.get() != null) {
            viewModel.updateSelectedShapeColor(selectedColor);
        }
        redraw();
    }

    @FXML
    private void onToolSelected(final ActionEvent event) {
        final ToggleButton source = (ToggleButton) event.getSource();
        if (source.getUserData() != null) {
            viewModel.activeTool.set((ToolType) source.getUserData());
            if (viewModel.activeTool.get() != ToolType.SELECT) {
                viewModel.selectedShapeId.set(null);
            }
            redraw();
        }
    }

    @FXML
    private void onDelete() {
        viewModel.deleteSelectedShape();
    }
    
    @FXML
    private void onRegularize() {
        System.out.println("Regularize button clicked (no logic assigned).");
    }

    private void onKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) {
            viewModel.deleteSelectedShape();
        }
    }

    @FXML private void onUndo() {
        viewModel.undo();
    }

    @FXML private void onRedo() {
        viewModel.redo();
    }
}