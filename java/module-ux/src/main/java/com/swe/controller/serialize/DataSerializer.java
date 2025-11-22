package com.swe.controller.serialize;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

public class DataSerializer {

    static ObjectMapper objectMapper = new ObjectMapper();

    public static byte[] serialize(Object participant) throws JsonProcessingException {
        objectMapper.registerModule(new ClientNodeModule());
        String data = objectMapper.writeValueAsString(participant);

        return data.getBytes(StandardCharsets.UTF_8);
    }

    public static <T> T deserialize(byte[] data, Class<T> datatype) throws JsonProcessingException {
        String json = new String(data, StandardCharsets.UTF_8);

        return objectMapper.readValue(json, datatype);
    }

    public static <T> T deserialize(byte[] data, TypeReference<T> typeReference) throws JsonProcessingException {
        String json = new String(data, StandardCharsets.UTF_8);

        return objectMapper.readValue(json, typeReference);
    }
}
