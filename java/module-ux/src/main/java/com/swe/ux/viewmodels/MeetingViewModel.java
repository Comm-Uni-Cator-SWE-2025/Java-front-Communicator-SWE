package com.swe.ux.viewmodels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.swe.controller.Meeting.UserProfile;
import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.screenNVideo.Utils;
import com.swe.ux.analytics.CanvasShapeMetricsCollector;
import com.swe.ux.analytics.ScreenShareTelemetryCollector;
import com.swe.ux.binding.BindableProperty;
import com.swe.ux.model.Meeting;

/**
 * ViewModel for managing meeting-related business logic and state.
 */
public class MeetingViewModel extends BaseViewModel {
    /** Title segment length constant. */
    private static final int TITLE_SEGMENT_LENGTH = 8;

    /** Current user in the meeting. */
    private final UserProfile currentUser;
    /** Current meeting instance. */
    private Meeting currentMeeting = new Meeting("Meeting");
    /** RPC instance for communication. */
    private final AbstractRPC rpc;

    /** IP to email mapping. */
    private final HashMap<String, String> ipToMail;

    /** Meeting title property. */
    private final BindableProperty<String> meetingTitle = new BindableProperty<>("", "meetingTitle");
    /** Meeting ID property. */
    private final BindableProperty<String> meetingId = new BindableProperty<>("", "meetingId");
    /** Message text property. */
    public final BindableProperty<String> messageText = new BindableProperty<>("", "messageText");
    /** Messages property. */
    public final BindableProperty<List<String>> messages = new BindableProperty<>(new ArrayList<>(), "messages");
    /** Meeting active state property. */
    public final BindableProperty<Boolean> isMeetingActive = new BindableProperty<>(false, "isMeetingActive");
    /** Video enabled property. */
    public final BindableProperty<Boolean> isVideoEnabled = new BindableProperty<>(false, "isVideoEnabled");
    /** Audio enabled property. */
    public final BindableProperty<Boolean> isAudioEnabled = new BindableProperty<>(false, "isAudioEnabled");
    /** Screen share enabled property. */
    public final BindableProperty<Boolean> isScreenShareEnabled = new BindableProperty<>(false, "isScreenShareEnabled");
    /** Participants property. */
    public final BindableProperty<List<UserProfile>> participants = new BindableProperty<>(new ArrayList<>(),
            "participants");
    /** Role property. */
    private final BindableProperty<String> role = new BindableProperty<>("", "role");

    /**
     * Creates a new MeetingViewModel.
     * 
     * @param currentUserParam The current user
     * @param rpcParam         The RPC instance
     */
    public MeetingViewModel(final UserProfile currentUserParam, final AbstractRPC rpcParam) {
        System.out.println("User  " + currentUserParam);
        this.currentUser = currentUserParam;
        this.rpc = rpcParam;
        this.ipToMail = new HashMap<>();
    }

    /**
     * Creates a new MeetingViewModel with role.
     * 
     * @param currentUserParam The current user
     * @param roleParam        The user's role
     * @param rpcParam         The RPC instance
     */
    public MeetingViewModel(final UserProfile currentUserParam, final String roleParam, final AbstractRPC rpcParam) {
        this.currentUser = currentUserParam;
        this.rpc = rpcParam;
        this.role.set(roleParam);
        this.ipToMail = new HashMap<>();
    }

    /**
     * Sets the meeting ID for this meeting.
     * This should be called before startMeeting() to use an existing meeting ID.
     * 
     * @param id The meeting ID to set
     */
    public void setMeetingId(final String id) {
        if (id != null && !id.trim().isEmpty()) {
            meetingId.set(id);
        }
    }

    /**
     * Clear all participants from the current meeting and notify observers.
     */
    public void clearParticipants() {
        applyParticipantSnapshot(Collections.emptyList());
    }

    /**
     * Replace the current participant list with a server-provided snapshot.
     * @param snapshot the ordered list of participants from the backend
     */
    public void applyParticipantSnapshot(final List<UserProfile> snapshot) {
        final List<UserProfile> safeSnapshot = snapshot == null
                ? new ArrayList<>()
                : new ArrayList<>(snapshot);

        if (currentMeeting != null) {
            currentMeeting.setParticipants(safeSnapshot);
        }

        participants.set(safeSnapshot);
    }

