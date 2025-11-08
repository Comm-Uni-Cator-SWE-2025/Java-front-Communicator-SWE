package com.swe.controller.Auth;

import java.io.IOException;
import java.security.GeneralSecurityException;

import com.google.api.client.auth.oauth2.Credential;
import com.swe.controller.Auth.AuthHelper.GoogleUserInfo;
import com.swe.controller.Meeting.ParticipantRole;
import com.swe.controller.Meeting.UserProfile;

/**
 * Handles Google authentication and returns UserProfile objects.
 * Allows anyone with a Google account to login - no domain restrictions.
 */
public class GoogleAuthService {

    public GoogleAuthService() {
    }

    /**
     * Authenticates a user using Google OAuth2.
     * Allows any Google account - no domain restrictions.
     *
     * @return UserProfile if authentication succeeds, null otherwise
     */
    public UserProfile authenticateWithGoogle() throws GeneralSecurityException, IOException {
        return authenticateWithGoogle(false);
    }

    /**
     * Authenticates a user using Google OAuth2.
     * Allows any Google account - no domain restrictions.
     * 
     * @param forceAccountSelection If true, clears stored tokens to allow selecting
     *                              a different account
     * @return UserProfile if authentication succeeds, null otherwise
     */
    public UserProfile authenticateWithGoogle(boolean forceAccountSelection)
            throws GeneralSecurityException, IOException {
        final GoogleAuthServices googleAuthService = new GoogleAuthServices();
        final Credential credential = googleAuthService.getCredentials(forceAccountSelection);

        final AuthHelper authHelper = new AuthHelper();
        final GoogleUserInfo userInfo = authHelper.handleGoogleLogin(credential);

        if (userInfo == null || userInfo.getEmail() == null) {
            return null;
        }

        // Determine role based on email domain (but allow any domain)
        // Default to STUDENT for non-institutional emails, but allow all domains
        ParticipantRole role = ParticipantRole.GUEST;
        String email = userInfo.getEmail();

        // Create UserProfile using the new controller logic
        // This allows anyone with a Google account to login
        return new UserProfile(
                userInfo.getEmail(),
                userInfo.getName() != null ? userInfo.getName() : email,
                userInfo.getLogoUrl(),
                role);
    }
}