package com.swe.canvas.datamodel.collaboration;

import com.swe.app.RPC;
import com.swe.networking.ClientNode;
import com.swe.networking.NetworkFront;


public class CanvasNetworkService implements NetworkService {

    private NetworkFront network = NetworkFront.getInstance();
    private RPC rpc = null;

    public CanvasNetworkService() {
        this.rpc = RPC.getInstance();

        rpc.subscribe("canvas:getHostIp", this::handleSubscribeRPC);
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

    private ClientNode hostNode = null;

    private byte[] handleSubscribeRPC(byte[] params) {
        // throw new UnsupportedOperationException("Not supported yet.");
        ClientNode hostNode = deserializeClientNodee(params);
        this.hostNode = hostNode;

        return params;
    }

    @Override
    public void sendMessageToHost(NetworkMessage message) {
        // throw new UnsupportedOperationException("Not supported yet.");

        String serializedMessage = message.serialize();

        // dest: is the host

        ClientNode dest = null;
        rpc = RPC.getInstance();

        
        dest = this.hostNode;

        ClientNode[] host = {dest};

        network.sendData(serializedMessage.getBytes(), host, 2, 0);
    }

    @Override
    public void broadcastMessage(NetworkMessage message) {
        // throw new UnsupportedOperationException("Not supported yet.");
        String serializedMessage = message.serialize();
        network.broadcast(serializedMessage.getBytes(), 2, 0);
    }

    

}
