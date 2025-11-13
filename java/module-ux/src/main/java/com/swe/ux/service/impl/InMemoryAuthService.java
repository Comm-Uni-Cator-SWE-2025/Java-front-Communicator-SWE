//package com.swe.ux.service.impl;
//
//import java.io.IOException;
//import java.security.GeneralSecurityException;
//import java.util.HashMap;
//import java.util.Map;
//
//import com.swe.controller.Auth.AuthService;
//import com.swe.controller.Auth.GoogleAuthService;
//import com.swe.controller.Meeting.ParticipantRole;
//import com.swe.controller.Meeting.UserProfile;
//
///**
// * In-memory implementation of AuthService for demonstration purposes.
// * In a real application, this would connect to a proper authentication service.
// */
//public class InMemoryAuthService implements AuthService {
//    private final Map<String, UserProfile> users = new HashMap<>();
//    private UserProfile currentUser = null;
//
//    public InMemoryAuthService() {
//        // Add a demo user
//        UserProfile demoUser = new UserProfile(
//                "demo@example.com",
//                "Demo User",
//                null,
//                ParticipantRole.STUDENT);
//        users.put("demo@example.com", demoUser);
//    }
//
//    public UserProfile authenticate(String username, String password) throws AuthenticationException {
//        // Simulate network delay
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//
//        // For demo purposes, any password works for the demo user
//        if ("demo".equals(username)) {
//            this.currentUser = users.get("demo@example.com");
//            return currentUser;
//        }
//
//        throw new AuthenticationException("Invalid username or password");
//    }
//
//    @Override
//    public void logout() {
//        // Clear current user
//        this.currentUser = null;
//
//        // Remove all Google-authenticated users (keep only demo user)
//        users.entrySet().removeIf(entry -> {
//            String email = entry.getKey();
//            // Keep demo user, remove all others (especially Google users)
//            return !"demo@example.com".equals(email);
//        });
//
//        // Clear stored Google OAuth tokens to allow account selection on next login
//        try {
//            GoogleAuthService googleAuthService = new GoogleAuthService();
//            googleAuthService.clearStoredTokens();
//        } catch (IOException e) {
//            // Log but don't fail logout if token clearing fails
//            System.err.println("Warning: Failed to clear Google OAuth tokens: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public boolean isLoggedIn() {
//        return currentUser != null;
//    }
//
//    public UserProfile getCurrentUser() {
//        return currentUser;
//    }
//
//    public UserProfile register(String username, String password, String email) throws RegistrationException {
//        if (users.containsKey(email)) {
//            throw new RegistrationException("Email already exists");
//        }
//
//        // In a real app, we'd validate the email and password strength here
//        if (username == null || username.trim().isEmpty()) {
//            throw new RegistrationException("Username is required");
//        }
//
//        if (password == null || password.length() < 6) {
//            throw new RegistrationException("Password must be at least 6 characters");
//        }
//
//        if (email == null || email.trim().isEmpty()) {
//            throw new RegistrationException("Email is required");
//        }
//
//        UserProfile newUser = new UserProfile(
//                email.trim(),
//                username.trim(), // Use username as display name
//                null,
//                ParticipantRole.STUDENT);
//
//        users.put(email.trim(), newUser);
//        return newUser;
//    }
//
//    public UserProfile loginWithGoogle(String email, String displayName) {
//        // This method signature is kept for backward compatibility but now uses real
//        // Google auth
//        // The actual Google authentication happens in LoginViewModel
//
//        // Check if user already exists, otherwise create new one
//        UserProfile user = users.get(email);
//        if (user == null) {
//            user = new UserProfile(
//                    email,
//                    displayName != null ? displayName : email,
//                    null,
//                    ParticipantRole.STUDENT);
//            users.put(email, user);
//        }
//
//        // Set as current user
//        this.currentUser = user;
//        return user;
//    }
//
//    /**
//     * Authenticates a user using Google OAuth2.
//     * This method performs the actual Google authentication flow.
//     *
//     * @return The authenticated user
//     * @throws AuthService.AuthenticationException If authentication fails
//     */
//    public UserProfile authenticateWithGoogle() throws AuthService.AuthenticationException {
//        return authenticateWithGoogle(false);
//    }
//
//    /**
//     * Authenticates a user using Google OAuth2.
//     * This method performs the actual Google authentication flow.
//     * Uses the new controller logic that allows anyone with a Google account to
//     * login.
//     *
//     * @param forceAccountSelection If true, clears stored tokens to allow selecting
//     *                              a different account
//     * @return The authenticated user
//     * @throws AuthService.AuthenticationException If authentication fails
//     */
//    public UserProfile authenticateWithGoogle(boolean forceAccountSelection) throws AuthService.AuthenticationException {
//        try {
//            GoogleAuthService googleAuthService = new GoogleAuthService();
//            // Use new controller logic - returns UserProfile, allows any Google account
//            UserProfile userProfile = googleAuthService.authenticateWithGoogle(forceAccountSelection);
//
//            if (userProfile == null || userProfile.getEmail() == null) {
//                throw new AuthService.AuthenticationException("Google authentication failed");
//            }
//
//            String email = userProfile.getEmail();
//
//            // Check if user already exists, otherwise create new one
//            UserProfile user = users.get(email);
//            if (user == null) {
//                // Use the userProfile directly
//                user = userProfile;
//                users.put(email, user);
//            } else {
//                // Update display name and other fields if they changed
//                user.setDisplayName(userProfile.getDisplayName());
//                user.setLogoUrl(userProfile.getLogoUrl());
//                if (userProfile.getRole() != null) {
//                    user.setRole(userProfile.getRole());
//                }
//            }
//
//            // Set as current user
//            this.currentUser = user;
//            return user;
//
//        } catch (GeneralSecurityException | IOException e) {
//            throw new AuthService.AuthenticationException("Google authentication error: " + e.getMessage(), e);
//        }
//    }
//
//    @Override
//    public UserProfile authenticate() throws AuthenticationException {
//        // Delegate to Google authentication
//        return authenticateWithGoogle();
//    }
//
//    public static class RegistrationException extends Exception {
//        public RegistrationException(String message) {
//            super(message);
//        }
//    }
//}
