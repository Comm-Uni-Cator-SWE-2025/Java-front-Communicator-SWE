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
 */
public class NetworkMessage {

    private final MessageType messageType;
    private final byte[] serializedAction;

    public NetworkMessage(MessageType messageType, byte[] serializedAction) {
        this.messageType = messageType;
        this.serializedAction = serializedAction;
    }

    public MessageType getMessageType() {
        return messageType;
    }

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
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"type\":\"").append(messageType.toString()).append("\",");
        
        // Encode byte array as Base64 string
        String payload = Base64.getEncoder().encodeToString(serializedAction);
        sb.append("\"payload\":\"").append(payload).append("\"");
        
        sb.append("}");
        return sb.toString();
    }

    /**
     * Deserializes a JSON string back into a NetworkMessage.
     * @param json The JSON string.
     * @return A NetworkMessage object, or null if parsing fails.
     */
    public static NetworkMessage deserialize(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }

        try {
            // Pattern to extract "key":"value"
            Pattern pattern = Pattern.compile("\"(.*?)\":\"(.*?)\"");
            Matcher matcher = pattern.matcher(json);

            String type = null;
            String payload = null;

            while (matcher.find()) {
                String key = matcher.group(1);
                String value = matcher.group(2);
                if ("type".equals(key)) {
                    type = value;
                } else if ("payload".equals(key)) {
                    payload = value;
                }
            }

            if (type != null && payload != null) {
                MessageType msgType = MessageType.valueOf(type);
                byte[] actionBytes = Base64.getDecoder().decode(payload);
                return new NetworkMessage(msgType, actionBytes);
            }
            return null;
        } catch (Exception e) {
            // Handle parsing/decoding errors
            return null;
        }
    }
}