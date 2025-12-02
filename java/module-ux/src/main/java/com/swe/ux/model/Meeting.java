package com.swe.ux.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import com.swe.controller.Meeting.UserProfile;

/**
 * Represents a meeting with participants and messages.
 */
public class Meeting {
    /**
     * Unique meeting ID.
     */
    private final String id;
    /**
     * Meeting title.
     */
    private String title;
    /**
     * Start time of the meeting.
     */
    private final LocalDateTime startTime;
    /**
     * End time of the meeting.
     */
    private LocalDateTime endTime;
    /**
     * List of participants.
     */
    private final List<UserProfile> participants;
    /**
     * List of chat messages.
     */
    private final List<ChatMessage> messages;
    /**
     * Whether video is enabled.
     */
    private boolean videoEnabled;
    /**
     * Whether screen sharing is enabled.
     */
    private boolean screenSharingEnabled;
    /**
     * Whether audio is enabled.
     */
    private boolean audioEnabled;

    /**
     * Creates a new meeting.
     *
     * @param meetingTitle the title of the meeting
     */
    public Meeting(final String meetingTitle) {
        this.id = UUID.randomUUID().toString();
        this.title = meetingTitle;
        this.startTime = LocalDateTime.now();
        this.participants = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.videoEnabled = false;
        this.screenSharingEnabled = false;
    }

    /**
     * Gets the meeting ID.
     *
     * @return the meeting ID
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the meeting title.
     *
     * @return the meeting title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the start time.
     *
     * @return the start time
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * Gets the end time.
     *
     * @return the end time
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * Gets the participants list.
     *
     * @return copy of participants list
     */
    public List<UserProfile> getParticipants() {
        return new ArrayList<>(participants);
    }

    /**
     * Gets the messages list.
     *
     * @return copy of messages list
     */
    public List<ChatMessage> getMessages() {
        return new ArrayList<>(messages);
    }

    /**
     * Adds a participant to the meeting.
     *
     * @param user the user profile to add
     */
    public void addParticipant(final UserProfile user) {
        final boolean userPresent = participants.stream()
                .map(UserProfile::getEmail)
                .toList()
                .contains(user.getEmail());
        if (!userPresent) {
            participants.add(user);
        }
    }

    /**
     * Removes every participant from the meeting.
     */
    public void clearParticipants() {
        participants.clear();
    }

    /**
     * Replaces the existing participant list with the provided collection.
     * Ensures we don't duplicate entries when the authoritative list has repeats.
     *
     * @param newParticipants the new set of participants from the backend
     */
    public void setParticipants(final Collection<UserProfile> newParticipants) {
        participants.clear();
        if (newParticipants == null) {
            return;
        }

        final HashSet<String> seenEmails = new HashSet<>();
        for (final UserProfile participant : newParticipants) {
            if (participant == null) {
                continue;
            }

            final String email = participant.getEmail();
            if (email == null || seenEmails.add(email)) {
                participants.add(participant);
            }
        }
    }

    /**
     * Checks if audio is enabled.
     *
     * @return true if audio is enabled
     */
    public boolean isAudioEnabled() {
        return audioEnabled;
    }

    /**
     * Sets audio enabled state.
     *
     * @param enabled the audio enabled state
     */
    public void setAudioEnabled(final boolean enabled) {
        this.audioEnabled = enabled;
    }

    /**
     * Sets video enabled state.
     *
     * @param enabled the video enabled state
     */
    public void setVideoEnabled(final boolean enabled) {
        this.videoEnabled = enabled;
    }

    /**
     * Sets screen sharing enabled state.
     *
     * @param enabled the screen sharing enabled state
     */
    public void setScreenSharingEnabled(final boolean enabled) {
        this.screenSharingEnabled = enabled;
    }

    /**
     * Sets the meeting title.
     *
     * @param meetingTitle the meeting title
     */
    public void setMeetingTitle(final String meetingTitle) {
        this.title = meetingTitle;
    }

    /**
     * Removes a participant from the meeting.
     *
     * @param user the user profile to remove
     */
    public void removeParticipant(final UserProfile user) {
        participants.remove(user);
    }

    /**
     * Adds a message to the meeting.
     *
     * @param message the chat message to add
     */
    public void addMessage(final ChatMessage message) {
        messages.add(message);
    }

    /**
     * Ends the meeting.
     */
    public void endMeeting() {
        this.endTime = LocalDateTime.now();
    }

    /**
     * Checks if meeting is active.
     *
     * @return true if active
     */
    public boolean isActive() {
        return endTime == null;
    }

    /**
     * Represents a chat message within a meeting.
     */
    public static class ChatMessage {
        /**
         * The sender of the message.
         */
        private final UserProfile sender;
        /**
         * The message content.
         */
        private final String content;
        /**
         * The timestamp of the message.
         */
        private final LocalDateTime timestamp;

        /**
         * Creates a new chat message.
         *
         * @param messageSender the sender profile
         * @param messageContent the message content
         */
        public ChatMessage(final UserProfile messageSender, final String messageContent) {
            this.sender = messageSender;
            this.content = messageContent;
            this.timestamp = LocalDateTime.now();
        }

        /**
         * Gets the sender.
         *
         * @return the sender profile
         */
        public UserProfile getSender() {
            return sender;
        }

        /**
         * Gets the content.
         *
         * @return the message content
         */
        public String getContent() {
            return content;
        }

        /**
         * Gets the timestamp.
         *
         * @return the timestamp
         */
        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return String.format("%s: %s", sender.getDisplayName(), content);
        }
    }
}
