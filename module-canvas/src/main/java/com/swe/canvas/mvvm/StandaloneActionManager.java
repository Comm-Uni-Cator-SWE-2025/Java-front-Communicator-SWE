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
 * @author Bhogaraju Shanmukha Sri Krishna
 */
public class StandaloneActionManager implements ActionManager {

    private final CanvasState canvasState;
    private final UndoRedoStack undoRedoStack;
    private final ActionFactory actionFactory;
    private final String userId;
    private Runnable onUpdateCallback;

    /**
     * Constructor for the class
     * @param canvasState_ State of the canvas
     * @param actionFactory_ action factory
     * @param userId_ id of the user
     */
    public StandaloneActionManager(final CanvasState canvasState_, final ActionFactory actionFactory_, final String userId_) {
        this.canvasState = canvasState_;
        this.undoRedoStack = new UndoRedoStack();
        this.actionFactory = actionFactory_;
        this.userId = userId_;
    }

    /**
     * Set the canvas on update
     * @param callback callback function to runnable
     */
    public void setOnUpdate(final Runnable callback) {
        this.onUpdateCallback = callback;
    }

    private void notifyUpdate() {
        if (onUpdateCallback != null) {
            onUpdateCallback.run();
        }
    }

    @Override
    public void processIncomingAction(final SerializedAction serializedAction) {
        // Not used in standalone mode
    }

    @Override
    public synchronized void requestLocalAction(final Action action) {
        // In single user mode, we trust local actions implicitly.
        // Apply immediately.
        canvasState.applyState(action.getShapeId(), action.getNewState());
        undoRedoStack.pushUndo(action);
        notifyUpdate();
    }

    @Override
    public synchronized void performUndo() {
        final Action actionToUndo = undoRedoStack.popUndo();
        if (actionToUndo != null) {
            final Action inverse = actionFactory.createInverseAction(actionToUndo, userId);
            // Apply inverse directly, don't push to standard undo stack yet
            canvasState.applyState(inverse.getShapeId(), inverse.getNewState());
            notifyUpdate();
        }
    }

    @Override
    public synchronized void performRedo() {
        final Action actionToRedo = undoRedoStack.popRedo();
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