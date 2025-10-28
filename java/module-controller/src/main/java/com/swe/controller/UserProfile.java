package com.swe.controller;
import java.util.UUID;

/**
 * Represents a user's core, permanent profile in the system.
 * This object is the single source of truth for a user's identity.
 * It is created during registration and persists between sessions.
 */
public class UserProfile {

    /**
     * A unique, non-changing identifier for the user (e.g., a UUID).
     * This is the primary key in the user database.
     */
    private final String userId;

    /**
     * The user's email address, used as their login username.
     */
    private final String email;

    /**
     * The user's display name.
     */
    private String displayName;

    /**
     * The user's role, either "student" or "instructor".
     */
    private final String role;

    /**
     * The URL to the user's profile picture/logo.
     */
    private String logoUrl;

    /**
     * The securely hashed password.
     */
    private final String passwordHash;

    /**
     * @param email The user's unique email address.
     * @param displayName The user's display name.
     * @param passwordHash The BCrypt-hashed password.
     * @param logoUrl The URL for the user's avatar.
     */
    public UserProfile(String email, String displayName, String passwordHash, String logoUrl, String userRole) {
        this.userId = UUID.randomUUID().toString(); // Generate a unique ID on creation
        this.email = email;
        this.displayName = displayName;
        this.role = userRole;
        this.passwordHash = passwordHash;
        this.logoUrl = logoUrl;
    }

    // --- Getters ---

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public String getRole() {
        return role;
    }

    // --- Setters for mutable fields ---

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
}