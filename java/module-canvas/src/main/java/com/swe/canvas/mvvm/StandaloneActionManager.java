package com.swe.canvas.mvvm;

import com.swe.canvas.datamodel.action.Action;
import com.swe.canvas.datamodel.action.ActionFactory;
import com.swe.canvas.datamodel.canvas.CanvasState;
import com.swe.canvas.datamodel.manager.ActionManager;
import com.swe.canvas.datamodel.manager.UndoRedoStack;
import com.swe.canvas.datamodel.serialization.SerializedAction;

/**
 * A simplified ActionManager for single-user mode.
 * Bypasses network queues and validates/applies immediately.
 */
public class StandaloneActionManager implements ActionManager {

    private final CanvasState canvasState;
    private final UndoRedoStack undoRedoStack;
    private final ActionFactory actionFactory;
    private final String userId;
    private Runnable onUpdateCallback;

    public StandaloneActionManager(CanvasState canvasState, ActionFactory actionFactory, String userId) {
        this.canvasState = canvasState;
        this.undoRedoStack = new UndoRedoStack();
        this.actionFactory = actionFactory;
        this.userId = userId;
    }

    public void setOnUpdate(Runnable callback) {
        this.onUpdateCallback = callback;
    }

    private void notifyUpdate() {
        if (onUpdateCallback != null) onUpdateCallback.run();
    }

    @Override
    public void processIncomingAction(SerializedAction serializedAction) {
        // Not used in standalone mode
    }

    @Override
    public synchronized void requestLocalAction(Action action) {
        // In single user mode, we trust local actions implicitly.
        // Apply immediately.
        canvasState.applyState(action.getShapeId(), action.getNewState());
        undoRedoStack.pushUndo(action);
        notifyUpdate();
    }

    @Override
    public synchronized void performUndo() {
        Action actionToUndo = undoRedoStack.popUndo();
        if (actionToUndo != null) {
            Action inverse = actionFactory.createInverseAction(actionToUndo, userId);
            // Apply inverse directly, don't push to standard undo stack yet
            canvasState.applyState(inverse.getShapeId(), inverse.getNewState());
            notifyUpdate();
        }
    }

    @Override
    public synchronized void performRedo() {
        Action actionToRedo = undoRedoStack.popRedo();
        if (actionToRedo != null) {
            // Re-apply original new state
            canvasState.applyState(actionToRedo.getShapeId(), actionToRedo.getNewState());
            notifyUpdate();
        }
    }

    @Override
    public UndoRedoStack getUndoRedoStack() {
        return undoRedoStack;
    }
}