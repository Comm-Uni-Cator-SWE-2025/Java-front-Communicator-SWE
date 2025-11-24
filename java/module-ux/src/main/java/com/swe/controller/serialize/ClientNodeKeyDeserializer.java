package com.swe.controller.serialize;

import com.fasterxml.jackson.databind.KeyDeserializer;
import com.swe.controller.ClientNode;

/**
 * JSON key deserializer for ClientNode.
 */
public class ClientNodeKeyDeserializer extends KeyDeserializer {

    /**
     * Deserializes a string key into a ClientNode object.
     *
     * @param key the string key in format "hostname:port"
     * @param ctxt the deserialization context
     * @return the deserialized ClientNode object
     */
    @Override
    public Object deserializeKey(final String key,
            final com.fasterxml.jackson.databind.DeserializationContext ctxt) {
        final String[] parts = key.split(":");
        return new ClientNode(parts[0], Integer.parseInt(parts[1]));
    }
}
