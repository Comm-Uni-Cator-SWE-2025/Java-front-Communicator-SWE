package com.swe.canvas.datamodel.manager;

import java.util.Objects;

import com.swe.canvas.datamodel.action.Action;
import com.swe.canvas.datamodel.action.ActionFactory;
import com.swe.canvas.datamodel.action.ActionType;
import com.swe.canvas.datamodel.canvas.CanvasState;
import com.swe.canvas.datamodel.canvas.ShapeState;
import com.swe.canvas.datamodel.collaboration.CanvasNetworkService;
import com.swe.canvas.datamodel.collaboration.MessageType;
import com.swe.canvas.datamodel.collaboration.NetworkMessage;
import com.swe.canvas.datamodel.collaboration.NetworkService;
import com.swe.canvas.datamodel.serialization.NetActionSerializer;
import com.swe.canvas.datamodel.serialization.SerializationException;
import com.swe.canvas.datamodel.shape.Shape;
import com.swe.controller.RPCinterface.AbstractRPC;

/**
 * Implements the host-side logic for collaboration, as specified in prompt.
 * - Owns the authoritative (master) CanvasState.
 * - Validates all incoming actions from clients.
 * - Rejects actions that have conflicts (stale prevState).
 * - Broadcasts all *valid* actions to all clients.
 */
public class HostActionManager implements ActionManager {

    private final String userId; // Host's own user ID
    private final CanvasState canvasState; // Authoritative state
    private final ActionFactory actionFactory;
    private final UndoRedoManager undoRedoManager; // Host's local undo/redo
    private final NetworkService networkService;
    
    private Runnable onUpdateCallback = () -> {}; // No-op default


    public HostActionManager(final String userId, final CanvasState canvasState, final NetworkService networkService) {
        this.userId = userId;
        this.canvasState = canvasState;
        this.networkService = networkService;
        this.actionFactory = new ActionFactory();
        this.undoRedoManager = new UndoRedoManager();
        this.networkService.registerHostHandler(this::processIncomingMessage);
    }

    public HostActionManager(final String userId, final CanvasState canvasState,
                             final AbstractRPC rpc) {
        this(userId, canvasState, new CanvasNetworkService(rpc));
    }

    @Override
    public ActionFactory getActionFactory() { return actionFactory; }
    @Override
    public CanvasState getCanvasState() { return canvasState; }
    @Override
    public UndoRedoManager getUndoRedoManager() { return undoRedoManager; }
    @Override
    public void setOnUpdate(Runnable callback) { this.onUpdateCallback = callback; }

    /**
     * Validates an action against the authoritative state.
     * This is the core logic from the prompt.
     */
    private boolean validate(Action action) {
        // As per prompt: "if the action type is CREATE directly he will create"
        if (action.getActionType() == ActionType.CREATE) {
            // A more robust check is to ensure it doesn't already exist,
            // but following the prompt, we auto-validate CREATE.
            return true;
        }

        // For MODIFY, DELETE, RESURRECT (from UNDO/REDO), we do the full prevState check.
        ShapeState currentState = canvasState.getShapeState(action.getShapeId());
        ShapeState actionPrevState = action.getPrevState();

        // This Objects.equals() check is the "Optimistic Concurrency" check.
        if (Objects.equals(currentState, actionPrevState)) {
            return true;
        } else {
            // CONFLICT!
            System.err.println("[Host] CONFLICT DETECTED for Action: " + action.getActionId());
            System.err.println("  Action's prevState: " + actionPrevState);
            System.err.println("  Host's currentState: " + currentState);
            return false;
        }
    }

    /**
     * Helper to apply a valid action and broadcast it.
     */
    private void applyAndBroadcast(Action action, NetworkMessage originalMessage) {
        // 1. Apply to host's authoritative state
        canvasState.applyState(action.getShapeId(), action.getNewState());
        
        // 2. Broadcast the *original* message to all clients
        networkService.broadcastMessage(originalMessage);
    }

    // --- Local User Action Requests (Host is also a user) ---
    // The Host's actions are just local requests that are auto-validated.

