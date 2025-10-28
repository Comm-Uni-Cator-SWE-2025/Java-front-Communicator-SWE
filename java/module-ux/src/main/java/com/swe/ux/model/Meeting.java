package com.swe.ux.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a meeting with participants and messages.
 */
public class Meeting {
    private final String id;
    private final String title;
    private final LocalDateTime startTime;
    private LocalDateTime endTime;
    private final List<User> participants;
    private final List<ChatMessage> messages;

    public Meeting(String title) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.startTime = LocalDateTime.now();
        this.participants = new ArrayList<>();
        this.messages = new ArrayList<>();
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public List<User> getParticipants() { return new ArrayList<>(participants); }
    public List<ChatMessage> getMessages() { return new ArrayList<>(messages); }

    // Methods
    public void addParticipant(User user) {
        if (!participants.contains(user)) {
            participants.add(user);
        }
    }

    public void removeParticipant(User user) {
        participants.remove(user);
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
    }

    public void endMeeting() {
        this.endTime = LocalDateTime.now();
    }

    public boolean isActive() {
        return endTime == null;
    }

    /**
     * Represents a chat message within a meeting.
     */
    public static class ChatMessage {
        private final User sender;
        private final String content;
        private final LocalDateTime timestamp;

        public ChatMessage(User sender, String content) {
            this.sender = sender;
            this.content = content;
            this.timestamp = LocalDateTime.now();
        }

        // Getters
        public User getSender() { return sender; }
        public String getContent() { return content; }
        public LocalDateTime getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            return String.format("%s: %s", sender.getUsername(), content);
        }
    }
}
