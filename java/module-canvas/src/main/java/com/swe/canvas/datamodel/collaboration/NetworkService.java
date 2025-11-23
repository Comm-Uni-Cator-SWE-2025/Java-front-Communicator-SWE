package com.swe.canvas.datamodel.collaboration;

import java.util.function.Consumer;

/**
 * Abstraction for the canvas collaboration transport.
 * Implementations can be real network bridges, RPC shims, or simple in-memory simulators.
 */
public interface NetworkService {

    /**
     * A Client sends a message to the Host.
     */
    void sendMessageToHost(NetworkMessage message);

    /**
     * The Host broadcasts a message to all Clients.
     */
    void broadcastMessage(NetworkMessage message);

    /**
     * Allows implementations to register the host-side message handler.
     * Default no-op so existing remote transports can ignore it.
     */
    default void registerHostHandler(Consumer<NetworkMessage> handler) {
        // no-op by default
    }

    /**
     * Allows implementations to register client-side message listeners.
     * Default no-op so remote transports can ignore it.
     */
    default void registerClientHandler(Consumer<NetworkMessage> handler) {
        // no-op by default
    }
}