    @Override
    public void requestCreate(Shape newShape) {
        Action action = actionFactory.createCreateAction(newShape, userId);
        try {
            String sa = NetActionSerializer.serializeAction(action);
            NetworkMessage msg = new NetworkMessage(MessageType.NORMAL, sa.getBytes());
            // Host processes its own message (which applies state and broadcasts)
            processIncomingMessage(msg);
        } catch (SerializationException e) {
            System.err.println("Host failed to serialize local action: " + e.getMessage());
        }
    }

    @Override
    public void requestModify(ShapeState prevState, Shape modifiedShape) {
        Action action = actionFactory.createModifyAction(canvasState, prevState.getShapeId(), modifiedShape, userId);
        try {
            String sa = NetActionSerializer.serializeAction(action);
            NetworkMessage msg = new NetworkMessage(MessageType.NORMAL, sa.getBytes());
            processIncomingMessage(msg);
        } catch (Exception e) {
            System.err.println("Host failed to serialize local action: " + e.getMessage());
        }
    }

    @Override
    public void requestDelete(ShapeState shapeToDelete) {
        Action action = actionFactory.createDeleteAction(canvasState, shapeToDelete.getShapeId(), userId);
        try {
            String sa = NetActionSerializer.serializeAction(action);
            NetworkMessage msg = new NetworkMessage(MessageType.NORMAL, sa.getBytes());
            processIncomingMessage(msg);
        } catch (Exception e) {
            System.err.println("Host failed to serialize local action: " + e.getMessage());
        }
    }

    @Override
    public void requestUndo() {
        Action actionToUndo = undoRedoManager.getActionToUndo(); // Gets action
        if (actionToUndo != null) {
            Action inverseAction = actionFactory.createInverseAction(actionToUndo, userId);
            try {
                String sa = NetActionSerializer.serializeAction(inverseAction);
                NetworkMessage msg = new NetworkMessage(MessageType.UNDO, sa.getBytes());
                // Host processes its own undo, which validates, applies, and broadcasts
                processIncomingMessage(msg);
            } catch (Exception e) {
                System.err.println("Host failed to serialize local undo: " + e.getMessage());
            }
        }
    }

    @Override
    public void requestRedo() {
        Action actionToRedo = undoRedoManager.getActionToRedo(); // Gets action
        if (actionToRedo != null) {
            try {
                String sa = NetActionSerializer.serializeAction(actionToRedo);
                NetworkMessage msg = new NetworkMessage(MessageType.REDO, sa.getBytes());
                processIncomingMessage(msg);
            } catch (Exception e) {
                System.err.println("Host failed to serialize local redo: " + e.getMessage());
            }
        }
    }

    // --- Network-facing Method (as specified in prompt) ---

    @Override
    public void processIncomingMessage(NetworkMessage message) {
        try {
            Action action = NetActionSerializer.deserializeAction(new String(message.getSerializedAction()));
            if (action == null) return;
            
            boolean isHostSelfAction = action.getNewState().getShape().getLastUpdatedBy().equals(userId);

            // Host-side validation logic
            if (validate(action)) {
                System.out.println("[Host] Action Validated. Applying and Broadcasting.");
                
                // If it's the Host's *own* action, manage its undo stack
                if (isHostSelfAction) {
                    switch (message.getMessageType()) {
                        case NORMAL:
                            undoRedoManager.push(action);
                            break;
                        case UNDO:
                            undoRedoManager.applyHostUndo(); // Move pointer back
                            break;
                        case REDO:
                            undoRedoManager.applyHostRedo(); // Move pointer forward
                            break;
                    }
                }
                
                // Apply and broadcast to everyone (including self and other clients)
                applyAndBroadcast(action, message);

            } else {
                // CONFLICT: Action is invalid (stale prevState)
                System.err.println("[Host] Action REJECTED due to conflict. No broadcast.");
                // We do nothing, as requested in the prompt.
            }

            // Redraw the Host's local UI
            onUpdateCallback.run();

        } catch (Exception e) {
            System.err.println("Host failed to process message: " + e.getMessage());
        }
    }
}
