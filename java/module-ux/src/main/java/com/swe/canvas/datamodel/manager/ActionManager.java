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
     *
     * @return The ActionFactory instance.
     */
    ActionFactory getActionFactory();

    /**
     * Gets the local CanvasState (mirror for client, authority for host).
     *
     * @return The CanvasState instance.
     */
    CanvasState getCanvasState();
    
    /**
     * Gets the local UndoRedoManager.
     *
     * @return The UndoRedoManager instance.
     */
    UndoRedoManager getUndoRedoManager();

    /**
     * Sets the callback to trigger a UI redraw.
     *
     * @param callback The callback to invoke when updates occur.
     */
    void setOnUpdate(Runnable callback);

    // --- Local User Action Requests ---

    /**
     * Requests creation of a new shape.
     *
     * @param newShape The shape to create.
     */
    void requestCreate(Shape newShape);

    /**
     * Requests modification of an existing shape.
     *
     * @param prevState The previous state of the shape.
     * @param modifiedShape The modified shape.
     */
    void requestModify(ShapeState prevState, Shape modifiedShape);

    /**
     * Requests deletion of a shape.
     *
     * @param shapeToDelete The shape to delete.
     */
    void requestDelete(ShapeState shapeToDelete);

    /**
     * Requests an undo operation.
     */
    void requestUndo();

    /**
     * Requests a redo operation.
     */
    void requestRedo();

    // --- Network-facing Method ---

    /**
     * Processes an incoming message from the network.
     * This is the ProcessIncomingMessage() function you requested.
     *
     * @param message The network message to process.
     */
    void processIncomingMessage(NetworkMessage message);
}