    /**
     * Start a new meeting with the current user as a participant.
     * The meeting ID must be set via setMeetingId() before calling this method.
     * The meeting ID comes from either RPC (when creating) or user input (when
     * joining).
     */
    public void startMeeting() {
        // Get the meeting ID that was set via setMeetingId()
        final String newMeetingId = meetingId.get();

        // Meeting ID must be provided before starting the meeting
        if (newMeetingId == null || newMeetingId.trim().isEmpty()) {
            System.err.println("Error: Meeting ID must be set before starting a meeting");
            return;
        }

        // Create the meeting with the provided ID
        String title = meetingTitle.get();
        if (title == null || title.trim().isEmpty()) {
            final String displaySegment;
            if (newMeetingId.length() > TITLE_SEGMENT_LENGTH) {
                displaySegment = newMeetingId.substring(0, TITLE_SEGMENT_LENGTH);
            } else {
                displaySegment = newMeetingId;
            }
            title = "Meeting " + displaySegment;
        }
        currentMeeting.setMeetingTitle(title);
        currentMeeting.addParticipant(currentUser);

        ipToMail.put(currentUser.getEmail(), Utils.getSelfIP());

        isMeetingActive.set(true);
        updateParticipants();
        addSystemMessage("Meeting started with ID: " + newMeetingId);
    }

    /**
     * End the current meeting.
     */
    public void endMeeting() {
        if (currentMeeting != null) {
            // Call RPC to end the meeting
            if (rpc != null) {
                rpc.call("core/endMeeting", new byte[0]);
            }

            addSystemMessage("Meeting ended");
            currentMeeting.endMeeting();
            isMeetingActive.set(false);
            currentMeeting = null;
            // Clear meeting ID when meeting ends
            meetingId.set("");
            CanvasShapeMetricsCollector.getInstance().reset();
            ScreenShareTelemetryCollector.getInstance().resetSession();
        }
    }

    /**
     * Send a message to the current meeting.
     */
    public void sendMessage() {
        if (currentMeeting != null && messageText.get() != null && !messageText.get().trim().isEmpty()) {
            final String message = messageText.get().trim();
            currentMeeting.addMessage(new Meeting.ChatMessage(currentUser, message));
            updateMessages();
            messageText.set("");
        }
    }

    /**
     * Captures a quick doubt entry and surfaces it to the meeting chat log so
     * analytics panels can pick it up.
     * 
     * @param quickDoubt the quick doubt text entered by the participant
     */
    public void submitQuickDoubt(final String quickDoubt) {
        if (currentMeeting == null || quickDoubt == null) {
            return;
        }
        final String trimmed = quickDoubt.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        final String formatted = "[Quick Doubt] " + trimmed;
        currentMeeting.addMessage(new Meeting.ChatMessage(currentUser, formatted));
        updateMessages();
    }

    /**
     * Add a participant to the current meeting.
     * 
     * @param user The user to add
     */
    public void addParticipant(final UserProfile user) {
        if (currentMeeting != null) {
            currentMeeting.addParticipant(user);
            updateParticipants();
            addSystemMessage(user.getDisplayName() + " joined the meeting");
        } else {
            System.out.println("MeetingViewModel: currentMeeting is null, saving user to list");
            // Create a new list to trigger property change notification
            final List<UserProfile> updatedParticipants = new ArrayList<>(participants.get());
            updatedParticipants.add(user);
            participants.set(updatedParticipants);
        }
    }

