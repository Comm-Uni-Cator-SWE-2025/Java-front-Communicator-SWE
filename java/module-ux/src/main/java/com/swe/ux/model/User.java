package com.swe.ux.model;

/**
 * Represents a user in the system.
 */
public class User {
    private final String id;
    private final String username;
    private String displayName;
    private String email;
    private String profileImageUrl;
    private boolean isOnline;

    public User(String id, String username, String displayName, String email) {
        this.id = id;
        this.username = username;
        this.displayName = displayName != null ? displayName : username;
        this.email = email;
        this.isOnline = false;
    }

    // Getters and Setters
    public String getId() { 
        return id; 
    }

    public String getUsername() { 
        return username; 
    }

    public String getDisplayName() { 
        return displayName; 
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() { 
        return email; 
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", displayName='" + displayName + '\'' +
                ", email='" + email + '\'' +
                ", isOnline=" + isOnline +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
