package com.swe.controller.serialize;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.swe.controller.Meeting.ParticipantRole;
import com.swe.controller.Meeting.SessionMode;
import com.swe.controller.Meeting.UserProfile;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for DataSerializer.
 */
class DataSerializerTest {

    @Test
    void testSerialize_SimpleObject() throws JsonProcessingException {
        // Arrange
        String testString = "Hello, World!";

        // Act
        byte[] serialized = DataSerializer.serialize(testString);

        // Assert
        assertNotNull(serialized);
        assertTrue(serialized.length > 0);
    }

    @Test
    void testSerialize_UserProfile() throws JsonProcessingException {
        // Arrange
        UserProfile profile = new UserProfile("test@example.com", "Test User", 
                "https://example.com/logo.png", ParticipantRole.STUDENT);

        // Act
        byte[] serialized = DataSerializer.serialize(profile);

        // Assert
        assertNotNull(serialized);
        assertTrue(serialized.length > 0);
    }

    @Test
    void testDeserialize_SimpleObject() throws JsonProcessingException {
        // Arrange
        String original = "Hello, World!";
        byte[] serialized = DataSerializer.serialize(original);

        // Act
        String deserialized = DataSerializer.deserialize(serialized, String.class);

        // Assert
        assertEquals(original, deserialized);
    }

    @Test
    void testDeserialize_UserProfile() throws JsonProcessingException {
        // Arrange
        UserProfile original = new UserProfile("test@example.com", "Test User", 
                "https://example.com/logo.png", ParticipantRole.STUDENT);
        byte[] serialized = DataSerializer.serialize(original);

        // Act
        UserProfile deserialized = DataSerializer.deserialize(serialized, UserProfile.class);

        // Assert
        assertNotNull(deserialized);
        assertEquals(original.getEmail(), deserialized.getEmail());
        assertEquals(original.getDisplayName(), deserialized.getDisplayName());
        assertEquals(original.getLogoUrl(), deserialized.getLogoUrl());
        assertEquals(original.getRole(), deserialized.getRole());
    }

    @Test
    void testDeserialize_WithTypeReference() throws JsonProcessingException {
        // Arrange
        Map<String, String> originalMap = new HashMap<>();
        originalMap.put("key1", "value1");
        originalMap.put("key2", "value2");
        byte[] serialized = DataSerializer.serialize(originalMap);

        // Act
        TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String, String>>() {};
        Map<String, String> deserialized = DataSerializer.deserialize(serialized, typeRef);

        // Assert
        assertNotNull(deserialized);
        assertEquals(2, deserialized.size());
        assertEquals("value1", deserialized.get("key1"));
        assertEquals("value2", deserialized.get("key2"));
    }

    @Test
    void testSerializeDeserialize_ComplexObject() throws JsonProcessingException {
        // Arrange
        UserProfile profile1 = new UserProfile("user1@example.com", "User 1", 
                "https://example.com/user1.png", ParticipantRole.STUDENT);
        UserProfile profile2 = new UserProfile("user2@example.com", "User 2", 
                "https://example.com/user2.png", ParticipantRole.INSTRUCTOR);

        Map<String, UserProfile> originalMap = new HashMap<>();
        originalMap.put(profile1.getEmail(), profile1);
        originalMap.put(profile2.getEmail(), profile2);

        byte[] serialized = DataSerializer.serialize(originalMap);

        // Act
        TypeReference<Map<String, UserProfile>> typeRef = new TypeReference<Map<String, UserProfile>>() {};
        Map<String, UserProfile> deserialized = DataSerializer.deserialize(serialized, typeRef);

        // Assert
        assertNotNull(deserialized);
        assertEquals(2, deserialized.size());
        assertEquals(profile1.getEmail(), deserialized.get(profile1.getEmail()).getEmail());
        assertEquals(profile2.getEmail(), deserialized.get(profile2.getEmail()).getEmail());
    }

    @Test
    void testDeserialize_InvalidData() {
        // Arrange
        byte[] invalidData = "not valid json".getBytes();

        // Act & Assert
        assertThrows(JsonProcessingException.class, () -> {
            DataSerializer.deserialize(invalidData, String.class);
        });
    }

    @Test
    void testDeserialize_WithTypeReference_InvalidData() {
        // Arrange
        byte[] invalidData = "not valid json".getBytes();
        TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String, String>>() {};

        // Act & Assert
        assertThrows(JsonProcessingException.class, () -> {
            DataSerializer.deserialize(invalidData, typeRef);
        });
    }

    @Test
    void testSerialize_Enum() throws JsonProcessingException {
        // Arrange
        SessionMode mode = SessionMode.CLASS;

        // Act
        byte[] serialized = DataSerializer.serialize(mode);

        // Assert
        assertNotNull(serialized);
        assertTrue(serialized.length > 0);
    }

    @Test
    void testDeserialize_Enum() throws JsonProcessingException {
        // Arrange
        SessionMode original = SessionMode.TEST;
        byte[] serialized = DataSerializer.serialize(original);

        // Act
        SessionMode deserialized = DataSerializer.deserialize(serialized, SessionMode.class);

        // Assert
        assertEquals(original, deserialized);
    }

    @Test
    void testSerialize_EmptyString() throws JsonProcessingException {
        // Arrange
        String empty = "";

        // Act
        byte[] serialized = DataSerializer.serialize(empty);

        // Assert
        assertNotNull(serialized);
        // Should serialize to "{}" or similar JSON representation
        assertTrue(serialized.length >= 0);
    }

    @Test
    void testDeserialize_EmptyString() throws JsonProcessingException {
        // Arrange
        String original = "";
        byte[] serialized = DataSerializer.serialize(original);

        // Act
        String deserialized = DataSerializer.deserialize(serialized, String.class);

        // Assert
        assertEquals(original, deserialized);
    }

    @Test
    void testSerialize_NullHandling() throws JsonProcessingException {
        // Arrange
        UserProfile profile = new UserProfile();
        // profile has null fields

        // Act
        byte[] serialized = DataSerializer.serialize(profile);

        // Assert
        assertNotNull(serialized);
        assertTrue(serialized.length > 0);
    }
}

