package com.swe.canvas.datamodel.collaboration;

import com.swe.app.RPC;
import com.swe.networking.ClientNode;
import com.swe.networking.NetworkFront;


public class CanvasNetworkService implements NetworkService {

    private NetworkFront network = NetworkFront.getInstance();
    private RPC rpc = null;


    @Override
    public void sendMessageToHost(NetworkMessage message) {
        // throw new UnsupportedOperationException("Not supported yet.");

        String serializedMessage = message.serialize();

        // dest: is the host

        ClientNode dest = null;
        rpc = RPC.getInstance();

        byte[] res = new byte[0];
        try {
            res  = rpc.call("canvas/ipOfHost", new byte[0]).get();    
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        dest = new ClientNode(new String(res), 6942);

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
