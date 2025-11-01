package com.swe.ux.service;

import com.swe.ux.model.User;

/**
 * Service responsible for handling authentication-related operations.
 */
public interface AuthService {
    /**
     * Attempts to authenticate a user with the provided credentials.
     * @param username The username
     * @param password The password
     * @return The authenticated user
     * @throws AuthenticationException If authentication fails
     */
    User authenticate(String username, String password) throws AuthenticationException;

    /**
     * Logs out the current user.
     */
    void logout();

    /**
     * Checks if there is an authenticated user.
     * @return true if a user is authenticated, false otherwise
     */
    boolean isAuthenticated();

    /**
     * Gets the currently authenticated user.
     * @return The current user, or null if no user is authenticated
     */
    User getCurrentUser();

    /**
     * Registers a new user.
     * @param username The username
     * @param password The password
     * @param email The email address
     * @return The newly registered user
     * @throws RegistrationException If registration fails
     */
    User register(String username, String password, String email) throws RegistrationException;

    /**
     * Exception thrown when authentication fails.
     */
    class AuthenticationException extends Exception {
        public AuthenticationException(String message) {
            super(message);
        }

        public AuthenticationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Exception thrown when user registration fails.
     */
    class RegistrationException extends Exception {
        public RegistrationException(String message) {
            super(message);
        }

        public RegistrationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
