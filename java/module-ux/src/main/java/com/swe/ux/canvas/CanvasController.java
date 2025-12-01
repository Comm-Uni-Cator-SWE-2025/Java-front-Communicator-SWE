/*
 * -----------------------------------------------------------------------------
 *  File: CanvasController.java
 *  Owner: Darla Manohar
 *  Roll Number: 112201034
 *  Module: Canvas
 *
 * -----------------------------------------------------------------------------
 */

package com.swe.ux.canvas;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swe.canvas.datamodel.canvas.ShapeState;
import com.swe.canvas.datamodel.manager.ActionManager;
import com.swe.canvas.datamodel.manager.HostActionManager;
import com.swe.canvas.datamodel.serialization.ShapeSerializer;
import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.ux.canvas.util.ColorConverter;
import com.swe.ux.viewmodels.CanvasViewModel;
import com.swe.ux.viewmodels.ToolType;

import datastructures.Entity;
import functionlibrary.CloudFunctionLibrary;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;

/**
 * Controller for the fxml view.
 * Includes logic for Drawing, Pan/Zoom, and Host-specific Save/Restore.
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
    @FXML private Button captureBtn;

    // --- NEW BUTTONS ---
    @FXML private Button saveBtn;
    @FXML private Button restoreBtn;

    @FXML private Canvas canvas;
    @FXML private StackPane canvasContainer;
    @FXML private StackPane canvasHolder;
    @FXML private Button describeBtn; // Added new button reference

    private CanvasViewModel viewModel;
    private ActionManager actionManager;
    private CanvasRenderer renderer;
    private boolean isUpdatingUI = false;

    private AbstractRPC rpc; // Added RPC reference
    private CloudFunctionLibrary cloudLib; // Cloud Library

    // --- Pan and Zoom State ---
    private Translate canvasTranslate;
    private Scale canvasScale;
    private boolean isPanning = false;
    private double panStartX, panStartY;

    private static final double ZOOM_FACTOR = 1.1;
    private static final double MAX_ZOOM = 5.0;
    private static final double MIN_ZOOM = 0.5;

    public void initModel(ActionManager manager, AbstractRPC rpcInstance) {
        this.actionManager = manager;
        this.rpc = rpcInstance; // Store RPC
        this.cloudLib = new CloudFunctionLibrary();
        this.viewModel = new CanvasViewModel("user-" + System.nanoTime() % 10000, manager);

        initializeControls();

        // --- Host vs Client UI Logic ---
        // If we are the Host, show Save/Restore buttons
        if (manager instanceof HostActionManager) {
            saveBtn.setVisible(true);
            saveBtn.setManaged(true);
            restoreBtn.setVisible(true);
            restoreBtn.setManaged(true);
        } else {
            saveBtn.setVisible(false);
            saveBtn.setManaged(false);
            restoreBtn.setVisible(false);
            restoreBtn.setManaged(false);
        }
    }

    @FXML
    public void initialize() {
        // Logic moved to initModel/initializeControls
    }

    private void initializeControls() {
        renderer = new CanvasRenderer(canvas);

        canvas.widthProperty().bind(canvasHolder.widthProperty());
        canvas.heightProperty().bind(canvasHolder.heightProperty());
        
        // Redraw when size changes
        canvas.widthProperty().addListener(o -> redraw());
        canvas.heightProperty().addListener(o -> redraw());

        // --- Setup Transforms for Pan and Zoom ---
        canvasTranslate = new Translate();
        canvasScale = new Scale();
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
        canvasContainer.setOnMouseClicked(e -> canvas.requestFocus());
        canvasContainer.setOnKeyPressed(this::onKeyPressed);

        this.actionManager.setOnUpdate(() -> {
            Platform.runLater(() -> {
                if (viewModel != null) {
                    viewModel.handleValidatedUpdate();
                }
                redraw();
            });
        });

        // Set UserData for tool selection
        freehandBtn.setUserData(ToolType.FREEHAND);
        selectBtn.setUserData(ToolType.SELECT);
        rectBtn.setUserData(ToolType.RECTANGLE);
        ellipseBtn.setUserData(ToolType.ELLIPSE);
        lineBtn.setUserData(ToolType.LINE);
        triangleBtn.setUserData(ToolType.TRIANGLE);

        redraw();
    }

    @FXML 
    private void onRegularize() { 
        if (viewModel.selectedShapeId.get() == null || rpc == null) return;

        System.out.println("Regularizing selected shape...");
        
        // 1. Get the current shape state
        ShapeState state = viewModel.getCanvasState().getShapeState(viewModel.selectedShapeId.get());
        if (state == null || state.isDeleted()) return;

        // 2. Serialize the shape
        String shapeJson = ShapeSerializer.serializeShape(state);
        
        // 3. Call Core RPC
        CompletableFuture.runAsync(() -> {
            try {
                // "core/regularizeShape" is the custom RPC you made in core
                byte[] response = rpc.call("core/regularizeShape", shapeJson.getBytes(StandardCharsets.UTF_8)).get();
                
                if (response != null && response.length > 0) {
                    String updatedShapeJson = new String(response, StandardCharsets.UTF_8);
                    
                    // 4. Deserialize and update Canvas
                    ShapeState validatedState = ShapeSerializer.deserializeShape(updatedShapeJson);
                    if (validatedState != null && validatedState.getShape() != null) {
                        Platform.runLater(() -> {
                            // Update via ActionManager to ensure consistency
                            actionManager.requestModify(state, validatedState.getShape());
                            System.out.println("Shape regularized successfully.");
                        });
                    }
                }
            } catch (Exception e) {
                System.err.println("AI Regularization failed: " + e.getMessage());
            }
        });
    }

    @FXML
    private void onDescribe() {
        if (rpc == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Analyze");
        File file = fileChooser.showOpenDialog(canvas.getScene().getWindow());

        if (file != null) {
            String path = file.getAbsolutePath();
            System.out.println("Sending file path to AI: " + path);

            CompletableFuture.runAsync(() -> {
                try {
                    // Send path to core RPC
                    byte[] response = rpc.call("core/describeImage", path.getBytes(StandardCharsets.UTF_8)).get();
                    String description = new String(response, StandardCharsets.UTF_8);

                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("AI Analysis Result");
                        alert.setHeaderText("Description of " + file.getName());
                        alert.setContentText(description);
                        alert.showAndWait();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("Analysis failed: " + e.getMessage());
                        alert.show();
                    });
                }
            });
        }
    }

    @FXML
    private void onSave() {
        if (actionManager == null) return;

        // Choice dialog: Cloud or Local
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Cloud", "Cloud", "Local File");
        dialog.setTitle("Save Canvas");
        dialog.setHeaderText("Where do you want to save the canvas state?");
        dialog.setContentText("Storage Type:");

        dialog.showAndWait().ifPresent(result -> {
            try {
                String jsonState = actionManager.saveMap();

                if ("Local File".equals(result)) {
                    // ... Existing Local Save Logic ...
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setInitialFileName("canvas.json");
                    File file = fileChooser.showSaveDialog(canvas.getScene().getWindow());
                    if (file != null) java.nio.file.Files.writeString(file.toPath(), jsonState);
                } else {
                    // --- CLOUD SAVE ---
                    String boardId = UUID.randomUUID().toString();
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode dataNode = mapper.readTree(jsonState);
                    
                    // Create Entity for Cloud module
                    Entity req = new Entity("Canvas", "Snapshots", boardId, null, -1, null, dataNode);
                    
                    // Call cloud library
                    cloudLib.cloudPost(req).thenAccept(res -> {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Saved to Cloud! ID: " + boardId);
                            alert.show();
                        });
                    }).exceptionally(ex -> {
                        ex.printStackTrace();
                        return null;
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    @FXML
    private void onRestore() {
        if (actionManager == null) return;

        ChoiceDialog<String> dialog = new ChoiceDialog<>("Cloud", "Cloud", "Local File");
        dialog.setTitle("Restore Canvas");
        dialog.setHeaderText("Source?");
        dialog.showAndWait().ifPresent(result -> {
            try {
                if ("Local File".equals(result)) {
                    // ... Existing Local Restore Logic ...
                    FileChooser fileChooser = new FileChooser();
                    File file = fileChooser.showOpenDialog(canvas.getScene().getWindow());
                    if (file != null) {
                        String json = java.nio.file.Files.readString(file.toPath());
                        actionManager.restoreMap(json);
                    }
                } else {
                    // --- CLOUD RESTORE ---
                    
                    
                    // Create Request Entity
                    Entity req = new Entity("Canvas", "Snapshots", "", null, 1, null, null);

                    cloudLib.cloudGet(req).thenAccept(res -> {
                        if (res.data() != null) {
                            String jsonState = null;
                            
                            // 1. Check if the response is an Array (since you requested with empty ID)
                            if (res.data().isArray() && !res.data().isEmpty()) {
                                // Get the first snapshot (latest one) and extract its "data" field
                                jsonState = res.data().get(0).get("data").toString();
                            } 
                            // 2. Fallback: Check if it's a single Object (in case backend behavior changes)
                            else if (res.data().isObject() && res.data().has("data")) {
                                jsonState = res.data().get("data").toString();
                            }

                            // 3. Restore only if we successfully extracted the state
                            if (jsonState != null) {
                                final String stateToRestore = jsonState;
                                Platform.runLater(() -> {
                                    try {
                                        actionManager.restoreMap(stateToRestore);
                                        System.out.println("Restored from cloud.");
                                    } catch (Exception e) {
                                        System.err.println("Failed to restore map: " + e.getMessage());
                                        e.printStackTrace();
                                    }
                                });
                            } else {
                                System.out.println("No snapshots found to restore.");
                            }
                        }
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }


    private void redraw() {
        Platform.runLater(() -> {
            if (renderer != null && viewModel != null) {
                renderer.render(viewModel.getCanvasState(), viewModel.getTransientShape(), viewModel.selectedShapeId.get(), viewModel.isDraggingSelection);
            }
        });
    }

    // =========================================================================
    // --- Pan and Zoom Handlers ---
    // =========================================================================
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

    @FXML
    private void onViewportMousePressed(MouseEvent event) {
        if (event.isSecondaryButtonDown()) {
            isPanning = true;
            panStartX = event.getSceneX();
            panStartY = event.getSceneY();
            event.consume();
        }
    }

    @FXML
    private void onViewportMouseDragged(MouseEvent event) {
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

    @FXML
    private void onViewportMouseReleased(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            isPanning = false;
            event.consume();
        }
    }

    // =========================================================================
    // --- Drawing Handlers ---
    // =========================================================================
    private Point2D getLogicalCoords(MouseEvent event) {
        return canvas.sceneToLocal(event.getSceneX(), event.getSceneY());
    }

    @FXML
    private void onCanvasMousePressed(final MouseEvent e) {
        if (e.isPrimaryButtonDown()) {
            Point2D logicalCoords = getLogicalCoords(e);
            viewModel.onMousePressed(logicalCoords.getX(), logicalCoords.getY());
            redraw();
            e.consume();
        }
    }

    @FXML
    private void onCanvasMouseDragged(final MouseEvent e) {
        if (e.isPrimaryButtonDown()) {
            Point2D logicalCoords = getLogicalCoords(e);
            viewModel.onMouseDragged(logicalCoords.getX(), logicalCoords.getY());
            redraw();
            e.consume();
        }
    }

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
    // --- Toolbar Button Handlers ---
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

    @FXML private void onDelete() { viewModel.deleteSelectedShape(); }
    @FXML private void onUndo() { viewModel.undo(); }
    @FXML private void onRedo() { viewModel.redo(); }

    private void onKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) {
            viewModel.deleteSelectedShape();
        }
    }

    @FXML
    private void onCapture() {
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
            } catch (IOException ex) {
                System.err.println("Error capturing or saving canvas: " + ex.getMessage());
            }
        }
    }

    // =========================================================================
    // --- HOST ONLY: SAVE & RESTORE ---
    // =========================================================================

    // @FXML
    // private void onSave() {
    //     if (actionManager == null) return;

    //     FileChooser fileChooser = new FileChooser();
    //     fileChooser.setTitle("Save Canvas State");
    //     fileChooser.setInitialFileName("canvas-state.json");
    //     fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json"));
    //     File file = fileChooser.showSaveDialog(canvas.getScene().getWindow());

    //     if (file != null) {
    //         try {
    //             String json = actionManager.saveMap();
    //             Files.writeString(file.toPath(), json);
    //             System.out.println("State saved to: " + file.getAbsolutePath());
    //         } catch (Exception ex) {
    //             System.err.println("Error saving state: " + ex.getMessage());
    //         }
    //     }
    // }

    // @FXML
    // private void onRestore() {
    //     if (actionManager == null) return;

    //     FileChooser fileChooser = new FileChooser();
    //     fileChooser.setTitle("Restore Canvas State");
    //     fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json"));
    //     File file = fileChooser.showOpenDialog(canvas.getScene().getWindow());

    //     if (file != null) {
    //         try {
    //             String json = Files.readString(file.toPath());
    //             actionManager.restoreMap(json);
    //             System.out.println("State restored from: " + file.getAbsolutePath());
    //         } catch (Exception ex) {
    //             System.err.println("Error restoring state: " + ex.getMessage());
    //         }
    //     }
    // }
}