package com.swe.ux.viewmodel;

import java.util.ArrayList;
import java.util.List;

import com.swe.controller.Meeting.UserProfile;
import com.swe.screenNVideo.AbstractRPC;
import com.swe.screenNVideo.DummyRPC;
import com.swe.screenNVideo.Utils;
import com.swe.ux.binding.BindableProperty;
import com.swe.ux.model.Meeting;

/**
 * ViewModel for managing meeting-related business logic and state.
 */
public class MeetingViewModel extends BaseViewModel {
    private final UserProfile currentUser;
    private Meeting currentMeeting;
    private AbstractRPC rpc;
    
    // Bindable properties
    public final BindableProperty<String> meetingTitle = new BindableProperty<>("", "meetingTitle");
    public final BindableProperty<String> meetingId = new BindableProperty<>("", "meetingId");
    public final BindableProperty<String> messageText = new BindableProperty<>("", "messageText");
    public final BindableProperty<List<String>> messages = new BindableProperty<>(new ArrayList<>(), "messages");
    public final BindableProperty<Boolean> isMeetingActive = new BindableProperty<>(false, "isMeetingActive");
    public final BindableProperty<Boolean> isVideoEnabled = new BindableProperty<>(false, "isVideoEnabled");
    public final BindableProperty<Boolean> isScreenShareEnabled = new BindableProperty<>(false, "isScreenShareEnabled");
    public final BindableProperty<List<UserProfile>> participants = new BindableProperty<>(new ArrayList<>(), "participants");
    public final BindableProperty<String> role = new BindableProperty<>("", "role");

    public MeetingViewModel(UserProfile currentUser) {
        System.out.println("User  " + currentUser);
        this.currentUser = currentUser;
        this.rpc = DummyRPC.getInstance();
    }
    
    public MeetingViewModel(UserProfile currentUser, String role) {
        this.currentUser = currentUser;
        this.rpc = DummyRPC.getInstance();
        this.role.set(role);
    }

    /**
     * Start a new meeting with the current user as a participant.
     * Creates a new meeting with a unique meeting ID and sets role as Instructor.
     */
    public void startMeeting() {
        // Generate a new unique meeting ID
        String newMeetingId = java.util.UUID.randomUUID().toString();
        meetingId.set(newMeetingId);
        
        // Ensure role is set to Instructor when starting a meeting
        if (!"Instructor".equals(role.get())) {
            role.set("Instructor");
        }
        
        // Create the meeting with the generated ID
        String title = meetingTitle.get();
        if (title == null || title.trim().isEmpty()) {
            title = "Meeting " + newMeetingId.substring(0, 8);
        }
        currentMeeting = new Meeting(title);
        currentMeeting.addParticipant(currentUser);
        
        isMeetingActive.set(true);
        updateParticipants();
        addSystemMessage("Meeting started with ID: " + newMeetingId);
    }

    /**
     * End the current meeting.
     */
    public void endMeeting() {
        if (currentMeeting != null) {
            addSystemMessage("Meeting ended");
            currentMeeting.endMeeting();
            isMeetingActive.set(false);
            currentMeeting = null;
            // Clear meeting ID when meeting ends
            meetingId.set("");
        }
    }

    /**
     * Send a message to the current meeting.
     */
    public void sendMessage() {
        if (currentMeeting != null && messageText.get() != null && !messageText.get().trim().isEmpty()) {
            String message = messageText.get().trim();
            currentMeeting.addMessage(new Meeting.ChatMessage(currentUser, message));
            updateMessages();
            messageText.set("");
        }
    }

    /**
     * Add a participant to the current meeting.
     */
    public void addParticipant(UserProfile user) {
        if (currentMeeting != null) {
            currentMeeting.addParticipant(user);
            updateParticipants();
            addSystemMessage(user.getDisplayName() + " joined the meeting");
        }
    }

    /**
     * Remove a participant from the current meeting.
     */
    public void removeParticipant(UserProfile user) {
        if (currentMeeting != null) {
            currentMeeting.removeParticipant(user);
            updateParticipants();
            addSystemMessage(user.getDisplayName() + " left the meeting");
        }
    }

    /**
     * Toggle video state for the current meeting.
     */
    public void toggleVideo() {
        if (currentMeeting != null) {
            boolean newState = !isVideoEnabled.get();
            isVideoEnabled.set(newState);
            if (newState) {
                rpc.call(Utils.START_VIDEO_CAPTURE, new byte[0]);
            } else {
                rpc.call(Utils.STOP_VIDEO_CAPTURE, new byte[0]);
            }
            currentMeeting.setVideoEnabled(newState);
            addSystemMessage("Video " + (newState ? "enabled" : "disabled"));
        }
    }

    /**
     * Toggle screen sharing state for the current meeting.
     */
    public void toggleScreenSharing() {
        if (currentMeeting != null) {
            boolean newState = !isScreenShareEnabled.get();
            isScreenShareEnabled.set(newState);
            if (newState) {
                rpc.call(Utils.START_SCREEN_CAPTURE, new byte[0]);
            } else {
                rpc.call(Utils.STOP_SCREEN_CAPTURE, new byte[0]);
            }
            currentMeeting.setScreenSharingEnabled(newState);
            addSystemMessage("Screen sharing " + (newState ? "enabled" : "disabled"));
        }
    }

    private void updateMessages() {
        if (currentMeeting != null) {
            List<String> messageList = new ArrayList<>();
            for (Meeting.ChatMessage msg : currentMeeting.getMessages()) {
                messageList.add(msg.toString());
            }
            messages.set(messageList);
        }
    }

    private void updateParticipants() {
        if (currentMeeting != null) {
            participants.set(new ArrayList<>(currentMeeting.getParticipants()));
        }
    }

    private void addSystemMessage(String message) {
        if (currentMeeting != null) {
//            currentMeeting.addMessage(new Meeting.ChatMessage(null, "[System] " + message));
            updateMessages();
        }
    }

    // Getters
    public Meeting getCurrentMeeting() {
        return currentMeeting;
    }

    public boolean isCurrentUserInMeeting() {
        return currentMeeting != null && 
               currentMeeting.getParticipants().contains(currentUser);
    }
}
