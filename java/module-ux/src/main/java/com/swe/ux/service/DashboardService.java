package com.swe.ux.service;

import com.swe.ux.model.analytics.DashboardModel;

public class DashboardService {

    public DashboardModel fetchDashboardData() {
        // Hardcoded values - replace with actual API call later
        int usersPresent = 120;
        int usersLoggedOut = 15;
        String meetingSummary = "The previous meeting was very productive and tasks were assigned.";

        return new DashboardModel(usersPresent, usersLoggedOut, meetingSummary);
    }
}