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
 * Bridge class to handle JavaFX and Swing integration.
 * Provides a clean way to launch JavaFX windows from Swing applications
 * without using the problematic JFXPanel on macOS.
 */
public class JavaFXSwingBridge {

    /** Minimum window width for JavaFX stages. */
    private static final int MIN_WINDOW_WIDTH = 800;
    /** Minimum window height for JavaFX stages. */
    private static final int MIN_WINDOW_HEIGHT = 600;

    /** Flag indicating if JavaFX has been initialized. */
    private static boolean javaFXInitialized = false;

    /**
     * Initialize JavaFX toolkit (call once at app startup).
     * Must be called from a non-JavaFX thread.
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
     * Launch a JavaFX window with FXML from Swing.
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
            final String fxmlPath,
            final String cssPath,
            final String title,
            final int width,
            final int height,
            final Consumer<Object> onControllerLoaded,
            final Consumer<Stage> onStageCreated
    ) {
        initializeJavaFX();
        
        Platform.runLater(() -> {
            try {
                final java.net.URL fxmlUrl = loadResource(fxmlPath);
                
                final FXMLLoader loader = new FXMLLoader(fxmlUrl);
                final Parent root = loader.load();
                final Object controller = loader.getController();

                // Notify controller loaded
                if (onControllerLoaded != null) {
                    onControllerLoaded.accept(controller);
                }

                // Create scene
                final Scene scene = new Scene(root, width, height);

                // Load CSS if provided
                loadCssForScene(scene, cssPath);

                // Create stage with minimal operations to avoid macOS conflicts
                final Stage stage = createAndConfigureStage(title, scene);

                // Notify stage created
                if (onStageCreated != null) {
                    onStageCreated.accept(stage);
                }

                // Show stage - minimal operations to avoid macOS window management crashes
                // Don't use toFront() or requestFocus() as they trigger NSTrackingRectTag errors
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                showErrorDialog(e);
            }
        });
    }

    /**
     * Loads a resource using multiple classloaders.
     *
     * @param resourcePath the path to the resource
     * @return the URL of the resource
     * @throws IOException if the resource cannot be found
     */
    private static java.net.URL loadResource(final String resourcePath) throws IOException {
        java.net.URL url = ClassLoader.getSystemClassLoader().getResource(resourcePath);
        if (url == null) {
            url = JavaFXSwingBridge.class.getClassLoader().getResource(resourcePath);
        }
        if (url == null) {
            url = Thread.currentThread().getContextClassLoader().getResource(resourcePath);
        }
        
        if (url == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }
        
        return url;
    }

    /**
     * Loads CSS stylesheet for a JavaFX scene.
     *
     * @param scene the scene to apply CSS to
     * @param cssPath the path to the CSS file
     */
    private static void loadCssForScene(final Scene scene, final String cssPath) {
        if (cssPath != null && !cssPath.isEmpty()) {
            try {
                final java.net.URL cssUrl = loadResource(cssPath);
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } catch (IOException e) {
                System.err.println("Warning: CSS resource not found: " + cssPath);
            }
        }
    }

    /**
     * Creates and configures a JavaFX stage.
     *
     * @param title the window title
     * @param scene the scene to set on the stage
     * @return the configured stage
     */
    private static Stage createAndConfigureStage(final String title, final Scene scene) {
        final Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(scene);
        
        // Set minimum sizes AFTER scene is set (macOS requirement)
        Platform.runLater(() -> {
            stage.setMinWidth(MIN_WINDOW_WIDTH);
            stage.setMinHeight(MIN_WINDOW_HEIGHT);
        });
        
        return stage;
    }

    /**
     * Shows an error dialog on the Swing EDT.
     *
     * @param exception the exception to display
     */
    private static void showErrorDialog(final IOException exception) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                null,
                "<html><b>Failed to open window</b><br/><br/>"
                    + "Error: " + exception.getMessage() + "<br/><br/>"
                    + "Please try again or restart the application.</html>",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        });
    }

    /**
     * Setup common keyboard shortcuts for a JavaFX scene.
     *
     * @param scene the scene to add shortcuts to
     * @param onUndo the runnable to execute on undo command
     * @param onRedo the runnable to execute on redo command
     */
    public static void setupKeyboardShortcuts(final Scene scene, final Runnable onUndo,
                                              final Runnable onRedo) {
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
     * Run code on JavaFX thread safely.
     *
     * @param action the action to run on the JavaFX thread
     */
    public static void runOnFXThread(final Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }

    /**
     * Run code on Swing EDT safely.
     *
     * @param action the action to run on the Swing EDT
     */
    public static void runOnSwingThread(final Runnable action) {
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
        } else {
            SwingUtilities.invokeLater(action);
        }
    }
}

