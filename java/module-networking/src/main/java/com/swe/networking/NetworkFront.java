package com.swe.networking;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class NetworkFront implements AbstractController, AbstractNetworking {

    /** Variable to store the function mappings. */
    private HashMap<Integer, MessageListener> listeners;

    /** Variable to track the number of functions. */
    private int functionCount = 1;

    @Override
    public void sendData(byte[] data, ClientNode[] dest, int module, int priority) {
        int dataLength = data.length;
        int destSize = 0;
        for (ClientNode record : dest) {
            byte[] hostName = record.hostName().getBytes(StandardCharsets.UTF_8);
            destSize += 1 + hostName.length + Integer.BYTES; // 1 byte length + host + port
        }
        // 1 - data length 1 - dest count 1 - module 1 - priority
        final int bufferSize = dataLength + destSize + 4 * Integer.BYTES;
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        buffer.putInt(dest.length);
        for (ClientNode record : dest) {
            byte[] hostName = record.hostName().getBytes(StandardCharsets.UTF_8);
            buffer.put((byte) hostName.length);
            buffer.put(hostName);
            buffer.putInt(record.port());
        }
        buffer.putInt(dataLength);
        buffer.put(data);
        buffer.putInt(module);
        buffer.putInt(priority);
        byte[] args = buffer.array();

        // TODO: Call RPC broadcast
    }

    @Override
    public void broadcast(byte[] data, int module, int priority) {
        int dataLength = data.length;
        final int bufferSize = dataLength + 3 * Integer.BYTES;
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        buffer.putInt(dataLength);
        buffer.put(data);
        buffer.putInt(module);
        buffer.putInt(priority);
        byte[] args = buffer.array();

        // TODO: Call RPC broadcast
    }

    @Override
    public void subscribe(int name, MessageListener function) {
        listeners.put(name, function);
        final int bufferSize = Integer.BYTES;
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        buffer.putInt(name);
        byte[] args = buffer.array();

        // TODO: Call RPC Add function
    }

    @Override
    public void removeSubscription(int name) {
        final int bufferSize = Integer.BYTES;
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        buffer.putInt(name);
        byte[] args = buffer.array();

        // TODO: Call RPC remove
    }

    @Override
    public void addUser(ClientNode deviceAddress, ClientNode mainServerAddress) {
        final int bufferSize = 2 + deviceAddress.hostName().length() + mainServerAddress.hostName().length()
                + 2 * Integer.BYTES;
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        byte[] hostName = deviceAddress.hostName().getBytes(StandardCharsets.UTF_8);
        buffer.put((byte) hostName.length);
        buffer.put(hostName);
        buffer.putInt(deviceAddress.port());
        hostName = mainServerAddress.hostName().getBytes(StandardCharsets.UTF_8);
        buffer.put((byte) hostName.length);
        buffer.put(hostName);
        buffer.putInt(mainServerAddress.port());
        byte[] args = buffer.array();

        // TODO: Call RPC addUser
    }

    /**
     * Function to call the subscriber in frontend.
     *
     * @param data the data to send
     */
    public void networkFrontCallSubscriber(byte[] data) {
        int dataSize = data.length - 1;
        ByteBuffer buffer = ByteBuffer.wrap(data);
        int module = buffer.getInt();
        byte[] newData = new byte[dataSize];
        MessageListener function = listeners.get(module);
        if (function != null) {
            function.receiveData(newData);
        }

    }
}
