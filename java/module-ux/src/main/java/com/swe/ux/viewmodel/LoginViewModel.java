package com.swe.ux.viewmodel;

import com.swe.ux.model.User;
import com.swe.ux.service.AuthService;
import com.swe.ux.binding.BindableProperty;
import javax.swing.SwingUtilities;

/**
 * ViewModel for the login screen.
 */
public class LoginViewModel extends BaseViewModel {
    // Bindable properties for the view
    public final BindableProperty<String> username = new BindableProperty<>("", "username");
    public final BindableProperty<String> password = new BindableProperty<>("", "password");
    public final BindableProperty<Boolean> isLoading = new BindableProperty<>(false, "isLoading");
    public final BindableProperty<String> errorMessage = new BindableProperty<>("", "errorMessage");
    public final BindableProperty<Boolean> loginSuccess = new BindableProperty<>(false, "loginSuccess");
    public final BindableProperty<Boolean> showRegisterRequested = new BindableProperty<>(false, "showRegisterRequested");

    private final AuthService authService;

    /**
     * Creates a new LoginViewModel.
     * @param authService The authentication service to use
     */
    public LoginViewModel(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Attempts to log in with the current username and password.
     */
    public void login() {
        // Validate input
        if (username.get().trim().isEmpty()) {
            errorMessage.set("Username is required");
            return;
        }

        if (password.get().isEmpty()) {
            errorMessage.set("Password is required");
            return;
        }

        // Clear previous errors
        errorMessage.set("");
        isLoading.set(true);

        // Perform login in background thread
        new Thread(() -> {
            try {
                // Simulate network delay
                Thread.sleep(1000);
                
                // Call the auth service
                User user = authService.authenticate(username.get(), password.get());
                
                // Update UI on the EDT
                SwingUtilities.invokeLater(() -> {
                    isLoading.set(false);
                    loginSuccess.set(true);
                });
                
            } catch (AuthService.AuthenticationException e) {
                SwingUtilities.invokeLater(() -> {
                    errorMessage.set("Login failed: " + e.getMessage());
                    isLoading.set(false);
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    errorMessage.set("An unexpected error occurred");
                    isLoading.set(false);
                });
            }
        }).start();
    }

    /**
     * Resets the login form.
     */
    public void reset() {
        username.set("");
        password.set("");
        errorMessage.set("");
        isLoading.set(false);
        loginSuccess.set(false);
    }
}
