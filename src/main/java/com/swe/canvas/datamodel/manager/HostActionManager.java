package com.swe.canvas.datamodel.manager;

import com.swe.canvas.datamodel.action.Action;
import com.swe.canvas.datamodel.action.ActionFactory;
import com.swe.canvas.datamodel.canvas.CanvasState;
import com.swe.canvas.datamodel.canvas.ShapeState;
import com.swe.canvas.datamodel.serialization.ActionDeserializer;
import com.swe.canvas.datamodel.serialization.ActionSerializer;
import com.swe.canvas.datamodel.serialization.SerializationException;
import com.swe.canvas.datamodel.serialization.SerializedAction;

import java.util.Objects;

/**
 * Implements the {@link ActionManager} strategy for the <b>Host</b> user.
 *
 * <p>The Host is the authoritative source of truth. Its logic is:
 * <ol>
 * <li>Receive an action (from a participant or itself).</li>
 * <li><b>Validate:</b> Check if `action.prevState` *exactly* matches the
 * `currentState` in its {@link CanvasState}.</li>
 * <li>If validation fails: Drop the action (conflict detected).</li>
 * <li>If validation passes: Apply the `action.newState` to the local
 * {@link CanvasState}, add the action to the local undo stack (if it's
 * the host's own action), and broadcast the *same* serialized action
 * to all participants.</li>
 * </ol>
 * </p>
 *
 * <p><b>Thread Safety:</b> This class is thread-safe.
 * It coordinates access to shared state objects (CanvasState, UndoRedoStack)
 * and uses synchronized methods for critical sections.
 * </p>
 *
 * <p><b>Design Pattern:</b> Strategy</p>
 *
 * @author Gajjala Bhavani Shankar
 
 
 */
public class HostActionManager implements ActionManager {

    /**
     * The authoritative source of truth for the canvas.
     */
    private final CanvasState canvasState;

    /**
     * The host's local undo/redo stack.
     */
    private final UndoRedoStack undoRedoStack;

    /**
     * Factory for creating inverse actions (undo/redo).
     */
    private final ActionFactory actionFactory;

    /**
     * Serializer for broadcasting actions.
     */
    private final ActionSerializer serializer;

    /**
     * Deserializer for incoming actions.
     */
    private final ActionDeserializer deserializer;

    /**
     * The output queue to broadcast validated actions to all participants.
     */
    private final MessageQueue broadcastQueue;

    /**
     * The host's own user ID.
     */
    private final String hostUserId;

    /**
     * Constructs a new HostActionManager.
     *
     * @param canvasState    The single, authoritative canvas state.
     * @param undoRedoStack  The host's local undo/redo stack.
     * @param actionFactory  Factory for creating inverse actions.
     * @param serializer     For broadcasting actions.
     * @param deserializer   For receiving actions.
     * @param broadcastQueue The message queue to send actions to all participants.
     * @param hostUserId     The user ID of the host.
     */
    public HostActionManager(final CanvasState canvasState, final UndoRedoStack undoRedoStack,
                             final ActionFactory actionFactory, final ActionSerializer serializer,
                             final ActionDeserializer deserializer, final MessageQueue broadcastQueue,
                             final String hostUserId) {
        this.canvasState = canvasState;
        this.undoRedoStack = undoRedoStack;
        this.actionFactory = actionFactory;
        this.serializer = serializer;
        this.deserializer = deserializer;
        this.broadcastQueue = broadcastQueue;
        this.hostUserId = hostUserId;
    }

    @Override
    public synchronized void processIncomingAction(final SerializedAction serializedAction) {
        try {
            final Action action = deserializer.deserialize(serializedAction);

            System.out.printf("[Host] Received %s from %s.\n", action, action.getUserId());

            if (validate(action)) {
                System.out.println("[Host] Validation SUCCESS. Applying and broadcasting.");
                // 1. Apply to local state
                apply(action);

                // 2. Add to host's own undo stack
                // Only add if the action was from the host
                if (action.getUserId().equals(hostUserId)) {
                    // This push clears the redo stack
                    undoRedoStack.pushUndo(action);
                }

                // 3. Broadcast the *original* serialized action
                broadcast(serializedAction);
            } else {
                // Conflict detected
                System.out.printf("[Host] Validation FAILED (Conflict). Dropping %s.\n", action);
                // Silently drop the action
            }
        } catch (SerializationException e) {
            System.err.println("[Host] Failed to deserialize action: " + e.getMessage());
        }
    }

    @Override
    public void requestLocalAction(final Action action) {
        // A local action from the host is just serialized and fed back
        // into its own processing queue.
        System.out.printf("[Host] Requesting local action: %s\n", action);
        try {
            final SerializedAction serializedAction = serializer.serialize(action);
            // Process it as if it came from the network
            processIncomingAction(serializedAction);
        } catch (SerializationException e) {
            System.err.println("[Host] Failed to serialize local action: " + e.getMessage());
        }
    }

    @Override
    public void performUndo() {
        final Action actionToUndo = undoRedoStack.popUndo();
        if (actionToUndo != null) {
            System.out.printf("[Host] Performing UNDO on %s.\n", actionToUndo);
            final Action inverseAction = actionFactory.createInverseAction(actionToUndo, hostUserId);
            // Request the inverse action
            requestLocalAction(inverseAction);
        } else {
            System.out.println("[Host] Undo stack empty.");
        }
    }

    @Override
    public void performRedo() {
        // This logic relies on the UndoRedoStack.popRedo()
        // implementation, which pops from redo and pushes to undo.
        final Action actionToRedo = undoRedoStack.popRedo();
        if (actionToRedo != null) {
            System.out.printf("[Host] Performing REDO on %s.\n", actionToRedo);
            // The action is now on the undo stack.
            // We just need to request it again for validation and broadcast.
            // Its prevState *might* be stale, so this is a "best-effort" redo.
            // A more robust system would re-base the action.
            requestLocalAction(actionToRedo);
        } else {
            System.out.println("[Host] Redo stack empty.");
        }
    }

    @Override
    public UndoRedoStack getUndoRedoStack() {
        return this.undoRedoStack;
    }

    /**
     * Applies a validated action to the state.
     *
     * @param action The action to apply.
     */
    private void apply(final Action action) {
        canvasState.applyState(action.getShapeId(), action.getNewState());
    }

    /**
     * Broadcasts a validated action to all participants.
     *
     * @param serializedAction The action to send.
     */
    private void broadcast(final SerializedAction serializedAction) {
        try {
            broadcastQueue.post(serializedAction);
        } catch (InterruptedException e) {
            System.err.println("[Host] Broadcast interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * The core validation logic for the Host.
     *
     * @param action The action to validate.
     * @return True if the action is valid, false if a conflict is detected.
     */
    private boolean validate(final Action action) {
        final ShapeState currentState = canvasState.getShapeState(action.getShapeId());
        final ShapeState actionPrevState = action.getPrevState();

        // Host validation is a direct, exact comparison.
        // Objects.equals handles null cases correctly.
        //
        // Case 1: CREATE action
        //   - action.prevState is null
        //   - currentState must also be null (shape shouldn't exist)
        // Case 2: MODIFY/DELETE/RESURRECT
        //   - action.prevState is non-null
        //   - currentState must be non-null and *exactly equal*

        final boolean isValid = Objects.equals(actionPrevState, currentState);

        if (!isValid) {
            System.err.println("[Host] Validation Conflict!");
            System.err.println("  Action PrevState: " + actionPrevState);
            System.err.println("  Canvas CurrState: " + currentState);
        }

        return isValid;
    }
}