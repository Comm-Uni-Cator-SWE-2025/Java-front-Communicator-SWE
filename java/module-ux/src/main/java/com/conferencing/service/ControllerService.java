package com.conferencing.service;

import com.swe.controller.AuthService;
import com.swe.controller.MeetingServices;
import com.swe.controller.UserProfile;

/**
 * Singleton service that manages the controller module integration.
 * Provides centralized access to AuthService, MeetingServices, and current user session.
 */
public class ControllerService {
    private static ControllerService instance;
    
    private final AuthService authService;
    private final MeetingServices meetingServices;
    private UserProfile currentUser;
    
    private ControllerService() {
        this.authService = new AuthService();
        this.meetingServices = new MeetingServices();
        this.currentUser = null;
    }
    
    public static ControllerService getInstance() {
        if (instance == null) {
            instance = new ControllerService();
        }
        return instance;
    }
    
    // AuthService methods
    public UserProfile login(String email, String password) {
        UserProfile user = authService.login(email, password);
        if (user != null) {
            this.currentUser = user;
        }
        return user;
    }
    
    public UserProfile register(String email, String password, String displayName, String logoUrl) {
        UserProfile user = authService.register(email, password, displayName, logoUrl);
        if (user != null) {
            this.currentUser = user;
        }
        return user;
    }
    
    // Session management
    public UserProfile getCurrentUser() {
        return currentUser;
    }
    
    public void setCurrentUser(UserProfile user) {
        this.currentUser = user;
    }
    
    public void logout() {
        this.currentUser = null;
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    // Direct access to services (if needed)
    public AuthService getAuthService() {
        return authService;
    }
    
    public MeetingServices getMeetingServices() {
        return meetingServices;
    }
}

