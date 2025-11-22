package com.swe.canvas.datamodel.serialization;

import com.swe.canvas.datamodel.action.Action;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Default implementation of {@link ActionDeserializer}.
 *
 * <p>This implementation uses Java's built-in {@link ObjectInputStream}
 * to deserialize byte arrays back into {@link Action} objects.
 * </p>
 *
 * <p><b>Thread Safety:</b> This class is stateless and thread-safe.
 * New streams are created for each operation.
 * </p>
 *
 * @author Gajjala Bhavani Shankar
 
 
 */
public class DefaultActionDeserializer implements ActionDeserializer {

    /**
     * Deserializes data using Java's object serialization.
     *
     * @param data The DTO containing the serialized data.
     * @return The reconstituted {@link Action} object.
     * @throws SerializationException if an {@link IOException} or
     * {@link ClassNotFoundException} occurs.
     */
    @Override
    public Action deserialize(final SerializedAction data) throws SerializationException {
        final byte[] bytes = data.getData();
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bis)) {

            final Object obj = ois.readObject();
            String className = "";
            if (obj != null){
                className = obj.getClass().getName();
            }
            else {
                className = "null";
            }

            if (obj instanceof Action) {
                return (Action) obj;
            } else {
                throw new SerializationException("Deserialized object is not of type Action: " 
                + className);
            }

        } catch (IOException | ClassNotFoundException e) {
            throw new SerializationException("Failed to deserialize action", e);
        }
    }
}