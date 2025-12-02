/*
 * -----------------------------------------------------------------------------
 * File: ClientActionManager.java
 * Owner: Gajjala Bhavani Shankar
 * Roll Number : 112201026
 * Module: Canvas
 * -----------------------------------------------------------------------------
 */

package com.swe.canvas.datamodel.manager;

import com.swe.canvas.datamodel.action.Action;
import com.swe.canvas.datamodel.action.ActionFactory;
import com.swe.canvas.datamodel.canvas.CanvasState;
import com.swe.canvas.datamodel.canvas.ShapeState;
import com.swe.canvas.datamodel.collaboration.MessageType;
import com.swe.canvas.datamodel.collaboration.NetworkMessage;
import com.swe.canvas.datamodel.collaboration.NetworkService;
import com.swe.canvas.datamodel.serialization.NetActionSerializer;
import com.swe.canvas.datamodel.serialization.ShapeSerializer;
import com.swe.canvas.datamodel.shape.Shape;
import com.swe.canvas.datamodel.shape.ShapeId;

import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.controller.RPC;
import com.swe.controller.ClientNode;
import com.swe.controller.serialize.DataSerializer;

/**
 * The ActionManager implementation for the Client role.
 */
public class ClientActionManager implements ActionManager {

    private final String userId;
    private final CanvasState canvasState;
    private final ActionFactory actionFactory;
    private final UndoRedoManager undoRedoManager;
    private final NetworkService networkService;
    private Runnable onUpdateCallback = () -> {};
    private final AbstractRPC rpc;

    public ClientActionManager(final String clientId,
            final CanvasState state,
            final NetworkService netService) {
        this(clientId, state, netService, null);
    }

    public ClientActionManager(final String clientId,
            final CanvasState state,
            final NetworkService netService,
            final AbstractRPC rpcParam) {
        this.userId = clientId;
        this.canvasState = state;
        this.networkService = netService;
        this.actionFactory = new ActionFactory();
        this.undoRedoManager = new UndoRedoManager();

        if (rpcParam != null) {
            this.rpc = rpcParam;
        } else {
            this.rpc = RPC.getInstance();
        }
        this.rpc.subscribe("canvas:update", this::handleUpdate);
    }

