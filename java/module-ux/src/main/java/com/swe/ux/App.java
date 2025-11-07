package com.swe.ux;

import com.swe.screenNVideo.AbstractRPC;
import com.swe.screenNVideo.DummyRPC;
import com.swe.screenNVideo.Utils;
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
import java.io.IOException;
import java.util.Stack;
import java.util.concurrent.ExecutionException;

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
    
    // ViewModel references for resetting on logout
    private LoginViewModel loginViewModel;
    private MainViewModel mainViewModel;
    
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

        // Set up the main panel with card layout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Initialize views
        initViews();

    }

    public void start() {

        // Set up the main window
        setTitle("Comm-Uni-Cate");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        // Add main panel to the frame
        add(mainPanel, BorderLayout.CENTER);

        // Apply theme
        ThemeManager themeManager = ThemeManager.getInstance();
        themeManager.setMainFrame(this);
        themeManager.setApp(this);

        // Show login view by default
        showView(LOGIN_VIEW);

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
        loginViewModel = new LoginViewModel(authService);
        mainViewModel = new MainViewModel(authService);
        System.out.println("Meeting");
        MeetingViewModel meetingViewModel = new MeetingViewModel(new User(Utils.getSelfIP(),"You", "You", "you")); // Will be set when user joins a meeting


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

        final DummyRPC rpc = DummyRPC.getInstance();

        // New participant
        rpc.subscribe(Utils.SUBSCRIBE_AS_VIEWER, data -> {
            final String viewerIP = new String(data);
            User new_user = new User(viewerIP, viewerIP, "New", "new");
            meetingViewModel.addParticipant(new_user);
            return new byte[0];
        });

        meetingViewModel.startMeeting();
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
                // Reset the flag
                mainViewModel.logoutRequested.set(false);
            }
        }));
        
        // Use an array to hold the meeting view reference for use in lambda
        MeetingPage[] meetingViewRef = new MeetingPage[]{meetingView};
        
        // Handle meeting navigation - Start Meeting (Instructor role)
        mainViewModel.startMeetingRequested.addListener(PropertyListeners.onBooleanChanged(startMeeting -> {
            if (startMeeting && currentUser != null) {
                // Create a new meeting view model for this meeting with Instructor role
                MeetingViewModel newMeetingViewModel = new MeetingViewModel(currentUser, "Instructor");
                
                // Set up listener for when meeting ends - navigate back to main view
                newMeetingViewModel.isMeetingActive.addListener(PropertyListeners.onBooleanChanged(isActive -> {
                    if (!isActive) {
                        showView(MAIN_VIEW);
                        // Reset the flag so the button can be clicked again
                        mainViewModel.startMeetingRequested.set(false);
                    }
                }));
                
                // Create a new MeetingPage with the new view model
                meetingViewRef[0] = new MeetingPage(newMeetingViewModel);
                newMeetingViewModel.startMeeting();
                mainPanel.add(meetingViewRef[0], MEETING_VIEW);
                showView(MEETING_VIEW);
                
                // Reset the flag
                mainViewModel.startMeetingRequested.set(false);
            }
        }));
        
        // Handle join meeting navigation - Join Meeting (Student role)
        mainViewModel.joinMeetingRequested.addListener(PropertyListeners.onBooleanChanged(joinMeeting -> {
            if (joinMeeting && currentUser != null) {
                // Create a new meeting view model for joining meeting with Student role
                MeetingViewModel newMeetingViewModel = new MeetingViewModel(currentUser, "Student");
                newMeetingViewModel.startMeeting();
                
                // Set up listener for when meeting ends - navigate back to main view
                newMeetingViewModel.isMeetingActive.addListener(PropertyListeners.onBooleanChanged(isActive -> {
                    if (!isActive) {
                        showView(MAIN_VIEW);
                        // Reset the flag so the button can be clicked again
                        mainViewModel.joinMeetingRequested.set(false);
                    }
                }));
                
                // Create a new MeetingPage with the new view model
                meetingViewRef[0] = new MeetingPage(newMeetingViewModel);
                mainPanel.add(meetingViewRef[0], MEETING_VIEW);
                System.out.println("here");
                showView(MEETING_VIEW);
                
                // Reset the flag
                mainViewModel.joinMeetingRequested.set(false);
            }
        }));
        
        // Update the original reference when the array changes
        meetingView = meetingViewRef[0];
        
        // When meeting ends (for initial meeting view model), go back to main view
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

        final AbstractRPC rpc = DummyRPC.getInstance();

        // Create and show the application window
        App app = App.getInstance();


         Thread handler = null;
         try {
             handler = rpc.connect();
         } catch (IOException | ExecutionException | InterruptedException e) {
             throw new RuntimeException(e);
         }

        // Run on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            app.start();
            app.setVisible(true);
        });


         try {
             handler.join();
         } catch (InterruptedException e) {
             throw new RuntimeException(e);
         }
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
     * Logs out the current user completely.
     * Clears all user state and returns to login view.
     */
    public void logout() {
        // Clear current user
        this.currentUser = null;
        
        // Reset MainViewModel's current user
        if (mainViewModel != null) {
            mainViewModel.setCurrentUser(null);
            // Reset all flags
            mainViewModel.logoutRequested.set(false);
            mainViewModel.startMeetingRequested.set(false);
            mainViewModel.joinMeetingRequested.set(false);
        }
        
        // Reset LoginViewModel to ensure clean state
        if (loginViewModel != null) {
            loginViewModel.reset();
        }
        
        // Logout from auth service (this will clear all user data)
        authService.logout();
        
        // Clear view history
        viewHistory.clear();
        
        // Navigate to login view - this will trigger reset on LoginPage
        showView(LOGIN_VIEW);
    }
}
