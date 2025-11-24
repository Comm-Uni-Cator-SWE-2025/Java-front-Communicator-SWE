package com.swe.controller.serialize;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.swe.controller.ClientNode;

/**
 * Jackson module for ClientNode serialization and deserialization.
 */
public class ClientNodeModule extends SimpleModule {

    /** Serial version UID for serialization. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new ClientNode module with key serializers.
     */
    @SuppressWarnings("this-escape")
    public ClientNodeModule() {
        super();
        addKeySerializer(ClientNode.class, new ClientNodeKeySerializer());
        addKeyDeserializer(ClientNode.class, new ClientNodeKeyDeserializer());
    }
}
