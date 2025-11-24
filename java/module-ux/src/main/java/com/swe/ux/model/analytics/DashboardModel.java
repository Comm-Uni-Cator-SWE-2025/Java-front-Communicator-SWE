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

    /**
     * Creates a new dashboard model.
     *
     * @param present number of users present
     * @param loggedOut number of users logged out
     * @param summary meeting summary
     */
    public DashboardModel(final int present,
                          final int loggedOut,
                          final String summary) {
        this.usersPresent = present;
        this.usersLoggedOut = loggedOut;
        this.meetingSummary = summary;
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
}
