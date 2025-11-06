package com.swe.canvas.datamodel.serialization;

import com.swe.canvas.datamodel.action.Action;

/**
 * Interface for serializing an {@link Action} object.
 *
 * <p><b>Design Pattern:</b> Interface Segregation</p>
 *
 * @author Gajjala Bhavani Shankar
 
 
 */
@FunctionalInterface
public interface ActionSerializer {

    /**
     * Serializes an {@link Action} into a {@link SerializedAction} DTO.
     *
     * @param action The action object to serialize.
     * @return A DTO containing the serialized data.
     * @throws SerializationException if serialization fails.
     */
    SerializedAction serialize(Action action) throws SerializationException;
}