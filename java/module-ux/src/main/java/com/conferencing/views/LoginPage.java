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

public class LoginPage extends JPanel {

    private final App app;
    private JPanel headerPanel;
    private JPanel mainContent;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JPanel themePanel;
    private JLabel noAccountLabel;

    public LoginPage(App app) {
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
        
        // Left Panel (Login)
        leftPanel = new JPanel(new GridBagLayout());
        GridBagConstraints leftGbc = new GridBagConstraints();
        leftGbc.insets = new Insets(10, 10, 10, 10);
        leftGbc.anchor = GridBagConstraints.WEST;
        
        JLabel loginTitle = new JLabel("Welcome Back!");
        loginTitle.setFont(new Font("SansSerif", Font.BOLD, 32));
        leftGbc.gridx = 0;
        leftGbc.gridy = 0;
        leftGbc.gridwidth = 2;
        leftPanel.add(loginTitle, leftGbc);

        JLabel loginSubtitle = new JLabel("Use your @iitpkd.ac.in or @smail.iitpkd.ac.in email");
        loginSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        leftGbc.gridy = 1;
        leftGbc.insets = new Insets(0, 10, 20, 10);
        leftPanel.add(loginSubtitle, leftGbc);

        PlaceholderTextField emailField = new PlaceholderTextField("Email (e.g. user@iitpkd.ac.in)");
        emailField.setPreferredSize(new Dimension(350, 45));
        leftGbc.gridy = 2;
        leftGbc.insets = new Insets(10, 10, 10, 10);
        leftPanel.add(emailField, leftGbc);
        
        PlaceholderPasswordField passwordField = new PlaceholderPasswordField("Password");
        passwordField.setPreferredSize(new Dimension(350, 45));
        leftGbc.gridy = 3;
        leftPanel.add(passwordField, leftGbc);
        
        noAccountLabel = new JLabel("<html><u>Don't have an account? Sign up here</u></html>");
        noAccountLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        noAccountLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        noAccountLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                app.showPage(App.REGISTER_PAGE);
            }
        });
        leftGbc.gridy = 4;
        leftGbc.insets = new Insets(5, 10, 10, 10);
        leftPanel.add(noAccountLabel, leftGbc);

        CustomButton loginButton = new CustomButton("Sign In", true);
        loginButton.setPreferredSize(new Dimension(350, 45));
        loginButton.addActionListener(e -> {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            
            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter both email and password", 
                    "Login Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            UserProfile user = app.getControllerService().login(email, password);
            if (user != null) {
                emailField.setText("");
                passwordField.setText("");
                app.showPage(App.MAIN_PAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Invalid email or password", 
                    "Login Failed", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        leftGbc.gridy = 5;
        leftGbc.insets = new Insets(10, 10, 10, 10);
        leftGbc.gridwidth = 1;
        leftPanel.add(loginButton, leftGbc);

        gbc.gridx = 0;
        mainContent.add(leftPanel, gbc);

        // Right Panel (Title)
        rightPanel = new JPanel(new GridBagLayout());
        GridBagConstraints rightGbc = new GridBagConstraints();
        
        JLabel appTitle = new JLabel("Connect Anywhere");
        appTitle.setFont(new Font("SansSerif", Font.BOLD, 42));
        rightGbc.gridx = 0;
        rightGbc.gridy = 0;
        rightPanel.add(appTitle, rightGbc);

        JLabel subtitle = new JLabel("<html><center>Your professional video<br>conferencing solution</center></html>");
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
        if (noAccountLabel != null) {
            noAccountLabel.setForeground(theme.getPrimary());
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

