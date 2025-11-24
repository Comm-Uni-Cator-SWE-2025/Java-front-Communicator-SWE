package com.swe.ux.viewmodels;

import com.swe.ux.model.analytics.DashboardModel;
import com.swe.ux.service.DashboardService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

public class DashboardViewModel {
    private final DashboardService apiService;

    private final IntegerProperty usersPresent = new SimpleIntegerProperty();
    private final IntegerProperty usersLoggedOut = new SimpleIntegerProperty();
    private final StringProperty meetingSummary = new SimpleStringProperty();

    public DashboardViewModel() {
        this.apiService = new DashboardService();
        loadData();
    }

    private void loadData() {
        DashboardModel model = apiService.fetchDashboardData();
        usersPresent.set(model.getUsersPresent());
        usersLoggedOut.set(model.getUsersLoggedOut());
        meetingSummary.set(model.getMeetingSummary());
    }

    public IntegerProperty usersPresentProperty() {
        return usersPresent;
    }

    public IntegerProperty usersLoggedOutProperty() {
        return usersLoggedOut;
    }

    public StringProperty meetingSummaryProperty() {
        return meetingSummary;
    }
}