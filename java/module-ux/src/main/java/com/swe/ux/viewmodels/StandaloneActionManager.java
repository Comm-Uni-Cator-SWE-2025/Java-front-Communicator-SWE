package com.swe.ux.viewmodels;

import com.swe.canvas.datamodel.action.Action;
import com.swe.canvas.datamodel.action.ActionFactory;
import com.swe.canvas.datamodel.canvas.CanvasState;
import com.swe.canvas.datamodel.canvas.ShapeState;
import com.swe.canvas.datamodel.collaboration.NetworkMessage;
import com.swe.canvas.datamodel.manager.ActionManager;
import com.swe.canvas.datamodel.manager.UndoRedoManager;
import com.swe.canvas.datamodel.shape.Shape;

/**
 * A simplified ActionManager for single-user mode.
 * Bypasses network queues and validates/applies immediately.
 */
public class StandaloneActionManager implements ActionManager {

    private final CanvasState canvasState;
    private final UndoRedoManager undoRedoManager;
    private final ActionFactory actionFactory;
    private final String userId;
    private Runnable onUpdateCallback = () -> {};

    public StandaloneActionManager(CanvasState canvasState, String userId) {
        this.canvasState = canvasState;
        this.undoRedoManager = new UndoRedoManager();
        this.actionFactory = new ActionFactory();
        this.userId = userId;
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
    public void setOnUpdate(Runnable callback) {
        this.onUpdateCallback = callback != null ? callback : () -> {};
    }

    private void notifyUpdate() {
        onUpdateCallback.run();
    }

    @Override
    public void requestCreate(Shape newShape) {
        Action action = actionFactory.createCreateAction(newShape, userId);
        // In standalone mode, apply immediately
        canvasState.applyState(action.getShapeId(), action.getNewState());
        undoRedoManager.push(action);
        notifyUpdate();
    }

    @Override
    public void requestModify(ShapeState prevState, Shape modifiedShape) {
        Action action = actionFactory.createModifyAction(canvasState, prevState.getShapeId(), modifiedShape, userId);
        // In standalone mode, apply immediately
        canvasState.applyState(action.getShapeId(), action.getNewState());
        undoRedoManager.push(action);
        notifyUpdate();
    }

    @Override
    public void requestDelete(ShapeState shapeToDelete) {
        Action action = actionFactory.createDeleteAction(canvasState, shapeToDelete.getShapeId(), userId);
        // In standalone mode, apply immediately
        canvasState.applyState(action.getShapeId(), action.getNewState());
        undoRedoManager.push(action);
        notifyUpdate();
    }

    @Override
    public void requestUndo() {
        Action actionToUndo = undoRedoManager.getActionToUndo();
        if (actionToUndo != null) {
            Action inverseAction = actionFactory.createInverseAction(actionToUndo, userId);
            // Apply inverse immediately
            canvasState.applyState(inverseAction.getShapeId(), inverseAction.getNewState());
            undoRedoManager.applyHostUndo(); // Move pointer back
            notifyUpdate();
        }
    }

    @Override
    public void requestRedo() {
        Action actionToRedo = undoRedoManager.getActionToRedo();
        if (actionToRedo != null) {
            // Re-apply original action
            canvasState.applyState(actionToRedo.getShapeId(), actionToRedo.getNewState());
            undoRedoManager.applyHostRedo(); // Move pointer forward
            notifyUpdate();
        }
    }

    @Override
    public void processIncomingMessage(NetworkMessage message) {
        // Not used in standalone mode
    }
}

