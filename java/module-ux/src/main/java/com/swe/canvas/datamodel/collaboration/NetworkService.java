/*
 * -----------------------------------------------------------------------------
 * File: NetworkService.java
 * Owner: B S S Krishna
 * Roll Number: 112201013
 * Module: Canvas
 * -----------------------------------------------------------------------------
 */

package com.swe.canvas.datamodel.collaboration;

import com.swe.canvas.datamodel.manager.ActionManager;

/**
 * Abstract interface for the network layer.
 *
 * <p>
 * This interface defines the contract for sending messages between the
 * Client and the Host. It abstracts the underlying transport mechanism
 * (e.g., sockets, WebSockets, or in-memory simulation).
 * </p>
 */
public interface NetworkService {

    /**
     * Sends a message from a Client to the Host.
     *
     * @param message The message to send.
     */
    void sendMessageToHost(NetworkMessage message);

    /**
     * Broadcasts a message from the Host to all connected Clients.
     *
     * @param message The message to broadcast.
     */
    void broadcastMessage(NetworkMessage message);

    /**
     * Registers the host action manager when using simulated transports.
     *
     * @param hostManager host-side manager
     */
    default void registerHost(final ActionManager hostManager) { }

    /**
     * Registers a client action manager when using simulated transports.
     *
     * @param clientManager client-side manager
     */
    default void registerClient(final ActionManager clientManager) { }
}
