package com.swe.chat;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;

import com.swe.ux.model.FileMessage;

/**
 * Serializer for FileMessage objects to/from byte arrays.
 */
public final class FileMessageSerializer {

    /** Size of an integer in bytes. */
    private static final int INT_SIZE = 4;

    /** Size of a long in bytes. */
    private static final int LONG_SIZE = 8;

    /**
     * Private constructor to prevent instantiation.
     */
    private FileMessageSerializer() {
        // Utility class
    }

    /**
     * Helper to write a length-prefixed byte array to the buffer.
     * Writes 0 for null or empty arrays.
     *
     * @param buffer the buffer to write to
     * @param data the data to write
     */
    private static void writeBytes(final ByteBuffer buffer, final byte[] data) {
        if (data == null) {
            buffer.putInt(0);
        } else {
            buffer.putInt(data.length);
            buffer.put(data);
        }
    }

    /**
     * Helper to read a length-prefixed byte array from the buffer.
     * Returns null if length is 0.
     *
     * @param buffer the buffer to read from
     * @return the byte array read from the buffer
     */
    private static byte[] readBytes(final ByteBuffer buffer) {
        final int len = buffer.getInt();
        if (len <= 0) {
            return null;
        }
        final byte[] bytes = new byte[len];
        buffer.get(bytes);
        return bytes;
    }

