package com.swe.ux.viewmodels;

import com.swe.controller.Meeting.UserProfile;
import com.swe.ux.model.analytics.DashboardModel;
import com.swe.ux.service.DashboardService;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * ViewModel for dashboard analytics display.
 */
public class DashboardViewModel {
    /** Placeholder summary shown while loading. */
    private static final String LOADING_MESSAGE = "Loading analytics from cloud…";
    /** API service for dashboard data. */
    private final DashboardService apiService;

    /** Users present property. */
    private final IntegerProperty usersPresent = new SimpleIntegerProperty();
    /** Users logged out property. */
    private final IntegerProperty usersLoggedOut = new SimpleIntegerProperty();
    /** Meeting summary property. */
    private final StringProperty meetingSummary = new SimpleStringProperty(LOADING_MESSAGE);
    /** Raw payload property. */
    private final StringProperty rawPayload = new SimpleStringProperty("No data loaded.");

    /**
     * Creates a new DashboardViewModel.
     */
    public DashboardViewModel() {
        this(new DashboardService());
    }

    DashboardViewModel(final DashboardService service) {
        this.apiService = service;
    }

    /**
     * Refreshes dashboard analytics for the given user.
     * @param user current signed-in user
     */
    public void refreshForUser(final UserProfile user) {
        final String userId = user == null ? null : user.getEmail();
        showLoadingMessage();
        if (apiService == null) {
            applyModel(new DashboardModel(0, 0, LOADING_MESSAGE, "No cloud data returned yet."));
            return;
        }
        apiService.fetchDashboardData(userId)
                .thenAccept(this::applyModel);
    }

    private void applyModel(final DashboardModel model) {
        if (model == null) {
            return;
        }
        final Runnable updateTask = () -> {
            usersPresent.set(model.getUsersPresent());
            usersLoggedOut.set(model.getUsersLoggedOut());
            meetingSummary.set(model.getMeetingSummary());
            final String payload = model.getRawPayload() == null ? "No cloud data returned yet." : model.getRawPayload();
            rawPayload.set(payload);
            System.out.println("Dashboard raw payload:\n" + payload);
        };
        if (Platform.isFxApplicationThread()) {
            updateTask.run();
        } else {
            Platform.runLater(updateTask);
        }
    }

    private void showLoadingMessage() {
        final Runnable task = () -> meetingSummary.set(LOADING_MESSAGE);
        if (Platform.isFxApplicationThread()) {
            task.run();
        } else {
            Platform.runLater(task);
        }
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

    /**
     * Returns the raw payload property.
     * @return raw payload property
     */
    public StringProperty rawPayloadProperty() {
        return rawPayload;
    }
}
