/**
 *  Contributed by Jyoti.
 */

package com.swe.ux.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Standalone launcher for the Sentiment analytics dashboard.
 * Delegates all UI construction to SentimentViewPane so it can be reused
 * elsewhere.
 */
public class SentimentView extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Sentiment vs Time");
        SentimentViewPane pane = new SentimentViewPane();
        Scene scene = new Scene(pane, 1200, 800);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
