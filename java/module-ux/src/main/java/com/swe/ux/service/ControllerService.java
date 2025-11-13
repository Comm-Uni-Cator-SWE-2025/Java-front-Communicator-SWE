//package com.swe.ux.service;
//
//import java.io.IOException;
//import java.security.GeneralSecurityException;
//
//import com.swe.controller.Auth.GoogleAuthService;
//import com.swe.controller.MeetingServices;
//import com.swe.controller.Meeting.UserProfile;
//
///**
// * Singleton service that manages the controller module integration.
// * Uses the new controller logic from module-ux/controller that allows any Google account.
// *
// * NOTE: This service uses GoogleAuthService which allows anyone with a Google account to login.
// * The old restricted AuthService from module-controller is no longer used.
// */
//public class ControllerService {
//    private static ControllerService instance;
//
//    private final GoogleAuthService googleAuthService;
//    private final MeetingServices meetingServices;
//    private UserProfile currentUser;
//
//    private ControllerService() {
//        // Use new controller logic that allows any Google account
//        this.googleAuthService = new GoogleAuthService();
//        this.meetingServices = new MeetingServices();
//        this.currentUser = null;
//    }
//
//    public static ControllerService getInstance() {
//        if (instance == null) {
//            instance = new ControllerService();
//        }
//        return instance;
//    }
//
//    /**
//     * Authenticates a user using Google OAuth2.
//     * Allows any Google account - no domain restrictions.
//     *
//     * @param forceAccountSelection If true, clears stored tokens to allow selecting a different account
//     * @return UserProfile if authentication succeeds, null otherwise
//     * @throws GeneralSecurityException if authentication fails
//     * @throws IOException if authentication fails
//     */
//    public UserProfile loginWithGoogle(boolean forceAccountSelection) throws GeneralSecurityException, IOException {
//        UserProfile user = googleAuthService.authenticateWithGoogle(forceAccountSelection);
//        if (user != null) {
//            this.currentUser = user;
//        }
//        return user;
//    }
//
//    /**
//     * Authenticates a user using Google OAuth2.
//     * Allows any Google account - no domain restrictions.
//     *
//     * @return UserProfile if authentication succeeds, null otherwise
//     * @throws GeneralSecurityException if authentication fails
//     * @throws IOException if authentication fails
//     */
//    public UserProfile loginWithGoogle() throws GeneralSecurityException, IOException {
//        return loginWithGoogle(false);
//    }
//
//    // Session management
//    public UserProfile getCurrentUser() {
//        return currentUser;
//    }
//
//    public void setCurrentUser(UserProfile user) {
//        this.currentUser = user;
//    }
//
//    public void logout() {
//        this.currentUser = null;
//        // Clear stored Google OAuth tokens
//        try {
//            com.swe.controller.Auth.GoogleAuthServices googleAuthServices = new com.swe.controller.Auth.GoogleAuthServices();
//            googleAuthServices.clearStoredTokens();
//        } catch (IOException e) {
//            // Log but don't fail logout if token clearing fails
//            System.err.println("Warning: Failed to clear Google OAuth tokens: " + e.getMessage());
//        }
//    }
//
//    public boolean isLoggedIn() {
//        return currentUser != null;
//    }
//
//    // Direct access to services (if needed)
//    public GoogleAuthService getGoogleAuthService() {
//        return googleAuthService;
//    }
//
//    public MeetingServices getMeetingServices() {
//        return meetingServices;
//    }
//}
//
