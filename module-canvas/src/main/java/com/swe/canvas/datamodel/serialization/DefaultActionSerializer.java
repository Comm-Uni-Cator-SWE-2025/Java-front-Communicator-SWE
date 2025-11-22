package com.swe.canvas.datamodel.serialization;

import java.nio.charset.StandardCharsets;

import com.swe.canvas.datamodel.action.Action;

/**
 * Default implementation of {@link ActionSerializer}.
 *
 * <p>This implementation now uses manual JSON serialization via
 * {@link ManualJsonConverter}, replacing the need for Java's
 * built-in binary serialization.
 * </p>
 */
public class DefaultActionSerializer implements ActionSerializer {

    /**
     * Serializes an action using manual JSON construction.
     *
     * @param action The action object to serialize.
     * @return A DTO containing the serialized byte array.
     * @throws SerializationException if serialization fails.
     */
    @Override
    public SerializedAction serialize(final Action action) throws SerializationException {
        try {
            // Call the manual JSON serializer
            final String json = NetActionSerializer.serializeAction(action);

            // Convert the JSON string to a UTF-8 byte array
            final byte[] data = json.getBytes(StandardCharsets.UTF_8);

            return new SerializedAction(data);

        } catch (Exception e) {
            throw new SerializationException("Failed to manually serialize action object.", e);
        }
    }
}