package com.swe.canvas.datamodel.manager;

import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.canvas.datamodel.action.Action;
import com.swe.canvas.datamodel.action.ActionFactory;
import com.swe.canvas.datamodel.canvas.CanvasState;
import com.swe.canvas.datamodel.canvas.ShapeState;
import com.swe.canvas.datamodel.collaboration.CanvasNetworkService;
import com.swe.canvas.datamodel.collaboration.MessageType;
import com.swe.canvas.datamodel.collaboration.NetworkMessage;
import com.swe.canvas.datamodel.collaboration.NetworkService;
import com.swe.canvas.datamodel.serialization.NetActionSerializer;
import com.swe.canvas.datamodel.shape.Shape;

/**
 * Replaces ParticipantActionManager.
 * Implements the client-side logic from the prompt.
 * - Sends all local actions to the host for validation.
 * - Does not modify its own state until a broadcast is received.
 * - Manages a local undo/redo stack only for its *own* confirmed actions.
 *
 * @author Canvas Team
 */
public class ClientActionManager implements ActionManager {

    /** User ID for this client. */
    private final String userId;
    /** Local mirror of the canvas state. */
    private final CanvasState canvasState;
    /** Factory for creating action instances. */
    private final ActionFactory actionFactory;
    /** Manager for undo/redo operations. */
    private final UndoRedoManager undoRedoManager;
    /** Network service for communicating with the host. */
    private final NetworkService networkService;
    /** Callback to invoke when updates occur. */
    private Runnable onUpdateCallback = () -> { }; // No-op default

    /**
     * Creates a new client action manager.
     *
     * @param userIdentifier User ID for this client.
     * @param canvas Local canvas state.
     * @param network Network service for communication.
     */
    public ClientActionManager(final String userIdentifier, final CanvasState canvas,
                               final NetworkService network) {
        this.userId = userIdentifier;
        this.canvasState = canvas;
        this.networkService = network;
        this.actionFactory = new ActionFactory();
        this.undoRedoManager = new UndoRedoManager();
        this.networkService.registerClientHandler(this::processIncomingMessage);
    }

    /**
     * Creates a new client action manager with RPC.
     *
     * @param userIdentifier User ID for this client.
     * @param canvas Local canvas state.
     * @param rpc RPC service for communication.
     */
    public ClientActionManager(final String userIdentifier, final CanvasState canvas,
                               final AbstractRPC rpc) {
        this(userIdentifier, canvas, new CanvasNetworkService(rpc));
    }

    @Override
    public ActionFactory getActionFactory() { 
        return actionFactory; 
    }
    
    @Override
    public CanvasState getCanvasState() { 
        return canvasState; 
    }
    
    @Override
    public UndoRedoManager getUndoRedoManager() { 
        return undoRedoManager; 
    }
    
    @Override
    public void setOnUpdate(final Runnable callback) { 
        this.onUpdateCallback = callback; 
    }

    /**
     * Serializes an action, wraps it in a message, and sends it to the host.
     * @param action The action to send.
     * @param type The message type.
     */
    private void sendActionToHost(final Action action, final MessageType type) {
        try {
            final String serializedAction = NetActionSerializer.serializeAction(action);
            final NetworkMessage message = new NetworkMessage(type, serializedAction.getBytes());
            networkService.sendMessageToHost(message);
        } catch (final Exception e) {
            System.err.println("Client failed to send action: " + e.getMessage());
        }
    }

    // --- Local User Action Requests (as specified in prompt) ---

    @Override
    public void requestCreate(final Shape newShape) {
        final Action action = actionFactory.createCreateAction(newShape, userId);
        sendActionToHost(action, MessageType.NORMAL);
    }

    @Override
    public void requestModify(final ShapeState prevState, final Shape modifiedShape) {
        final Action action = actionFactory.createModifyAction(canvasState, prevState.getShapeId(), 
                modifiedShape, userId);
        sendActionToHost(action, MessageType.NORMAL);
    }

    @Override
    public void requestDelete(final ShapeState shapeToDelete) {
        final Action action = actionFactory.createDeleteAction(canvasState, shapeToDelete.getShapeId(), userId);
        sendActionToHost(action, MessageType.NORMAL);
    }

    @Override
    public void requestUndo() {
        final Action actionToUndo = undoRedoManager.getActionToUndo(); // Gets action, does NOT move pointer
        if (actionToUndo != null) {
            // Create the inverse action and send it
            final Action inverseAction = actionFactory.createInverseAction(actionToUndo, userId);
            sendActionToHost(inverseAction, MessageType.UNDO);
        } else {
            System.out.println("[Client " + userId + "] Nothing to undo.");
        }
    }

    @Override
    public void requestRedo() {
        final Action actionToRedo = undoRedoManager.getActionToRedo(); // Gets action, does NOT move pointer
        if (actionToRedo != null) {
            // Send the original action again for re-application
            sendActionToHost(actionToRedo, MessageType.REDO);
        } else {
            System.out.println("[Client " + userId + "] Nothing to redo.");
        }
    }

    // --- Network-facing Method (as specified in prompt) ---

    @Override
    public void processIncomingMessage(final NetworkMessage message) {
        
        
        System.out.println("[Client " + userId + "] Processing incoming message...");
        try {
            final String data = new String(message.getSerializedAction(), "UTF-8");
            final Action action = NetActionSerializer.deserializeAction(data);
            if (action == null) {
                return;
            }

            // This is the key logic from the prompt
            final boolean isMyAction = action.getNewState().getShape().getLastUpdatedBy().equals(userId);
            
            System.out.println("[Client " + userId + "] Received broadcast. IsMyAction=" + isMyAction);

            // 1. ALWAYS apply the change to the local state (it's from the host)
            canvasState.applyState(action.getShapeId(), action.getNewState());

            // 2. Conditionally manage the local undo/redo stack
            if (isMyAction) {
                switch (message.getMessageType()) {
                    case NORMAL:
                        // This is confirmation of our action. Add it to history.
                        System.out.println("[Client " + userId + "] My NORMAL action confirmed. Pushing to stack.");
                        undoRedoManager.push(action);
                        break;
                    case UNDO:
                        // This is confirmation of our undo. Move pointer back.
                        System.out.println("[Client " + userId + "] My UNDO confirmed. Fixing stack.");
                        undoRedoManager.applyHostUndo();
                        break;
                    case REDO:
                        // This is confirmation of our redo. Move pointer forward.
                        System.out.println("[Client " + userId + "] My REDO confirmed. Fixing stack.");
                        undoRedoManager.applyHostRedo();
                        break;
                    default:
                        break;
                }
            } else {
                // This is someone else's action. We just apply it and do nothing
                // to our local undo/redo history.
                System.out.println("[Client " + userId + "] Other user's action. Applying state, not touching stack.");
            }

            // 3. Redraw the UI
            onUpdateCallback.run();

        } catch (final Exception e) {
            System.err.println("Client failed to process message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
