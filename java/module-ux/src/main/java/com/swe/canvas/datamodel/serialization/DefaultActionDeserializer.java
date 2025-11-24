package com.swe.canvas.datamodel.serialization;

import java.nio.charset.StandardCharsets;

import com.swe.canvas.datamodel.action.Action;

/**
 * Default implementation of {@link ActionDeserializer}.
 *
 * <p>This implementation now uses manual JSON deserialization via
 * {@link ManualJsonConverter}, replacing the need for Java's
 * built-in binary serialization.
 * </p>
 */
public class DefaultActionDeserializer implements ActionDeserializer {

    /**
     * Deserializes data using manual JSON parsing.
     *
     * @param data The DTO containing the serialized data.
     * @return The reconstituted {@link Action} object.
     * @throws SerializationException if deserialization fails.
     */
    @Override
    public Action deserialize(final SerializedAction data) throws SerializationException {
        final byte[] bytes = data.getData();

        try {
            // Convert the byte array (which is JSON) to a string
            final String json = new String(bytes, StandardCharsets.UTF_8);

            // Call the manual JSON deserializer
            final Action action = NetActionSerializer.deserializeAction(json);

            if (action != null) {
                return action;
            } else {
                throw new SerializationException("Deserialized object is null or not of type Action.");
            }

        } catch (SerializationException e) {
            throw new SerializationException("Failed to manually deserialize action from bytes.", e);
        }
    }
}