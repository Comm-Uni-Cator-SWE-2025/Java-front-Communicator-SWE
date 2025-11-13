package com.swe.canvas.datamodel.manager;

import com.swe.canvas.datamodel.action.Action;
import java.util.Stack;

/**
 * Manages the local undo and redo stacks for a user.
 *
 * <p>This class provides synchronized, stack-based operations for
 * {@link Action} objects. It ensures that adding new actions
 * correctly clears the redo stack.
 * </p>
 *
 * <p><b>Thread Safety:</b> This class is thread-safe.
 * All public methods are {@code synchronized}.
 * </p>
 *
 * @author Gajjala Bhavani Shankar
 
 
 */
public class UndoRedoStack {

    /**
     * Stack for actions that can be undone.
     */
    private final Stack<Action> undoStack;

    /**
     * Stack for actions that can be redone.
     */
    private final Stack<Action> redoStack;

    /**
     * Constructs a new, empty UndoRedoStack.
     */
    public UndoRedoStack() {
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
    }

    /**
     * Pushes a new action onto the undo stack.
     *
     * <p>This action typically comes *after* it has been validated and
     * applied. Calling this method will clear the redo stack.
     * </p>
     *
     * @param action The action to add.
     */
    public synchronized void pushUndo(final Action action) {
        undoStack.push(action);
        // A new action invalidates the previous redo history
        redoStack.clear();
    }

    /**
     * Pops an action from the undo stack and pushes it onto the redo stack.
     *
     * @return The {@link Action} to be undone, or {@code null} if the
     * undo stack is empty.
     */
    public synchronized Action popUndo() {
        if (undoStack.isEmpty()) {
            return null;
        }
        final Action action = undoStack.pop();
        redoStack.push(action);
        return action;
    }

    /**
     * Pops an action from the redo stack and pushes it onto the undo stack.
     *
     * <p><b>Note:</b> This implementation is for a simple distributed model.
     * It pushes the action to the undo stack *immediately*. The ActionManager
     * is responsible for re-validating and re-broadcasting this action.
     * If validation fails, a more complex implementation would be needed
     * to remove it from the undo stack.
     * </p>
     *
     * @return The {@link Action} to be redone, or {@code null} if the
     * redo stack is empty.
     */
    public synchronized Action popRedo() {
        if (redoStack.isEmpty()) {
            return null;
        }
        final Action action = redoStack.pop();
        // We push to undo *without* clearing redo.
        undoStack.push(action);
        return action;
    }

    /**
     * Clears both the undo and redo stacks.
     */
    public synchronized void clear() {
        undoStack.clear();
        redoStack.clear();
    }

    /**
     * Checks if there are actions that can be undone.
     * @return True if the undo stack is not empty.
     */
    public synchronized boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * Checks if there are actions that can be redone
     * @return True if the redo stack is not empty.
     */
    public synchronized boolean canRedo() {
        return !redoStack.isEmpty();
    }
}