package com.swe.ux.service;

import com.swe.canvas.datamodel.canvas.CanvasState;
import com.swe.canvas.datamodel.canvas.ShapeState;
import com.swe.ux.viewmodel.CanvasViewModel;
import com.swe.ux.viewmodel.ToolType;

import com.swe.canvas.ui.CanvasRenderer;
import com.swe.canvas.ui.util.ColorConverter;
import com.swe.networking.AbstractNetworking;
import com.swe.networking.NetworkFront;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Controller for the fxml view
 */
public class CanvasController {

    /**
     * The select button
     */
    @FXML
    private ToggleButton selectBtn;

    /**
     * The freehand draw button
     */
    @FXML
    private ToggleButton freehandBtn;

    /**
     * Rectangle button
     */
    @FXML
    private ToggleButton rectBtn;

    /**
     * Ellipse button
     */
    @FXML private ToggleButton ellipseBtn;

    /**
     * Line button
     */
    @FXML private ToggleButton lineBtn;

    /**
     * Triangle button
     */
    @FXML private ToggleButton triangleBtn;

    /**
     * Slider
     */
    @FXML private Slider sizeSlider;

    /**
     * Current color
     */
    @FXML private Rectangle currentColorRect;

    /**
     * Delete button
     */
    @FXML private Button deleteBtn;

    /**
     * Canvas(Whiteboard) object
     */
    @FXML private Canvas canvas;

    /**
     * Regularize button
     */
    @FXML private Button regularizeBtn;


    /**
     * Container for canvas
     */
    @FXML private Pane canvasContainer;

    private AbstractNetworking network;

    private CanvasViewModel viewModel;
    private CanvasRenderer renderer;
    private boolean isUpdatingUI = false;
    private final Label sizeValueLabel = new Label();


    public void setNetwork(AbstractNetworking abstractNetwork) {

        this.network = abstractNetwork;

        viewModel.setNetworking(abstractNetwork);
        // If viewModel is ready you can subscribe now:
        if (viewModel != null) {
            this.network.subscribe(2, (data) -> {
                Platform.runLater(() -> {
                    viewModel.processIncomingData(data);
                });
            });
        }
    }

    /**
     * Initializes the view/GUI
     */
    public void initialize() {
        viewModel = new CanvasViewModel(new CanvasState());
        renderer = new CanvasRenderer(canvas);

        sizeSlider.setValue(viewModel.activeStrokeWidth.get());
        currentColorRect.setFill(viewModel.activeColor.get());
    setupSliderValueDisplay();

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
        regularizeBtn.disableProperty().bind(viewModel.selectedShapeId.isNull());

        viewModel.selectedShapeId.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                ShapeState state = viewModel.getCanvasState().getShapeState(newVal);
                if (state != null && !state.isDeleted()) {
                    isUpdatingUI = true;
                    try {
                        currentColorRect.setFill(ColorConverter.toFx(state.getShape().getColor()));
                        sizeSlider.setValue(state.getShape().getThickness());
                        viewModel.activeColor.set((Color) currentColorRect.getFill());
                        viewModel.activeStrokeWidth.set(sizeSlider.getValue());
                    } finally {
                        isUpdatingUI = false;
                    }
                }
            }
            redraw();
        });

        // --- Standard Setup ---
        canvas.widthProperty().bind(canvasContainer.widthProperty());
        canvas.heightProperty().bind(canvasContainer.heightProperty());
        canvas.widthProperty().addListener(o -> redraw());
        canvas.heightProperty().addListener(o -> redraw());

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

    }

    public CanvasViewModel getViewModel() { return viewModel; }

    private void redraw() {
        renderer.render(viewModel.getCanvasState(), viewModel.getGhostShape(), viewModel.selectedShapeId.get(), viewModel.isDraggingSelection);
    }

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

    // --- FXML Event Handlers ---

    @FXML
    private void onColorClick(final ActionEvent event) {
        final Object data = ((Button) event.getSource()).getUserData();
        if (data instanceof String) {
            final Color selectedColor = Color.web((String) data);
            currentColorRect.setFill(selectedColor);
            viewModel.activeColor.set(selectedColor);
            if (viewModel.selectedShapeId.get() != null) {
                viewModel.updateSelectedShapeColor(selectedColor);
            }
            redraw();
        }
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

    /**
     * Regularize the currently selected freehand shape.
     * <p>
     * This handler uses a default of 6 sides. You can change the hard-coded
     * value or show a dialog to ask the user for a value if desired.
     * </p>
     */
    @FXML private void onRegularize() {
        viewModel.regularizeSelectedShape();
        redraw();
    }
}