package com.swe.ux.service.impl;

import com.swe.ux.model.User;
import com.swe.ux.service.AuthService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
            UUID.randomUUID().toString(),
            "demo",
            "Demo User",
            "demo@example.com"
        );
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
        this.currentUser = null;
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
            UUID.randomUUID().toString(),
            username.trim(),
            username.trim(), // Use username as display name by default
            email != null ? email.trim() : null
        );

        users.put(username, newUser);
        return newUser;
    }
}
