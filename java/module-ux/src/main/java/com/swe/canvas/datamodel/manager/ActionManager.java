/*
 * -----------------------------------------------------------------------------
 * File: ActionManager.java
 * Owner: Gajjala Bhavani Shankar
 * Roll Number : 112201026
 * Module: Canvas
 * -----------------------------------------------------------------------------
 */

package com.swe.canvas.datamodel.manager;

import com.swe.canvas.datamodel.action.ActionFactory;
import com.swe.canvas.datamodel.canvas.CanvasState;
import com.swe.canvas.datamodel.canvas.ShapeState;
import com.swe.canvas.datamodel.collaboration.NetworkMessage;
import com.swe.canvas.datamodel.shape.Shape;

/**
 * Interface defining the contract for managing canvas actions.
 */
public interface ActionManager {

    /**
     * Initializes the manager. 
     * For clients, this triggers the handshake to fetch initial state from the Host.
     */
    void initialize();

    ActionFactory getActionFactory();

    CanvasState getCanvasState();

    UndoRedoManager getUndoRedoManager();

    void setOnUpdate(Runnable callback);

    void requestCreate(Shape newShape);

    void requestModify(ShapeState prevState, Shape modifiedShape);

    void requestDelete(ShapeState shapeToDelete);

    void requestUndo();

    void requestRedo();

    String saveMap();

    void restoreMap(String json);

    void processIncomingMessage(NetworkMessage message);

    byte[] handleUpdate(byte[] data);

    void handleUserJoined(String userId);
}