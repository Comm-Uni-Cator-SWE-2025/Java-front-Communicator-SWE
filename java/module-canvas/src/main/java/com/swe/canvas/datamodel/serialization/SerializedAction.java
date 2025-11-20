package com.swe.canvas.datamodel.serialization;

// import com.swe.canvas.datamodel.action.Action;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A Data Transfer Object (DTO) that wraps the serialized representation
 * of an {@link Action}.
 *
 * <p>This implementation wraps a {@code byte[]} which is the output
 * of Java's {@link java.io.ObjectOutputStream}.
 * </p>
 *
 * <p><b>Thread Safety:</b> This class is immutable and thread-safe.</p>
 *
 * @author Gajjala Bhavani Shankar
 
 
 */
public final class SerializedAction implements Serializable {

    /**
     * Used for Java serialization (e.g., if this object itself
     * needed to be nested, though it's unlikely).
     */
    private static final long serialVersionUID = 1L;

    /**
     * The raw byte data of the serialized action.
     */
    private final byte[] data;

    /**
     * Constructs a new SerializedAction.
     *
     * @param data The raw byte data.
     */
    public SerializedAction(final byte[] data) {
        // Defensive copy for immutability
        this.data = data.clone();
    }

    /**
     * Gets a copy of the raw byte data.
     *
     * @return A copy of the serialized data.
     */
    public byte[] getData() {
        return data.clone();
    }

    @Override
    public String toString() {
        return "SerializedAction[size=" + data.length + " bytes]";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SerializedAction that = (SerializedAction) o;
        return Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }
}