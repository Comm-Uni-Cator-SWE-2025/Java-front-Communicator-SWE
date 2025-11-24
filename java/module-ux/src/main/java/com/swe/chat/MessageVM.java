package com.swe.chat;

/**
 * View model representing a chat message for display.
 */
public class MessageVM {

    /** The unique message identifier. */
    private final String messageId;

    /** The username of the sender. */
    private final String username;

    /** The timestamp string. */
    private final String timestamp;

    /** Whether this message was sent by the current user. */
    private final boolean isSentByMe;

    /** The quoted content if this is a reply. */
    private final String quotedContent;

    /** The message content or caption. */
    private final String content;

    /** The file name if this is a file message. */
    private final String fileName;

    /** The size of the compressed file. */
    private final long compressedFileSize;

    /** The file content bytes. */
    private final byte[] fileContent;

    /** The ID of the message being replied to. */
    private final String replyToId;

    /**
     * Creates a new message view model.
     *
     * @param msgId the unique message identifier
     * @param user the username of the sender
     * @param cont the message content or caption
     * @param fName the file name if this is a file message
     * @param compSize the size of the compressed file
     * @param fContent the file content bytes
     * @param time the timestamp string
     */
    public MessageVM(final String msgId,
                     final String user,
                     final String cont,
                     final String fName,
                     final long compSize,
                     final byte[] fContent,
                     final String time) {
        this(msgId, user, cont, fName, compSize,
                fContent, time, false, null, null);
    }

    /**
     * Creates a new message view model with all parameters.
     *
     * @param msgId the unique message identifier
     * @param user the username of the sender
     * @param cont the message content or caption
     * @param fName the file name if this is a file message
     * @param compSize the size of the compressed file
     * @param fContent the file content bytes
     * @param time the timestamp string
     * @param sentByMe whether this message was sent by the current user
     * @param quotedCont the quoted content if this is a reply
     * @param replyTo the ID of the message being replied to
     */
    public MessageVM(final String msgId,
                     final String user,
                     final String cont,
                     final String fName,
                     final long compSize,
                     final byte[] fContent,
                     final String time,
                     final boolean sentByMe,
                     final String quotedCont,
                     final String replyTo) {
        this.messageId = msgId;
        this.username = user;
        this.content = cont;
        this.fileName = fName;
        this.compressedFileSize = compSize;
        this.fileContent = fContent;
        this.timestamp = time;
        this.isSentByMe = sentByMe;
        this.quotedContent = quotedCont;
        this.replyToId = replyTo;
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
     * Gets the username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the timestamp.
     *
     * @return the timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Checks if this message was sent by the current user.
     *
     * @return true if sent by current user
     */
    public boolean isSentByMe() {
        return isSentByMe;
    }

    /**
     * Gets the quoted content.
     *
     * @return the quoted content
     */
    public String getQuotedContent() {
        return quotedContent;
    }

    /**
     * Gets the message content.
     *
     * @return the message content
     */
    public String getContent() {
        return content;
    }

    /**
     * Gets the file name.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Gets the compressed file size.
     *
     * @return the compressed file size
     */
    public long getCompressedFileSize() {
        return compressedFileSize;
    }

    /**
     * Gets the file content bytes.
     *
     * @return the file content bytes
     */
    public byte[] getFileContent() {
        return fileContent;
    }

    /**
     * Gets the reply-to message ID.
     *
     * @return the reply-to message ID
     */
    public String getReplyToId() {
        return replyToId;
    }

    /**
     * Checks if this message has quoted content.
     *
     * @return true if has quoted content
     */
    public boolean hasQuote() {
        return quotedContent != null;
    }

    /**
     * Checks if this is a file message.
     *
     * @return true if is file message
     */
    public boolean isFileMessage() {
        return fileName != null;
    }
}
