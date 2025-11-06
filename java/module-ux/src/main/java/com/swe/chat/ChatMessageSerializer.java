package com.swe.chat;

import com.swe.ux.model.ChatMessage;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;

public final class ChatMessageSerializer {

    private ChatMessageSerializer() {}

    public static byte[] serialize(final ChatMessage message) {
        byte[] idBytes = message.getMessageId().getBytes(StandardCharsets.UTF_8);
        byte[] userBytes = message.getUserId().getBytes(StandardCharsets.UTF_8);
        byte[] nameBytes = message.getSenderDisplayName().getBytes(StandardCharsets.UTF_8);
        byte[] contentBytes = message.getContent().getBytes(StandardCharsets.UTF_8);
        long timestamp = message.getTimestamp().toEpochSecond(ZoneOffset.UTC);
        byte[] replyBytes = message.getReplyToMessageId() == null ? new byte[0] : message.getReplyToMessageId().getBytes(StandardCharsets.UTF_8);

        int size = 4 + idBytes.length + 4 + userBytes.length + 4 + nameBytes.length + 4 + contentBytes.length + 8 + 4 + replyBytes.length;
        ByteBuffer buffer = ByteBuffer.allocate(size);

        writeString(buffer, message.getMessageId());
        writeString(buffer, message.getUserId());
        writeString(buffer, message.getSenderDisplayName());
        writeString(buffer, message.getContent());
        buffer.putLong(timestamp);
        writeString(buffer, message.getReplyToMessageId());

        return buffer.array();
    }

    public static ChatMessage deserialize(final byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        String id = readString(buffer);
        String user = readString(buffer);
        String name = readString(buffer);
        String content = readString(buffer);
        long timestamp = buffer.getLong();
        String replyId = readString(buffer);

        return new ChatMessage(id, user, name, content, timestamp, replyId);
    }

    private static void writeString(ByteBuffer buffer, String s) {
        if (s == null) {
            buffer.putInt(0);
        } else {
            byte[] b = s.getBytes(StandardCharsets.UTF_8);
            buffer.putInt(b.length);
            buffer.put(b);
        }
    }

    private static String readString(ByteBuffer buffer) {
        int len = buffer.getInt();
        if (len == 0) return null;
        byte[] b = new byte[len];
        buffer.get(b);
        return new String(b, StandardCharsets.UTF_8);
    }
}
