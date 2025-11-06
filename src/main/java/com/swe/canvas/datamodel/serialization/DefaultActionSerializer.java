package com.swe.canvas.datamodel.serialization;

import com.swe.canvas.datamodel.action.Action;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Default implementation of {@link ActionSerializer}.
 *
 * <p>This implementation uses Java's built-in {@link ObjectOutputStream}
 * to serialize {@link Action} objects, which must all implement
 * {@link java.io.Serializable}.
 * </p>
 *
 * <p><b>Thread Safety:</b> This class is stateless and thread-safe.
 * New streams are created for each operation.
 * </p>
 *
 * @author Gajjala Bhavani Shankar
 
 
 */
public class DefaultActionSerializer implements ActionSerializer {

    /**
     * Serializes an action using Java's object serialization.
     *
     * @param action The action object to serialize.
     * @return A DTO containing the serialized byte array.
     * @throws SerializationException if an {@link IOException} occurs.
     */
    @Override
    public SerializedAction serialize(Action action) throws SerializationException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {

            oos.writeObject(action);
            oos.flush();
            byte[] data = bos.toByteArray();
            return new SerializedAction(data);

        } catch (IOException e) {
            throw new SerializationException("Failed to serialize action: " + action.getActionId(), e);
        }
    }
}