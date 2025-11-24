package com.swe.chat;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;

import com.swe.ux.model.ChatMessage;

/**
 * Serializer for ChatMessage objects to/from byte arrays.
 */
public final class ChatMessageSerializer {

    /** Size of an integer in bytes. */
    private static final int INT_SIZE = 4;

    /** Size of a long in bytes. */
    private static final int LONG_SIZE = 8;

    /**
     * Private constructor to prevent instantiation.
     */
    private ChatMessageSerializer() {
        // Utility class
    }

    /**
     * Serializes a ChatMessage to a byte array.
     *
     * @param message the message to serialize
     * @return the serialized byte array
     */
    public static byte[] serialize(final ChatMessage message) {
        final byte[] idBytes =
                message.getMessageId().getBytes(StandardCharsets.UTF_8);
        final byte[] userBytes =
                message.getUserId().getBytes(StandardCharsets.UTF_8);
        final byte[] nameBytes =
                message.getSenderDisplayName().getBytes(StandardCharsets.UTF_8);
        final byte[] contentBytes =
                message.getContent().getBytes(StandardCharsets.UTF_8);
        final long timestamp =
                message.getTimestamp().toEpochSecond(ZoneOffset.UTC);
        final byte[] replyBytes = getReplyBytes(message);

        final int size = INT_SIZE + idBytes.length
                + INT_SIZE + userBytes.length
                + INT_SIZE + nameBytes.length
                + INT_SIZE + contentBytes.length
                + LONG_SIZE
                + INT_SIZE + replyBytes.length;
        final ByteBuffer buffer = ByteBuffer.allocate(size);

        writeString(buffer, message.getMessageId());
        writeString(buffer, message.getUserId());
        writeString(buffer, message.getSenderDisplayName());
        writeString(buffer, message.getContent());
        buffer.putLong(timestamp);
        writeString(buffer, message.getReplyToMessageId());

        return buffer.array();
    }

    /**
     * Gets the reply bytes from a message.
     *
     * @param message the message
     * @return the reply bytes
     */
    private static byte[] getReplyBytes(final ChatMessage message) {
        if (message.getReplyToMessageId() == null) {
            return new byte[0];
        }
        return message.getReplyToMessageId().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Deserializes a byte array to a ChatMessage.
     *
     * @param data the byte array to deserialize
     * @return the deserialized ChatMessage
     */
    public static ChatMessage deserialize(final byte[] data) {
        final ByteBuffer buffer = ByteBuffer.wrap(data);
        final String id = readString(buffer);
        final String user = readString(buffer);
        final String name = readString(buffer);
        final String content = readString(buffer);
        final long timestamp = buffer.getLong();
        final String replyId = readString(buffer);

        return new ChatMessage(id, user, name, content, timestamp, replyId);
    }

    /**
     * Writes a string to the buffer with length prefix.
     *
     * @param buffer the buffer to write to
     * @param s the string to write
     */
    private static void writeString(final ByteBuffer buffer, final String s) {
        if (s == null) {
            buffer.putInt(0);
        } else {
            final byte[] b = s.getBytes(StandardCharsets.UTF_8);
            buffer.putInt(b.length);
            buffer.put(b);
        }
    }

    /**
     * Reads a length-prefixed string from the buffer.
     *
     * @param buffer the buffer to read from
     * @return the string read from the buffer
     */
    private static String readString(final ByteBuffer buffer) {
        final int len = buffer.getInt();
        if (len == 0) {
            return null;
        }
        final byte[] b = new byte[len];
        buffer.get(b);
        return new String(b, StandardCharsets.UTF_8);
    }
}
