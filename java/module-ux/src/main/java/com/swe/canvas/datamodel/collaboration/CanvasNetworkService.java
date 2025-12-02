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

import com.swe.canvas.datamodel.serialization.JsonUtils;
import com.swe.controller.RPC;
import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.networking.ClientNode;
import com.swe.networking.NetworkFront;

/**
 * NetworkService implementation that relies on the networking module + RPC bridge.
 */
public class CanvasNetworkService implements NetworkService {

    private final NetworkFront network;
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
        final String serializedMessage = message.serialize();
        // System.out.println("[CanvasNetworkService] sendMessageToHost called. Type="
        //        + message.getMessageType() + ", bytes=" + serializedMessage.length());

        if (this.rpc != null) {
            this.rpc.call("canvas:sendToHost", serializedMessage.getBytes(StandardCharsets.UTF_8))
                    .whenComplete((resp, err) -> {
                        if (err != null) {
                            System.err.println("[CanvasNetworkService] sendMessageToHost failed: " + err.getMessage());
                        }
                    });
        } else {
            System.err.println("[CanvasNetworkService] RPC instance is null; cannot send to host.");
        }
    }

    @Override
    public void broadcastMessage(NetworkMessage message) {
        final String serializedMessage = message.serialize();
        // System.out.println("[CanvasNetworkService] broadcastMessage called. Type="
        //        + message.getMessageType() + ", bytes=" + serializedMessage.length());

        if (this.rpc != null) {
            this.rpc.call("canvas:broadcast", serializedMessage.getBytes())
                    .whenComplete((resp, err) -> {
                        if (err != null) {
                            System.err.println("[CanvasNetworkService] broadcastMessage failed: " + err.getMessage());
                        }
                    });
        } else {
            System.err.println("[CanvasNetworkService] RPC instance is null; cannot broadcast.");
        }
    }

    @Override
    public void sendToClient(NetworkMessage message, String targetClientId) {
        if (targetClientId == null || targetClientId.isEmpty()) {
            return;
        }
        
        final String serializedMessage = message.serialize();
        
        // Construct JSON payload: { "target": "email", "data": "serialized_msg" }
        // We use manual string concatenation to match JsonUtils style/avoid extra dependencies here
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(JsonUtils.jsonEscape("target")).append(":").append(JsonUtils.jsonEscape(targetClientId));
        sb.append(",");
        sb.append(JsonUtils.jsonEscape("data")).append(":").append(JsonUtils.jsonEscape(serializedMessage));
        sb.append("}");
        
        final String payload = sb.toString();
        
        System.out.println("[CanvasNetworkService] sendToClient called for " + targetClientId);

        if (this.rpc != null) {
            this.rpc.call("canvas:sendToClient", payload.getBytes(StandardCharsets.UTF_8))
                    .whenComplete((resp, err) -> {
                        if (err != null) {
                            System.err.println("[CanvasNetworkService] sendToClient failed: " + err.getMessage());
                        } else {
                            System.out.println("[CanvasNetworkService] sendToClient delivered to " + targetClientId);
                        }
                    });
        } else {
            System.err.println("[CanvasNetworkService] RPC instance is null; cannot send to client.");
        }
    }
}