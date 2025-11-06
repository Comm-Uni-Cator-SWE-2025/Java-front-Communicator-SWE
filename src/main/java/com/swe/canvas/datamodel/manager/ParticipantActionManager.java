package com.swe.canvas.datamodel.manager;

import com.swe.canvas.datamodel.action.Action;
import com.swe.canvas.datamodel.action.ActionFactory;
import com.swe.canvas.datamodel.canvas.CanvasState;
import com.swe.canvas.datamodel.serialization.ActionDeserializer;
import com.swe.canvas.datamodel.serialization.ActionSerializer;
import com.swe.canvas.datamodel.serialization.SerializationException;
import com.swe.canvas.datamodel.serialization.SerializedAction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements the {@link ActionManager} strategy for a <b>Participant</b> user.
 *
 * <p>The Participant is not authoritative. Its logic is:
 * <ol>
 * <li><b>Local Action:</b> When the user acts, create an {@link Action},
 * serialize it, and send it to the Host via `sendToHostQueue`.
 * Store the action in a `pendingActions` map.</li>
 * <li><b>Incoming Action:</b> Receive a broadcasted action from the Host.
 * <ul>
 * <li>This action is considered *validated* by the Host. Apply it
 * to the local {@link CanvasState} immediately.</li>
 * <li>Check if the incoming action's ID is in `pendingActions`.</li>
 * <li>If YES: It's our action, confirmed. Move it from `pendingActions`
 * to the `undoRedoStack`.</li>
 * <li>If NO: It's someone else's action. Do *not* add to undo stack.</li>
 * </ul>
 * </li>
 * <li><b>Undo:</b> Pop from local `undoRedoStack`, create inverse,
 * and send to Host (just like a new local action).</li>
 * </ol>
 * </p>
 *
 * <p><b>Thread Safety:</b> This class is thread-safe.
 * It uses {@link ConcurrentHashMap} for pending actions.
 * </p>
 *
 * <p><b>Design Pattern:</b> Strategy</p>
 *
 * @author Gajjala Bhavani Shankar
 
 
 */
public class ParticipantActionManager implements ActionManager {

    /**
     * The participant's local (non-authoritative) copy of the state.
     */
    private final CanvasState canvasState;

    /**
     * The participant's local undo/redo stack.
     */
    private final UndoRedoStack undoRedoStack;

    /**
     * Factory for creating inverse actions (undo/redo).
     */
    private final ActionFactory actionFactory;

    /**
     * Serializer for sending actions to the host.
     */
    private final ActionSerializer serializer;

    /**
     * Deserializer for incoming broadcasts from the host.
     */
    private final ActionDeserializer deserializer;

    /**
     * The output queue to send locally-created actions to the Host.
     */
    private final MessageQueue sendToHostQueue;

    /**
     * The participant's own user ID.
     */
    private final String participantUserId;

    /**
     * Actions created locally and sent to the host, awaiting confirmation.
     * Key: actionId
     * Value: The full Action object
     */
    private final Map<String, Action> pendingActions;

    /**
     * Constructs a new ParticipantActionManager.
     *
     * @param canvasState     The local canvas state.
     * @param undoRedoStack   The local undo/redo stack.
     * @param actionFactory   Factory for creating inverse actions.
     * @param serializer      For sending actions.
     * @param deserializer    For receiving actions.
     * @param sendToHostQueue The message queue to send actions *to* the Host.
     * @param participantUserId The user ID of this participant.
     */
    public ParticipantActionManager(CanvasState canvasState, UndoRedoStack undoRedoStack,
                                    ActionFactory actionFactory, ActionSerializer serializer,
                                    ActionDeserializer deserializer, MessageQueue sendToHostQueue,
                                    String participantUserId) {
        this.canvasState = canvasState;
        this.undoRedoStack = undoRedoStack;
        this.actionFactory = actionFactory;
        this.serializer = serializer;
        this.deserializer = deserializer;
        this.sendToHostQueue = sendToHostQueue;
        this.participantUserId = participantUserId;
        this.pendingActions = new ConcurrentHashMap<>();
    }

    @Override
    public synchronized void processIncomingAction(SerializedAction serializedAction) {
        // This action is from the Host, so it is *always* valid.
        // No validation step is needed.
        try {
            Action action = deserializer.deserialize(serializedAction);
            System.out.printf("[Part. %s] Received broadcast: %s\n", participantUserId, action);

            // 1. Apply to local state
            apply(action);

            // 2. Check if it was one of our pending actions
            Action pending = pendingActions.remove(action.getActionId());

            if (pending != null) {
                // Yes, this is our action, confirmed by the host.
                // We can now add it to our local undo stack.
                System.out.printf("[Part. %s] Confirmed local action. Adding to undo stack.\n", participantUserId);
                undoRedoStack.pushUndo(action);
            } else {
                // This was another user's action. We just apply it.
                // We do NOT add it to our undo stack.
                System.out.printf("[Part. %s] Applying other user's action.\n", participantUserId);
            }

        } catch (SerializationException e) {
            System.err.printf("[Part. %s] Failed to deserialize action: %s\n", participantUserId, e.getMessage());
        }
    }

    @Override
    public void requestLocalAction(Action action) {
        // This is a new action from our local UI.
        // We must send it to the host for validation.
        System.out.printf("[Part. %s] Requesting local action: %s\n", participantUserId, action);
        try {
            // 1. Add to pending map
            pendingActions.put(action.getActionId(), action);

            // 2. Serialize
            SerializedAction serializedAction = serializer.serialize(action);

            // 3. Send to host
            sendToHostQueue.post(serializedAction);
            System.out.printf("[Part. %s] Action sent to host. Awaiting confirmation.\n", participantUserId);

        } catch (SerializationException e) {
            System.err.printf("[Part. %s] Failed to serialize local action: %s\n", participantUserId, e.getMessage());
            pendingActions.remove(action.getActionId()); // Rollback
        } catch (InterruptedException e) {
            System.err.printf("[Part. %s] Failed to send action to host: %s\n", participantUserId, e.getMessage());
            pendingActions.remove(action.getActionId()); // Rollback
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void performUndo() {
        Action actionToUndo = undoRedoStack.popUndo();
        if (actionToUndo != null) {
            System.out.printf("[Part. %s] Performing UNDO on %s.\n", participantUserId, actionToUndo);
            Action inverseAction = actionFactory.createInverseAction(actionToUndo, participantUserId);
            // Request the inverse action (send to host for validation)
            requestLocalAction(inverseAction);
        } else {
            System.out.printf("[Part. %s] Undo stack empty.\n", participantUserId);
        }
    }

    @Override
    public void performRedo() {
        // This logic relies on the UndoRedoStack.popRedo()
        // implementation, which pops from redo and pushes to undo.
        Action actionToRedo = undoRedoStack.popRedo();
        if (actionToRedo != null) {
            System.out.printf("[Part. %s] Performing REDO on %s.\n", participantUserId, actionToRedo);
            // The action is now on the undo stack.
            // We just need to request it again for validation and broadcast.
            // Its prevState *might* be stale, so this is a "best-effort" redo.
            requestLocalAction(actionToRedo);
        } else {
            System.out.printf("[Part. %s] Redo stack empty.\n", participantUserId);
        }
    }

    @Override
    public UndoRedoStack getUndoRedoStack() {
        return this.undoRedoStack;
    }

    /**
     * Applies a broadcasted action to the local state.
     *
     * @param action The validated action from the host.
     */
    private void apply(Action action) {
        canvasState.applyState(action.getShapeId(), action.getNewState());
    }
}