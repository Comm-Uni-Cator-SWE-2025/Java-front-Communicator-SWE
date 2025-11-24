package com.swe.canvas.datamodel.canvas;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import com.swe.canvas.datamodel.shape.Shape;
import com.swe.canvas.datamodel.shape.ShapeId;

/**
 * Manages the complete, concurrent state of the canvas.
 * (NOW INCLUDES A CALLBACK TO NOTIFY THE UI OF UPDATES)
 *
 * @author Darla Manohar
 */
public class CanvasState {

    /**
     * The core data structure holding the state of all shapes.
     */
    private final ConcurrentMap<ShapeId, ShapeState> state;

    /**
     * A callback to notify the UI that it needs to redraw.
     * This is set by the ActionManager.
     */
    private Runnable onUpdateCallback = () -> {};

    /**
     * Constructs a new, empty CanvasState.
     */
    public CanvasState() {
        this.state = new ConcurrentHashMap<>();
    }
    
    /**
     * Sets the callback function to be triggered when the state changes.
     * This is set by the ActionManager.
     * @param onUpdate The callback to invoke when state changes.
     */
    public void setOnUpdate(final Runnable onUpdate) {
        if (onUpdate != null) {
            this.onUpdateCallback = onUpdate;
        } else {
            this.onUpdateCallback = () -> {};
        }
    }
    
    /**
     * Notifies the listener (the UI) that a redraw is needed.
     * This is the method that was missing.
     */
    public void notifyUpdate() {
        this.onUpdateCallback.run();
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