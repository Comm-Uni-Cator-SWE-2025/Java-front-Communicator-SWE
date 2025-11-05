package com.swe.ux.viewmodel;

import com.swe.ux.model.Meeting;
import com.swe.ux.model.User;
import com.swe.ux.binding.BindableProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for managing meeting-related business logic and state.
 */
public class MeetingViewModel extends BaseViewModel {
    private final User currentUser;
    private Meeting currentMeeting;
    
    // Bindable properties
    public final BindableProperty<String> meetingTitle = new BindableProperty<>("", "meetingTitle");
    public final BindableProperty<String> messageText = new BindableProperty<>("", "messageText");
    public final BindableProperty<List<String>> messages = new BindableProperty<>(new ArrayList<>(), "messages");
    public final BindableProperty<Boolean> isMeetingActive = new BindableProperty<>(false, "isMeetingActive");
    public final BindableProperty<List<User>> participants = new BindableProperty<>(new ArrayList<>(), "participants");

    public MeetingViewModel(User currentUser) {
        this.currentUser = currentUser;
    }

    /**
     * Start a new meeting with the current user as a participant.
     */
    public void startMeeting() {
        currentMeeting = new Meeting(meetingTitle.get());
        currentMeeting.addParticipant(currentUser);
        isMeetingActive.set(true);
        updateParticipants();
        addSystemMessage("Meeting started");
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
    public void addParticipant(User user) {
        if (currentMeeting != null) {
            currentMeeting.addParticipant(user);
            updateParticipants();
            addSystemMessage(user.getUsername() + " joined the meeting");
        }
    }

    /**
     * Remove a participant from the current meeting.
     */
    public void removeParticipant(User user) {
        if (currentMeeting != null) {
            currentMeeting.removeParticipant(user);
            updateParticipants();
            addSystemMessage(user.getUsername() + " left the meeting");
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
            currentMeeting.addMessage(new Meeting.ChatMessage(null, "[System] " + message));
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
