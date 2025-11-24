package com.swe.controller.serialize;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.swe.controller.ClientNode;

/**
 * JSON serializer for ClientNode as a map key.
 */
public class ClientNodeKeySerializer extends JsonSerializer<ClientNode> {

    @Override
    public void serialize(final ClientNode value,
                          final JsonGenerator gen,
                          final SerializerProvider serializers)
            throws IOException {
        gen.writeFieldName(value.hostName() + ":" + value.port());
    }
}
