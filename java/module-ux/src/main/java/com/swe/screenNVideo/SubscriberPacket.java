/**
 *  Contributed by Sandeep Kumar.
 */
package com.swe.screenNVideo;


import com.fasterxml.jackson.annotation.JsonProperty;

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

        int[] ipInts;
        ipInts = Arrays.stream(ip.split("\\.")).mapToInt(Integer::parseInt).toArray();

        for (int ipInt : ipInts) {
            buffer.putInt(ipInt);
        }
        buffer.put((byte) (reqCompression ? 1 : 0));

        return buffer.array();
    }

}
