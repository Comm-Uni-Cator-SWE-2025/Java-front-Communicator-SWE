package com.swe.ux.viewmodel;

import com.swe.controller.Meeting.ParticipantRole;
import com.swe.controller.Meeting.UserProfile;
import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.controller.serialize.DataSerializer;
import com.swe.ux.binding.BindableProperty;

/**
 * ViewModel for the bypass authentication screen.
 */
public class BypassAuthViewModel extends BaseViewModel {
    public final BindableProperty<String> email = new BindableProperty<>("", "email");
    public final BindableProperty<String> displayName = new BindableProperty<>("", "displayName");
    public final BindableProperty<Boolean> isLoading = new BindableProperty<>(false, "isLoading");
    public final BindableProperty<String> errorMessage = new BindableProperty<>("", "errorMessage");
    public final BindableProperty<UserProfile> currentUser = new BindableProperty<>(null, "currentUser");

    private final AbstractRPC rpc;

    public BypassAuthViewModel(AbstractRPC rpc) {
        this.rpc = rpc;
    }

    public void submitBypassAuth() {
        String emailValue = email.get();
        String displayNameValue = displayName.get();

        // Validate inputs
        if (emailValue == null || emailValue.trim().isEmpty()) {
            errorMessage.set("Email is required");
            return;
        }
        if (displayNameValue == null || displayNameValue.trim().isEmpty()) {
            errorMessage.set("Display name is required");
            return;
        }

        errorMessage.set("");
        isLoading.set(true);

        try {
            // Create UserProfile with entered values
            UserProfile userProfile = new UserProfile(
                emailValue.trim(),
                displayNameValue.trim(),
                ParticipantRole.STUDENT
            );

            // Serialize and call RPC
            byte[] serializedProfile = DataSerializer.serialize(userProfile);
            byte[] response = rpc.call("core/FakeUser", serializedProfile).get();

            // Deserialize response (assuming it returns a UserProfile)
            UserProfile returnedProfile = DataSerializer.deserialize(response, UserProfile.class);
            currentUser.set(returnedProfile);
            isLoading.set(false);
        } catch (Exception e) {
            errorMessage.set("Authentication failed: " + e.getMessage());
            isLoading.set(false);
        }
    }

    public void reset() {
        email.set("");
        displayName.set("");
        errorMessage.set("");
        isLoading.set(false);
        currentUser.set(null);
    }
}

