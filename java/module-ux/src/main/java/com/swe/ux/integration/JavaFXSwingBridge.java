/*
 * -----------------------------------------------------------------------------
 *  File: JavaFXSwingBridge.java
 *  Owner: Vaibhav Yadav
 *  Roll Number : 142201015
 *  Module : UX
 *
 * -----------------------------------------------------------------------------
 */
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
     */
    public static void initializeJavaFX() {
        if (!javaFXInitialized) {
            Platform.setImplicitExit(false);
            // Initialize JavaFX toolkit
            Platform.startup(() -> {});
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
                // Load FXML
                FXMLLoader loader = new FXMLLoader(
                    ClassLoader.getSystemClassLoader().getResource(fxmlPath)
                );
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
                    String cssUrl = ClassLoader.getSystemClassLoader()
                        .getResource(cssPath).toExternalForm();
                    scene.getStylesheets().add(cssUrl);
                }

                // Create stage
                Stage stage = new Stage();
                stage.setTitle(title);
                stage.setScene(scene);
                stage.setMinWidth(800);
                stage.setMinHeight(600);

                // Notify stage created
                if (onStageCreated != null) {
                    onStageCreated.accept(stage);
                }

                stage.show();
                stage.toFront();
                stage.requestFocus();

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

