package com.swe.ux.model;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Represents a single chat message.
 * matches the Core's ChatMessage exactly for serialization.
 */
public class ChatMessage {

    private final String messageId;
    private final String userId;
    private final String senderDisplayName;
    private final String content;
    private final LocalDateTime timestamp;
    private final String replyToMessageId;

    // Constructor for creating NEW messages (sets time to now)
    public ChatMessage(String messageId, String userId, String senderDisplayName, String content, String replyToId) {
        this.messageId = messageId;
        this.userId = userId;
        this.senderDisplayName = senderDisplayName;
        this.content = content;
        this.replyToMessageId = replyToId;
        this.timestamp = LocalDateTime.now(ZoneOffset.UTC);
    }

    // Constructor for DESERIALIZATION (takes raw timestamp)
    public ChatMessage(String messageId, String userId, String senderDisplayName, String content, long timestampEpoch, String replyToId) {
        this.messageId = messageId;
        this.userId = userId;
        this.senderDisplayName = senderDisplayName;
        this.content = content;
        this.replyToMessageId = replyToId;
        this.timestamp = LocalDateTime.ofEpochSecond(timestampEpoch, 0, ZoneOffset.UTC);
    }

    public String getMessageId() { return messageId; }
    public String getUserId() { return userId; }
    public String getSenderDisplayName() { return senderDisplayName; }
    public String getContent() { return content; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getReplyToMessageId() { return replyToMessageId; }
}