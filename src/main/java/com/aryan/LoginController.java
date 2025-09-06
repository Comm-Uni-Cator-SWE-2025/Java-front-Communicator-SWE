package com.aryan;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Controller for the login page.
 * Handles login, registration, and password recovery actions.
 */
public class LoginController {

    /** Administrator username. */
    private static final String ADMIN_USER = "admin";

    /** Administrator password. */
    private static final String ADMIN_PASS = "1234";

    /** Root container of the login page. */
    @FXML
    private VBox root;

    /** Text field for entering the username. */
    @FXML
    private TextField usernameField;

    /** Text field for entering the password. */
    @FXML
    private PasswordField passwordField;

    /** Button to log into the application. */
    @FXML
    private Button loginButton;

    /** Button to register a new account. */
    @FXML
    private Button registerButton;

    /** Link to recover a forgotten password. */
    @FXML
    private Hyperlink forgotPasswordLink;

    /** Initializes the controller after FXML loading. */
    @FXML
    private void initialize() {
        loginButton.setOnAction(event -> handleLogin());
        registerButton.setOnAction(event -> handleRegister());
        forgotPasswordLink.setOnAction(event -> handleForgotPassword());
    }

    /** Handles login button click. */
    private void handleLogin() {
        final String user = usernameField.getText();
        final String pass = passwordField.getText();

        if (ADMIN_USER.equals(user) && ADMIN_PASS.equals(pass)) {
            showAlert(Alert.AlertType.INFORMATION, "Login Successful", "Welcome, " + user + "!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid credentials.");
        }
    }

    /** Handles register button click. */
    private void handleRegister() {
        showAlert(Alert.AlertType.INFORMATION, "Register", "Redirecting to Registration Page...");
    }

    /** Handles forgot password link click. */
    private void handleForgotPassword() {
        showAlert(Alert.AlertType.INFORMATION, "Forgot Password", "Redirecting to Password Recovery...");
    }

    /**
     * Displays an alert with the given parameters.
     *
     * @param type    the alert type
     * @param title   the alert title
     * @param message the alert message
     */
    private void showAlert(final Alert.AlertType type, final String title, final String message) {
        final Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
