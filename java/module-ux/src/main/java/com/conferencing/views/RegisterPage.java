package com.conferencing.views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.conferencing.App;
import com.conferencing.theme.Theme;
import com.conferencing.theme.ThemeManager;
import com.conferencing.ui.CustomButton;
import com.conferencing.ui.PlaceholderPasswordField;
import com.conferencing.ui.PlaceholderTextField;
import com.conferencing.ui.ThemeToggleButton;
import com.swe.controller.UserProfile;

public class RegisterPage extends JPanel {

    private final App app;
    private JPanel headerPanel;
    private JPanel mainContent;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JPanel themePanel;
    private JLabel haveAccountLabel;

    public RegisterPage(App app) {
        this.app = app;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 25, 15, 25));
        
        // Header
        headerPanel = new JPanel(new BorderLayout());
        JLabel logoLabel = new JLabel("Comm-Uni-Cate");
        logoLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        headerPanel.add(logoLabel, BorderLayout.WEST);

        JLabel dateTimeLabel = new JLabel(new SimpleDateFormat("HH:mm, E, MMM d").format(new Date()));
        dateTimeLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        headerPanel.add(dateTimeLabel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Main content panel
        mainContent = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        
        // Left Panel (Register)
        leftPanel = new JPanel(new GridBagLayout());
        GridBagConstraints leftGbc = new GridBagConstraints();
        leftGbc.insets = new Insets(8, 10, 8, 10);
        leftGbc.anchor = GridBagConstraints.WEST;
        
        JLabel registerTitle = new JLabel("Create Account");
        registerTitle.setFont(new Font("SansSerif", Font.BOLD, 32));
        leftGbc.gridx = 0;
        leftGbc.gridy = 0;
        leftGbc.gridwidth = 2;
        leftPanel.add(registerTitle, leftGbc);

        JLabel registerSubtitle = new JLabel("Use your @iitpkd.ac.in or @smail.iitpkd.ac.in email");
        registerSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        leftGbc.gridy = 1;
        leftGbc.insets = new Insets(0, 10, 15, 10);
        leftPanel.add(registerSubtitle, leftGbc);

        PlaceholderTextField emailField = new PlaceholderTextField("Email (e.g. user@iitpkd.ac.in)");
        emailField.setPreferredSize(new Dimension(350, 45));
        leftGbc.gridy = 2;
        leftGbc.insets = new Insets(8, 10, 8, 10);
        leftPanel.add(emailField, leftGbc);
        
        PlaceholderPasswordField passwordField = new PlaceholderPasswordField("Password");
        passwordField.setPreferredSize(new Dimension(350, 45));
        leftGbc.gridy = 3;
        leftPanel.add(passwordField, leftGbc);
        
        PlaceholderTextField displayNameField = new PlaceholderTextField("Display Name");
        displayNameField.setPreferredSize(new Dimension(350, 45));
        leftGbc.gridy = 4;
        leftPanel.add(displayNameField, leftGbc);
        
        PlaceholderTextField logoURLField = new PlaceholderTextField("Profile Picture URL (optional)");
        logoURLField.setPreferredSize(new Dimension(350, 45));
        leftGbc.gridy = 5;
        leftPanel.add(logoURLField, leftGbc);
        
        haveAccountLabel = new JLabel("<html><u>Already have an account? Login here</u></html>");
        haveAccountLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        haveAccountLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        haveAccountLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                app.showPage(App.LOGIN_PAGE);
            }
        });
        leftGbc.gridy = 6;
        leftGbc.insets = new Insets(5, 10, 8, 10);
        leftPanel.add(haveAccountLabel, leftGbc);

        CustomButton registerButton = new CustomButton("Create Account", true);
        registerButton.setPreferredSize(new Dimension(350, 45));
        registerButton.addActionListener(e -> {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            String displayName = displayNameField.getText().trim();
            String logoUrl = logoURLField.getText().trim();
            
            // Validation
            if (email.isEmpty() || password.isEmpty() || displayName.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please fill in all required fields (email, password, and display name)", 
                    "Registration Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Use empty string if logoUrl is not provided
            if (logoUrl.isEmpty()) {
                logoUrl = "";
            }
            
            UserProfile user = app.getControllerService().register(email, password, displayName, logoUrl);
            if (user != null) {
                JOptionPane.showMessageDialog(this, 
                    "Registration successful! Welcome, " + displayName + "!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                emailField.setText("");
                passwordField.setText("");
                displayNameField.setText("");
                logoURLField.setText("");
                app.showPage(App.MAIN_PAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Registration failed. Email may already be registered or invalid.\n" +
                    "Please use @iitpkd.ac.in or @smail.iitpkd.ac.in email.", 
                    "Registration Failed", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        leftGbc.gridy = 7;
        leftGbc.insets = new Insets(8, 10, 8, 10);
        leftGbc.gridwidth = 1;
        leftPanel.add(registerButton, leftGbc);

        gbc.gridx = 0;
        mainContent.add(leftPanel, gbc);

        // Right Panel (Title)
        rightPanel = new JPanel(new GridBagLayout());
        GridBagConstraints rightGbc = new GridBagConstraints();
        
        JLabel appTitle = new JLabel("Start Connecting");
        appTitle.setFont(new Font("SansSerif", Font.BOLD, 42));
        rightGbc.gridx = 0;
        rightGbc.gridy = 0;
        rightPanel.add(appTitle, rightGbc);

        JLabel subtitle = new JLabel("<html><center>Join thousands of users<br>connecting worldwide</center></html>");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 18));
        subtitle.setHorizontalAlignment(JLabel.CENTER);
        rightGbc.gridy = 1;
        rightGbc.insets = new Insets(10, 0, 0, 0);
        rightPanel.add(subtitle, rightGbc);

        // Theme Toggle
        themePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel themeLabel = new JLabel("Theme:");
        themeLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        themePanel.add(themeLabel);
        themePanel.add(new ThemeToggleButton());
        rightGbc.gridy = 2;
        rightGbc.insets = new Insets(30, 0, 0, 0);
        rightPanel.add(themePanel, rightGbc);

        gbc.gridx = 1;
        mainContent.add(rightPanel, gbc);

        add(mainContent, BorderLayout.CENTER);
        
        applyTheme();
    }
    
    private void applyTheme() {
        Theme theme = ThemeManager.getInstance().getTheme();
        setBackground(theme.getBackground());
        if (headerPanel != null) {
            headerPanel.setBackground(theme.getBackground());
        }
        if (mainContent != null) {
            mainContent.setBackground(theme.getBackground());
        }
        if (leftPanel != null) {
            leftPanel.setBackground(theme.getBackground());
        }
        if (rightPanel != null) {
            rightPanel.setBackground(theme.getBackground());
        }
        if (themePanel != null) {
            themePanel.setBackground(theme.getBackground());
        }
        if (haveAccountLabel != null) {
            haveAccountLabel.setForeground(theme.getPrimary());
        }
    }
    
    @Override
    public void updateUI() {
        super.updateUI();
        if (ThemeManager.getInstance() != null) {
            applyTheme();
        }
    }
}

