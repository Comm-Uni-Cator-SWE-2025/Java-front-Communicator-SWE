package com.swe.controller.Meeting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for SessionMode enum.
 */
class SessionModeTest {

    @Test
    void testEnumValues() {
        // Assert - verify all enum values exist
        SessionMode[] values = SessionMode.values();
        assertEquals(2, values.length);
        
        assertNotNull(SessionMode.TEST);
        assertNotNull(SessionMode.CLASS);
    }

    @Test
    void testValueOf() {
        // Act & Assert - verify valueOf works correctly
        assertEquals(SessionMode.TEST, SessionMode.valueOf("TEST"));
        assertEquals(SessionMode.CLASS, SessionMode.valueOf("CLASS"));
    }

    @Test
    void testEnumToString() {
        // Act & Assert - verify toString returns the enum name
        assertEquals("TEST", SessionMode.TEST.toString());
        assertEquals("CLASS", SessionMode.CLASS.toString());
    }
}

