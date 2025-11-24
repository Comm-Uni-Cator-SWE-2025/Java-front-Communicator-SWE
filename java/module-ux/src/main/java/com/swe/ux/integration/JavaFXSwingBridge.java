package com.swe.ux.integration;

import java.io.IOException;
import java.util.function.Consumer;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

/**
 * Bridge class to handle JavaFX and Swing integration
 * Provides a clean way to launch JavaFX windows from Swing applications
 * without using the problematic JFXPanel on macOS
 */
public class JavaFXSwingBridge {

    private static boolean javaFXInitialized = false;

    /**
     * Initialize JavaFX toolkit (call once at app startup)
     * Must be called from a non-JavaFX thread
     */
    public static void initializeJavaFX() {
        if (!javaFXInitialized) {
            Platform.setImplicitExit(false);
            // Try to initialize JavaFX toolkit, but catch exception if already initialized
            try {
                // Check if already running
                if (!Platform.isFxApplicationThread()) {
                    Platform.startup(() -> {
                        // Empty startup callback
                    });
                }
            } catch (IllegalStateException e) {
                // JavaFX toolkit is already initialized (e.g., by another part of the app)
                // This is fine, we can continue using it
                System.out.println("JavaFX toolkit already initialized, continuing...");
            }
            javaFXInitialized = true;
        }
    }

    /**
     * Launch a JavaFX window with FXML from Swing
     * 
     * @param fxmlPath Path to FXML file (e.g., "com/swe/canvas/fxml/canvas-view.fxml")
     * @param cssPath Path to CSS file (e.g., "com/swe/canvas/fxml/canvas-view.css")
     * @param title Window title
     * @param width Initial width
     * @param height Initial height
     * @param onControllerLoaded Callback when controller is loaded
     * @param onStageCreated Callback when stage is created
     */
    public static void launchFXMLWindow(
            String fxmlPath,
            String cssPath,
            String title,
            int width,
            int height,
            Consumer<Object> onControllerLoaded,
            Consumer<Stage> onStageCreated
    ) {
        initializeJavaFX();
        
        Platform.runLater(() -> {
            try {
                // Load FXML - try multiple classloaders to find the resource
                java.net.URL fxmlUrl = ClassLoader.getSystemClassLoader().getResource(fxmlPath);
                if (fxmlUrl == null) {
                    fxmlUrl = JavaFXSwingBridge.class.getClassLoader().getResource(fxmlPath);
                }
                if (fxmlUrl == null) {
                    fxmlUrl = Thread.currentThread().getContextClassLoader().getResource(fxmlPath);
                }
                
                if (fxmlUrl == null) {
                    throw new IOException("FXML resource not found: " + fxmlPath);
                }
                
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Parent root = loader.load();
                Object controller = loader.getController();

                // Notify controller loaded
                if (onControllerLoaded != null) {
                    onControllerLoaded.accept(controller);
                }

                // Create scene
                Scene scene = new Scene(root, width, height);

                // Load CSS if provided
                if (cssPath != null && !cssPath.isEmpty()) {
                    java.net.URL cssUrl = ClassLoader.getSystemClassLoader().getResource(cssPath);
                    if (cssUrl == null) {
                        cssUrl = JavaFXSwingBridge.class.getClassLoader().getResource(cssPath);
                    }
                    if (cssUrl == null) {
                        cssUrl = Thread.currentThread().getContextClassLoader().getResource(cssPath);
                    }
                    if (cssUrl != null) {
                        scene.getStylesheets().add(cssUrl.toExternalForm());
                    } else {
                        System.err.println("Warning: CSS resource not found: " + cssPath);
                    }
                }

                // Create stage with minimal operations to avoid macOS conflicts
                Stage stage = new Stage();
                stage.setTitle(title);
                stage.setScene(scene);
                
                // Set minimum sizes AFTER scene is set (macOS requirement)
                Platform.runLater(() -> {
                    stage.setMinWidth(800);
                    stage.setMinHeight(600);
                });

                // Notify stage created
                if (onStageCreated != null) {
                    onStageCreated.accept(stage);
                }

                // Show stage - minimal operations to avoid macOS window management crashes
                // Don't use toFront() or requestFocus() as they trigger NSTrackingRectTag errors
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                        null,
                        "<html><b>Failed to open window</b><br/><br/>" +
                        "Error: " + e.getMessage() + "<br/><br/>" +
                        "Please try again or restart the application.</html>",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                });
            }
        });
    }

    /**
     * Setup common keyboard shortcuts for a JavaFX scene
     */
    public static void setupKeyboardShortcuts(Scene scene, Runnable onUndo, Runnable onRedo) {
        if (onUndo != null) {
            scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN),
                onUndo
            );
        }
        if (onRedo != null) {
            scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN),
                onRedo
            );
        }
    }

    /**
     * Run code on JavaFX thread safely
     */
    public static void runOnFXThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }

    /**
     * Run code on Swing EDT safely
     */
    public static void runOnSwingThread(Runnable action) {
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
        } else {
            SwingUtilities.invokeLater(action);
        }
    }
}

