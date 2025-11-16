package com.swe.canvas.ui;

import java.io.IOException;

import com.swe.networking.NetworkFront;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

/**
 * Main running class in the app
 * @author Bhogaraju Shanmukha Sri Krishna
 */
public class CanvasApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        final FXMLLoader loader = new FXMLLoader(CanvasApp.class.getResource("../fxml/canvas-view.fxml"));
        final Parent root = loader.load();
        final CanvasController controller = loader.getController();

        final Scene scene = new Scene(root);

        controller.setNetwork(NetworkFront.getInstance());

        // Setup Accelerators
        // Ctrl+Z -> Undo
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN),
                () -> controller.getViewModel().undo());
        // Ctrl+Y -> Redo
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN),
                () -> controller.getViewModel().redo());
        // Ctrl+X -> Undo (per specific request)
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.X, KeyCombination.SHORTCUT_DOWN),
                () -> {
                    System.out.println("Ctrl+X (Custom Undo) triggered");
                    controller.getViewModel().undo();
                });

        primaryStage.setTitle("JavaFX Canvas (FXML)");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(final String[] args) {
        launch(args);
    }
}