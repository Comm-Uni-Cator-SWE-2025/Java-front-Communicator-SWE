package com.homepage;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class HomeController {
    @FXML
    private VBox root;

    @FXML
    private Button newMeetingButton;

    @FXML
    private Button joinMeetingButton;

    @FXML
    private Button themeToggleButton;

    // Static theme state shared across all instances
    private static boolean isDarkTheme = false;

    @FXML
    private void initialize() {
        // Set initial button states
        newMeetingButton.setOnAction(event -> handleNewMeeting());
        joinMeetingButton.setOnAction(event -> handleJoinMeeting());
        themeToggleButton.setOnAction(event -> toggleTheme());
    }

    private void handleNewMeeting() {
        // Implement new meeting functionality
        System.out.println("New Meeting clicked");
    }

    private void handleJoinMeeting() {
        // Implement join meeting functionality
        System.out.println("Join Meeting clicked");
    }

    private void toggleTheme() {
        try {
            // Toggle the theme state
            isDarkTheme = !isDarkTheme;
            
            // Create a new FXML loader
            String fxmlFile = isDarkTheme ? "/com/homepage/home_dark.fxml" : "/com/homepage/home_light.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent newRoot = loader.load();
            
            // Switch the scene root
            Scene scene = root.getScene();
            scene.setRoot(newRoot);

        } catch (IOException e) {
            System.err.println("Error loading theme: " + e.getMessage());
        }
    }
}
