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
        writeAllBytes(buffer, messageBytes);
        buffer.putLong(timestampEpoch);

        return buffer.array();
    }

    /**
     * Extracts all byte arrays from a message.
     *
     * @param message the message
     * @return the message bytes container
     */
    private static MessageBytes extractMessageBytes(final FileMessage message) {
        return new MessageBytes(
                convertToBytes(message.getMessageId()),
                convertToBytes(message.getUserId()),
                convertToBytes(message.getSenderDisplayName()),
                convertToBytes(message.getFileName()),
                convertToBytes(message.getFilePath()),
                message.getFileContent(),
                convertToBytes(message.getReplyToMessageId()),
                convertToBytes(message.getCaption())
        );
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
        totalSize += INT_SIZE + getLength(messageBytes.messageIdBytes);
        totalSize += INT_SIZE + getLength(messageBytes.userIdBytes);
        totalSize += INT_SIZE + getLength(messageBytes.senderNameBytes);
        totalSize += INT_SIZE + getLength(messageBytes.captionBytes);
        totalSize += INT_SIZE + getLength(messageBytes.fileNameBytes);
        totalSize += INT_SIZE + getLength(messageBytes.filePathBytes);
        totalSize += INT_SIZE + getLength(messageBytes.fileContentBytes);
        totalSize += LONG_SIZE;
        totalSize += INT_SIZE + getLength(messageBytes.replyIdBytes);
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
        writeBytes(buffer, messageBytes.messageIdBytes);
        writeBytes(buffer, messageBytes.userIdBytes);
        writeBytes(buffer, messageBytes.senderNameBytes);
        writeBytes(buffer, messageBytes.captionBytes);
        writeBytes(buffer, messageBytes.fileNameBytes);
        writeBytes(buffer, messageBytes.filePathBytes);
        writeBytes(buffer, messageBytes.fileContentBytes);
        writeBytes(buffer, messageBytes.replyIdBytes);
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
        final byte[] messageIdBytes;

        /** User ID bytes. */
        final byte[] userIdBytes;

        /** Sender name bytes. */
        final byte[] senderNameBytes;

        /** File name bytes. */
        final byte[] fileNameBytes;

        /** File path bytes. */
        final byte[] filePathBytes;

        /** File content bytes. */
        final byte[] fileContentBytes;

        /** Reply ID bytes. */
        final byte[] replyIdBytes;

        /** Caption bytes. */
        final byte[] captionBytes;

        /**
         * Creates a new MessageBytes container.
         *
         * @param messageIdBytes the message ID bytes
         * @param userIdBytes the user ID bytes
         * @param senderNameBytes the sender name bytes
         * @param fileNameBytes the file name bytes
         * @param filePathBytes the file path bytes
         * @param fileContentBytes the file content bytes
         * @param replyIdBytes the reply ID bytes
         * @param captionBytes the caption bytes
         */
        MessageBytes(final byte[] messageIdBytes,
                     final byte[] userIdBytes,
                     final byte[] senderNameBytes,
                     final byte[] fileNameBytes,
                     final byte[] filePathBytes,
                     final byte[] fileContentBytes,
                     final byte[] replyIdBytes,
                     final byte[] captionBytes) {
            this.messageIdBytes = messageIdBytes;
            this.userIdBytes = userIdBytes;
            this.senderNameBytes = senderNameBytes;
            this.fileNameBytes = fileNameBytes;
            this.filePathBytes = filePathBytes;
            this.fileContentBytes = fileContentBytes;
            this.replyIdBytes = replyIdBytes;
            this.captionBytes = captionBytes;
        }
    }
}
