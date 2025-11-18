package com.swe.canvas.datamodel.collaboration;

import java.util.ArrayList;
import java.util.List;

import com.swe.canvas.datamodel.manager.ActionManager;

/**
 * A network stub that simulates the Host-Client connection.
 * It directly calls the 'processIncomingMessage' methods on the registered managers
 * to fulfill the prompt's requirement (no real network, just a processing function).
 */
public class NetworkSimulator implements NetworkService {

    private ActionManager hostManager;
    private final List<ActionManager> clientManagers = new ArrayList<>();

    @Override
    public void registerHost(ActionManager host) {
        this.hostManager = host;
    }

    @Override
    public void registerClient(ActionManager client) {
        this.clientManagers.add(client);
    }

    @Override
    public void sendMessageToHost(NetworkMessage message) {
        System.out.println("[NETWORK] Client -> Host: " + message.getMessageType());
        if (hostManager != null) {
            // Simulate network: directly call the Host's processing function
            hostManager.processIncomingMessage(message);
        }
    }

    @Override
    public void broadcastMessage(NetworkMessage message) {
        System.out.println("[NETWORK] Host -> ALL Clients: " + message.getMessageType());
        // if (hostManager != null) {
        //     // Host also receives its own broadcasts
        //     hostManager.processIncomingMessage(message);
        // }
        for (ActionManager client : clientManagers) {
            // Simulate network: directly call the Client's processing function
            client.processIncomingMessage(message);
        }
    }
}