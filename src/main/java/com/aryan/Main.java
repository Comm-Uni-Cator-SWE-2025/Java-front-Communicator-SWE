package com.aryan;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


/**
 * Entry point for the JavaFX Login Application.
 */
public class Main extends Application {

    /** Default window width. */
    private static final int WINDOW_WIDTH = 400;

    /** Default window height. */
    private static final int WINDOW_HEIGHT = 300;

    /**
     * Main method that launches the JavaFX application.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        final FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("login.fxml"));
        final Scene scene = new Scene(fxmlLoader.load(), WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(Main.class.getResource("styles.css").toExternalForm());
        stage.setTitle("Login Page");
        stage.setScene(scene);
        stage.show();
    }
}
