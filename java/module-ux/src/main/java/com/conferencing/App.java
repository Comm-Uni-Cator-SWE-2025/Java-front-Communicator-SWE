package com.conferencing;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.conferencing.service.ControllerService;
import com.conferencing.theme.ThemeManager;
import com.conferencing.ui.CustomButton;
import com.conferencing.views.LoginPage;
import com.conferencing.views.MainPage;
import com.conferencing.views.MeetingInterface;
import com.conferencing.views.RegisterPage;
import com.swe.controller.UserProfile;

public class App {
    private JFrame frame;
    private JPanel mainPanel;
    private JPanel navPanel;
    private CardLayout cardLayout;
    private List<String> history = new ArrayList<>();
    private int currentIndex = -1;
    private CustomButton backButton;
    private CustomButton forwardButton;
    
    // Controller service for managing authentication and meetings
    private final ControllerService controllerService;

    public static final String LOGIN_PAGE = "Login";
    public static final String REGISTER_PAGE = "Register";
    public static final String MAIN_PAGE = "Main";
    public static final String MEETING_PAGE = "Meeting";

    public App() {
        this.controllerService = ControllerService.getInstance();
        initComponents();
        ThemeManager.getInstance().setApp(this);
    }
    
    public ControllerService getControllerService() {
        return controllerService;
    }
    
    public UserProfile getCurrentUser() {
        return controllerService.getCurrentUser();
    }
    
    public void setCurrentUser(UserProfile user) {
        controllerService.setCurrentUser(user);
    }

    private void initComponents() {
        frame = new JFrame("Comm-Uni-Cate");
        ThemeManager.getInstance().setMainFrame(frame); // Pass frame to manager

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);

        JPanel contentPanel = new JPanel(new BorderLayout());
        
        // Navigation Panel
        navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        navPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        backButton = new CustomButton("← Back", false);
        backButton.setEnabled(false);
        backButton.addActionListener(e -> navigateBack());
        
        forwardButton = new CustomButton("Forward →", false);
        forwardButton.setEnabled(false);
        forwardButton.addActionListener(e -> navigateForward());
        
        navPanel.add(backButton);
        navPanel.add(forwardButton);
        
        contentPanel.add(navPanel, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(new LoginPage(this), LOGIN_PAGE);
        mainPanel.add(new RegisterPage(this), REGISTER_PAGE);
        mainPanel.add(new MainPage(this), MAIN_PAGE);
        mainPanel.add(new MeetingInterface(this), MEETING_PAGE);

        contentPanel.add(mainPanel, BorderLayout.CENTER);
        frame.add(contentPanel);
        
        applyTheme();
    }

    public void showPage(String pageName) {
        // Add to history
        if (currentIndex < history.size() - 1) {
            // Remove forward history if we're not at the end
            history.subList(currentIndex + 1, history.size()).clear();
        }
        history.add(pageName);
        currentIndex++;
        
        cardLayout.show(mainPanel, pageName);
        updateNavigationButtons();
    }
    
    private void navigateBack() {
        if (currentIndex > 0) {
            currentIndex--;
            cardLayout.show(mainPanel, history.get(currentIndex));
            updateNavigationButtons();
        }
    }
    
    private void navigateForward() {
        if (currentIndex < history.size() - 1) {
            currentIndex++;
            cardLayout.show(mainPanel, history.get(currentIndex));
            updateNavigationButtons();
        }
    }
    
    private void updateNavigationButtons() {
        backButton.setEnabled(currentIndex > 0);
        forwardButton.setEnabled(currentIndex < history.size() - 1);
    }
    
    private void applyTheme() {
        if (navPanel != null) {
            navPanel.setBackground(ThemeManager.getInstance().getTheme().getBackground());
        }
    }
    
    public void refreshTheme() {
        applyTheme();
    }

    public void start() {
        frame.setVisible(true);
        showPage(LOGIN_PAGE); // Start with login page
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App().start());
    }
}

