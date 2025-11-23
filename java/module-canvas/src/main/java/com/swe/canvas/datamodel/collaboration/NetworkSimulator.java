package com.swe.canvas.datamodel.collaboration;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Simple in-memory implementation of {@link NetworkService}.
 * Used by the desktop frontend to simulate host/client collaboration without
 * the full networking stack.
 */
public class NetworkSimulator implements NetworkService {

    private Consumer<NetworkMessage> hostHandler;
    private final List<Consumer<NetworkMessage>> clientHandlers = new CopyOnWriteArrayList<>();

    @Override
    public void registerHostHandler(Consumer<NetworkMessage> handler) {
        this.hostHandler = handler;
    }

    @Override
    public void registerClientHandler(Consumer<NetworkMessage> handler) {
        if (handler != null) {
            clientHandlers.add(handler);
        }
    }

    @Override
    public void sendMessageToHost(NetworkMessage message) {
        if (hostHandler != null) {
            hostHandler.accept(message);
        }
    }

    @Override
    public void broadcastMessage(NetworkMessage message) {
        for (Consumer<NetworkMessage> handler : clientHandlers) {
            handler.accept(message);
        }
    }
}
