package com.swe.ux;

import com.swe.ux.model.User;
import com.swe.ux.service.AuthService;
import com.swe.ux.service.impl.InMemoryAuthService;
import com.swe.ux.theme.ThemeManager;
import com.swe.ux.view.LoginPage;
import com.swe.ux.view.MainPage;
import com.swe.ux.view.MeetingPage;
import com.swe.ux.view.RegisterPage;
import com.swe.ux.viewmodel.LoginViewModel;
import com.swe.ux.viewmodel.MainViewModel;
import com.swe.ux.viewmodel.MeetingViewModel;
import com.swe.ux.viewmodel.RegisterViewModel;

import javax.swing.*;
import java.awt.*;
import java.util.Stack;

import com.swe.ux.binding.PropertyListeners;

/**
 * Main application class that initializes the UI and coordinates between different views.
 */
public class App extends JFrame {
    private static App instance;
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final Stack<String> viewHistory = new Stack<>();
    private final AuthService authService;
    
    // View names
    public static final String LOGIN_VIEW = "LOGIN";
    public static final String REGISTER_VIEW = "REGISTER";
    public static final String MAIN_VIEW = "MAIN";
    public static final String MEETING_VIEW = "MEETING";
    
    // Current user
    private User currentUser;
    
    /**
     * Gets the singleton instance of the application.
     */
    public static App getInstance() {
        if (instance == null) {
            instance = new App();
        }
        return instance;
    }
    
    /**
     * Private constructor for singleton pattern.
     */
    private App() {
        // Initialize services
        this.authService = new InMemoryAuthService();
        
        // Set up the main window
        setTitle("Comm-Uni-Cate");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // Set up the main panel with card layout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Initialize views
        initViews();
        
        // Add main panel to the frame
        add(mainPanel, BorderLayout.CENTER);
        
        // Apply theme
        ThemeManager themeManager = ThemeManager.getInstance();
        themeManager.setMainFrame(this);
        themeManager.setApp(this);
        
        // Show login view by default
        showView(MEETING_VIEW);
        
        // Center the window
        setLocationRelativeTo(null);
    }
    
    /**
     * Refreshes the theme for the entire application
     */
    public void refreshTheme() {
        ThemeManager themeManager = ThemeManager.getInstance();
        themeManager.applyTheme(mainPanel);
        SwingUtilities.updateComponentTreeUI(this);
        revalidate();
        repaint();
    }

    /**
     * Initializes all the views and adds them to the card layout.
     */
    private void initViews() {
        // Initialize ViewModels
        LoginViewModel loginViewModel = new LoginViewModel(authService);
        MainViewModel mainViewModel = new MainViewModel(authService);
        MeetingViewModel meetingViewModel = new MeetingViewModel(null); // Will be set when user joins a meeting
        
        // Initialize Views with their respective ViewModels
        LoginPage loginView = new LoginPage(loginViewModel);
        RegisterPage registerView = new RegisterPage(new RegisterViewModel(authService));
        MainPage mainView = new MainPage(mainViewModel);
        MeetingPage meetingView = new MeetingPage(meetingViewModel);
        
        // Add views to card layout
        mainPanel.add(loginView, LOGIN_VIEW);
        mainPanel.add(registerView, REGISTER_VIEW);
        mainPanel.add(mainView, MAIN_VIEW);
        mainPanel.add(meetingView, MEETING_VIEW);
        
        // Set up navigation listeners
        loginViewModel.loginSuccess.addListener(PropertyListeners.onBooleanChanged(loggedIn -> {
            if (loggedIn) {
                this.currentUser = authService.getCurrentUser();
                mainViewModel.setCurrentUser(currentUser);
                showView(MAIN_VIEW);
            }
        }));
        
        // Handle register navigation from login
        loginViewModel.showRegisterRequested.addListener(PropertyListeners.onBooleanChanged(showRegister -> {
            if (showRegister) {
                showView(REGISTER_VIEW);
                // Reset the flag
                loginViewModel.showRegisterRequested.set(false);
            }
        }));
        
        mainViewModel.logoutRequested.addListener(PropertyListeners.onBooleanChanged(logoutRequested -> {
            if (logoutRequested) {
                logout();
            }
        }));
        
        // Use an array to hold the meeting view reference for use in lambda
        MeetingPage[] meetingViewRef = new MeetingPage[]{meetingView};
        
        // Handle meeting navigation
        mainViewModel.startMeetingRequested.addListener(PropertyListeners.onBooleanChanged(startMeeting -> {
            if (startMeeting && currentUser != null) {
                // Create a new meeting view model for this meeting
                MeetingViewModel newMeetingViewModel = new MeetingViewModel(currentUser);
                newMeetingViewModel.startMeeting();
                
                // Create a new MeetingPage with the new view model
                meetingViewRef[0] = new MeetingPage(newMeetingViewModel);
                mainPanel.add(meetingViewRef[0], MEETING_VIEW);
                showView(MEETING_VIEW);
            }
        }));
        
        // Update the original reference when the array changes
        meetingView = meetingViewRef[0];
        
        // When meeting ends, go back to main view
        meetingViewModel.isMeetingActive.addListener(PropertyListeners.onBooleanChanged(isActive -> {
            if (!isActive) {
                showView(MAIN_VIEW);
            }
        }));
    }
    
    /**
     * Shows the specified view.
     * @param viewName The name of the view to show
     */
    public void showView(String viewName) {
        cardLayout.show(mainPanel, viewName);
        
        // Add to history if it's different from the current view
        if (viewHistory.isEmpty() || !viewHistory.peek().equals(viewName)) {
            viewHistory.push(viewName);
        }
    }
    
    /**
     * Navigates back to the previous view.
     */
    public void navigateBack() {
        if (viewHistory.size() > 1) {
            viewHistory.pop(); // Remove current view
            String previousView = viewHistory.pop(); // Get previous view
            showView(previousView); // Show previous view (will be added back to history)
        }
    }

    /**
     * Main entry point of the application.
     */
    public static void main(String[] args) {
        // Run on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // Create and show the application window
            App app = App.getInstance();
            app.setVisible(true);
        });
    }
    
    // Getters
    public AuthService getAuthService() {
        return authService;
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Sets the current user and updates the UI accordingly.
     * @param user The user to set as current, or null to log out
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        // Update UI based on authentication state
        if (user != null) {
            showView(MAIN_VIEW);
        } else {
            showView(LOGIN_VIEW);
        }
    }
    
    /**
     * Logs out the current user.
     */
    public void logout() {
        authService.logout();
        setCurrentUser(null);
    }
}
