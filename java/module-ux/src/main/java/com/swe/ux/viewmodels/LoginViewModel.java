package com.swe.ux.viewmodels;

import com.swe.controller.Meeting.UserProfile;
import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.controller.serialize.DataSerializer;
import com.swe.ux.binding.BindableProperty;

/**
 * ViewModel for the login screen.
 */
public class LoginViewModel extends BaseViewModel {
    /** Loading state property. */
    public final BindableProperty<Boolean> isLoading = new BindableProperty<>(false, "isLoading");
    /** Authentication error message property. */
    public final BindableProperty<String> authErrorMessage = new BindableProperty<>("", "authErrorMessage");
    /** Current user property. */
    public final BindableProperty<UserProfile> currentUser = new BindableProperty<>(null, "currentUser");

    /** RPC instance. */
    private final AbstractRPC rpc;

    /**
     * Creates a new LoginViewModel.
     * @param rpcParam The RPC instance for communication
     */
    public LoginViewModel(final AbstractRPC rpcParam) {
        this.rpc = rpcParam;
    }

    /**
     * Initiates Google login flow.
     */
    public void loginWithGoogle() {
        System.out.println("Login with Google");
        authErrorMessage.set("");
        isLoading.set(true);

        try {
            final byte[] data = rpc.call("core/register", new byte[0]).get();
            final UserProfile user = DataSerializer.deserialize(data, UserProfile.class);
            currentUser.set(user);
        } catch (final Exception e) {
            authErrorMessage.set("Authentication failed: " + e.getMessage());
            isLoading.set(false);
        }
    }

    /**
     * Resets the login state.
     */
    public void reset() {
        authErrorMessage.set("");
        isLoading.set(false);
        currentUser.set(null);
    }

    /**
     * Gets the loading state property.
     * @return The loading state property
     */
    public BindableProperty<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Gets the authentication error message property.
     * @return The authentication error message property
     */
    public BindableProperty<String> getAuthErrorMessage() {
        return authErrorMessage;
    }

    /**
     * Gets the current user property.
     * @return The current user property
     */
    public BindableProperty<UserProfile> getCurrentUser() {
        return currentUser;
    }
}
