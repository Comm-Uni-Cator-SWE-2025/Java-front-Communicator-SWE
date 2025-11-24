package com.swe.canvas.datamodel.collaboration;

import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.networking.ClientNode;
import com.swe.networking.NetworkFront;

/**
 * NetworkService implementation that relies on the networking module + RPC bridge.
 * NOTE: The backend still needs to send the host IP through the "canvas:getHostIp"
 * subscription for this service to become fully operational.
 *
 * @author Canvas Team
 */
public class CanvasNetworkService implements NetworkService {

    /** Network front for sending messages. */
    private final NetworkFront network;
    /** RPC bridge for subscriptions. */
    private final AbstractRPC rpc;
    /** The host node to send messages to. */
    private ClientNode hostNode;

    /**
     * Creates a new canvas network service.
     *
     * @param rpcService The RPC service.
     */
    public CanvasNetworkService(final AbstractRPC rpcService) {
        this(rpcService, NetworkFront.getInstance());
        if (this.rpc != null) {
            this.rpc.subscribe("canvas:getHostIp", this::handleSubscribeRPC);
        }
    }

    /**
     * Creates a new canvas network service.
     *
     * @param rpcService The RPC service.
     * @param networkFront The network front.
     */
    public CanvasNetworkService(final AbstractRPC rpcService, final NetworkFront networkFront) {
        this.rpc = rpcService;
        this.network = networkFront;
        if (this.rpc != null) {
            this.rpc.subscribe("canvas:getHostIp", this::handleSubscribeRPC);
        }
    }

    /**
     * Deserializes client node from byte array.
     * @param data The data to deserialize.
     * @return The deserialized ClientNode.
     */
    ClientNode deserializeClientNodee(final byte[] data) {
        final String dataStr = new String(data);
        final String[] parts = dataStr.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid ClientNode data: " + dataStr);
        }
        final String ip = parts[0];
        final int port = Integer.parseInt(parts[1]);
        return new ClientNode(ip, port);
    }

    /**
     * Handles the RPC subscription callback.
     * @param params The parameters from the subscription.
     * @return The parameters.
     */
    private byte[] handleSubscribeRPC(final byte[] params) {
        final ClientNode hostNodeLocal = deserializeClientNodee(params);
        this.hostNode = hostNodeLocal;

        return params;
    }

    @Override
    public void sendMessageToHost(final NetworkMessage message) {
        System.out.println("CanvasNetworkService: Sending message to host");
        final String serializedMessage = message.serialize();
        if (hostNode == null) {
            System.err.println("CanvasNetworkService: Host node unknown, cannot send message");
            return;
        }

        final ClientNode[] host = {hostNode};
        final int two = 2;
        final int zero = 0;
        network.sendData(serializedMessage.getBytes(), host, two, zero);
    }

    @Override
    public void broadcastMessage(final NetworkMessage message) {
        final String serializedMessage = message.serialize();
        final int two = 2;
        final int zero = 0;
        network.broadcast(serializedMessage.getBytes(), two, zero);
    }
}
