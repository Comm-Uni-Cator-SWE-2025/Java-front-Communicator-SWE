package com.swe.canvas.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

import java.io.IOException;

public class CanvasApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(CanvasApp.class.getResource("../fxml/canvas-view.fxml"));
        Parent root = loader.load();
        CanvasController controller = loader.getController();

        Scene scene = new Scene(root);

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

    public static void main(String[] args) {
        launch(args);
    }
}