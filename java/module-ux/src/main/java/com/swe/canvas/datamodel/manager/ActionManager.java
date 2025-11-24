package com.swe.canvas.datamodel.manager;

import com.swe.canvas.datamodel.action.ActionFactory;
import com.swe.canvas.datamodel.canvas.CanvasState;
import com.swe.canvas.datamodel.canvas.ShapeState;
import com.swe.canvas.datamodel.collaboration.NetworkMessage;
import com.swe.canvas.datamodel.shape.Shape;

/**
 * Interface defining the core operations for managing actions.
 * This will be implemented by ClientActionManager, HostActionManager,
 * and the existing StandaloneActionManager.
 */
public interface ActionManager {

    /**
     * Gets the central ActionFactory.
     */
    ActionFactory getActionFactory();

    /**
     * Gets the local CanvasState (mirror for client, authority for host).
     */
    CanvasState getCanvasState();
    
    /**
     * Gets the local UndoRedoManager.
     */
    UndoRedoManager getUndoRedoManager();

    /**
     * Sets the callback to trigger a UI redraw.
     */
    void setOnUpdate(Runnable callback);

    // --- Local User Action Requests ---

    void requestCreate(Shape newShape);
    void requestModify(ShapeState prevState, Shape modifiedShape);
    void requestDelete(ShapeState shapeToDelete);
    void requestUndo();
    void requestRedo();

    // --- Network-facing Method ---

    /**
     * Processes an incoming message from the network.
     * This is the ProcessIncomingMessage() function you requested.
     */
    void processIncomingMessage(NetworkMessage message);
}