/*
 * -----------------------------------------------------------------------------
 * File: NetworkSimulator.java
 * Owner: B S S Krishna
 * Roll Number: 112201013
 * Module: Canvas
 * -----------------------------------------------------------------------------
 */

package com.swe.canvas.datamodel.collaboration;

import java.util.ArrayList;
import java.util.List;

import com.swe.canvas.datamodel.manager.ActionManager;

/**
 * A network stub that simulates the Host-Client connection in memory.
 */
public class NetworkSimulator implements NetworkService {

    /** Reference to the Host manager. */
    private ActionManager hostManager;

    /** List of registered Client managers. */
    private final List<ActionManager> clientManagers = new ArrayList<>();

    @Override
    public void sendMessageToHost(final NetworkMessage message) {
        if (hostManager != null) {
            hostManager.processIncomingMessage(message);
        }
    }

    @Override
    public void broadcastMessage(final NetworkMessage message) {
        for (ActionManager client : clientManagers) {
            client.processIncomingMessage(message);
        }
    }

    @Override
    public void sendToClient(NetworkMessage message, String targetClientId) {
        // Simple simulation: broadcast to all for now in tests, or ignore.
        // In a real simulation we'd map IDs to managers.
        for (ActionManager client : clientManagers) {
            client.processIncomingMessage(message);
        }
    }
}