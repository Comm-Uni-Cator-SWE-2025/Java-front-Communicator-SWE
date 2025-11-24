/*
 * -----------------------------------------------------------------------------
 *  File: LoginViewModel.java
 *  Owner: Vaibhav Yadav
 *  Roll Number : 142201015
 *  Module : UX
 *
 * -------------------------------------------------------------------------------------------------------------------------------------------------------
 */
package com.swe.ux.viewmodel;

import com.swe.controller.Meeting.UserProfile;
import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.controller.serialize.DataSerializer;
import com.swe.screenNVideo.Utils;
import com.swe.ux.binding.BindableProperty;

/**
 * ViewModel for the login screen.
 */
public class LoginViewModel extends BaseViewModel {
    public final BindableProperty<Boolean> isLoading = new BindableProperty<>(false, "isLoading");
    public final BindableProperty<String> authErrorMessage = new BindableProperty<>("", "authErrorMessage");
    public final BindableProperty<UserProfile> currentUser = new BindableProperty<>(null, "currentUser");

    private final AbstractRPC rpc;

    public LoginViewModel(AbstractRPC rpc) {
        this.rpc = rpc;
    }

    public void loginWithGoogle() {
        System.out.println("Login with Google");
        authErrorMessage.set("");
        isLoading.set(true);

        try {
            byte[] data = rpc.call("core/register", new byte[0]).get();
            UserProfile user = DataSerializer.deserialize(data, UserProfile.class);
            currentUser.set(user);
        } catch (Exception e) {
            authErrorMessage.set("Authentication failed: " + e.getMessage());
            isLoading.set(false);
        }
    }

    public void reset() {
        authErrorMessage.set("");
        isLoading.set(false);
        currentUser.set(null);
    }
}
