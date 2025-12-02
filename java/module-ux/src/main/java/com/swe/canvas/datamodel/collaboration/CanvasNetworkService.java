/*
 * -----------------------------------------------------------------------------
 * File: CanvasNetworkService.java
 * Owner: Bhogaraju Shanmukha Sri Krishna
 * Roll Number: 112201013
 * Module: Canvas
 *
 * -----------------------------------------------------------------------------
 */


package com.swe.canvas.datamodel.collaboration;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.swe.controller.RPC;
import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.controller.serialize.DataSerializer;
import com.swe.networking.ClientNode;
import com.swe.networking.NetworkFront;

/**
 * NetworkService implementation that relies on the networking module + RPC bridge.
 * NOTE: The backend still needs to send the host IP through the "canvas:getHostIp"
 * subscription for this service to become fully operational.
 */
public class CanvasNetworkService implements NetworkService {

    private final NetworkFront network;
    private final AbstractRPC rpc;

    private ClientNode hostNode = null;

    public CanvasNetworkService(final AbstractRPC rpc) {
        this(rpc, NetworkFront.getInstance());
    }

    public CanvasNetworkService(final AbstractRPC rpc, final NetworkFront network) {
        if(rpc != null) {
            this.rpc = rpc;
        }
        else {
            this.rpc = RPC.getInstance();
        }
        
        this.network = network;

    }


    @Override
    public void sendMessageToHost(NetworkMessage message) {
        final String serializedMessage = message.serialize();
        System.out.println("[CanvasNetworkService] sendMessageToHost called. Type="
                + message.getMessageType() + ", bytes=" + serializedMessage.length());

        byte[] data = null;
        try {
            data = DataSerializer.serialize(serializedMessage);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        


        if (this.rpc != null) {

            this.rpc.call("canvas:sendToHost", data)
                    .whenComplete((resp, err) -> {
                        if (err != null) {
                            System.err.println("[CanvasNetworkService] sendMessageToHost failed: " + err.getMessage());
                        } else {
                            System.out.println("[CanvasNetworkService] sendMessageToHost delivered.");
                        }
                    });
        } else {
            System.err.println("[CanvasNetworkService] RPC instance is null; cannot send to host.");
        }
    }

    @Override
    public void broadcastMessage(NetworkMessage message) {
        final String serializedMessage = message.serialize();

        byte[] data = null;
        try {
            data = DataSerializer.serialize(serializedMessage);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        System.out.println("[CanvasNetworkService] broadcastMessage called. Type="
                + message.getMessageType() + ", bytes=" + serializedMessage.length());

        if (this.rpc != null) {
            this.rpc.call("canvas:broadcast", data)
                    .whenComplete((resp, err) -> {
                        if (err != null) {
                            System.err.println("[CanvasNetworkService] broadcastMessage failed: " + err.getMessage());
                        } else {
                            System.out.println("[CanvasNetworkService] broadcastMessage delivered.");
                        }
                    });
        } else {
            System.err.println("[CanvasNetworkService] RPC instance is null; cannot broadcast.");
        }
    }
}
