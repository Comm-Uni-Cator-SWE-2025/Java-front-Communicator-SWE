package com.conferencing.views;

import com.conferencing.App;
import com.conferencing.ui.CustomButton;
import com.conferencing.ui.PlaceholderTextField;
import com.conferencing.ui.ThemeToggleButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LoginPage extends JPanel {

    private final App app;

    public LoginPage(App app) {
        this.app = app;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 25, 15, 25));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel logoLabel = new JLabel("LOGO");
        logoLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerPanel.add(logoLabel, BorderLayout.WEST);

        JLabel dateTimeLabel = new JLabel(new SimpleDateFormat("HH:mm, E, MMM d").format(new Date()));
        dateTimeLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        headerPanel.add(dateTimeLabel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Main content panel
        JPanel mainContent = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        
        // Left Panel (Login)
        JPanel leftPanel = new JPanel(new GridBagLayout());
        GridBagConstraints leftGbc = new GridBagConstraints();
        leftGbc.insets = new Insets(10, 10, 10, 10);
        leftGbc.anchor = GridBagConstraints.WEST;
        
        JLabel loginTitle = new JLabel("LOGIN / SIGN IN");
        loginTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        leftGbc.gridx = 0;
        leftGbc.gridy = 0;
        leftGbc.gridwidth = 2;
        leftPanel.add(loginTitle, leftGbc);

        PlaceholderTextField usernameField = new PlaceholderTextField("Username");
        usernameField.setPreferredSize(new Dimension(300, 40));
        leftGbc.gridy = 1;
        leftPanel.add(usernameField, leftGbc);
        
        JPasswordField passwordField = new JPasswordField();
        // Placeholder functionality for JPasswordField is more complex, setting a default look
        passwordField.setPreferredSize(new Dimension(300, 40));
        leftGbc.gridy = 2;
        leftPanel.add(passwordField, leftGbc);
        
        JLabel noAccountLabel = new JLabel("Don't have an account, sign up here");
        noAccountLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        leftGbc.gridy = 3;
        leftPanel.add(noAccountLabel, leftGbc);

        CustomButton loginButton = new CustomButton("Login", true);
        loginButton.addActionListener(e -> app.showPage(App.MAIN_PAGE));
        leftGbc.gridy = 4;
        leftGbc.gridwidth = 1;
        leftPanel.add(loginButton, leftGbc);

        gbc.gridx = 0;
        mainContent.add(leftPanel, gbc);

        // Right Panel (Title)
        JPanel rightPanel = new JPanel(new GridBagLayout());
        GridBagConstraints rightGbc = new GridBagConstraints();
        
        JLabel appTitle = new JLabel("Comm-Uni-Cate");
        appTitle.setFont(new Font("SansSerif", Font.BOLD, 48));
        rightGbc.gridx = 0;
        rightGbc.gridy = 0;
        rightPanel.add(appTitle, rightGbc);

        JLabel subtitle = new JLabel("Software Engineering Project by Batch B22");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 18));
        rightGbc.gridy = 1;
        rightPanel.add(subtitle, rightGbc);

        // Theme Toggle
        JPanel themePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        themePanel.add(new JLabel("Toggle Theme:"));
        themePanel.add(new ThemeToggleButton());
        rightGbc.gridy = 2;
        rightGbc.insets = new Insets(20, 0, 0, 0);
        rightPanel.add(themePanel, rightGbc);

        gbc.gridx = 1;
        mainContent.add(rightPanel, gbc);

        add(mainContent, BorderLayout.CENTER);
    }
}