    /**
     * Remove a participant from the current meeting.
     * 
     * @param user The user to remove
     */
    public void removeParticipant(final UserProfile user) {
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
            final boolean newState = !isVideoEnabled.get();
            isVideoEnabled.set(newState);
            if (newState) {
                rpc.call(Utils.START_VIDEO_CAPTURE, new byte[0]);
            } else {
                rpc.call(Utils.STOP_VIDEO_CAPTURE, new byte[0]);
            }
            currentMeeting.setVideoEnabled(newState);
            ScreenShareTelemetryCollector.getInstance().setCameraActive(newState);
            if (newState) {
                addSystemMessage("Video enabled");
            } else {
                addSystemMessage("Video disabled");
            }
        }
    }

    /**
     * Toggle audio state for the current meeting.
     */
    public void toggleAudio() {
        if (currentMeeting != null) {
            final boolean newState = !isAudioEnabled.get();
            isAudioEnabled.set(newState);
            if (newState) {
                rpc.call(Utils.START_AUDIO_CAPTURE, new byte[0]);
            } else {
                rpc.call(Utils.STOP_AUDIO_CAPTURE, new byte[0]);
            }
            currentMeeting.setAudioEnabled(newState);
            if (newState) {
                addSystemMessage("Audio enabled");
            } else {
                addSystemMessage("Audio disabled");
            }
        }
    }

    /**
     * Toggle screen sharing state for the current meeting.
     */
    public void toggleScreenSharing() {
        if (currentMeeting != null) {
            final boolean newState = !isScreenShareEnabled.get();
            isScreenShareEnabled.set(newState);
            if (newState) {
                rpc.call(Utils.START_SCREEN_CAPTURE, new byte[0]);
            } else {
                rpc.call(Utils.STOP_SCREEN_CAPTURE, new byte[0]);
            }
            currentMeeting.setScreenSharingEnabled(newState);
            ScreenShareTelemetryCollector.getInstance().setScreenActive(newState);
            if (newState) {
                addSystemMessage("Screen sharing enabled");
            } else {
                addSystemMessage("Screen sharing disabled");
            }
        }
    }

    private void updateMessages() {
        if (currentMeeting != null) {
            final List<String> messageList = new ArrayList<>();
            for (final Meeting.ChatMessage msg : currentMeeting.getMessages()) {
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

    private void addSystemMessage(final String message) {
        if (currentMeeting != null) {
            // currentMeeting.addMessage(new Meeting.ChatMessage(null, "[System] " +
            // message));
            updateMessages();
        }
    }

    /**
     * Gets the current meeting.
     * 
     * @return The current meeting
     */
    public Meeting getCurrentMeeting() {
        return currentMeeting;
    }

    /**
     * Checks if the current user is in the meeting.
     * 
     * @return True if user is in meeting
     */
    public boolean isCurrentUserInMeeting() {
        return currentMeeting != null
                && currentMeeting.getParticipants().contains(currentUser);
    }

    /**
     * Gets the current user.
     * 
     * @return The current user
     */
    public UserProfile getCurrentUser() {
        return currentUser;
    }

    /**
     * Gets the RPC instance.
     * 
     * @return The RPC instance
     */
    public AbstractRPC getRpc() {
        return rpc;
    }

    /**
     * Gets the IP to email mapping.
     * 
     * @return The IP to email mapping
     */
    public HashMap<String, String> getIpToMail() {
        return ipToMail;
    }

    /**
     * Gets the meeting title property.
     * 
     * @return The meeting title property
     */
    public BindableProperty<String> getMeetingTitle() {
        return meetingTitle;
    }

    /**
     * Gets the meeting ID property.
     * 
     * @return The meeting ID property
     */
    public BindableProperty<String> getMeetingId() {
        return meetingId;
    }

    /**
     * Gets the message text property.
     * 
     * @return The message text property
     */
    public BindableProperty<String> getMessageText() {
        return messageText;
    }

    /**
     * Gets the messages property.
     * 
     * @return The messages property
     */
    public BindableProperty<List<String>> getMessages() {
        return messages;
    }

    /**
     * Gets the meeting active state property.
     * 
     * @return The meeting active state property
     */
    public BindableProperty<Boolean> getIsMeetingActive() {
        return isMeetingActive;
    }

    /**
     * Gets the video enabled property.
     * 
     * @return The video enabled property
     */
    public BindableProperty<Boolean> getIsVideoEnabled() {
        return isVideoEnabled;
    }

    /**
     * Gets the audio enabled property.
     * 
     * @return The audio enabled property
     */
    public BindableProperty<Boolean> getIsAudioEnabled() {
        return isAudioEnabled;
    }

    /**
     * Gets the screen share enabled property.
     * 
     * @return The screen share enabled property
     */
    public BindableProperty<Boolean> getIsScreenShareEnabled() {
        return isScreenShareEnabled;
    }

    /**
     * Gets the participants property.
     * 
     * @return The participants property
     */
    public BindableProperty<List<UserProfile>> getParticipants() {
        return participants;
    }

    /**
     * Gets the role property.
     * 
     * @return The role property
     */
    public BindableProperty<String> getRole() {
        return role;
    }
}
