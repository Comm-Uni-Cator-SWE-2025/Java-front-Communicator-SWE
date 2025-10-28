package com.swe.ux.service;

import com.swe.ux.model.UserProfile;

public class ControllerService {
    private static ControllerService instance;
    private UserProfile currentUser;
    
    private ControllerService() {
        // Private constructor to enforce singleton pattern
    }
    
    public static synchronized ControllerService getInstance() {
        if (instance == null) {
            instance = new ControllerService();
        }
        return instance;
    }
    
    public UserProfile getCurrentUser() {
        return currentUser;
    }
    
    public void setCurrentUser(UserProfile user) {
        this.currentUser = user;
    }
    
    /**
     * Attempt to log in a user with the given credentials
     * @param username The username
     * @param password The password
     * @return true if login is successful, false otherwise
     */
    public boolean login(String username, String password) {
        // TODO: Implement actual authentication logic
        // For now, we'll just create a dummy user
        if (username != null && !username.trim().isEmpty() && password != null && !password.trim().isEmpty()) {
            this.currentUser = new UserProfile(username, username + "@example.com", username, "user-" + System.currentTimeMillis());
            return true;
        }
        return false;
    }
    
    /**
     * Register a new user
     * @param username The username
     * @param email The email address
     * @param password The password
     * @param confirmPassword The password confirmation
     * @return true if registration is successful, false otherwise
     */
    public boolean register(String username, String email, String password, String confirmPassword) {
        // TODO: Implement actual registration logic
        // For now, we'll just create a dummy user if passwords match
        if (password.equals(confirmPassword) && username != null && !username.trim().isEmpty()) {
            this.currentUser = new UserProfile(username, email, username, "user-" + System.currentTimeMillis());
            return true;
        }
        return false;
    }
    
    public void logout() {
        this.currentUser = null;
    }
}
