package com.swe.ux.service;

import com.swe.ux.model.analytics.DashboardModel;

/**
 * Service for fetching dashboard data.
 */
public class DashboardService {
    /**
     * Default number of users present.
     */
    private static final int DEFAULT_USERS_PRESENT = 120;
    /**
     * Default number of users logged out.
     */
    private static final int DEFAULT_USERS_LOGGED_OUT = 15;

    /**
     * Fetches dashboard data with hardcoded values.
     *
     * @return DashboardModel containing dashboard data
     */
    public DashboardModel fetchDashboardData() {
        // Hardcoded values - replace with actual API call later
        final int usersPresent = DEFAULT_USERS_PRESENT;
        final int usersLoggedOut = DEFAULT_USERS_LOGGED_OUT;
        final String meetingSummary = "The previous meeting was very productive and tasks were assigned.";

        return new DashboardModel(usersPresent, usersLoggedOut, meetingSummary);
    }
}