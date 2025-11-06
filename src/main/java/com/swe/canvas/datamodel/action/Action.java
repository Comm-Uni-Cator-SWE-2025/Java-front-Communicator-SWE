package com.swe.canvas.datamodel.action;

import com.swe.canvas.datamodel.canvas.CanvasState;
import com.swe.canvas.datamodel.canvas.ShapeState;
import com.swe.canvas.datamodel.shape.ShapeId;

import java.io.Serializable;
import java.util.Objects;

/**
 * Abstract base class for all operations (Commands) on the canvas.
 *
 * <p>This class implements the <b>Command Pattern</b>. Each action is a
 * self-contained object that describes a single change to the canvas state.
 * </p>
 *
 * <p>It also incorporates the <b>Memento Pattern</b> by storing
 * {@link ShapeState} snapshots:
 * <ul>
 * <li>`prevState`: The state of the shape *before* the action. Used by the
 * Host for validation and conflict detection.</li>
 * <li>`newState`: The state of the shape *after* the action. This is what
 * gets applied to the {@link CanvasState}
 * upon successful validation.</li>
 * </ul>
 * </p>
 *
 * <p><b>Thread Safety:</b> This class is immutable and therefore thread-safe.
 * It is designed to be passed between threads and serialized over the network.
 * </p>
 *
 * <p><b>Design Pattern:</b> Command, Memento</p>
 *
 * @author Gajula Sri Siva Sai Shashank
 */
public abstract class Action implements Serializable {

    /**
     * Used for Java serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier for this specific action instance (e.g., a UUID).
     */
    protected final String actionId;

    /**
     * The ID of the user who initiated this action.
     */
    protected final String userId;

    /**
     * The timestamp of when the action was created by the client.
     */
    protected final long timestamp;

    /**
     * The type of action (CREATE, MODIFY, etc.).
     */
    protected final ActionType actionType;

    /**
     * The ID of the shape this action targets.
     */
    protected final ShapeId shapeId;

    /**
     * The <b>Memento</b> of the state *before* this action.
     * This is {@code null} for {@link CreateShapeAction}.
     */
    protected final ShapeState prevState;

    /**
     * The <b>Memento</b> of the state *after* this action.
     */
    protected final ShapeState newState;

    /**
     * Constructs a new Action.
     *
     * @param actionId   A unique ID for this action.
     * @param userId     The user performing the action.
     * @param timestamp  The time of action creation.
     * @param actionType The type of action.
     * @param shapeId    The target shape's ID.
     * @param prevState  The state before the action (null for CREATE).
     * @param newState   The state after the action.
     */
    protected Action(String actionId, String userId, long timestamp, ActionType actionType,
                     ShapeId shapeId, ShapeState prevState, ShapeState newState) {
        this.actionId = Objects.requireNonNull(actionId, "actionId cannot be null");
        this.userId = Objects.requireNonNull(userId, "userId cannot be null");
        this.timestamp = timestamp;
        this.actionType = Objects.requireNonNull(actionType, "actionType cannot be null");
        this.shapeId = Objects.requireNonNull(shapeId, "shapeId cannot be null");
        // prevState can be null (for CREATE), but newState cannot
        this.prevState = prevState;
        this.newState = Objects.requireNonNull(newState, "newState cannot be null");
    }

    // --- Getters ---

    /**
     * @return The unique action ID.
     */
    public String getActionId() {
        return actionId;
    }

    /**
     * @return The ID of the user who initiated the action.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @return The creation timestamp.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @return The {@link ActionType}.
     */
    public ActionType getActionType() {
        return actionType;
    }

    /**
     * @return The {@link ShapeId} of the target shape.
     */
    public ShapeId getShapeId() {
        return shapeId;
    }

    /**
     * @return The {@link ShapeState} before the action (the "prevState" Memento).
     */
    public ShapeState getPrevState() {
        return prevState;
    }

    /**
     * @return The {@link ShapeState} after the action (the "newState" Memento).
     */
    public ShapeState getNewState() {
        return newState;
    }

    @Override
    public String toString() {
        return String.format("%s[actionId=%s..., user=%s, shapeId=%s...]",
                actionType,
                actionId.substring(0, 8),
                userId,
                shapeId.getValue().substring(0, 8)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Action action = (Action) o;
        // Two actions are identical if their actionId is the same.
        return actionId.equals(action.actionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actionId);
    }
}