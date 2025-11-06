package com.swe.canvas.datamodel.shape;

import com.swe.canvas.datamodel.canvas.CanvasState;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * A type-safe wrapper for a shape's unique identifier.
 *
 * <p>This class encapsulates a String ID (typically a UUID) to prevent
 * accidental misuse of plain strings as identifiers. It is used as the key
 * in the {@link CanvasState}.
 * </p>
 *
 * <p><b>Thread Safety:</b> This class is immutable and therefore thread-safe.</p>
 *
 * @author Gajjala Bhavani Shankar
 
 
 */
public final class ShapeId implements Serializable {

    /**
     * Used for Java serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The underlying string identifier.
     */
    private final String id;

    /**
     * Constructs a ShapeId from a given string.
     *
     * @param id The string ID.
     * @throws NullPointerException if the id is null.
     */
    public ShapeId(final String id) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
    }

    /**
     * Generates a new, random ShapeId using UUID.
     *
     * @return A new ShapeId.
     */
    public static ShapeId randomId() {
        return new ShapeId(UUID.randomUUID().toString());
    }

    /**
     * Gets the raw string value of the ID.
     *
     * @return The string ID.
     */
    public String getValue() {
        return id;
    }

    @Override
    public String toString() {
        return "ShapeId(" + id + ")";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        } 
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ShapeId shapeId = (ShapeId) o;
        return id.equals(shapeId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}