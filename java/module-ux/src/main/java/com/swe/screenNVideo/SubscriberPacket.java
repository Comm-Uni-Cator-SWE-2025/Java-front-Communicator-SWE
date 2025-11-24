package com.swe.screenNVideo;

/*
 * Contributed by Sandeep Kumar.
 */

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Subscribe Packet.
 *
 * @param ip the IP address as a string
 * @param reqCompression whether compression is requested
 */
public record SubscriberPacket(String ip, boolean reqCompression) {

    /**
     * Serializes the string for networking layer.
     *
     * @return serialized byte array
     */
    public byte[] serialize() {
        final int len = 4 * Integer.BYTES + 1; // 4 int for ip and one for boolean
        final ByteBuffer buffer = ByteBuffer.allocate(len + 1);
        buffer.put((byte) 0); // dummy to reuse a func in core

        final int[] ipInts = Arrays.stream(ip.split("\\.")).mapToInt(Integer::parseInt).toArray();

        for (final int ipInt : ipInts) {
            buffer.putInt(ipInt);
        }
        final byte compressionByte;
        if (reqCompression) {
            compressionByte = 1;
        } else {
            compressionByte = 0;
        }
        buffer.put(compressionByte);

        return buffer.array();
    }

}
