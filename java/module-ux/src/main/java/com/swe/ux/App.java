package com.swe.ux;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.swe.controller.ClientNode;
import com.swe.controller.RPC;
import com.swe.controller.Meeting.ParticipantRole;
import com.swe.controller.Meeting.UserProfile;
import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.controller.serialize.DataSerializer;
import com.swe.screenNVideo.Utils;
import com.swe.ux.theme.ThemeManager;

import com.swe.ux.view.BypassAuthPage;
import com.swe.ux.view.LoginPage;
import com.swe.ux.view.MainPage;
import com.swe.ux.view.MeetingPage;
import com.swe.ux.viewmodel.BypassAuthViewModel;
import com.swe.ux.viewmodel.LoginViewModel;
import com.swe.ux.viewmodel.MainViewModel;
import com.swe.ux.viewmodel.MeetingViewModel;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ExecutionException;

import com.swe.ux.binding.PropertyListeners;

/**
 * Main application class that initializes the UI and coordinates between
 * different views.
 */
public class App extends JFrame {
    private static App instance;
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final Stack<String> viewHistory = new Stack<>();

    // View names
    public static final String LOGIN_VIEW = "LOGIN";
    public static final String BYPASS_AUTH_VIEW = "BYPASS_AUTH";
    public static final String MAIN_VIEW = "MAIN";
    public static final String MEETING_VIEW = "MEETING";


    // Current user
    private UserProfile currentUser;

    // ViewModel references for resetting on logout
    private LoginViewModel loginViewModel;
    private BypassAuthViewModel bypassAuthViewModel;
    private MainViewModel mainViewModel;

    private AbstractRPC rpc;
    private static boolean bypassAuthMode = false;

    /**
     * Gets the singleton instance of the application.
     */
    public static App getInstance(AbstractRPC rpc) {
        if (instance == null) {
            instance = new App(rpc);
        }
        return instance;
    }

    /**
     * Private constructor for singleton pattern.
     */
    private App(AbstractRPC rpc) {
        // Initialize services
        this.rpc = rpc;
        // Initialize RPC in Utils for IP retrieval
        com.swe.screenNVideo.Utils.initializeRPC(rpc);
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

        // Show login view or bypass auth view based on mode
        if (bypassAuthMode) {
            showView(BYPASS_AUTH_VIEW);
        } else {
            showView(LOGIN_VIEW);
        }

        // Center the window
        setLocationRelativeTo(null);
    }

    /**
     * Refreshes the theme for the entire application
     */
    public void refreshTheme() {
        ThemeManager themeManager = ThemeManager.getInstance();
        themeManager.applyThemeRecursively(mainPanel);
        SwingUtilities.updateComponentTreeUI(this);
        revalidate();
        repaint();
    }

    private String getIPFromClientNodeString(String val) {
        if (!val.startsWith("ClientNode")) {
            return val;
        }
        final String ipVal = val.substring(val.indexOf("hostName=") + 9);
        return ipVal.substring(0, ipVal.indexOf(",") );
    }

