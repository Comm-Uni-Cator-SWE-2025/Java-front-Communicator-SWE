package com.swe.canvas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import java.io.IOException;

public class Launcher extends Application {
    @Override
    public void start(final Stage stage) throws IOException {  
        final FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getResource("fxml/canvas-view.fxml"));
        final Parent root = fxmlLoader.load();
        final Scene scene = new Scene(root, 900, 600);
        stage.setTitle("Whiteboard");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
