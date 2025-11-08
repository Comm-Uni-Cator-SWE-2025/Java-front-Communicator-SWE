package com.swe.controller.Auth;

import com.swe.controller.Meeting.UserProfile;
import java.security.GeneralSecurityException;

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
    UserProfile authenticate() throws AuthenticationException, GeneralSecurityException;

    /**
     * Logs out the current user.
     */
    void logout();

    /**
     * Checks if there is an authenticated user.
     * @return true if a user is authenticated, false otherwise
     */
    boolean isLoggedIn();

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
}
