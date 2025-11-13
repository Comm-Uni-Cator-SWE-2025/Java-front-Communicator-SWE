package com.swe.canvas.datamodel.canvas;

import com.swe.canvas.datamodel.shape.Shape;
import com.swe.canvas.datamodel.shape.ShapeId;

// import com.swe.canvas.datamodel.action.Action;
// import com.swe.canvas.datamodel.manager.ActionManager;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Manages the complete, concurrent state of the canvas.
 *
 * <p>This class is the single source of truth for the current state of all shapes
 * on the canvas. It uses a {@link ConcurrentHashMap} to store
 * {@link ShapeState} objects, keyed by their {@link ShapeId}.
 * </p>
 *
 * <p>All operations that read or write to the canvas state must go through this
 * class to ensure thread safety. The {@link ActionManager}
 * uses this class for action validation and application.
 * </p>
 *
 * <p><b>Thread Safety:</b> This class is thread-safe.
 * It uses {@link ConcurrentMap} for atomic, thread-safe operations
 * on the underlying state map.
 * </p>
 *
 * @author Darla Manohar
 
 
 */
public class CanvasState {

    /**
     * The core data structure holding the state of all shapes.
     * Key: ShapeId
     * Value: The complete state (Shape object + metadata) for that ID.
     */
    private final ConcurrentMap<ShapeId, ShapeState> state;

    /**
     * Constructs a new, empty CanvasState.
     */
    public CanvasState() {
        this.state = new ConcurrentHashMap<>();
    }

    /**
     * Retrieves the current state for a given shape.
     *
     * @param shapeId The ID of the shape to retrieve.
     * @return The current {@link ShapeState}, or {@code null} if the shape
     * does not exist in the state map.
     */
    public ShapeState getShapeState(final ShapeId shapeId) {
        return state.get(shapeId);
    }

    /**
     * Applies a new state for a shape.
     *
     * <p>This method is used by the ActionManager to commit the `newState`
     * of a validated {@link Action}. It will
     * atomically insert or replace the state associated with the shape ID.
     * </p>
     *
     * @param shapeId    The ID of the shape to update.
     * @param newState   The new state to apply.
     */
    public void applyState(final ShapeId shapeId, final ShapeState newState) {
        Objects.requireNonNull(shapeId, "shapeId cannot be null");
        Objects.requireNonNull(newState, "newState cannot be null");
        state.put(shapeId, newState);
    }

    /**
     * Gets a collection of all *visible* (not deleted) shapes.
     *
     * <p>This method is intended for use by the Rendering team. It provides
     * a snapshot of all shapes that should currently be drawn on the canvas.
     * </p>
     *
     * @return An immutable collection of {@link Shape} objects.
     */
    public Collection<Shape> getVisibleShapes() {
        return state.values().stream()
                .filter(shapeState -> !shapeState.isDeleted())
                .map(ShapeState::getShape)
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Clears all state from the canvas.
     */
    public void clear() {
        state.clear();
    }

    private static final int SUBSTRING_LENGTH = 8;

    /**
     * Provides a string representation of the current state for debugging.
     *
     * @return A string summary of the canvas state.
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CanvasState:\n");
        if (state.isEmpty()) {
            sb.append("  [Empty]\n");
        }
        for (Map.Entry<ShapeId, ShapeState> entry : state.entrySet()) {
            sb.append(String.format("  - ID: %s... | State: %s\n",
                    entry.getKey().getValue().substring(0, SUBSTRING_LENGTH),
                    entry.getValue().toString()));
        }
        return sb.toString();
    }
}