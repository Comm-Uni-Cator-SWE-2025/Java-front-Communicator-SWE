package com.swe.ux.viewmodels;

import com.swe.ux.model.analytics.DashboardModel;
import com.swe.ux.service.DashboardService;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * ViewModel for dashboard analytics display.
 */
public class DashboardViewModel {
    /** API service for dashboard data. */
    private final DashboardService apiService;

    /** Users present property. */
    private final IntegerProperty usersPresent = new SimpleIntegerProperty();
    /** Users logged out property. */
    private final IntegerProperty usersLoggedOut = new SimpleIntegerProperty();
    /** Meeting summary property. */
    private final StringProperty meetingSummary = new SimpleStringProperty();

    /**
     * Creates a new DashboardViewModel.
     */
    public DashboardViewModel() {
        this.apiService = new DashboardService();
        loadData();
    }

    private void loadData() {
        final DashboardModel model = apiService.fetchDashboardData();
        usersPresent.set(model.getUsersPresent());
        usersLoggedOut.set(model.getUsersLoggedOut());
        meetingSummary.set(model.getMeetingSummary());
    }

    /**
     * Returns the users present property.
     * @return The users present property
     */
    public IntegerProperty usersPresentProperty() {
        return usersPresent;
    }

    /**
     * Returns the users logged out property.
     * @return The users logged out property
     */
    public IntegerProperty usersLoggedOutProperty() {
        return usersLoggedOut;
    }

    /**
     * Returns the meeting summary property.
     * @return The meeting summary property
     */
    public StringProperty meetingSummaryProperty() {
        return meetingSummary;
    }
}