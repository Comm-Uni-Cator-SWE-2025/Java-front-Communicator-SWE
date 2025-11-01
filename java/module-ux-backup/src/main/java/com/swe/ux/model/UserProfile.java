package com.swe.ux.model;

import java.io.Serializable;

public class UserProfile implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String username;
    private String email;
    private String displayName;
    private String userId;
    
    public UserProfile() {
        // Default constructor
    }
    
    public UserProfile(String username, String email, String displayName, String userId) {
        this.username = username;
        this.email = email;
        this.displayName = displayName;
        this.userId = userId;
    }
    
    // Getters and Setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getDisplayName() {
        return displayName != null && !displayName.isEmpty() ? displayName : username;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    @Override
    public String toString() {
        return "UserProfile{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", displayName='" + displayName + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
