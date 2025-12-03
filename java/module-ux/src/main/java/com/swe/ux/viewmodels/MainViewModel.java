package com.swe.ux.viewmodels;

import com.swe.controller.Meeting.MeetingSession;
import com.swe.controller.Meeting.SessionMode;
import com.swe.controller.Meeting.UserProfile;
import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.controller.serialize.DataSerializer;
import com.swe.ux.binding.BindableProperty;

/**
 * ViewModel for the main application screen.
 */
public class MainViewModel extends BaseViewModel {
    /** RPC instance. */
    private final AbstractRPC rpc;
    
    /** Current user property. */
    public final BindableProperty<UserProfile> currentUser = new BindableProperty<>(null, "currentUser");
    /** Logout requested property. */
    public final BindableProperty<Boolean> logoutRequested = new BindableProperty<>(false, "logoutRequested");
    /** Start meeting requested property. */
    public final BindableProperty<Boolean> startMeetingRequested = 
        new BindableProperty<>(false, "startMeetingRequested");
    /** Join meeting requested property. */
    public final BindableProperty<Boolean> joinMeetingRequested = 
        new BindableProperty<>(false, "joinMeetingRequested");
    /** Meeting code property. */
    public final BindableProperty<String> meetingCode = new BindableProperty<>("", "meetingCode");
    
    /**
     * Creates a new MainViewModel.
     * @param rpcParam The RPC instance for communication
     */
    public MainViewModel(final AbstractRPC rpcParam) {
        this.rpc = rpcParam;
    }
    
    /**
     * Sets the current user and updates the UI accordingly.
     * @param user The current user, or null if logged out
     */
    public void setCurrentUser(final UserProfile user) {
        this.currentUser.set(user);
    }

    /**
     * Starts a new meeting.
     * @return The meeting ID, or null if failed
     */
    public String startMeeting() {
        try {
            final byte[] response = rpc.call("core/createMeeting", new byte[0]).get();
            if (response != null && response.length > 0) {
                final MeetingSession meetingSession = DataSerializer.deserialize(response, MeetingSession.class);
                if (meetingSession != null && meetingSession.getMeetingId() != null) {
                    meetingCode.set(meetingSession.getMeetingId());
                    return meetingSession.getMeetingId();
                }
            }
            System.err.println(
                "MainViewModel: createMeeting RPC returned empty payload, falling back to local session");
        } catch (final Exception e) {
            System.err.println("MainViewModel: createMeeting RPC failed - " + e.getMessage());
        }

        // Fall back to a local meeting so the instructor can still start the session
        final MeetingSession localSession = createLocalMeetingSession();
        meetingCode.set(localSession.getMeetingId());
        System.out.println("MainViewModel: Using locally generated meeting ID " + localSession.getMeetingId());
        return localSession.getMeetingId();
    }

    /**
     * Joins an existing meeting.
     * @param meetingCodeParam The meeting code to join
     */
    public void joinMeeting(final String meetingCodeParam) {
        joinMeetingRequested.set(true);
        try {
            rpc.call("core/joinMeeting", DataSerializer.serialize(meetingCodeParam))
                    .exceptionally(ex -> {
                        ex.printStackTrace();
                        return new byte[0];
                    });
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Logs out the current user.
     */
    public void logout() {
        // TODO USE RPC TO LOGOUT
        logoutRequested.set(true);
    }

    private MeetingSession createLocalMeetingSession() {
        final UserProfile host = currentUser.get();
        final String createdBy;
        if (host != null && host.getEmail() != null) {
            createdBy = host.getEmail();
        } else {
            createdBy = "demo-user@example.com";
        }
        final MeetingSession session = new MeetingSession(createdBy, SessionMode.CLASS);
        if (host != null) {
            session.addParticipant(host);
        }
        return session;
    }

    /**
     * Gets the current user property.
     * @return The current user property
     */
    public BindableProperty<UserProfile> getCurrentUser() {
        return currentUser;
    }

    /**
     * Gets the logout requested property.
     * @return The logout requested property
     */
    public BindableProperty<Boolean> getLogoutRequested() {
        return logoutRequested;
    }

    /**
     * Gets the start meeting requested property.
     * @return The start meeting requested property
     */
    public BindableProperty<Boolean> getStartMeetingRequested() {
        return startMeetingRequested;
    }

    /**
     * Gets the join meeting requested property.
     * @return The join meeting requested property
     */
    public BindableProperty<Boolean> getJoinMeetingRequested() {
        return joinMeetingRequested;
    }

    /**
     * Gets the meeting code property.
     * @return The meeting code property
     */
    public BindableProperty<String> getMeetingCode() {
        return meetingCode;
    }
}
