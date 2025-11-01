package com.swe.ux.views;

import com.swe.ux.App;
import com.swe.ux.theme.ThemeManager;
import com.swe.ux.ui.CustomButton;
import com.swe.ux.ui.PlaceholderPasswordField;
import com.swe.ux.ui.PlaceholderTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegisterPage extends JPanel {
    private final App app;
    private final ThemeManager themeManager;
    private PlaceholderTextField usernameField;
    private PlaceholderTextField emailField;
    private PlaceholderPasswordField passwordField;
    private PlaceholderPasswordField confirmPasswordField;
    private CustomButton registerButton;
    private JLabel errorLabel;
    private JLabel loginLink;

    public RegisterPage(App app) {
        this.app = app;
        this.themeManager = ThemeManager.getInstance();
        initComponents();
        applyTheme();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        setOpaque(false);

        // Main container panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setMaximumSize(new Dimension(400, 600));

        // Title
        JLabel titleLabel = new JLabel("Create Account");
        titleLabel.setFont(themeManager.getTitleFont());
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(themeManager.getTextColor());

        // Username field
        usernameField = new PlaceholderTextField("Username");
        usernameField.setMaximumSize(new Dimension(300, 40));
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Email field
        emailField = new PlaceholderTextField("Email");
        emailField.setMaximumSize(new Dimension(300, 40));
        emailField.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Password field
        passwordField = new PlaceholderPasswordField("Password");
        passwordField.setMaximumSize(new Dimension(300, 40));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Confirm password field
        confirmPasswordField = new PlaceholderPasswordField("Confirm Password");
        confirmPasswordField.setMaximumSize(new Dimension(300, 40));
        confirmPasswordField.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Register button
        registerButton = new CustomButton("Register", false);
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.addActionListener(e -> attemptRegistration());

        // Error label
        errorLabel = new JLabel("");
        errorLabel.setForeground(Color.RED);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorLabel.setVisible(false);

        // Login link
        JPanel loginPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        loginPanel.setOpaque(false);
        JLabel loginText = new JLabel("Already have an account? ");
        loginText.setForeground(themeManager.getTextColor());
        loginLink = new JLabel("Login");
        loginLink.setForeground(themeManager.getAccentColor());
        loginLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                app.showPage(App.LOGIN_PAGE);
            }
        });
        loginPanel.add(loginText);
        loginPanel.add(loginLink);

        // Add components to main panel
        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        mainPanel.add(usernameField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(emailField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(passwordField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(confirmPasswordField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(registerButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(errorLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(loginPanel);
        mainPanel.add(Box.createVerticalGlue());

        // Add main panel to this panel
        add(mainPanel);

        // Add enter key listener for registration
        confirmPasswordField.addActionListener(e -> attemptRegistration());
    }

    private void attemptRegistration() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }
        
        // Attempt registration through controller service
        boolean success = app.getControllerService().register(username, email, password, confirmPassword);
        
        if (success) {
            // Clear fields and navigate to main page
            usernameField.setText("");
            emailField.setText("");
            passwordField.setText("");
            confirmPasswordField.setText("");
            errorLabel.setVisible(false);
            app.showPage(App.MAIN_PAGE);
        } else {
            showError("Registration failed. Please try again.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    public void applyTheme() {
        if (usernameField != null) {
            usernameField.setBackground(themeManager.getPanelBackground());
            usernameField.setForeground(themeManager.getTextColor());
            usernameField.setCaretColor(themeManager.getTextColor());
        }
        if (emailField != null) {
            emailField.setBackground(themeManager.getPanelBackground());
            emailField.setForeground(themeManager.getTextColor());
            emailField.setCaretColor(themeManager.getTextColor());
        }
        if (passwordField != null) {
            passwordField.setBackground(themeManager.getPanelBackground());
            passwordField.setForeground(themeManager.getTextColor());
            passwordField.setCaretColor(themeManager.getTextColor());
        }
        if (confirmPasswordField != null) {
            confirmPasswordField.setBackground(themeManager.getPanelBackground());
            confirmPasswordField.setForeground(themeManager.getTextColor());
            confirmPasswordField.setCaretColor(themeManager.getTextColor());
        }
        if (errorLabel != null) {
            errorLabel.setForeground(themeManager.getAccentColor());
        }
        if (loginLink != null) {
            loginLink.setForeground(themeManager.getAccentColor());
        }
        repaint();
    }
}
