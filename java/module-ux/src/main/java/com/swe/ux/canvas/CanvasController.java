package com.swe.ux.canvas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swe.canvas.datamodel.canvas.ShapeState;
import com.swe.canvas.datamodel.manager.ActionManager;
import com.swe.canvas.datamodel.manager.HostActionManager;
import com.swe.canvas.datamodel.serialization.ShapeSerializer;
import com.swe.canvas.datamodel.shape.Shape;
import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.ux.canvas.util.ColorConverter;
import com.swe.ux.viewmodels.CanvasViewModel;
import com.swe.ux.viewmodels.ToolType;

import com.swe.cloud.datastructures.Entity;
import com.swe.cloud.functionlibrary.CloudFunctionLibrary;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.swe.controller.serialize.DataSerializer;

public class CanvasController {

    // ... [Keep existing FXML fields] ...
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
    @FXML private Button captureBtn;
    @FXML private Button saveBtn;
    @FXML private Button restoreBtn;
    @FXML private Button analyzeBtn;
    @FXML private Canvas canvas;
    @FXML private StackPane canvasContainer;
    @FXML private StackPane canvasHolder;
    @FXML private VBox aiSidebar;
    @FXML private TextArea aiDescriptionArea;

    private CanvasViewModel viewModel;
    private ActionManager actionManager;
    private CanvasRenderer renderer;
    private boolean isUpdatingUI = false;
    private AbstractRPC rpc;
    
    private final CloudFunctionLibrary cloudLib = new CloudFunctionLibrary();

    private Translate canvasTranslate;
    private Scale canvasScale;
    private boolean isPanning = false;
    private double panStartX, panStartY;
    private static final double ZOOM_FACTOR = 1.1;
    private static final double MAX_ZOOM = 5.0;
    private static final double MIN_ZOOM = 0.5;

    public void initModel(ActionManager manager, AbstractRPC rpcInstance) {
        this.actionManager = manager;
        this.rpc = rpcInstance;
        this.viewModel = new CanvasViewModel("user-" + System.nanoTime() % 10000, manager);

        initializeControls();

        boolean isHost = (manager instanceof HostActionManager);
        if (saveBtn != null) { saveBtn.setVisible(isHost); saveBtn.setManaged(isHost); }
        if (restoreBtn != null) { restoreBtn.setVisible(isHost); restoreBtn.setManaged(isHost); }
        
        // --- NEW: Trigger initialization sequence (Handshake) ---
        this.actionManager.initialize();
    }

