package com.swe.canvas.datamodel.collaboration;

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
        this.rpc = rpc;
        this.network = network;
        if (this.rpc != null) {
            this.rpc.subscribe("canvas:getHostIp", this::handleSubscribeRPC);
        }
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

    private byte[] handleSubscribeRPC(byte[] params) {
        ClientNode hostNode = deserializeClientNodee(params);
        this.hostNode = hostNode;

        return params;
    }

    @Override
    public void sendMessageToHost(NetworkMessage message) {
        String serializedMessage = message.serialize();
        if (hostNode == null) {
            System.err.println("CanvasNetworkService: Host node unknown, cannot send message");
            return;
        }

        ClientNode[] host = {hostNode};
        network.sendData(serializedMessage.getBytes(), host, 2, 0);
    }

    @Override
    public void broadcastMessage(NetworkMessage message) {
        String serializedMessage = message.serialize();
        network.broadcast(serializedMessage.getBytes(), 2, 0);
    }
}
