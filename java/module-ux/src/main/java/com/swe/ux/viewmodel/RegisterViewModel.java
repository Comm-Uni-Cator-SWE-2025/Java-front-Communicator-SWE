package com.swe.ux.viewmodel;

import com.swe.ux.model.User;
import com.swe.ux.service.AuthService;
import com.swe.ux.binding.BindableProperty;
import javax.swing.SwingUtilities;

/**
 * ViewModel for the registration screen.
 */
public class RegisterViewModel extends BaseViewModel {
    private final AuthService authService;
    
    // Bindable properties
    public final BindableProperty<String> username = new BindableProperty<>("", "username");
    public final BindableProperty<String> email = new BindableProperty<>("", "email");
    public final BindableProperty<String> password = new BindableProperty<>("", "password");
    public final BindableProperty<String> confirmPassword = new BindableProperty<>("", "confirmPassword");
    public final BindableProperty<Boolean> isLoading = new BindableProperty<>(false, "isLoading");
    public final BindableProperty<String> errorMessage = new BindableProperty<>("", "errorMessage");
    public final BindableProperty<Boolean> registrationSuccess = new BindableProperty<>(false, "registrationSuccess");

    public RegisterViewModel(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Attempts to register a new user with the provided credentials.
     */
    public void register() {
        // Validate input
        if (username.get().trim().isEmpty() || 
            password.get().trim().isEmpty() || 
            confirmPassword.get().trim().isEmpty()) {
            errorMessage.set("All fields are required");
            return;
        }

        if (!password.get().equals(confirmPassword.get())) {
            errorMessage.set("Passwords do not match");
            return;
        }

        if (password.get().length() < 6) {
            errorMessage.set("Password must be at least 6 characters");
            return;
        }

        // Clear previous errors
        errorMessage.set("");
        isLoading.set(true);

        // Run registration in background
        new Thread(() -> {
            try {
                // In a real app, we'd make an API call here
                Thread.sleep(1000); // Simulate network delay
                
                // Call the auth service
                User newUser = authService.register(
                    username.get().trim(),
                    password.get(),
                    email.get().trim()
                );
                
                // Notify success
                SwingUtilities.invokeLater(() -> {
                    registrationSuccess.set(true);
                    isLoading.set(false);
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    errorMessage.set(e.getMessage());
                    isLoading.set(false);
                });
            }
        }).start();
    }
}
