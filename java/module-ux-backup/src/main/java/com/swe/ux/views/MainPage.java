package com.swe.ux.views;

import com.swe.ux.App;
import com.swe.ux.model.UserProfile;
import com.swe.ux.theme.ThemeManager;
import com.swe.ux.ui.CustomButton;

import javax.swing.*;
import java.awt.*;

public class MainPage extends JPanel {
    private final App app;
    private final ThemeManager themeManager;
    private JLabel welcomeLabel;
    private JButton logoutButton;

    public MainPage(App app) {
        this.app = app;
        this.themeManager = ThemeManager.getInstance();
        initComponents();
        applyTheme();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setOpaque(false);

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Welcome label
        welcomeLabel = new JLabel();
        welcomeLabel.setFont(themeManager.getTitleFont());
        updateWelcomeMessage();

        // Logout button
        logoutButton = new CustomButton("Logout", false);
        logoutButton.addActionListener(e -> {
            app.getControllerService().logout();
            app.showPage(App.LOGIN_PAGE);
        });

        // Add components to header
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        headerPanel.add(logoutButton, BorderLayout.EAST);

        // Main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 40, 40));

        // Add components to main panel
        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }

    private void updateWelcomeMessage() {
        UserProfile user = app.getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName();
            welcomeLabel.setText("Welcome, " + displayName + "!");
        } else {
            welcomeLabel.setText("Welcome!");
        }
    }

    public void applyTheme() {
        if (welcomeLabel != null) {
            welcomeLabel.setForeground(themeManager.getTextColor());
        }
        if (logoutButton != null) {
            logoutButton.setBackground(themeManager.getButtonColor());
            logoutButton.setForeground(themeManager.getButtonTextColor());
        }
        setBackground(themeManager.getBackground());
        repaint();
    }
}
