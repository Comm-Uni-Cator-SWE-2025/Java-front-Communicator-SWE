/*
 * -----------------------------------------------------------------------------
 * File: ActionDeserializer.java
 * Owner: Gajjala Bhavani Shankar
 * Roll Number : 112201026
 * Module: Canvas
 * -----------------------------------------------------------------------------
 */

package com.swe.canvas.datamodel.serialization;

import com.swe.canvas.datamodel.action.Action;

/**
 * Interface for deserializing a {@link SerializedAction} DTO.
 *
 * <p><b>Design Pattern:</b> Interface Segregation</p>
 *
 * @author Gajjala Bhavani Shankar
 */
@FunctionalInterface
public interface ActionDeserializer {

    /**
     * Deserializes a {@link SerializedAction} DTO back into an {@link Action}.
     *
     * @param data The DTO containing the serialized data.
     * @return The reconstituted {@link Action} object.
     * @throws SerializationException if deserialization fails (e.g., bad data,
     * class not found).
     */
    Action deserialize(SerializedAction data) throws SerializationException;
}