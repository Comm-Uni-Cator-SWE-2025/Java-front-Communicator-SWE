package com.swe.ux.model;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Represents a single chat message.
 * matches the Core's ChatMessage exactly for serialization.
 */
public class ChatMessage {
    /**
     * The message ID.
     */
    private final String messageId;
    /**
     * The user ID.
     */
    private final String userId;
    /**
     * The sender's display name.
     */
    private final String senderDisplayName;
    /**
     * The message content.
     */
    private final String content;
    /**
     * The timestamp.
     */
    private final LocalDateTime timestamp;
    /**
     * The ID of the message being replied to.
     */
    private final String replyToMessageId;

    /**
     * Constructor for creating NEW messages (sets time to now).
     *
     * @param msgId the message ID
     * @param usrId the user ID
     * @param displayName the sender display name
     * @param msgContent the message content
     * @param replyToId the ID of message being replied to
     */
    public ChatMessage(final String msgId, final String usrId,
                       final String displayName, final String msgContent,
                       final String replyToId) {
        this.messageId = msgId;
        this.userId = usrId;
        this.senderDisplayName = displayName;
        this.content = msgContent;
        this.replyToMessageId = replyToId;
        this.timestamp = LocalDateTime.now(ZoneOffset.UTC);
    }

    /**
     * Constructor for DESERIALIZATION (takes raw timestamp).
     *
     * @param msgId the message ID
     * @param usrId the user ID
     * @param displayName the sender display name
     * @param msgContent the message content
     * @param timestampEpoch the epoch timestamp
     * @param replyToId the ID of message being replied to
     */
    public ChatMessage(final String msgId, final String usrId,
                       final String displayName, final String msgContent,
                       final long timestampEpoch, final String replyToId) {
        this.messageId = msgId;
        this.userId = usrId;
        this.senderDisplayName = displayName;
        this.content = msgContent;
        this.replyToMessageId = replyToId;
        this.timestamp = LocalDateTime.ofEpochSecond(timestampEpoch, 0, ZoneOffset.UTC);
    }

    /**
     * Gets the message ID.
     *
     * @return the message ID
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Gets the user ID.
     *
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Gets the sender display name.
     *
     * @return the sender display name
     */
    public String getSenderDisplayName() {
        return senderDisplayName;
    }

    /**
     * Gets the message content.
     *
     * @return the content
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

    /**
     * Gets the reply-to message ID.
     *
     * @return the reply-to message ID
     */
    public String getReplyToMessageId() {
        return replyToMessageId;
    }
}
