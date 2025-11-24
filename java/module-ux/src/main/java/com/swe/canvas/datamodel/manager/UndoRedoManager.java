package com.swe.canvas.datamodel.manager;

import java.util.ArrayList;
import java.util.List;

import com.swe.canvas.datamodel.action.Action;

/**
 * Replaces UndoRedoStack.
 * Manages the undo/redo history using an ArrayList and a "pointer" (currentIndex).
 * This matches the "linked list" concept from the prompt.
 *
 * @author Canvas Team
 */
public class UndoRedoManager {

    /** The list of actions in the history. */
    private final List<Action> history = new ArrayList<>();
    
    /**
     * Points to the index of the last *applied* action.
     * -1 means the history is empty.
     */
    private int currentIndex = -1;

    /**
     * Pushes a new action onto the history stack.
     * This is called when a user's *own* action is confirmed by the host.
     * It clears all "redo" history.
     * @param action The action to push onto the history stack.
     */
    public synchronized void push(final Action action) {
        // If we have "undone" actions, clear them
        while (history.size() - 1 > currentIndex) {
            history.remove(history.size() - 1);
        }
        history.add(action);
        currentIndex++;
    }

    /**
     * Gets the action to be undone and moves the pointer back.
     * @return The Action to undo, or null if not possible.
     */
    public synchronized Action getActionToUndo() {
        if (canUndo()) {
            final Action actionToUndo = history.get(currentIndex);
            // We just move the pointer. The *host* will confirm.
            // We return the action so the manager can create an inverse.
            return actionToUndo;
        }
        return null;
    }

    /**
     * Gets the action to be redone and moves the pointer forward.
     * @return The Action to redo, or null if not possible.
     */
    public synchronized Action getActionToRedo() {
        if (canRedo()) {
            // We return the *next* action in the stack.
            final Action actionToRedo = history.get(currentIndex + 1);
            return actionToRedo;
        }
        return null;
    }
    
    /**
     * For clients receiving an undo message from the host.
     * This moves the pointer back without returning an action.
     * This is the "check" and "fix for consistency" mentioned in the prompt.
     */
    public synchronized void applyHostUndo() {
        if (canUndo()) {
            System.out.println("[Client UndoManager] Moving pointer back.");
            currentIndex--;
        }
    }

    /**
     * For clients receiving a redo message from the host.
     * This moves the pointer forward without returning an action.
     */
    public synchronized void applyHostRedo() {
        if (canRedo()) {
            System.out.println("[Client UndoManager] Moving pointer forward.");
            currentIndex++;
        }
    }

    /**
     * Checks if an undo operation is possible.
     * @return true if undo is possible, false otherwise.
     */
    public synchronized boolean canUndo() {
        return currentIndex > -1;
    }

    /**
     * Checks if a redo operation is possible.
     * @return true if redo is possible, false otherwise.
     */
    public synchronized boolean canRedo() {
        return currentIndex < history.size() - 1;
    }
    
    /**
     * Clears all undo/redo history.
     */
    public synchronized void clear() {
        history.clear();
        currentIndex = -1;
    }
}