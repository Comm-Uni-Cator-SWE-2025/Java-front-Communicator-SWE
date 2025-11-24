package com.swe.canvas.datamodel.collaboration;

import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A wrapper for data sent over the network.
 * It contains the MessageType and the serialized Action (as bytes).
 *
 * This class includes its own manual JSON serializer/deserializer to wrap
 * the byte[] payload as a Base64 string for safe JSON transport.
 *
 * @author Canvas Team
 */
public class NetworkMessage {

    private final MessageType messageType;
    private final byte[] serializedAction;

    /**
     * Constructs a NetworkMessage.
     * @param messageType The type of message.
     * @param serializedAction The serialized action bytes.
     */
    public NetworkMessage(final MessageType messageType, final byte[] serializedAction) {
        this.messageType = messageType;
        this.serializedAction = serializedAction;
    }

    /**
     * Gets the message type.
     * @return The message type.
     */
    public MessageType getMessageType() {
        return messageType;
    }

    /**
     * Gets the serialized action bytes.
     * @return The serialized action bytes.
     */
    public byte[] getSerializedAction() {
        return serializedAction;
    }

    // =========================================================================
    // Manual JSON Serialization/Deserialization for NetworkMessage
    // =========================================================================

    /**
     * Serializes this NetworkMessage into a JSON string.
     * @return A JSON string, e.g., {"type":"NORMAL","payload":"...Base64..."}
     */
    public String serialize() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"type\":\"").append(messageType.toString()).append("\",");
        
        // Encode byte array as Base64 string
        final String payload = Base64.getEncoder().encodeToString(serializedAction);
        sb.append("\"payload\":\"").append(payload).append("\"");
        
        sb.append("}");
        return sb.toString();
    }

    /**
     * Deserializes a JSON string back into a NetworkMessage.
     * @param json The JSON string.
     * @return A NetworkMessage object, or null if parsing fails.
     */
    public static NetworkMessage deserialize(final String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }

        try {
            // Pattern to extract "key":"value"
            final Pattern pattern = Pattern.compile("\"(.*?)\":\"(.*?)\"");
            final Matcher matcher = pattern.matcher(json);

            String type = null;
            String payload = null;

            while (matcher.find()) {
                final String key = matcher.group(1);
                final String value = matcher.group(2);
                if ("type".equals(key)) {
                    type = value;
                } else if ("payload".equals(key)) {
                    payload = value;
                }
            }

            if (type != null && payload != null) {
                final MessageType msgType = MessageType.valueOf(type);
                final byte[] actionBytes = Base64.getDecoder().decode(payload);
                return new NetworkMessage(msgType, actionBytes);
            }
            return null;
        } catch (final Exception e) {
            // Handle parsing/decoding errors
            return null;
        }
    }
}