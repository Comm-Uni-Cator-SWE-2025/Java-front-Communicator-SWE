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
import com.swe.networking.NetworkFront;

/**
 * NetworkService implementation that relies on the networking module + RPC bridge.
 * NOTE: The backend still needs to send the host IP through the "canvas:getHostIp"
 * subscription for this service to become fully operational.
 */
public class CanvasNetworkService implements NetworkService {

    private final AbstractRPC rpc;

    

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
        

    }


    @Override
    public void sendMessageToHost(NetworkMessage message) {
        String serializedMessage = message.serialize();
    
        this.rpc.call("canvas:sendToHost", serializedMessage.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void broadcastMessage(NetworkMessage message) {
        String serializedMessage = message.serialize();

        this.rpc.call("canvas:broadcast", serializedMessage.getBytes(StandardCharsets.UTF_8));       
    }
}
