package com.swe.ux.model;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Model for File Messages supporting two modes:
 * 1. Path-Mode — used for sending file paths from frontend
 * 2. Content-Mode — used internally after reading/compressing file
 */
public class FileMessage {
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
     * The timestamp.
     */
    private final LocalDateTime timestamp;
    /**
     * The ID of the message being replied to.
     */
    private final String replyToMessageId;

    /**
     * Caption or message with file.
     */
    private final String caption;
    /**
     * The file name.
     */
    private final String fileName;

    /**
     * Path from frontend.
     */
    private final String filePath;
    /**
     * Compressed content for network.
     */
    private final byte[] fileContent;

    /**
     * Constructor for Path-Mode (Frontend → Core).
     *
     * @param msgId the message ID
     * @param usrId the user ID
     * @param displayName the sender display name
     * @param fileCaption the caption
     * @param file the file name
     * @param path the file path
     * @param replyToId the ID of message being replied to
     */
    public FileMessage(final String msgId, final String usrId,
                       final String displayName, final String fileCaption,
                       final String file, final String path,
                       final String replyToId) {
        this.messageId = msgId;
        this.userId = usrId;
        this.senderDisplayName = displayName;
        this.caption = fileCaption;
        this.fileName = file;
        this.filePath = path;
        this.replyToMessageId = replyToId;
        this.timestamp = LocalDateTime.now(ZoneOffset.UTC);
        this.fileContent = null; // Not used in Path Mode
    }

    /**
     * Constructor for Content-Mode (Core → Network).
     *
     * @param msgId the message ID
     * @param usrId the user ID
     * @param displayName the sender display name
     * @param fileCaption the caption
     * @param file the file name
     * @param content the file content bytes
     * @param timestampEpoch the epoch timestamp
     * @param replyToId the ID of message being replied to
     */
    public FileMessage(final String msgId, final String usrId,
                       final String displayName, final String fileCaption,
                       final String file, final byte[] content,
                       final long timestampEpoch, final String replyToId) {
        this.messageId = msgId;
        this.userId = usrId;
        this.senderDisplayName = displayName;
        this.caption = fileCaption;
        this.fileName = file;
        this.fileContent = content;
        this.replyToMessageId = replyToId;
        this.timestamp = LocalDateTime.ofEpochSecond(timestampEpoch, 0, ZoneOffset.UTC);
        this.filePath = null; // Not used in Content Mode
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
     * Gets the caption.
     *
     * @return the caption
     */
    public String getCaption() {
        return caption;
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
     * Gets the file path.
     *
     * @return the file path
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Gets the file content.
     *
     * @return the file content
     */
    public byte[] getFileContent() {
        return fileContent;
    }

    /**
     * Gets the reply-to message ID.
     *
     * @return the reply-to message ID
     */
    public String getReplyToMessageId() {
        return replyToMessageId;
    }

    /**
     * Gets the timestamp.
     *
     * @return the timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
