package com.swe.ux.view;

import com.swe.ux.model.User;
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
        
        // Create header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        
        // Welcome label
        welcomeLabel = new JLabel("", JLabel.LEFT);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        
        // Start Meeting button
        JButton startMeetingButton = new CustomButton("Start Meeting", true);
        startMeetingButton.addActionListener(e -> viewModel.startMeetingRequested.set(true));
        buttonPanel.add(startMeetingButton);
        
        // Logout button
        logoutButton = new CustomButton("Logout", false);
        buttonPanel.add(logoutButton);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content area
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        // Add some sample content
        JLabel contentLabel = new JLabel("Welcome to Comm-Uni-Cate", JLabel.CENTER);
        contentLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        contentPanel.add(contentLabel, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Set up button action
        logoutButton.addActionListener(e -> viewModel.logout());
    }
    
    private void setupBindings() {
        // Update UI when user changes
        viewModel.currentUser.addListener(evt -> updateUserInfo());
        
        // Initial update
        updateUserInfo();
    }
    
    private void updateUserInfo() {
        User user = viewModel.currentUser.get();
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
        
        // Make sure the buttons are properly styled
        List<Component> components = getComponentsInPanel(this);
        for (Component comp : components) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                if (button.getText().equals("Start Meeting")) {
                    button.setBackground(theme.getPrimaryColor());
                    button.setForeground(Color.WHITE);
                } else if (button.getText().equals("Logout")) {
                    button.setBackground(theme.getForeground());
                    button.setForeground(theme.getTextColor());
                }
            }
        }
    }
}