    // ... [Rest of the file remains identical to previous versions] ...
    private void initializeControls() {
        renderer = new CanvasRenderer(canvas);
        canvas.widthProperty().bind(canvasHolder.widthProperty());
        canvas.heightProperty().bind(canvasHolder.heightProperty());
        canvas.widthProperty().addListener(o -> redraw());
        canvas.heightProperty().addListener(o -> redraw());

        canvasTranslate = new Translate();
        canvasScale = new Scale();
        canvasHolder.getTransforms().addAll(canvasTranslate, canvasScale);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(canvasContainer.widthProperty());
        clip.heightProperty().bind(canvasContainer.heightProperty());
        canvasContainer.setClip(clip);

        sizeSlider.setValue(viewModel.activeStrokeWidth.get());
        colorPicker.setValue(viewModel.activeColor.get());

        sizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (isUpdatingUI) return;
            viewModel.activeStrokeWidth.set(newVal.doubleValue());
            if (viewModel.selectedShapeId.get() != null) viewModel.updateSelectedShapeThickness(newVal.doubleValue());
        });
        
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
                    } finally { isUpdatingUI = false; }
                }
            }
            redraw();
        });

        this.actionManager.setOnUpdate(() -> Platform.runLater(() -> {
            if (viewModel != null) viewModel.handleValidatedUpdate();
            redraw();
        }));

        freehandBtn.setUserData(ToolType.FREEHAND);
        selectBtn.setUserData(ToolType.SELECT);
        rectBtn.setUserData(ToolType.RECTANGLE);
        ellipseBtn.setUserData(ToolType.ELLIPSE);
        lineBtn.setUserData(ToolType.LINE);
        triangleBtn.setUserData(ToolType.TRIANGLE);

        redraw();
    }

    @FXML
    private void onScroll(ScrollEvent event) {
        event.consume();
        double delta = event.getDeltaY();
        if (delta == 0) return;
        double zoomFactor = (delta > 0) ? ZOOM_FACTOR : (1.0 / ZOOM_FACTOR);
        double newScale = canvasScale.getX() * zoomFactor;
        newScale = Math.max(MIN_ZOOM, Math.min(newScale, MAX_ZOOM));
        Point2D mouseLogicalCoords = canvas.sceneToLocal(event.getSceneX(), event.getSceneY());
        canvasScale.setPivotX(mouseLogicalCoords.getX());
        canvasScale.setPivotY(mouseLogicalCoords.getY());
        canvasScale.setX(newScale);
        canvasScale.setY(newScale);
    }

    @FXML private void onViewportMousePressed(MouseEvent event) {
        if (event.isSecondaryButtonDown()) {
            isPanning = true;
            panStartX = event.getSceneX();
            panStartY = event.getSceneY();
            event.consume();
        }
    }

    @FXML private void onViewportMouseDragged(MouseEvent event) {
        if (isPanning && event.isSecondaryButtonDown()) {
            double dx = event.getSceneX() - panStartX;
            double dy = event.getSceneY() - panStartY;
            canvasTranslate.setX(canvasTranslate.getX() + dx);
            canvasTranslate.setY(canvasTranslate.getY() + dy);
            panStartX = event.getSceneX();
            panStartY = event.getSceneY();
            event.consume();
        }
    }

    @FXML private void onViewportMouseReleased(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            isPanning = false;
            event.consume();
        }
    }

    @FXML private void onCanvasMousePressed(MouseEvent e) { 
        if (e.isPrimaryButtonDown()) {
            Point2D p = canvas.sceneToLocal(e.getSceneX(), e.getSceneY());
            viewModel.onMousePressed(p.getX(), p.getY());
            redraw();
        }
    }
    @FXML private void onCanvasMouseDragged(MouseEvent e) {
        if (e.isPrimaryButtonDown()) {
            Point2D p = canvas.sceneToLocal(e.getSceneX(), e.getSceneY());
            viewModel.onMouseDragged(p.getX(), p.getY());
            redraw();
        }
    }
    @FXML private void onCanvasMouseReleased(MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY) {
            Point2D p = canvas.sceneToLocal(e.getSceneX(), e.getSceneY());
            viewModel.onMouseReleased(p.getX(), p.getY());
            redraw();
        }
    }
    @FXML private void onColorSelected(ActionEvent event) { 
        if(!isUpdatingUI) {
            viewModel.activeColor.set(colorPicker.getValue());
            if(viewModel.selectedShapeId.get() != null) viewModel.updateSelectedShapeColor(colorPicker.getValue());
            redraw();
        }
    }
    @FXML private void onToolSelected(ActionEvent event) {
        ToggleButton source = (ToggleButton) event.getSource();
        if (source.getUserData() != null) {
            viewModel.activeTool.set((ToolType) source.getUserData());
            if(viewModel.activeTool.get() != ToolType.SELECT) viewModel.selectedShapeId.set(null);
            redraw();
        }
    }
    @FXML private void onDelete() { viewModel.deleteSelectedShape(); }
    @FXML private void onUndo() { viewModel.undo(); }
    @FXML private void onRedo() { viewModel.redo(); }
    
    @FXML private void onCapture() { 
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Canvas as PNG");
        fileChooser.setInitialFileName("canvas-capture.png");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png"));
        File file = fileChooser.showSaveDialog(canvas.getScene().getWindow());
        if (file != null) {
            try {
                WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
                canvas.snapshot(null, writableImage);
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
                ImageIO.write(bufferedImage, "png", file);
                System.out.println("Canvas captured and saved to: " + file.getAbsolutePath());
            } catch (Exception ex) {
                System.err.println("Error capturing or saving canvas: " + ex.getMessage());
            }
        }
    }

    private void redraw() {
        Platform.runLater(() -> {
            if (renderer != null && viewModel != null) {
                renderer.render(viewModel.getCanvasState(), viewModel.getTransientShape(), viewModel.selectedShapeId.get(), viewModel.isDraggingSelection);
            }
        });
    }

    @FXML
    private void onSave() {
        if (actionManager == null) return;

        ChoiceDialog<String> dialog = new ChoiceDialog<>("Cloud", "Cloud", "Local File");
        dialog.setTitle("Save Canvas");
        dialog.setHeaderText("Choose Storage Method");
        
        dialog.showAndWait().ifPresent(result -> {
            try {
                String jsonState = actionManager.saveMap();

                if ("Local File".equals(result)) {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setInitialFileName("canvas.json");
                    File file = fileChooser.showSaveDialog(canvas.getScene().getWindow());
                    if (file != null) java.nio.file.Files.writeString(file.toPath(), jsonState);
                } else {
                    String boardId = UUID.randomUUID().toString();
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode dataNode = mapper.readTree(jsonState);

                    Entity req = new Entity("CANVAS", "saves", boardId, "state", -1, null, dataNode);

                    cloudLib.cloudPost(req).thenAccept(res -> {
                        Platform.runLater(() -> {
                            TextInputDialog info = new TextInputDialog(boardId);
                            info.setTitle("Saved to Cloud");
                            info.setHeaderText("Canvas Saved Successfully!");
                            info.setContentText("Your Board ID (Copy this to restore):");
                            // info.setEditable(false);
                            info.showAndWait();
                        });
                    }).exceptionally(ex -> {
                        ex.printStackTrace();
                        return null;
                    });
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });
    }

    @FXML
    private void onRestore() {
        if (actionManager == null) return;

        ChoiceDialog<String> dialog = new ChoiceDialog<>("Cloud", "Cloud", "Local File");
        dialog.setTitle("Restore Canvas");
        dialog.setHeaderText("Choose Source");

        dialog.showAndWait().ifPresent(result -> {
            try {
                if ("Local File".equals(result)) {
                    FileChooser fileChooser = new FileChooser();
                    File file = fileChooser.showOpenDialog(canvas.getScene().getWindow());
                    if (file != null) {
                        String json = java.nio.file.Files.readString(file.toPath());
                        actionManager.restoreMap(json);
                    }
                } else {
                    TextInputDialog idInput = new TextInputDialog();
                    idInput.setTitle("Cloud Restore");
                    idInput.setHeaderText("Enter the Board ID");
                    idInput.setContentText("Board UUID:");

                    idInput.showAndWait().ifPresent(boardId -> {
                        if (boardId.trim().isEmpty()) return;

                        Entity req = new Entity("CANVAS", "saves", boardId.trim(), null, -1, null, null);
                        
                        cloudLib.cloudGet(req).thenAccept(res -> {
                            if (res.data() != null) {
                                String jsonState = null;
                                if (res.data().isArray() && !res.data().isEmpty()) {
                                    jsonState = res.data().get(0).get("data").toString();
                                } else if (res.data().isObject() && res.data().has("data")) {
                                    jsonState = res.data().get("data").toString();
                                }

                                if (jsonState != null) {
                                    String finalState = jsonState;
                                    Platform.runLater(() -> {
                                        actionManager.restoreMap(finalState);
                                        new Alert(Alert.AlertType.INFORMATION, "Restored successfully!").show();
                                    });
                                } else {
                                    Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Invalid data format.").show());
                                }
                            } else {
                                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Board ID not found.").show());
                            }
                        });
                    });
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });
    }

    // [Keep onRegularize and onDescribe as corrected in previous step]
    @FXML 
    private void onRegularize() { 
        if (viewModel.selectedShapeId.get() == null || rpc == null) {
            System.err.println("Regularize skipped: No selection or RPC missing.");
            return;
        }
        
        ShapeState state = viewModel.getCanvasState().getShapeState(viewModel.selectedShapeId.get());
        if (state == null || state.isDeleted()) return;

        String shapeJson = ShapeSerializer.serializeShape(state);
        
        CompletableFuture.runAsync(() -> {
            try {
                byte[] data = DataSerializer.serialize(shapeJson); 
                byte[] response = rpc.call("canvas:regularize", data).get();
                
                if (response != null && response.length > 0) {
                    String updatedShapeJson = DataSerializer.deserialize(response, String.class);
                    ShapeState validatedState = ShapeSerializer.deserializeShape(updatedShapeJson);
                    
                    if (validatedState != null && validatedState.getShape() != null) {
                        Platform.runLater(() -> {
                            actionManager.requestModify(state, validatedState.getShape());
                            viewModel.selectedShapeId.set(state.getShapeId());
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void onAnalyze() {
        if (rpc == null) {
            new Alert(Alert.AlertType.ERROR, "RPC not initialized.").show();
            return;
        }

        aiSidebar.setVisible(true);
        aiSidebar.setManaged(true);
        aiDescriptionArea.setText("Analyzing canvas... please wait.");

        try {
            File tempFile = File.createTempFile("canvas_snapshot_", ".png");
            int width = (int) canvas.getWidth();
            int height = (int) canvas.getHeight();
            if (width <= 0) width = 800;
            if (height <= 0) height = 600;

            WritableImage writableImage = new WritableImage(width, height);
            canvas.snapshot(null, writableImage);
            
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
            ImageIO.write(bufferedImage, "png", tempFile);
            
            CompletableFuture.runAsync(() -> {
                try {
                    byte[] data = DataSerializer.serialize(tempFile.getAbsolutePath());
                    byte[] response = rpc.call("canvas:describe", data).get();
                    final String finalDescription = DataSerializer.deserialize(response, String.class);

                    Platform.runLater(() -> {
                        aiDescriptionArea.setText(finalDescription);
                        tempFile.deleteOnExit(); 
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> aiDescriptionArea.setText("Error analyzing image: " + e.getMessage()));
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            aiDescriptionArea.setText("Failed to capture canvas: " + e.getMessage());
        }
    }

    @FXML
    private void onCloseAiSidebar() {
        aiSidebar.setVisible(false);
        aiSidebar.setManaged(false);
        aiDescriptionArea.clear();
    }
}