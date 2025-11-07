package com.swe.networking;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * The frontend networking class to connect to the RPC and to the core
 * netwroking.
 */
public class NetworkFront implements AbstractController, AbstractNetworking {

    /** Variable to store the function mappings. */
    private HashMap<Integer, MessageListener> listeners;

    /** Variable to track the number of functions. */
    private int functionCount = 1;

    @Override
    public void sendData(final byte[] data, final ClientNode[] dest, final int module, final int priority) {
        final int dataLength = data.length;
        int destSize = 0;
        for (ClientNode record : dest) {
            final byte[] hostName = record.hostName().getBytes(StandardCharsets.UTF_8);
            destSize += 1 + hostName.length + Integer.BYTES; // 1 byte length + host + port
        }
        // 1 - data length 1 - dest count 1 - module 1 - priority
        final int bufferSize = dataLength + destSize + 4 * Integer.BYTES;
        final ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        buffer.putInt(dest.length);
        for (ClientNode record : dest) {
            final byte[] hostName = record.hostName().getBytes(StandardCharsets.UTF_8);
            buffer.put((byte) hostName.length);
            buffer.put(hostName);
            buffer.putInt(record.port());
        }
        buffer.putInt(dataLength);
        buffer.put(data);
        buffer.putInt(module);
        buffer.putInt(priority);
        final byte[] args = buffer.array();

        // TOD: Call RPC broadcast
    }

    @Override
    public void broadcast(final byte[] data, final int module, final int priority) {
        final int dataLength = data.length;
        final int bufferSize = dataLength + 3 * Integer.BYTES;
        final ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        buffer.putInt(dataLength);
        buffer.put(data);
        buffer.putInt(module);
        buffer.putInt(priority);
        final byte[] args = buffer.array();

        // TOD: Call RPC broadcast
    }

    @Override
    public void subscribe(final int name, final MessageListener function) {
        listeners.put(name, function);
        final int bufferSize = Integer.BYTES;
        final ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        buffer.putInt(name);
        final byte[] args = buffer.array();

        // TOD: Call RPC Add function
    }

    @Override
    public void removeSubscription(final int name) {
        final int bufferSize = Integer.BYTES;
        final ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        buffer.putInt(name);
        final byte[] args = buffer.array();

        // TOD: Call RPC remove
    }

    @Override
    public void addUser(final ClientNode deviceAddress, final ClientNode mainServerAddress) {
        final int bufferSize = 2 + deviceAddress.hostName().length() + mainServerAddress.hostName().length()
                + 2 * Integer.BYTES;
        final ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        byte[] hostName = deviceAddress.hostName().getBytes(StandardCharsets.UTF_8);
        buffer.put((byte) hostName.length);
        buffer.put(hostName);
        buffer.putInt(deviceAddress.port());
        hostName = mainServerAddress.hostName().getBytes(StandardCharsets.UTF_8);
        buffer.put((byte) hostName.length);
        buffer.put(hostName);
        buffer.putInt(mainServerAddress.port());
        final byte[] args = buffer.array();

        // TOD: Call RPC addUser
    }

    /**
     * Function to call the subscriber in frontend.
     *
     * @param data the data to send
     */
    public void networkFrontCallSubscriber(final byte[] data) {
        final int dataSize = data.length - 1;
        final ByteBuffer buffer = ByteBuffer.wrap(data);
        final int module = buffer.getInt();
        final byte[] newData = new byte[dataSize];
        final MessageListener function = listeners.get(module);
        if (function != null) {
            function.receiveData(newData);
        }
    }
}
