package com.swe.ux.view;

import com.swe.controller.Meeting.UserProfile;
import com.swe.ux.theme.ThemeManager;
import com.swe.ux.theme.Theme;
import com.swe.ux.ui.CustomButton;
import com.swe.ux.viewmodel.MainViewModel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Main application page that shows after successful login.
 */
public class MainPage extends JPanel {
    private final MainViewModel viewModel;
    private JLabel welcomeLabel;
    private JButton logoutButton;
    private JTextField meetingCodeField;
    
    /**
     * Creates a new MainPage.
     * @param viewModel The ViewModel for this view
     */
    public MainPage(MainViewModel viewModel) {
        this.viewModel = viewModel;
        initializeUI();
        setupBindings();
        applyTheme();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Logout button in top right corner
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setOpaque(false);
        logoutButton = new CustomButton("Logout", false);
        logoutButton.addActionListener(e -> viewModel.logout());
        topPanel.add(logoutButton);
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel for main content
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;
        
        // Welcome label - bigger and centered
        welcomeLabel = new JLabel("", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 48));
        gbc.gridy = 0;
        centerPanel.add(welcomeLabel, gbc);
        
        // App title
        JLabel titleLabel = new JLabel("Welcome to Comm-Uni-Cate", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 28));
        gbc.gridy = 1;
        gbc.insets = new Insets(20, 10, 30, 10);
        centerPanel.add(titleLabel, gbc);
        
        // Meeting code input panel
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        inputPanel.setOpaque(false);
        
        JLabel meetingCodeLabel = new JLabel("Meeting Code:");
        meetingCodeLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        inputPanel.add(meetingCodeLabel);
        
        meetingCodeField = new JTextField(20);
        meetingCodeField.setFont(new Font("Arial", Font.PLAIN, 18));
        meetingCodeField.setPreferredSize(new Dimension(250, 40));
        inputPanel.add(meetingCodeField);
        
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 10, 20, 10);
        centerPanel.add(inputPanel, gbc);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonsPanel.setOpaque(false);
        
        // Join Meeting button
        JButton joinMeetingButton = new CustomButton("Join Meeting", true);
        joinMeetingButton.setFont(new Font("Arial", Font.BOLD, 16));
        joinMeetingButton.setPreferredSize(new Dimension(160, 45));
        joinMeetingButton.addActionListener(e -> {
            viewModel.meetingCode.set(meetingCodeField.getText());
            viewModel.joinMeetingRequested.set(true);
        });
        buttonsPanel.add(joinMeetingButton);
        
        // Create Meeting button
        JButton createMeetingButton = new CustomButton("Create Meeting", true);
        createMeetingButton.setFont(new Font("Arial", Font.BOLD, 16));
        createMeetingButton.setPreferredSize(new Dimension(160, 45));
        createMeetingButton.addActionListener(e -> viewModel.startMeetingRequested.set(true));
        buttonsPanel.add(createMeetingButton);
        
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 10, 10, 10);
        centerPanel.add(buttonsPanel, gbc);
        
        add(centerPanel, BorderLayout.CENTER);
    }
    
    private void setupBindings() {
        // Update UI when user changes
        viewModel.currentUser.addListener(evt -> updateUserInfo());
        
        // Initial update
        updateUserInfo();
    }
    
    private void updateUserInfo() {
        UserProfile user = viewModel.currentUser.get();
        if (user != null) {
            welcomeLabel.setText("Welcome, " + user.getDisplayName() + "!");
        } else {
            welcomeLabel.setText("Welcome!");
        }
    }
    
    /**
     * Helper method to get all components in a container recursively.
     * @param container The container to search in
     * @return List of all components in the container
     */
    private List<Component> getComponentsInPanel(Container container) {
        List<Component> components = new ArrayList<>();
        for (Component comp : container.getComponents()) {
            components.add(comp);
            if (comp instanceof Container) {
                components.addAll(getComponentsInPanel((Container) comp));
            }
        }
        return components;
    }
    
    private void applyTheme() {
        // Apply theme colors using ThemeManager
        ThemeManager themeManager = ThemeManager.getInstance();
        themeManager.applyTheme(this);
        
        // Get theme for additional styling
        Theme theme = themeManager.getCurrentTheme();
        
        // Apply specific styles
        setBackground(theme.getBackgroundColor());
        welcomeLabel.setForeground(theme.getTextColor());
        
        // Make sure the buttons, text field, and labels are properly styled
        List<Component> components = getComponentsInPanel(this);
        for (Component comp : components) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                if (button.getText().equals("Create Meeting") || button.getText().equals("Join Meeting")) {
                    button.setBackground(theme.getPrimaryColor());
                    button.setForeground(Color.WHITE);
                } else if (button.getText().equals("Logout")) {
                    button.setBackground(theme.getForeground());
                    button.setForeground(theme.getTextColor());
                }
            } else if (comp instanceof JTextField) {
                JTextField textField = (JTextField) comp;
                textField.setBackground(theme.getInputBackgroundColor());
                textField.setForeground(theme.getTextColor());
                textField.setCaretColor(theme.getTextColor());
            } else if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                label.setForeground(theme.getTextColor());
            }
        }
    }
}
