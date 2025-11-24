package com.swe.canvas.datamodel.collaboration;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Simple in-memory implementation of {@link NetworkService}.
 * Used by the desktop frontend to simulate host/client collaboration without
 * the full networking stack.
 *
 * @author Canvas Team
 */
public class NetworkSimulator implements NetworkService {

    /** Handler for host-side messages. */
    private Consumer<NetworkMessage> hostHandler;
    /** List of client-side message handlers. */
    private final List<Consumer<NetworkMessage>> clientHandlers = new CopyOnWriteArrayList<>();

    @Override
    public void registerHostHandler(final Consumer<NetworkMessage> handler) {
        this.hostHandler = handler;
    }

    @Override
    public void registerClientHandler(final Consumer<NetworkMessage> handler) {
        if (handler != null) {
            clientHandlers.add(handler);
        }
    }

    @Override
    public void sendMessageToHost(final NetworkMessage message) {
        if (hostHandler != null) {
            hostHandler.accept(message);
        }
    }

    @Override
    public void broadcastMessage(final NetworkMessage message) {
        for (final Consumer<NetworkMessage> handler : clientHandlers) {
            handler.accept(message);
        }
    }
}
