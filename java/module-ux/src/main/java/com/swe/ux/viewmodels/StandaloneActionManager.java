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

    /** Canvas state for this manager. */
    private final CanvasState canvasState;
    /** Undo/redo manager. */
    private final UndoRedoManager undoRedoManager;
    /** Action factory. */
    private final ActionFactory actionFactory;
    /** User ID. */
    private final String userId;
    /** Update callback. */
    private Runnable onUpdateCallback = () -> { };

    /**
     * Creates a new StandaloneActionManager.
     * @param canvasStateParam The canvas state
     * @param userIdParam The user ID
     */
    public StandaloneActionManager(final CanvasState canvasStateParam, final String userIdParam) {
        this.canvasState = canvasStateParam;
        this.undoRedoManager = new UndoRedoManager();
        this.actionFactory = new ActionFactory();
        this.userId = userIdParam;
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
        if (callback != null) {
            this.onUpdateCallback = callback;
        } else {
            this.onUpdateCallback = () -> { };
        }
    }

    private void notifyUpdate() {
        onUpdateCallback.run();
    }

    @Override
    public void requestCreate(final Shape newShape) {
        final Action action = actionFactory.createCreateAction(newShape, userId);
        // In standalone mode, apply immediately
        canvasState.applyState(action.getShapeId(), action.getNewState());
        undoRedoManager.push(action);
        notifyUpdate();
    }

    @Override
    public void requestModify(final ShapeState prevState, final Shape modifiedShape) {
        final Action action = actionFactory.createModifyAction(
            canvasState, prevState.getShapeId(), modifiedShape, userId);
        // In standalone mode, apply immediately
        canvasState.applyState(action.getShapeId(), action.getNewState());
        undoRedoManager.push(action);
        notifyUpdate();
    }

    @Override
    public void requestDelete(final ShapeState shapeToDelete) {
        final Action action = actionFactory.createDeleteAction(canvasState, shapeToDelete.getShapeId(), userId);
        // In standalone mode, apply immediately
        canvasState.applyState(action.getShapeId(), action.getNewState());
        undoRedoManager.push(action);
        notifyUpdate();
    }

    @Override
    public void requestUndo() {
        final Action actionToUndo = undoRedoManager.getActionToUndo();
        if (actionToUndo != null) {
            final Action inverseAction = actionFactory.createInverseAction(actionToUndo, userId);
            // Apply inverse immediately
            canvasState.applyState(inverseAction.getShapeId(), inverseAction.getNewState());
            undoRedoManager.applyHostUndo(); // Move pointer back
            notifyUpdate();
        }
    }

    @Override
    public void requestRedo() {
        final Action actionToRedo = undoRedoManager.getActionToRedo();
        if (actionToRedo != null) {
            // Re-apply original action
            canvasState.applyState(actionToRedo.getShapeId(), actionToRedo.getNewState());
            undoRedoManager.applyHostRedo(); // Move pointer forward
            notifyUpdate();
        }
    }

    @Override
    public void processIncomingMessage(final NetworkMessage message) {
        // Not used in standalone mode
    }
}
