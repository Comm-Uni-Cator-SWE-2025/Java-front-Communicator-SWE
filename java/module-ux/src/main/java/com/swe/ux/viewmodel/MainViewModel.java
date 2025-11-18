package com.swe.ux.viewmodel;

import com.swe.controller.Meeting.UserProfile;
import com.swe.ux.binding.BindableProperty;

/**
 * ViewModel for the main application screen.
 */
public class MainViewModel extends BaseViewModel {
    
    // Bindable properties
    public final BindableProperty<UserProfile> currentUser = new BindableProperty<>(null, "currentUser");
    public final BindableProperty<Boolean> logoutRequested = new BindableProperty<>(false, "logoutRequested");
    public final BindableProperty<Boolean> startMeetingRequested = new BindableProperty<>(false, "startMeetingRequested");
    public final BindableProperty<Boolean> joinMeetingRequested = new BindableProperty<>(false, "joinMeetingRequested");
    
    /**
     * Creates a new MainViewModel.
     */
    public MainViewModel() {
    }
    
    /**
     * Sets the current user and updates the UI accordingly.
     * @param user The current user, or null if logged out
     */
    public void setCurrentUser(UserProfile user) {
        this.currentUser.set(user);
    }
    
    /**
     * Logs out the current user.
     */
    public void logout() {
        // TODO USE RPC TO LOGOUT
        logoutRequested.set(true);
    }
}
