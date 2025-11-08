package com.swe.ux.service.impl;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.swe.controller.Auth.GoogleAuthService;
import com.swe.controller.Meeting.UserProfile;
import com.swe.screenNVideo.Utils;
import com.swe.ux.model.User;
import com.swe.ux.service.AuthService;

/**
 * In-memory implementation of AuthService for demonstration purposes.
 * In a real application, this would connect to a proper authentication service.
 */
public class InMemoryAuthService implements AuthService {
    private final Map<String, User> users = new HashMap<>();
    private User currentUser = null;

    public InMemoryAuthService() {
        // Add a demo user
        User demoUser = new User(
                Utils.getSelfIP(),
                "demo",
                "Demo User",
                "demo@example.com");
        users.put("demo", demoUser);
    }

    @Override
    public User authenticate(String username, String password) throws AuthenticationException {
        // Simulate network delay
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // For demo purposes, any password works for the demo user
        if ("demo".equals(username)) {
            this.currentUser = users.get(username);
            return currentUser;
        }

        throw new AuthenticationException("Invalid username or password");
    }

    @Override
    public void logout() {
        // Clear current user
        this.currentUser = null;

        // Remove all Google-authenticated users (keep only demo user)
        users.entrySet().removeIf(entry -> {
            String username = entry.getKey();
            // Keep demo user, remove all others (especially Google users)
            return !"demo".equals(username);
        });

        // Clear stored Google OAuth tokens to allow account selection on next login
        try {
            com.swe.controller.Auth.GoogleAuthServices googleAuthServices = new com.swe.controller.Auth.GoogleAuthServices();
            googleAuthServices.clearStoredTokens();
        } catch (IOException e) {
            // Log but don't fail logout if token clearing fails
            System.err.println("Warning: Failed to clear Google OAuth tokens: " + e.getMessage());
        }
    }

    @Override
    public boolean isAuthenticated() {
        return currentUser != null;
    }

    @Override
    public User getCurrentUser() {
        return currentUser;
    }

    @Override
    public User register(String username, String password, String email) throws RegistrationException {
        if (users.containsKey(username)) {
            throw new RegistrationException("Username already exists");
        }

        // In a real app, we'd validate the email and password strength here
        if (username == null || username.trim().isEmpty()) {
            throw new RegistrationException("Username is required");
        }

        if (password == null || password.length() < 6) {
            throw new RegistrationException("Password must be at least 6 characters");
        }

        User newUser = new User(
                Utils.getSelfIP(),
                username.trim(),
                username.trim(), // Use username as display name by default
                email != null ? email.trim() : null);

        users.put(username, newUser);
        return newUser;
    }

    @Override
    public User loginWithGoogle(String email, String displayName) {
        // This method signature is kept for backward compatibility but now uses real
        // Google auth
        // The actual Google authentication happens in LoginViewModel
        // Extract username from email (part before @)
        String username = email != null && email.contains("@")
                ? email.substring(0, email.indexOf("@"))
                : "google_user_" + UUID.randomUUID().toString().substring(0, 8);

        // Check if user already exists, otherwise create new one
        User user = users.get(username);
        if (user == null) {
            user = new User(
                    Utils.getSelfIP(),
                    username,
                    displayName != null ? displayName : username,
                    email);
            users.put(username, user);
        }

        // Set as current user
        this.currentUser = user;
        return user;
    }

    /**
     * Authenticates a user using Google OAuth2.
     * This method performs the actual Google authentication flow.
     * 
     * @return The authenticated user
     * @throws AuthService.AuthenticationException If authentication fails
     */
    public User authenticateWithGoogle() throws AuthService.AuthenticationException {
        return authenticateWithGoogle(false);
    }

    /**
     * Authenticates a user using Google OAuth2.
     * This method performs the actual Google authentication flow.
     * Uses the new controller logic that allows anyone with a Google account to
     * login.
     * 
     * @param forceAccountSelection If true, clears stored tokens to allow selecting
     *                              a different account
     * @return The authenticated user
     * @throws AuthService.AuthenticationException If authentication fails
     */
    public User authenticateWithGoogle(boolean forceAccountSelection) throws AuthService.AuthenticationException {
        try {
            GoogleAuthService googleAuthService = new GoogleAuthService();
            // Use new controller logic - returns UserProfile, allows any Google account
            UserProfile userProfile = googleAuthService.authenticateWithGoogle(forceAccountSelection);

            if (userProfile == null || userProfile.getEmail() == null) {
                throw new AuthService.AuthenticationException("Google authentication failed");
            }

            // Convert UserProfile to User model for UX layer
            String email = userProfile.getEmail();
            String displayName = userProfile.getDisplayName() != null
                    ? userProfile.getDisplayName()
                    : email;

            // Check if user already exists, otherwise create new one
            User user = users.get(email);
            if (user == null) {
                user = new User(
                        Utils.getSelfIP(),
                        email,
                        displayName,
                        email);
                users.put(email, user);
            } else {
                // Update display name and email if they changed
                // Note: User model doesn't have setters, so we create a new one
                user = new User(
                        user.getId(),
                        email,
                        displayName,
                        email);
                users.put(email, user);
            }

            // Set as current user
            this.currentUser = user;
            return user;

        } catch (GeneralSecurityException | IOException e) {
            throw new AuthService.AuthenticationException("Google authentication error: " + e.getMessage(), e);
        }
    }
}
