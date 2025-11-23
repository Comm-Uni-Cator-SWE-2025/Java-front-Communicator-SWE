package com.swe.ux.viewmodels;

import com.swe.controller.Meeting.MeetingSession;
import com.swe.controller.Meeting.SessionMode;
import com.swe.controller.Meeting.UserProfile;
import com.swe.app.RPCinterface.AbstractRPC;
import com.swe.controller.serialize.DataSerializer;
import com.swe.ux.binding.BindableProperty;

/**
 * ViewModel for the main application screen.
 */
public class MainViewModel extends BaseViewModel {
    AbstractRPC rpc;
    
    // Bindable properties
    public final BindableProperty<UserProfile> currentUser = new BindableProperty<>(null, "currentUser");
    public final BindableProperty<Boolean> logoutRequested = new BindableProperty<>(false, "logoutRequested");
    public final BindableProperty<Boolean> startMeetingRequested = new BindableProperty<>(false, "startMeetingRequested");
    public final BindableProperty<Boolean> joinMeetingRequested = new BindableProperty<>(false, "joinMeetingRequested");
    public final BindableProperty<String> meetingCode = new BindableProperty<>("", "meetingCode");
    
    /**
     * Creates a new MainViewModel.
     */
    public MainViewModel(AbstractRPC rpc) {
        this.rpc = rpc;
    }
    
    /**
     * Sets the current user and updates the UI accordingly.
     * @param user The current user, or null if logged out
     */
    public void setCurrentUser(UserProfile user) {
        this.currentUser.set(user);
    }

    public String startMeeting() {
        try {
            byte[] response = rpc.call("core/createMeeting", new byte[0]).get();
            if (response != null && response.length > 0) {
                MeetingSession meetingSession = DataSerializer.deserialize(response, MeetingSession.class);
                if (meetingSession != null && meetingSession.getMeetingId() != null) {
                    meetingCode.set(meetingSession.getMeetingId());
                    return meetingSession.getMeetingId();
                }
            }
            System.err.println("MainViewModel: createMeeting RPC returned empty payload, falling back to local session");
        } catch (Exception e) {
            System.err.println("MainViewModel: createMeeting RPC failed - " + e.getMessage());
        }

        // Fall back to a local meeting so the instructor can still start the session
        MeetingSession localSession = createLocalMeetingSession();
        meetingCode.set(localSession.getMeetingId());
        System.out.println("MainViewModel: Using locally generated meeting ID " + localSession.getMeetingId());
        return localSession.getMeetingId();
    }

    public void joinMeeting(String meetingCode_) {
        joinMeetingRequested.set(true);
        try {
            byte[] _response = rpc.call("core/joinMeeting", DataSerializer.serialize(meetingCode_)).get();
        } catch (Exception e) {
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
        UserProfile host = currentUser.get();
        String createdBy = host != null && host.getEmail() != null ? host.getEmail() : "demo-user@example.com";
        MeetingSession session = new MeetingSession(createdBy, SessionMode.CLASS);
        if (host != null) {
            session.addParticipant(host);
        }
        return session;
    }
}