    /**
     * Initializes all the views and adds them to the card layout.
     */
    private void initViews() {
        // Initialize ViewModels
        loginViewModel = new LoginViewModel(rpc);
        bypassAuthViewModel = new BypassAuthViewModel(rpc);
        mainViewModel = new MainViewModel(rpc);

        // Initialize Views with their respective ViewModels
        LoginPage loginView = new LoginPage(loginViewModel);
        BypassAuthPage bypassAuthView = new BypassAuthPage(bypassAuthViewModel);
        MainPage mainView = new MainPage(mainViewModel);
        // MeetingPage and MeetingViewModel will be created lazily when needed

        // Add views to card layout
        mainPanel.add(loginView, LOGIN_VIEW);
        mainPanel.add(bypassAuthView, BYPASS_AUTH_VIEW);
        mainPanel.add(mainView, MAIN_VIEW);
        // MEETING_VIEW will be added when meeting is actually started/joined
        // Set up navigation listeners
        loginViewModel.currentUser.addListener(PropertyListeners.onUserProfileChanged(user -> {
            if (user != null) {
                this.currentUser = user;
                mainViewModel.setCurrentUser(currentUser);
                showView(MAIN_VIEW);
            }
        }));

        // Set up bypass auth navigation listener
        bypassAuthViewModel.currentUser.addListener(PropertyListeners.onUserProfileChanged(user -> {
            if (user != null) {
                this.currentUser = user;
                mainViewModel.setCurrentUser(currentUser);
                showView(MAIN_VIEW);
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
        // Will be initialized when meeting is actually started/joined
        MeetingPage[] meetingViewRef = new MeetingPage[] { null };

        // Use an array to hold the current active meeting view model reference
        // Will be initialized when meeting is actually started/joined
        MeetingViewModel[] activeMeetingViewModelRef = new MeetingViewModel[] { null };




        // New participant
        // rpc.subscribe(Utils.SUBSCRIBE_AS_VIEWER, data -> {
        //     final String viewerIP = new String(data);
        //     UserProfile new_user = new UserProfile(viewerIP, "New User", ParticipantRole.STUDENT);
        //     meetingViewModel.addParticipant(new_user);
        //     return new byte[0];
        // });

        rpc.subscribe("core/updateParticipants", (data) -> {
            try {
                // Only process if meeting view model exists (meeting is active)
                if (activeMeetingViewModelRef[0] == null) {
                    return new byte[0];
                }
                Map<ClientNode, UserProfile> participantsMap = DataSerializer.deserialize(data, new TypeReference<Map<ClientNode, UserProfile>>() {});
                System.out.println("App: participantsMap: " + participantsMap);
                participantsMap.forEach((clientNode, userProfile) -> {
                    System.out.println("App: clientNode: " + clientNode + " userProfile: " + userProfile);
                    activeMeetingViewModelRef[0].ipToMail.put(userProfile.getEmail(), clientNode.hostName());
                    // Use the currently active meeting view model
                    activeMeetingViewModelRef[0].addParticipant(userProfile);
                    System.out.println("App: participants: " + activeMeetingViewModelRef[0].participants.get());
                });
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return new byte[0];
        });

        // Handle meeting navigation - Start Meeting (Instructor role)
        mainViewModel.startMeetingRequested.addListener(PropertyListeners.onBooleanChanged(startMeeting -> {
            if (startMeeting && currentUser != null) {
                // First, get the meeting ID from MainViewModel by creating the meeting
                String meetingId = mainViewModel.startMeeting();
                
                // Only proceed if we successfully got a meeting ID
                if (meetingId == null || meetingId.trim().isEmpty()) {
                    System.err.println("App: Failed to create meeting - no meeting ID received from RPC");
                    mainViewModel.startMeetingRequested.set(false);
                    return;
                }

                // Create a new meeting view model for this meeting with Instructor role
                MeetingViewModel newMeetingViewModel = new MeetingViewModel(currentUser, "Instructor", rpc);
                
                // Update the active meeting view model reference
                activeMeetingViewModelRef[0] = newMeetingViewModel;
                
                // Explicitly pass the meeting ID from MainViewModel to MeetingViewModel
                newMeetingViewModel.setMeetingId(meetingId);
                System.out.println("App: Passing meeting ID from MainViewModel to MeetingViewModel: " + meetingId);

                // Create a new MeetingPage with the new view model
                meetingViewRef[0] = new MeetingPage(newMeetingViewModel);

                // Try to start the meeting
                newMeetingViewModel.startMeeting();

                // Only change view if meeting was successfully started
                if (!newMeetingViewModel.isMeetingActive.get()) {
                    System.err.println("App: Failed to start meeting - meeting not active");
                    mainViewModel.startMeetingRequested.set(false);
                    return;
                }

                // Set up listener for when meeting ends - navigate back to main view
                newMeetingViewModel.isMeetingActive.addListener(PropertyListeners.onBooleanChanged(isActive -> {
                    if (!isActive) {
                        showView(MAIN_VIEW);
                        // Reset the flag so the button can be clicked again
                        mainViewModel.startMeetingRequested.set(false);
                    }
                }));

                // Add meeting view to card layout if not already added
                // Check if component already exists to avoid duplicate additions
                boolean viewExists = false;
                for (Component comp : mainPanel.getComponents()) {
                    if (comp == meetingViewRef[0]) {
                        viewExists = true;
                        break;
                    }
                }
                if (!viewExists) {
                    mainPanel.add(meetingViewRef[0], MEETING_VIEW);
                }
                showView(MEETING_VIEW);

                // Reset the flag
                mainViewModel.startMeetingRequested.set(false);
            }
        }));

        // Handle join meeting navigation - Join Meeting (Student role)
        mainViewModel.joinMeetingRequested.addListener(PropertyListeners.onBooleanChanged(joinMeeting -> {
            if (joinMeeting && currentUser != null) {
                // Get the meeting code from MainViewModel
                String meetingCode = mainViewModel.meetingCode.get();

                
                // Only proceed if we have a valid meeting code
                if (meetingCode == null || meetingCode.trim().isEmpty()) {
                    System.err.println("App: Failed to join meeting - no meeting code provided");
                    mainViewModel.joinMeetingRequested.set(false);
                    return;
                }

                mainViewModel.joinMeeting(meetingCode);
                
                // Create a new meeting view model for joining meeting with Student role
                MeetingViewModel newMeetingViewModel = new MeetingViewModel(currentUser, "Student", rpc);

                // Create a new MeetingPage with the new view model
                meetingViewRef[0] = new MeetingPage(newMeetingViewModel);
                
                // Update the active meeting view model reference
                activeMeetingViewModelRef[0] = newMeetingViewModel;

                // Explicitly pass the meeting ID from MainViewModel to MeetingViewModel
                newMeetingViewModel.setMeetingId(meetingCode);
                System.out.println("App: Passing meeting code from MainViewModel to MeetingViewModel: " + meetingCode);
                
                // Try to start the meeting with the provided meeting ID
                newMeetingViewModel.startMeeting();

                // Only change view if meeting was successfully started
                if (!newMeetingViewModel.isMeetingActive.get()) {
                    System.err.println("App: Failed to join meeting - meeting not active");
                    mainViewModel.joinMeetingRequested.set(false);
                    mainViewModel.meetingCode.set("");
                    return;
                }

                // Set up listener for when meeting ends - navigate back to main view
                newMeetingViewModel.isMeetingActive.addListener(PropertyListeners.onBooleanChanged(isActive -> {
                    if (!isActive) {
                        showView(MAIN_VIEW);
                        // Reset the flag and meeting code so the button can be clicked again
                        mainViewModel.joinMeetingRequested.set(false);
                        mainViewModel.meetingCode.set("");

                    }
                }));

                // Add meeting view to card layout if not already added
                // Check if component already exists to avoid duplicate additions
                boolean viewExists = false;
                for (Component comp : mainPanel.getComponents()) {
                    if (comp == meetingViewRef[0]) {
                        viewExists = true;
                        break;
                    }
                }
                if (!viewExists) {
                    mainPanel.add(meetingViewRef[0], MEETING_VIEW);
                }
                showView(MEETING_VIEW);

                // Reset the flag and meeting code
                mainViewModel.joinMeetingRequested.set(false);
                mainViewModel.meetingCode.set("");
            }
        }));
    }

    /**
     * Shows the specified view.
     * 
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
        int portNumber = 6942;
        boolean bypassAuth = false;

        // Parse CLI arguments
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--bypass-auth".equals(arg) || "-b".equals(arg)) {
                bypassAuth = true;
            } else {
                // Try to parse as port number
                try {
                    portNumber = Integer.parseInt(arg);
                } catch (NumberFormatException e) {
                    // Ignore non-numeric arguments
                }
            }
        }

        bypassAuthMode = bypassAuth;

        final AbstractRPC rpc = new RPC();

        rpc.subscribe("core/updateParticipants", (data) -> {return new byte[0];});
        rpc.subscribe("chat:new-message", (d) -> {return new byte[0];});
        rpc.subscribe("chat:file-metadata-received", (d) -> {return new byte[0];});
        rpc.subscribe("chat:file-saved-success", (d) -> {return new byte[0];});
        rpc.subscribe("chat:file-saved-error", (d) -> {return new byte[0];});
        rpc.subscribe("chat:message-deleted", (d) -> {return new byte[0];});

        rpc.subscribe("updateUI", (d) -> {return new byte[0];});
        rpc.subscribe("stopShare", (d) -> {return new byte[0];});
    
        App app = App.getInstance(rpc);

        // Create and show the application window

        Thread handler = null;
        try {
            handler = rpc.connect(portNumber);
        } catch (IOException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        // try {
        //     byte[] data = rpc.call("core/register", new byte[0]).get();
        //     System.out.println("Data: " + data.length);
        // } catch (InterruptedException | ExecutionException e) {
        //     throw new RuntimeException(e);
        // }

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
    public UserProfile getCurrentUser() {
        return currentUser;
    }

    /**
     * Sets the current user and updates the UI accordingly.
     * 
     * @param user The user to set as current, or null to log out
     */
    public void setCurrentUser(UserProfile user) {
        this.currentUser = user;
        // Update UI based on authentication state
        if (user != null) {
            showView(MAIN_VIEW);
        } else {
            if (bypassAuthMode) {
                showView(BYPASS_AUTH_VIEW);
            } else {
                showView(LOGIN_VIEW);
            }
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
            // Reset all flags and meeting code
            mainViewModel.logoutRequested.set(false);
            mainViewModel.startMeetingRequested.set(false);
            mainViewModel.joinMeetingRequested.set(false);
            mainViewModel.meetingCode.set("");
        }

        // Reset LoginViewModel to ensure clean state
        if (loginViewModel != null) {
            loginViewModel.reset();
        }

        // Reset BypassAuthViewModel to ensure clean state
        if (bypassAuthViewModel != null) {
            bypassAuthViewModel.reset();
        }

        // Logout from auth service (this will clear all user data)
        // TODO USE RPC TO LOGOUT

        // Clear view history
        viewHistory.clear();

        // Navigate to appropriate auth view based on mode
        if (bypassAuthMode) {
            showView(BYPASS_AUTH_VIEW);
        } else {
            showView(LOGIN_VIEW);
        }
    }
}