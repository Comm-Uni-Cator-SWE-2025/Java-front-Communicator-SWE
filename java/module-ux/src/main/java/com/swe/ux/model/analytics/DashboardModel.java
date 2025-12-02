package com.swe.ux.model.analytics;

/**
 * Model representing dashboard analytics data.
 */
public class DashboardModel {

    /** Number of users currently present. */
    private int usersPresent;

    /** Number of users logged out. */
    private int usersLoggedOut;

    /** Meeting summary text. */
    private String meetingSummary;
    /** Raw payload string shown to the user. */
    private String rawPayload;

    /**
     * Creates a new dashboard model.
     *
     * @param present number of users present
     * @param loggedOut number of users logged out
     * @param summary meeting summary
     * @param payload full payload string
     */
    public DashboardModel(final int present,
                          final int loggedOut,
                          final String summary,
                          final String payload) {
        this.usersPresent = present;
        this.usersLoggedOut = loggedOut;
        this.meetingSummary = summary;
        this.rawPayload = payload;
    }

    /**
     * Gets the number of users present.
     *
     * @return users present count
     */
    public int getUsersPresent() {
        return usersPresent;
    }

    /**
     * Gets the number of users logged out.
     *
     * @return users logged out count
     */
    public int getUsersLoggedOut() {
        return usersLoggedOut;
    }

    /**
     * Gets the meeting summary.
     *
     * @return meeting summary
     */
    public String getMeetingSummary() {
        return meetingSummary;
    }

    /**
     * Gets the raw payload text.
     * @return raw payload text
     */
    public String getRawPayload() {
        return rawPayload;
    }
}
