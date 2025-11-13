package com.swe.canvas.datamodel.manager;

import com.swe.canvas.datamodel.action.Action;

// import com.swe.canvas.datamodel.action.ActionFactory;
import com.swe.canvas.datamodel.serialization.SerializedAction;

/**
 * Interface defining the core operations for managing action processing.
 *
 * <p>This interface serves as the <b>Strategy</b> pattern. The application
 * will use one of two concrete implementations:
 * <ul>
 * <li>{@link HostActionManager}: For the user who is the authoritative host.</li>
 * <li>{@link ParticipantActionManager}: For all other users.</li>
 * </ul>
 * </p>
 *
 * <p><b>Design Pattern:</b> Strategy</p>
 *
 * @author Gajjala Bhavani Shankar
 
 
 */
public interface ActionManager {

    /**
     * Processes an incoming {@link SerializedAction} from the network.
     *
     * <p>This is the main entry point for actions received from other users
     * (for a Participant) or from participants (for a Host).
     * </p>
     *
     * @param serializedAction The action data from the network queue.
     */
    void processIncomingAction(SerializedAction serializedAction);

    /**
     * Initiates a local action requested by the user's UI.
     *
     * <p>For a Participant, this serializes and sends the action to the Host.
     * For a Host, this validates and applies the action locally, then broadcasts it.
     * </p>
     *
     * @param action The new action created by the {@link ActionFactory}.
     */
    void requestLocalAction(Action action);

    /**
     * Performs a local "undo" operation.
     *
     * <p>This will pop the last action from the undo stack, create its
     * inverse, and submit that inverse action via {@link #requestLocalAction(Action)}.
     * </p>
     */
    void performUndo();

    /**
     * Performs a local "redo" operation.
     *
     * <p>This will pop the last action from the redo stack and resubmit it
     * via {@link #requestLocalAction(Action)}.
     * </p>
     */
    void performRedo();

    /**
     * @summary Retrieves the user's local undo/redo stack manager.
     * @return A reference to the user's local undo/redo stack manager.
     */
    UndoRedoStack getUndoRedoStack();
}