package com.swe.ux.views;

import com.swe.ux.App;
import com.swe.ux.service.ControllerService;
import com.swe.ux.theme.ThemeManager;
import com.swe.ux.ui.CustomButton;
import com.swe.ux.ui.PlaceholderPasswordField;
import com.swe.ux.ui.PlaceholderTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginPage extends JPanel {
    private final App app;
    private final ThemeManager themeManager;
    private PlaceholderTextField usernameField;
    private PlaceholderPasswordField passwordField;
    private CustomButton loginButton;
    private JLabel errorLabel;
    private JLabel registerLink;

    public LoginPage(App app) {
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
        mainPanel.setMaximumSize(new Dimension(400, 500));

        // Title
        JLabel titleLabel = new JLabel("Login");
        titleLabel.setFont(themeManager.getTitleFont());
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(themeManager.getTextColor());

        // Username field
        usernameField = new PlaceholderTextField("Username");
        usernameField.setMaximumSize(new Dimension(300, 40));
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Password field
        passwordField = new PlaceholderPasswordField("Password");
        passwordField.setMaximumSize(new Dimension(300, 40));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Login button
        loginButton = new CustomButton("Login", false);
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attemptLogin();
            }
        });

        // Error label
        errorLabel = new JLabel("");
        errorLabel.setForeground(Color.RED);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorLabel.setVisible(false);

        // Register link
        JPanel registerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        registerPanel.setOpaque(false);
        JLabel registerText = new JLabel("Don't have an account? ");
        registerText.setForeground(themeManager.getTextColor());
        registerLink = new JLabel("Register");
        registerLink.setForeground(themeManager.getAccentColor());
        registerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                app.showPage(App.REGISTER_PAGE);
            }
        });
        registerPanel.add(registerText);
        registerPanel.add(registerLink);

        // Add components to main panel
        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        mainPanel.add(usernameField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(passwordField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(loginButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(errorLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(registerPanel);
        mainPanel.add(Box.createVerticalGlue());

        // Add main panel to this panel
        add(mainPanel);

        // Add enter key listener for login
        passwordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attemptLogin();
            }
        });
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }
        
        // Attempt login through controller service
        boolean success = ControllerService.getInstance().login(username, password);
        
        if (success) {
            // Clear fields and navigate to main page
            usernameField.setText("");
            passwordField.setText("");
            errorLabel.setVisible(false);
            app.showPage(App.MAIN_PAGE);
        } else {
            showError("Invalid username or password");
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
        if (passwordField != null) {
            passwordField.setBackground(themeManager.getPanelBackground());
            passwordField.setForeground(themeManager.getTextColor());
            passwordField.setCaretColor(themeManager.getTextColor());
        }
        if (errorLabel != null) {
            errorLabel.setForeground(themeManager.getAccentColor());
        }
        if (registerLink != null) {
            registerLink.setForeground(themeManager.getAccentColor());
        }
        repaint();
    }
}