    /**
     * Initializes the client by requesting identity and then syncing history.
     * 1. Call "canvas:whoami" to get ClientNode.
     * 2. Send REQUEST_SHAPES with ClientNode payload to Host.
     */
    @Override
    public void initialize() {
        System.out.println("[ClientActionManager] Initializing... requesting whoami.");
        
        CompletableFuture.runAsync(() -> {
            try {
                // 1. Request identity
                byte[] whoAmIResponse = rpc.call("canvas:whoami", new byte[0]).get();
                if (whoAmIResponse == null || whoAmIResponse.length == 0) {
                     System.err.println("[ClientActionManager] Failed to get identity from canvas:whoami");
                     return;
                }

                // 2. Deserialize ClientNode
                ClientNode myClientNode = DataSerializer.deserialize(whoAmIResponse, ClientNode.class);
                if (myClientNode == null) {
                    System.err.println("[ClientActionManager] Deserialized ClientNode is null.");
                    return;
                }

                // 3. Prepare Payload (Serialize ClientNode to JSON string)
                byte[] payloadBytes = DataSerializer.serialize(myClientNode);
                String payloadJson = new String(payloadBytes, StandardCharsets.UTF_8);

                // 4. Create Network Message
                NetworkMessage requestMsg = new NetworkMessage(MessageType.REQUEST_SHAPES, null, payloadJson);

                // 5. Send to Host
                System.out.println("[ClientActionManager] Sending REQUEST_SHAPES to Host.");
                networkService.sendMessageToHost(requestMsg);

            } catch (Exception ex) {
                System.err.println("[ClientActionManager] Initialization failed: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    @Override
    public ActionFactory getActionFactory() { return actionFactory; }

    @Override
    public CanvasState getCanvasState() { return canvasState; }

    @Override
    public UndoRedoManager getUndoRedoManager() { return undoRedoManager; }

    @Override
    public void setOnUpdate(final Runnable callback) {
        if (callback != null) {
            this.onUpdateCallback = callback;
        }
    }

    private void sendActionToHost(final Action action, final MessageType type) {
        try {
            final String serializedAction = NetActionSerializer.serializeAction(action);
            final NetworkMessage message = new NetworkMessage(type, serializedAction.getBytes());
            networkService.sendMessageToHost(message);
        } catch (Exception e) {
            System.err.println("Client failed to send message: " + e.getMessage());
        }
    }

    @Override
    public void requestCreate(final Shape newShape) {
        try {
            final Action action = actionFactory.createCreateAction(newShape, userId);
            sendActionToHost(action, MessageType.NORMAL);
        } catch (Exception e) {
            System.err.println("Client create request failed: " + e.getMessage());
        }
    }

    @Override
    public void requestModify(final ShapeState prevState, final Shape modifiedShape) {
        try {
            final Action action = actionFactory.createModifyAction(
                    canvasState, prevState.getShapeId(), modifiedShape, userId);
            sendActionToHost(action, MessageType.NORMAL);
        } catch (Exception e) {
            System.err.println("Client modify request failed: " + e.getMessage());
        }
    }

    @Override
    public void requestDelete(final ShapeState shapeToDelete) {
        try {
            final Action action = actionFactory.createDeleteAction(
                    canvasState, shapeToDelete.getShapeId(), userId);
            sendActionToHost(action, MessageType.NORMAL);
        } catch (Exception e) {
            System.err.println("Client delete request failed: " + e.getMessage());
        }
    }

    @Override
    public void requestUndo() {
        try {
            final Action action = undoRedoManager.getActionToUndo();
            if (action != null) {
                final Action inverse = actionFactory.createInverseAction(action, userId);
                sendActionToHost(inverse, MessageType.UNDO);
            }
        } catch (Exception e) {
            System.err.println("Client undo request failed: " + e.getMessage());
        }
    }

    @Override
    public void requestRedo() {
        try {
            final Action action = undoRedoManager.getActionToRedo();
            if (action != null) {
                sendActionToHost(action, MessageType.REDO);
            }
        } catch (Exception e) {
            System.err.println("Client redo request failed: " + e.getMessage());
        }
    }

    @Override
    public String saveMap() {
        return ShapeSerializer.serializeShapesMap(canvasState.getAllStates());
    }

    @Override
    public void restoreMap(final String json) {
        System.out.println("[Client] Local restore requested. (No-op in typical flow)");
    }

    @Override
    public byte[] handleUpdate(final byte[] data) {
        String dataString = new String(data, StandardCharsets.UTF_8);
        NetworkMessage msg = NetworkMessage.deserialize(dataString);
        processIncomingMessage(msg);
        return data;
    }

    @Override
    public void handleUserJoined(String userId) {
        // Client does nothing when other users join.
    }

    @Override
    public void processIncomingMessage(final NetworkMessage message) {
        if (message == null) return;

        // 1. Handle RESTORE (Sync from Host)
        if (message.getMessageType() == MessageType.RESTORE) {
            if (message.getPayload() != null) {
                try {
                    System.out.println("[Client] Received RESTORE/SYNC from Host.");
                    final Map<ShapeId, ShapeState> newMap = ShapeSerializer.deserializeShapesMap(message.getPayload());
                    canvasState.setAllStates(newMap);
                    undoRedoManager.clear();
                    onUpdateCallback.run();
                } catch (Exception e) {
                    System.err.println("Client restore failed: " + e.getMessage());
                }
            }
            return;
        }

        // 2. Handle Normal Actions
        try {
            String json = new String(message.getSerializedAction(), StandardCharsets.UTF_8);
            final Action action = NetActionSerializer.deserializeAction(json);

            if (action == null) return;

            final boolean isMyAction = action.getNewState()
                    .getShape().getLastUpdatedBy().equals(userId);

            canvasState.applyState(action.getShapeId(), action.getNewState());

            if (isMyAction) {
                switch (message.getMessageType()) {
                    case NORMAL -> undoRedoManager.push(action);
                    case UNDO -> undoRedoManager.applyHostUndo();
                    case REDO -> undoRedoManager.applyHostRedo();
                    default -> { }
                }
            }
            onUpdateCallback.run();
        } catch (Exception e) {
            System.err.println("Client failed to process message: " + e.getMessage());
        }
    }
}