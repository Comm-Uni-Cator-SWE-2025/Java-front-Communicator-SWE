package com.swe.canvas.datamodel.collaboration;


import com.swe.canvas.datamodel.manager.ActionManager;
/**
 * Abstract interface for the network.
 * This is implemented by a stub/simulator.
 * It provides the sendMessage() and broadcast() functions.
 */
public interface NetworkService {
    
    /**
     * A Client sends a message to the Host.
     */
    void sendMessageToHost(NetworkMessage message);

    /**
     * The Host broadcasts a message to all Clients (and itself).
     */
    void broadcastMessage(NetworkMessage message);

    /**
     * A way for the network to know about the manager processing its messages.
     * (This is part of the simulation stub)
     */
    void registerHost(ActionManager host);
    void registerClient(ActionManager client);
}