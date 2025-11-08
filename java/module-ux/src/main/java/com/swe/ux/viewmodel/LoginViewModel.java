package com.swe.ux.viewmodel;

import javax.swing.SwingUtilities;

import com.swe.controller.Auth.AuthService;
import com.swe.controller.Meeting.UserProfile;
import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.controller.serialize.DataSerializer;
import com.swe.ux.binding.BindableProperty;

/**
 * ViewModel for the login screen.
 */
public class LoginViewModel extends BaseViewModel {
    // Bindable properties for the view
    // public final BindableProperty<String> username = new BindableProperty<>("", "username");
    // public final BindableProperty<String> password = new BindableProperty<>("", "password");
    public final BindableProperty<Boolean> isLoading = new BindableProperty<>(false, "isLoading");
    public final BindableProperty<String> authErrorMessage = new BindableProperty<>("", "authErrorMessage");
    public final BindableProperty<UserProfile> currentUser = new BindableProperty<>(null, "currentUser");

    private final AuthService authService;
    private final AbstractRPC rpc;
    /**
     * Creates a new LoginViewModel.
     * @param authService The authentication service to use
     * @param rpc The RPC service to use
     */
    public LoginViewModel(AuthService authService, AbstractRPC rpc) {
        this.authService = authService;
        this.rpc = rpc;
    }

    /**
     * Logs in with Google using OAuth2 authentication.
     * Always forces account selection to allow changing email/account.
     */
    public void loginWithGoogle() {
        System.out.println("Login with Google");
        // Clear previous errors
        authErrorMessage.set("");
        isLoading.set(true);

        try {
            System.out.println("Calling core/register" + rpc);
            byte[] data = rpc.call("core/register", new byte[0]).get();
            System.out.println("Data: " + data.length);
            UserProfile user = DataSerializer.deserialize(data, UserProfile.class);
            currentUser.set(user);
        } catch (Exception e) {
            authErrorMessage.set("Authentication failed with an unexpected error: " + e.getMessage());
            isLoading.set(false);
        }

        // Perform Google login in background thread
        // new Thread(() -> {
        //     // try {
        //     //     UserProfile user = authService.authenticate();
        //     //     currentUser.set(user);
        //     // } catch (AuthService.AuthenticationException e) {
        //     //     authErrorMessage.set("Authentication failed: " + e.getMessage());
        //     //     isLoading.set(false);
        //     // } catch (Exception e) {
        //     //     authErrorMessage.set("Authentication failed with an unexpected error: " + e.getMessage());
        //     //     isLoading.set(false);
        //     // }
            
        //     // try {
        //     //     // Check if authService supports Google authentication
        //     //     if (authService instanceof com.swe.ux.service.impl.InMemoryAuthService) {
        //     //         com.swe.ux.service.impl.InMemoryAuthService inMemoryAuthService = 
        //     //             (com.swe.ux.service.impl.InMemoryAuthService) authService;
                    
        //     //         // Perform actual Google OAuth2 authentication
        //     //         // Force account selection to allow changing email
        //     //         inMemoryAuthService.authenticateWithGoogle(true);
                    
        //     //         // Update UI on the EDT
        //     //         SwingUtilities.invokeLater(() -> {
        //     //             isLoading.set(false);
        //     //             loginSuccess.set(true);
        //     //         });
        //     //     } else {
        //     //         // Fallback to dummy implementation if service doesn't support Google auth
        //     //         authService.loginWithGoogle(
        //     //             "user@gmail.com",
        //     //             "Google User"
        //     //         );
                    
        //     //         SwingUtilities.invokeLater(() -> {
        //     //             isLoading.set(false);
        //     //             loginSuccess.set(true);
        //     //         });
        //     //     }
                
        //     // } catch (AuthService.AuthenticationException e) {
        //     //     SwingUtilities.invokeLater(() -> {
        //     //         authErrorMessage.set("Google login failed: " + e.getMessage());
        //     //         isLoading.set(false);
        //     //     });
        //     // } catch (Exception e) {
        //     //     SwingUtilities.invokeLater(() -> {
        //     //         authErrorMessage.set("Google login failed: " + e.getMessage());
        //     //         isLoading.set(false);
        //     //     });
        //     // }
        // }).start();
    }

    /**
     * Resets the login form.
     */
    public void reset() {
        authErrorMessage.set("");
        isLoading.set(false);
        currentUser.set(null);
    }
}
