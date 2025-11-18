package com.swe.screenNVideo;


import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Subscribe Packet.
 */
public record SubscriberPacket(String ip, boolean reqCompression) {


    /**
     * Serializes the string for networking layer.
     * @return serialized byte array
     */
    public byte[] serialize() {
        final int len = 4 * Integer.BYTES + 1; // 4 int for ip and one for boolean
        final ByteBuffer buffer = ByteBuffer.allocate(len + 1);
        buffer.put((byte) (0)); // dummy to reuse a func in core

        final int[] ipInts = Arrays.stream(ip.split("\\.")).mapToInt(Integer::parseInt).toArray();
        for (int i = 0; i < ipInts.length; i++) {
            buffer.putInt(ipInts[i]);
        }
        buffer.put((byte) (reqCompression ? 1 : 0));

        return buffer.array();
    }

}