    /**
     * Helper to read a length-prefixed string from the buffer.
     *
     * @param buffer the buffer to read from
     * @return the string read from the buffer
     */
    private static String readString(final ByteBuffer buffer) {
        final byte[] bytes = readBytes(buffer);
        if (bytes == null) {
            return null;
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Serializes a FileMessage to a byte array.
     *
     * @param message the message to serialize
     * @return the serialized byte array
     */
    public static byte[] serialize(final FileMessage message) {
        final MessageBytes messageBytes = extractMessageBytes(message);
        final long timestampEpoch =
                message.getTimestamp().toEpochSecond(ZoneOffset.UTC);

        final int totalSize = calculateTotalSize(messageBytes);

        final ByteBuffer buffer = ByteBuffer.allocate(totalSize);
        // writeAllBytes(buffer, messageBytes);
        writeBytes(buffer, messageBytes.getMessageIdBytes());
        writeBytes(buffer, messageBytes.getUserIdBytes());
        writeBytes(buffer, messageBytes.getSenderNameBytes());
        writeBytes(buffer, messageBytes.getCaptionBytes());
        writeBytes(buffer, messageBytes.getFileNameBytes());
        writeBytes(buffer, messageBytes.getFilePathBytes());
        writeBytes(buffer, messageBytes.getFileContentBytes());
        buffer.putLong(timestampEpoch);
        writeBytes(buffer, messageBytes.getReplyIdBytes());

        return buffer.array();
    }

    /**
     * Extracts all byte arrays from a message.
     *
     * @param message the message
     * @return the message bytes container
     */
    private static MessageBytes extractMessageBytes(final FileMessage message) {
        final MessageBytes bytes = new MessageBytes();
        bytes.setMessageIdBytes(convertToBytes(message.getMessageId()));
        bytes.setUserIdBytes(convertToBytes(message.getUserId()));
        bytes.setSenderNameBytes(convertToBytes(message.getSenderDisplayName()));
        bytes.setFileNameBytes(convertToBytes(message.getFileName()));
        bytes.setFilePathBytes(convertToBytes(message.getFilePath()));
        bytes.setFileContentBytes(message.getFileContent());
        bytes.setReplyIdBytes(convertToBytes(message.getReplyToMessageId()));
        bytes.setCaptionBytes(convertToBytes(message.getCaption()));
        return bytes;
    }

    /**
     * Converts a string to bytes, handling null values.
     *
     * @param str the string to convert
     * @return the byte array or null
     */
    private static byte[] convertToBytes(final String str) {
        if (str == null) {
            return null;
        }
        return str.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Calculates the total size needed for serialization.
     *
     * @param messageBytes the message bytes container
     * @return the total size in bytes
     */
    private static int calculateTotalSize(final MessageBytes messageBytes) {
        int totalSize = 0;
        totalSize += INT_SIZE + getLength(messageBytes.getMessageIdBytes());
        totalSize += INT_SIZE + getLength(messageBytes.getUserIdBytes());
        totalSize += INT_SIZE + getLength(messageBytes.getSenderNameBytes());
        totalSize += INT_SIZE + getLength(messageBytes.getCaptionBytes());
        totalSize += INT_SIZE + getLength(messageBytes.getFileNameBytes());
        totalSize += INT_SIZE + getLength(messageBytes.getFilePathBytes());
        totalSize += INT_SIZE + getLength(messageBytes.getFileContentBytes());
        totalSize += LONG_SIZE;
        totalSize += INT_SIZE + getLength(messageBytes.getReplyIdBytes());
        return totalSize;
    }

    /**
     * Writes all message bytes to the buffer.
     *
     * @param buffer the buffer to write to
     * @param messageBytes the message bytes container
     */
    private static void writeAllBytes(final ByteBuffer buffer,
                                      final MessageBytes messageBytes) {
        writeBytes(buffer, messageBytes.getMessageIdBytes());
        writeBytes(buffer, messageBytes.getUserIdBytes());
        writeBytes(buffer, messageBytes.getSenderNameBytes());
        writeBytes(buffer, messageBytes.getCaptionBytes());
        writeBytes(buffer, messageBytes.getFileNameBytes());
        writeBytes(buffer, messageBytes.getFilePathBytes());
        writeBytes(buffer, messageBytes.getFileContentBytes());
        writeBytes(buffer, messageBytes.getReplyIdBytes());
    }

    /**
     * Gets the length of a byte array, handling null values.
     *
     * @param bytes the byte array
     * @return the length or 0 if null
     */
    private static int getLength(final byte[] bytes) {
        if (bytes == null) {
            return 0;
        }
        return bytes.length;
    }

    /**
     * Deserializes a byte array to a FileMessage.
     *
     * @param data the byte array to deserialize
     * @return the deserialized FileMessage
     */
    public static FileMessage deserialize(final byte[] data) {
        final ByteBuffer buffer = ByteBuffer.wrap(data);

        final String messageId = readString(buffer);
        final String userId = readString(buffer);
        final String senderName = readString(buffer);
        final String caption = readString(buffer);
        final String fileName = readString(buffer);
        final String filePath = cleanFilePath(readString(buffer));
        final byte[] fileContent = readBytes(buffer);
        final long timestampEpoch = buffer.getLong();
        final String replyToId = readString(buffer);

        if (filePath != null) {
            return new FileMessage(messageId, userId, senderName,
                    caption, fileName, filePath, replyToId);
        } else {
            return new FileMessage(messageId, userId, senderName,
                    caption, fileName, fileContent, timestampEpoch, replyToId);
        }
    }

    /**
     * Cleans and validates a file path.
     *
     * @param filePath the file path to clean
     * @return the cleaned file path or null
     */
    private static String cleanFilePath(final String filePath) {
        if (filePath == null) {
            return null;
        }

        String cleaned = filePath.trim();
        if (cleaned.startsWith("*")) {
            cleaned = cleaned.substring(1).trim();
        }
        if (cleaned.isEmpty()) {
            return null;
        }
        return cleaned;
    }

    /**
     * Helper class to hold all message byte arrays.
     */
    private static class MessageBytes {

        /** Message ID bytes. */
        private byte[] messageIdBytes;

        /** User ID bytes. */
        private byte[] userIdBytes;

        /** Sender name bytes. */
        private byte[] senderNameBytes;

        /** File name bytes. */
        private byte[] fileNameBytes;

        /** File path bytes. */
        private byte[] filePathBytes;

        /** File content bytes. */
        private byte[] fileContentBytes;

        /** Reply ID bytes. */
        private byte[] replyIdBytes;

        /** Caption bytes. */
        private byte[] captionBytes;

        /**
         * Creates a new empty MessageBytes container.
         */
        MessageBytes() {
            // Default constructor
        }

        /**
         * Sets the message ID bytes.
         *
         * @param bytes the message ID bytes
         */
        void setMessageIdBytes(final byte[] bytes) {
            this.messageIdBytes = bytes;
        }

        /**
         * Sets the user ID bytes.
         *
         * @param bytes the user ID bytes
         */
        void setUserIdBytes(final byte[] bytes) {
            this.userIdBytes = bytes;
        }

        /**
         * Sets the sender name bytes.
         *
         * @param bytes the sender name bytes
         */
        void setSenderNameBytes(final byte[] bytes) {
            this.senderNameBytes = bytes;
        }

        /**
         * Sets the file name bytes.
         *
         * @param bytes the file name bytes
         */
        void setFileNameBytes(final byte[] bytes) {
            this.fileNameBytes = bytes;
        }

        /**
         * Sets the file path bytes.
         *
         * @param bytes the file path bytes
         */
        void setFilePathBytes(final byte[] bytes) {
            this.filePathBytes = bytes;
        }

        /**
         * Sets the file content bytes.
         *
         * @param bytes the file content bytes
         */
        void setFileContentBytes(final byte[] bytes) {
            this.fileContentBytes = bytes;
        }

        /**
         * Sets the reply ID bytes.
         *
         * @param bytes the reply ID bytes
         */
        void setReplyIdBytes(final byte[] bytes) {
            this.replyIdBytes = bytes;
        }

        /**
         * Sets the caption bytes.
         *
         * @param bytes the caption bytes
         */
        void setCaptionBytes(final byte[] bytes) {
            this.captionBytes = bytes;
        }

        /**
         * Gets the message ID bytes.
         *
         * @return the message ID bytes
         */
        byte[] getMessageIdBytes() {
            return messageIdBytes;
        }

        /**
         * Gets the user ID bytes.
         *
         * @return the user ID bytes
         */
        byte[] getUserIdBytes() {
            return userIdBytes;
        }

        /**
         * Gets the sender name bytes.
         *
         * @return the sender name bytes
         */
        byte[] getSenderNameBytes() {
            return senderNameBytes;
        }

        /**
         * Gets the file name bytes.
         *
         * @return the file name bytes
         */
        byte[] getFileNameBytes() {
            return fileNameBytes;
        }

        /**
         * Gets the file path bytes.
         *
         * @return the file path bytes
         */
        byte[] getFilePathBytes() {
            return filePathBytes;
        }

        /**
         * Gets the file content bytes.
         *
         * @return the file content bytes
         */
        byte[] getFileContentBytes() {
            return fileContentBytes;
        }

        /**
         * Gets the reply ID bytes.
         *
         * @return the reply ID bytes
         */
        byte[] getReplyIdBytes() {
            return replyIdBytes;
        }

        /**
         * Gets the caption bytes.
         *
         * @return the caption bytes
         */
        byte[] getCaptionBytes() {
            return captionBytes;
        }
    }
}
