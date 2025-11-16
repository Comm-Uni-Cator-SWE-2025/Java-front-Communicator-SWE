package com.swe.ux.viewmodel;

import com.swe.controller.Meeting.UserProfile;
import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.controller.serialize.DataSerializer;
import com.swe.ux.binding.BindableProperty;

/**
 * ViewModel for the login screen.
 */
public class LoginViewModel extends BaseViewModel {
    // Bindable properties for the view
    public final BindableProperty<Boolean> isLoading = new BindableProperty<>(false, "isLoading");
    public final BindableProperty<String> authErrorMessage = new BindableProperty<>("", "authErrorMessage");
    public final BindableProperty<UserProfile> currentUser = new BindableProperty<>(null, "currentUser");

    private final AbstractRPC rpc;
    /**
     * Creates a new LoginViewModel.
     * @param rpc The RPC service to use
     */
    public LoginViewModel(AbstractRPC rpc) {
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
