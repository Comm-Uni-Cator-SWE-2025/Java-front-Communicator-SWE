package com.swe.ux.viewmodel;

import com.swe.ux.model.User;
import com.swe.ux.service.AuthService;
import com.swe.ux.binding.BindableProperty;

/**
 * ViewModel for the main application screen.
 */
public class MainViewModel extends BaseViewModel {
    private final AuthService authService;
    
    // Bindable properties
    public final BindableProperty<User> currentUser = new BindableProperty<>(null, "currentUser");
    public final BindableProperty<Boolean> logoutRequested = new BindableProperty<>(false, "logoutRequested");
    public final BindableProperty<Boolean> startMeetingRequested = new BindableProperty<>(false, "startMeetingRequested");
    
    /**
     * Creates a new MainViewModel.
     * @param authService The authentication service
     */
    public MainViewModel(AuthService authService) {
        this.authService = authService;
    }
    
    /**
     * Sets the current user and updates the UI accordingly.
     * @param user The current user, or null if logged out
     */
    public void setCurrentUser(User user) {
        this.currentUser.set(user);
    }
    
    /**
     * Logs out the current user.
     */
    public void logout() {
        authService.logout();
        logoutRequested.set(true);
    }
}
