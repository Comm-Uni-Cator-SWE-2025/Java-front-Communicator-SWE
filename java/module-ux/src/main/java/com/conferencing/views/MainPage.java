package com.conferencing.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;

import com.conferencing.App;
import com.conferencing.theme.Theme;
import com.conferencing.theme.ThemeManager;
import com.conferencing.ui.CustomButton;
import com.conferencing.ui.PlaceholderTextField;
import com.conferencing.ui.ThemeToggleButton;
import com.conferencing.ui.TitledPanel;
import com.swe.controller.UserProfile;

public class MainPage extends JPanel {

    private final App app;
    private JLabel userInitialLabel;
    private JPanel headerPanel;
    private JPanel centerPanel;
    private JPanel meetingControlsPanel;
    private JPanel themePanelWrapper;
    private CustomButton newMeetingButton;

    public MainPage(App app) {
        this.app = app;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Header
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel logoLabel = new JLabel("Comm-Uni-Cate");
        logoLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        headerPanel.add(logoLabel, BorderLayout.WEST);

        JLabel dateTimeLabel = new JLabel(new SimpleDateFormat("HH:mm, E, MMM d").format(new Date()));
        dateTimeLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        headerPanel.add(dateTimeLabel, BorderLayout.CENTER);
        dateTimeLabel.setHorizontalAlignment(JLabel.CENTER);

        // Create user initial label (will be updated when page is shown)
        userInitialLabel = new JLabel("?") {
             @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
                g2.dispose();
            }
        };
        userInitialLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        userInitialLabel.setHorizontalAlignment(JLabel.CENTER);
        userInitialLabel.setVerticalAlignment(JLabel.CENTER);
        userInitialLabel.setPreferredSize(new Dimension(40, 40));
        userInitialLabel.setOpaque(false);
        userInitialLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        
        // Add click listener to show profile dropdown
        userInitialLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                showProfileDropdown(evt);
            }
        });
        
        headerPanel.add(userInitialLabel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Panel
        centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);

        JLabel appTitle = new JLabel("Comm-Uni-Cate");
        appTitle.setFont(new Font("SansSerif", Font.BOLD, 48));
        appTitle.setHorizontalAlignment(JLabel.CENTER);
        centerPanel.add(appTitle, gbc);
        
        JLabel subtitle = new JLabel("Start or join a video meeting");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 18));
        subtitle.setHorizontalAlignment(JLabel.CENTER);
        centerPanel.add(subtitle, gbc);
        
        centerPanel.add(Box.createRigidArea(new Dimension(0, 50)), gbc);

        // Meeting controls
        meetingControlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        
        // New Meeting button - will be enabled/disabled based on user role when page is shown
        newMeetingButton = new CustomButton("New Meeting", true);
        newMeetingButton.setPreferredSize(new Dimension(150, 45));
        newMeetingButton.addActionListener(e -> app.showPage(App.MEETING_PAGE));
        
        meetingControlsPanel.add(newMeetingButton);
        
        PlaceholderTextField meetingCodeField = new PlaceholderTextField("Enter meeting code");
        meetingCodeField.setPreferredSize(new Dimension(250, 45));

        CustomButton joinButton = new CustomButton("Join", true);
        joinButton.setPreferredSize(new Dimension(120, 45));
        joinButton.addActionListener(e -> {
            if (!meetingCodeField.getText().trim().isEmpty()) {
                app.showPage(App.MEETING_PAGE);
            }
        });

        meetingControlsPanel.add(meetingCodeField);
        meetingControlsPanel.add(joinButton);
        centerPanel.add(meetingControlsPanel, gbc);

        // Theme Toggle Panel
        TitledPanel themePanel = new TitledPanel();
        themePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 15));
        JLabel changeThemeLabel = new JLabel("Theme:");
        changeThemeLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        themePanel.add(changeThemeLabel);
        themePanel.add(new ThemeToggleButton());
        
        themePanelWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        themePanel.setPreferredSize(new Dimension(200, 70));
        themePanelWrapper.add(themePanel);
        
        gbc.gridy = 4;
        gbc.insets = new Insets(50, 0, 10, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        centerPanel.add(themePanelWrapper, gbc);

        add(centerPanel, BorderLayout.CENTER);
        
        applyTheme();
    }
    
    private void showProfileDropdown(java.awt.event.MouseEvent evt) {
        UserProfile currentUser = app.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        
        Theme theme = ThemeManager.getInstance().getTheme();
        
        // Create popup menu
        JPopupMenu profileMenu = new JPopupMenu();
        profileMenu.setBackground(theme.getForeground());
        profileMenu.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create a custom panel for the profile info
        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        profilePanel.setBackground(theme.getForeground());
        profilePanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        // Profile avatar/initial
        String initial = currentUser.getDisplayName().substring(0, 1).toUpperCase();
        JLabel avatarLabel = new JLabel(initial) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(theme.getPrimary());
                g2.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
                g2.dispose();
            }
        };
        avatarLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        avatarLabel.setHorizontalAlignment(JLabel.CENTER);
        avatarLabel.setVerticalAlignment(JLabel.CENTER);
        avatarLabel.setPreferredSize(new Dimension(60, 60));
        avatarLabel.setMaximumSize(new Dimension(60, 60));
        avatarLabel.setForeground(Color.WHITE);
        avatarLabel.setOpaque(false);
        avatarLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        
        // Display name
        JLabel nameLabel = new JLabel(currentUser.getDisplayName());
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        nameLabel.setForeground(theme.getText());
        nameLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        
        // Email
        JLabel emailLabel = new JLabel(currentUser.getEmail());
        emailLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        emailLabel.setForeground(theme.getText());
        emailLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        
        // Role
        JLabel roleLabel = new JLabel("Role: " + currentUser.getRole());
        roleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        roleLabel.setForeground(theme.getPrimary());
        roleLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        
        // Logo URL (if present)
        if (currentUser.getLogoUrl() != null && !currentUser.getLogoUrl().isEmpty()) {
            JLabel logoLabel = new JLabel("Profile: " + currentUser.getLogoUrl());
            logoLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
            logoLabel.setForeground(theme.getText().darker());
            logoLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            profilePanel.add(Box.createRigidArea(new Dimension(0, 5)));
            profilePanel.add(logoLabel);
        }
        
        // Add components to panel
        profilePanel.add(avatarLabel);
        profilePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        profilePanel.add(nameLabel);
        profilePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        profilePanel.add(emailLabel);
        profilePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        profilePanel.add(roleLabel);
        
        // Add panel to popup menu
        profileMenu.add(profilePanel);
        
        // Show popup below the profile icon
        profileMenu.show(evt.getComponent(), 0, evt.getComponent().getHeight() + 5);
    }
    
    private void applyTheme() {
        Theme theme = ThemeManager.getInstance().getTheme();
        setBackground(theme.getBackground());
        if (headerPanel != null) {
            headerPanel.setBackground(theme.getBackground());
        }
        if (centerPanel != null) {
            centerPanel.setBackground(theme.getBackground());
        }
        if (meetingControlsPanel != null) {
            meetingControlsPanel.setBackground(theme.getBackground());
        }
        if (themePanelWrapper != null) {
            themePanelWrapper.setBackground(theme.getBackground());
        }
        if (userInitialLabel != null) {
            userInitialLabel.setBackground(theme.getPrimary());
            userInitialLabel.setForeground(Color.WHITE);
        }
    }
    
    private void updateUserInterface() {
        UserProfile currentUser = app.getCurrentUser();
        
        System.out.println("=== MainPage.updateUserInterface() called ===");
        System.out.println("Current User: " + currentUser);
        
        // Update user initial label
        if (userInitialLabel != null && currentUser != null) {
            if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                userInitialLabel.setText(currentUser.getDisplayName().substring(0, 1).toUpperCase());
                userInitialLabel.setToolTipText(currentUser.getDisplayName() + " (" + currentUser.getRole() + ")");
            }
        }
        
        // Update new meeting button based on user role
        if (newMeetingButton != null && currentUser != null) {
            String email = currentUser.getEmail();
            boolean isInstructor = email != null && email.endsWith("@iitpkd.ac.in");
            
            System.out.println("Email: " + email);
            System.out.println("Is Instructor: " + isInstructor);
            
            if (isInstructor) {
                newMeetingButton.setEnabled(true);
                newMeetingButton.setToolTipText(null);
                System.out.println("New Meeting button ENABLED for instructor");
            } else {
                newMeetingButton.setEnabled(false);
                newMeetingButton.setToolTipText("Only instructors can create new meetings");
                System.out.println("New Meeting button DISABLED for student");
            }
        } else {
            System.out.println("newMeetingButton: " + newMeetingButton + ", currentUser: " + currentUser);
        }
    }
    
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            // Update UI when page becomes visible
            updateUserInterface();
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

