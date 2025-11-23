package com.swe.canvas.datamodel.collaboration;

/**
 * Defines the type of action being sent over the network,
 * as specified in your prompt.
 */
public enum MessageType {
    /**
     * A standard create, modify, or delete action.
     */
    NORMAL,
    /**
     * An undo request.
     */
    UNDO,
    /**
     * A redo request.
     */
    REDO
}