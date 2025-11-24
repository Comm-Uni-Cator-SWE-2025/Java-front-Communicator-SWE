package com.swe.canvas.datamodel.collaboration;

import java.util.function.Consumer;

/**
 * Abstraction for the canvas collaboration transport.
 * Implementations can be real network bridges, RPC shims, or simple in-memory simulators.
 */
public interface NetworkService {

    /**
     * A Client sends a message to the Host.
     *
     * @param message The message to send to the host.
     */
    void sendMessageToHost(NetworkMessage message);

    /**
     * The Host broadcasts a message to all Clients.
     *
     * @param message The message to broadcast.
     */
    void broadcastMessage(NetworkMessage message);

    /**
     * Allows implementations to register the host-side message handler.
     * Default no-op so existing remote transports can ignore it.
     *
     * @param handler The handler to register.
     */
    default void registerHostHandler(Consumer<NetworkMessage> handler) {
        // no-op by default
    }

    /**
     * Allows implementations to register client-side message listeners.
     * Default no-op so remote transports can ignore it.
     *
     * @param handler The handler to register.
     */
    default void registerClientHandler(Consumer<NetworkMessage> handler) {
        // no-op by default
    }
}
