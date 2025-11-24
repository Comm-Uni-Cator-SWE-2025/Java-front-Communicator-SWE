package com.swe.ux.model.analytics;

public class DashboardModel {
    private int usersPresent;
    private int usersLoggedOut;
    private String meetingSummary;
    
    public DashboardModel(int usersPresent, int usersLoggedOut, String meetingSummary) {
        this.usersPresent = usersPresent;
        this.usersLoggedOut = usersLoggedOut;
        this.meetingSummary = meetingSummary;
    }
    
    public int getUsersPresent() {
        return usersPresent;
    }
    
    public int getUsersLoggedOut() {
        return usersLoggedOut;
    }
    
    public String getMeetingSummary() {
        return meetingSummary;
    }
}