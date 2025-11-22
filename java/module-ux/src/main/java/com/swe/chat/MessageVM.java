package com.swe.chat;

public class MessageVM {
    public final String messageId;
    public final String username;
    public final String timestamp;
    public final boolean isSentByMe;
    public final String quotedContent;
    public final String content;          // Text OR caption
    public final String fileName;         // File name (if file message)
    public final long compressedFileSize; // Size from metadata
    public final byte[] fileContent;      // Usually NULL for metadata-only flow
    public final String replyToId;

    public MessageVM(String messageId, String username, String content, String fileName,
                     long compressedFileSize, byte[] fileContent,
                     String timestamp, boolean isSentByMe, String quotedContent,String replyToId) {
        this.messageId = messageId;
        this.username = username;
        this.content = content;
        this.fileName = fileName;
        this.compressedFileSize = compressedFileSize;
        this.fileContent = fileContent;
        this.timestamp = timestamp;
        this.isSentByMe = isSentByMe;
        this.quotedContent = quotedContent;
        this.replyToId = replyToId;
    }

    public boolean hasQuote() { return quotedContent != null; }
    public boolean isFileMessage() { return fileName != null; }
}