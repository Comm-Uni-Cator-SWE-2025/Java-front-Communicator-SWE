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

import com.swe.controller.RPC;
import com.swe.controller.RPCinterface.AbstractRPC;
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

    ClientNode deserializeClientNodee(byte[] data) {
        String dataStr = new String(data);
        String[] parts = dataStr.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid ClientNode data: " + dataStr);
        }
        String ip = parts[0];
        int port = Integer.parseInt(parts[1]);
        return new ClientNode(ip, port);
    }


    @Override
    public void sendMessageToHost(NetworkMessage message) {
        String serializedMessage = message.serialize();
    

        if (this.rpc != null) {
            this.rpc.call("canvas:sendToHost", serializedMessage.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Override
    public void broadcastMessage(NetworkMessage message) {
        String serializedMessage = message.serialize();

        // network.broadcast(serializedMessage.getBytes(), 2, 0);
    
        if (this.rpc != null) {
            this.rpc.call("canvas:broadcast", serializedMessage.getBytes());
        }
    }
}
