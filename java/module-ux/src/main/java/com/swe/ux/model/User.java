package com.swe.ux.model;

/**
 * Represents a user in the system.
 */
public class User {
    /**
     * The unique user ID.
     */
    private final String id;
    /**
     * The username.
     */
    private final String username;
    /**
     * The display name.
     */
    private String displayName;
    /**
     * The email address.
     */
    private String email;
    /**
     * The profile image URL.
     */
    private String profileImageUrl;
    /**
     * Whether the user is online.
     */
    private boolean isOnline;

    /**
     * Creates a new User.
     *
     * @param userId the user ID
     * @param userName the username
     * @param displayUserName the display name
     * @param userEmail the email address
     */
    public User(final String userId, final String userName,
                final String displayUserName, final String userEmail) {
        this.id = userId;
        this.username = userName;
        if (displayUserName != null) {
            this.displayName = displayUserName;
        } else {
            this.displayName = userName;
        }
        this.email = userEmail;
        this.isOnline = false;
    }

    /**
     * Gets the user ID.
     *
     * @return the user ID
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the display name.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name.
     *
     * @param displayUserName the display name
     */
    public void setDisplayName(final String displayUserName) {
        this.displayName = displayUserName;
    }

    /**
     * Gets the email.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email.
     *
     * @param userEmail the email
     */
    public void setEmail(final String userEmail) {
        this.email = userEmail;
    }

    /**
     * Gets the profile image URL.
     *
     * @return the profile image URL
     */
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    /**
     * Sets the profile image URL.
     *
     * @param profileUrl the profile image URL
     */
    public void setProfileImageUrl(final String profileUrl) {
        this.profileImageUrl = profileUrl;
    }

    /**
     * Checks if user is online.
     *
     * @return true if online
     */
    public boolean isOnline() {
        return isOnline;
    }

    /**
     * Sets the online status.
     *
     * @param online the online status
     */
    public void setOnline(final boolean online) {
        isOnline = online;
    }

    @Override
    public String toString() {
        return "User{"
                + "id='" + id + '\''
                + ", username='" + username + '\''
                + ", displayName='" + displayName + '\''
                + ", email='" + email + '\''
                + ", isOnline=" + isOnline
                + '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final User user = (User) o;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
