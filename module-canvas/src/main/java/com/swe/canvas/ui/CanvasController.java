package com.swe.canvas.ui;

import com.swe.canvas.datamodel.canvas.CanvasState;
import com.swe.canvas.datamodel.canvas.ShapeState;
import com.swe.canvas.mvvm.CanvasViewModel;
import com.swe.canvas.mvvm.ToolType;
import com.swe.canvas.ui.util.ColorConverter;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

/**
 * Controller for the fxml view
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
    
    // Replaced Rectangle with ColorPicker
    @FXML private ColorPicker colorPicker;

    @FXML private Button deleteBtn;
    @FXML private Button regularizeBtn; // Button from original FXML
    @FXML private Button undoBtn;
    @FXML private Button redoBtn;

    @FXML private Canvas canvas;
    
    // The Viewbox scaling model doesn't use canvasContainer for binding
    // @FXML private Pane canvasContainer; 

    private CanvasViewModel viewModel;
    private CanvasRenderer renderer;
    private boolean isUpdatingUI = false;
    
    // This label is no longer in the FXML, but we'll keep the logic
    // in case it's added back to the slider popup.
    private final Label sizeValueLabel = new Label();

    /**
     * Initializes the view/GUI
     */
    public void initialize() {
        viewModel = new CanvasViewModel(new CanvasState());
        renderer = new CanvasRenderer(canvas);

        sizeSlider.setValue(viewModel.activeStrokeWidth.get());
        colorPicker.setValue(viewModel.activeColor.get());
        
        // setupSliderValueDisplay(); // This logic is more complex with a MenuButton

        // --- Bindings & Listeners ---
        sizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (isUpdatingUI) {
                return;
            }

            final double thickness = newVal.doubleValue();
            viewModel.activeStrokeWidth.set(thickness);
            if (viewModel.selectedShapeId.get() != null) {
                viewModel.updateSelectedShapeThickness(thickness);
            }
        });

        // Enable/Disable Delete button based on selection
        deleteBtn.disableProperty().bind(viewModel.selectedShapeId.isNull());
        // Also bind the new regularizeBtn
        regularizeBtn.disableProperty().bind(viewModel.selectedShapeId.isNull());

        viewModel.selectedShapeId.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                ShapeState state = viewModel.getCanvasState().getShapeState(newVal);
                if (state != null && !state.isDeleted()) {
                    isUpdatingUI = true;
                    try {
                        // Update ColorPicker instead of Rectangle
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

        // --- CRITICAL CHANGE ---
        // The Canvas is now at a fixed size inside a Viewbox.
        // We MUST remove the property bindings that resize the canvas.
        
        // REMOVED: canvas.widthProperty().bind(canvasContainer.widthProperty());
        // REMOVED: canvas.heightProperty().bind(canvasContainer.heightProperty());
        
        // We still need to redraw if the canvas *control* dimensions change,
        // but this setup assumes a fixed 1600x900 canvas.
        // If the *Viewbox* resizes, it just scales the static canvas.
        // We only need to redraw on data changes.
        
        // REMOVED: canvas.widthProperty().addListener(o -> redraw());
        // REMOVED: canvas.heightProperty().addListener(o -> redraw());

        // --- Standard Setup ---
        
        // Focus canvas to receive key events
        canvas.setFocusTraversable(true);
        canvas.setOnMouseClicked(e -> canvas.requestFocus());

        // Handle DELETE key
        canvas.setOnKeyPressed(this::onKeyPressed);

        viewModel.setOnCanvasUpdate(this::redraw);

        freehandBtn.setUserData(ToolType.FREEHAND);
        selectBtn.setUserData(ToolType.SELECT);
        rectBtn.setUserData(ToolType.RECTANGLE);
        ellipseBtn.setUserData(ToolType.ELLIPSE);
        lineBtn.setUserData(ToolType.LINE);
        triangleBtn.setUserData(ToolType.TRIANGLE);
        
        // Initial draw
        redraw();
    }

    public CanvasViewModel getViewModel() { return viewModel; }

    private void redraw() {
        // We must pass the fixed width/height to the renderer's clearRect
        renderer.render(viewModel.getCanvasState(), viewModel.getGhostShape(), viewModel.selectedShapeId.get(), viewModel.isDraggingSelection);
    }

    // This setup is more complex with a MenuButton and CustomMenuItem.
    // We'll skip it for now as the XAML also doesn't show the value.
    /*
    private void setupSliderValueDisplay() {
        sizeValueLabel.getStyleClass().add("slider-value-label");
        sizeValueLabel.setMouseTransparent(true);
        sizeValueLabel.textProperty().bind(Bindings.format("%.0f", sizeSlider.valueProperty()));

        sizeSlider.skinProperty().addListener((obs, oldSkin, newSkin) -> attachValueLabelToThumb());
        Platform.runLater(this::attachValueLabelToThumb);
    }

    private void attachValueLabelToThumb() {
        final StackPane thumb = (StackPane) sizeSlider.lookup(".thumb");
        if (thumb != null && !thumb.getChildren().contains(sizeValueLabel)) {
            StackPane.setAlignment(sizeValueLabel, Pos.CENTER);
            thumb.getChildren().add(sizeValueLabel);
        }
    }
    */

    // --- FXML Event Handlers ---

    // This handler is for the old button TilePane
    // @FXML
    // private void onColorClick(final ActionEvent event) { ... }
    
    // NEW Handler for the ColorPicker
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
        // This button existed in the FXML, but no logic was in the controller.
        // Add logic here if needed.
        System.out.println("Regularize button clicked (no logic assigned).");
    }

    private void onKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) {
            viewModel.deleteSelectedShape();
        }
    }

    @FXML private void onCanvasMousePressed(final MouseEvent e) {
        viewModel.onMousePressed(e.getX(), e.getY()); redraw();
    }

    @FXML private void onCanvasMouseDragged(final MouseEvent e) {
        viewModel.onMouseDragged(e.getX(), e.getY()); redraw();
    }

    @FXML private void onCanvasMouseReleased(final MouseEvent e) {
        viewModel.onMouseReleased(e.getX(), e.getY()); redraw();
    }

    @FXML private void onUndo() {
        viewModel.undo();
    }

    @FXML private void onRedo() {
        viewModel.redo();
    }
}