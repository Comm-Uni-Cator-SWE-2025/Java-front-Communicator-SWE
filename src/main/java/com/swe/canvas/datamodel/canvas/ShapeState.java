package com.swe.canvas.datamodel.canvas;

import com.swe.canvas.datamodel.shape.Shape;
import com.swe.canvas.datamodel.shape.ShapeId;
import com.swe.canvas.datamodel.action.Action;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents the complete state of a single shape at a specific moment.
 *
 * <p>This class acts as the <b>Memento</b> in the Memento pattern. It stores a
 * snapshot of a {@link Shape} object, its deletion status (for soft deletes),
 * and a modification timestamp.
 * </p>
 *
 * <p>Instances of this class are stored as the `prevState` and `newState` in
 * {@link Action} objects, allowing for
 * validation, conflict detection, and undo/redo operations.
 * </p>
 *
 * <p><b>Thread Safety:</b> This class is immutable (or effectively immutable
 * as it's intended to be used as a snapshot) and therefore thread-safe.
 * The internal {@link Shape} is a deep copy.
 * </p>
 *
 * <p><b>Design Pattern:</b> Memento, State</p>
 *
 * @author Darla Manohar
 
 
 */
public final class ShapeState implements Serializable {

    /**
     * Used for Java serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * A deep-copy snapshot of the shape's geometry and metadata.
     */
    private final Shape shape;

    /**
     * The soft-delete flag. True if the shape is considered deleted.
     */
    private final boolean isDeleted;

    /**
     * The timestamp of the last modification (in milliseconds).
     */
    private final long lastModified;

    /**
     * Constructs a new ShapeState.
     *
     * @param shape        The shape snapshot. A deep copy MUST be provided.
     * @param isDeleted    The deletion status.
     * @param lastModified The modification timestamp.
     */
    public ShapeState(Shape shape, boolean isDeleted, long lastModified) {
        // We trust the shape is a deep copy, which Shape.copy() ensures.
        this.shape = shape;
        this.isDeleted = isDeleted;
        this.lastModified = lastModified;
    }

    /**
     * @return The shape snapshot.
     */
    public Shape getShape() {
        return shape;
    }

    /**
     * @return The shape's ID.
     */
    public ShapeId getShapeId() {
        return shape != null ? shape.getShapeId() : null;
    }

    /**
     * @return True if the shape is soft-deleted, false otherwise.
     */
    public boolean isDeleted() {
        return isDeleted;
    }

    /**
     * @return The modification timestamp.
     */
    public long getLastModified() {
        return lastModified;
    }

    /**
     * Creates a deep copy of this state.
     *
     * @return A new ShapeState instance with the same data.
     */
    public ShapeState copy() {
        // Shape.copy() creates a new Shape instance
        return new ShapeState(this.shape.copy(), this.isDeleted, this.lastModified);
    }

    @Override
    public String toString() {
        return String.format("ShapeState[shape=%s, deleted=%b, modified=%d]",
                shape, isDeleted, lastModified);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShapeState that = (ShapeState) o;

        // Note: We check shape equality using Objects.equals to handle
        // the null case (e.g., in a CreateAction's prevState).
        return isDeleted == that.isDeleted &&
                lastModified == that.lastModified &&
                Objects.equals(shape, that.shape);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shape, isDeleted, lastModified);
    }
}