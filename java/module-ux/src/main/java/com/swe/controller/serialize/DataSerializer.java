package com.swe.controller.serialize;

/*
 * Contributed by Pushti Vasoya.
 */

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class for serializing and deserializing data objects.
 */
public class DataSerializer {

    /** The Jackson ObjectMapper for JSON serialization. */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Serializes an object to a byte array.
     *
     * @param participant the object to serialize
     * @return the serialized byte array
     * @throws JsonProcessingException if serialization fails
     */
    public static byte[] serialize(final Object participant)
            throws JsonProcessingException {
        OBJECT_MAPPER.registerModule(new ClientNodeModule());
        final String data = OBJECT_MAPPER.writeValueAsString(participant);

        return data.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Deserializes a byte array to an object.
     *
     * @param data the byte array to deserialize
     * @param datatype the target class type
     * @param <T> the type parameter
     * @return the deserialized object
     * @throws JsonProcessingException if deserialization fails
     */
    public static <T> T deserialize(final byte[] data, final Class<T> datatype)
            throws JsonProcessingException {
        final String json = new String(data, StandardCharsets.UTF_8);

        return OBJECT_MAPPER.readValue(json, datatype);
    }

    /**
     * Deserializes a byte array to an object using a type reference.
     *
     * @param data the byte array to deserialize
     * @param typeReference the type reference
     * @param <T> the type parameter
     * @return the deserialized object
     * @throws JsonProcessingException if deserialization fails
     */
    public static <T> T deserialize(final byte[] data,
                                    final TypeReference<T> typeReference)
            throws JsonProcessingException {
        final String json = new String(data, StandardCharsets.UTF_8);
        OBJECT_MAPPER.registerModule(new ClientNodeModule());
        return OBJECT_MAPPER.readValue(json, typeReference);
    }
}
