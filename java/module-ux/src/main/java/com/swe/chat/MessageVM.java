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
     * Helper class to hold reply context.
     */
    public static class ReplyContext {
        /** Whether this message was sent by the current user. */
        private final boolean sentByMe;
        /** The quoted content if this is a reply. */
        private final String quotedContent;
        /** The ID of the message being replied to. */
        private final String replyToId;

        /**
         * Creates a reply context.
         *
         * @param isSentByMe whether sent by current user
         * @param quoted the quoted content
         * @param replyId the reply-to message ID
         */
        public ReplyContext(final boolean isSentByMe,
                           final String quoted,
                           final String replyId) {
            this.sentByMe = isSentByMe;
            this.quotedContent = quoted;
            this.replyToId = replyId;
        }

        /**
         * Gets whether sent by current user.
         *
         * @return true if sent by current user
         */
        public boolean isSentByMe() {
            return sentByMe;
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
         * Gets the reply-to message ID.
         *
         * @return the reply-to message ID
         */
        public String getReplyToId() {
            return replyToId;
        }
    }

    /**
     * Helper class to hold file attachment information.
     */
    public static class FileAttachment {
        /** The file name. */
        private final String fileName;
        /** The size of the compressed file. */
        private final long compressedFileSize;
        /** The file content bytes. */
        private final byte[] fileContent;

        /**
         * Creates a file attachment.
         *
         * @param name the file name
         * @param size the compressed file size
         * @param content the file content bytes
         */
        public FileAttachment(final String name,
                             final long size,
                             final byte[] content) {
            this.fileName = name;
            this.compressedFileSize = size;
            this.fileContent = content;
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
         * Gets the file content.
         *
         * @return the file content
         */
        public byte[] getFileContent() {
            return fileContent;
        }
    }

    /**
     * Creates a new message view model.
     *
     * @param msgId the unique message identifier
     * @param user the username of the sender
     * @param cont the message content or caption
     * @param attachment the file attachment (can be null)
     * @param time the timestamp string
     */
    public MessageVM(final String msgId,
                     final String user,
                     final String cont,
                     final FileAttachment attachment,
                     final String time) {
        this(msgId, user, cont, attachment, time,
                new ReplyContext(false, null, null));
    }

    /**
     * Creates a new message view model (legacy constructor).
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
        this(msgId, user, cont,
                createFileAttachmentIfNeeded(fName, compSize, fContent),
                time, new ReplyContext(false, null, null));
    }

    /**
     * Creates a file attachment if file name is not null.
     *
     * @param fName the file name
     * @param compSize the compressed file size
     * @param fContent the file content
     * @return a FileAttachment or null
     */
    private static FileAttachment createFileAttachmentIfNeeded(
            final String fName, final long compSize, final byte[] fContent) {
        if (fName != null) {
            return new FileAttachment(fName, compSize, fContent);
        }
        return null;
    }

    /**
     * Creates a new message view model with reply context.
     *
     * @param msgId the unique message identifier
     * @param user the username of the sender
     * @param cont the message content or caption
     * @param attachment the file attachment (can be null)
     * @param time the timestamp string
     * @param replyCtx the reply context
     */
    public MessageVM(final String msgId,
                     final String user,
                     final String cont,
                     final FileAttachment attachment,
                     final String time,
                     final ReplyContext replyCtx) {
        this.messageId = msgId;
        this.username = user;
        this.content = cont;
        if (attachment != null) {
            this.fileName = attachment.getFileName();
            this.compressedFileSize = attachment.getCompressedFileSize();
            this.fileContent = attachment.getFileContent();
        } else {
            this.fileName = null;
            this.compressedFileSize = 0;
            this.fileContent = null;
        }
        this.timestamp = time;
        this.isSentByMe = replyCtx.isSentByMe();
        this.quotedContent = replyCtx.getQuotedContent();
        this.replyToId = replyCtx.getReplyToId();
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
