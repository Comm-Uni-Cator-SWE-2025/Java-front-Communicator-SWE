package com.swe.controller.Meeting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for ParticipantRole enum.
 */
class ParticipantRoleTest {

    @Test
    void testEnumValues() {
        // Assert - verify all enum values exist
        ParticipantRole[] values = ParticipantRole.values();
        assertEquals(3, values.length);
        
        assertNotNull(ParticipantRole.INSTRUCTOR);
        assertNotNull(ParticipantRole.STUDENT);
        assertNotNull(ParticipantRole.GUEST);
    }

    @Test
    void testValueOf() {
        // Act & Assert - verify valueOf works correctly
        assertEquals(ParticipantRole.INSTRUCTOR, ParticipantRole.valueOf("INSTRUCTOR"));
        assertEquals(ParticipantRole.STUDENT, ParticipantRole.valueOf("STUDENT"));
        assertEquals(ParticipantRole.GUEST, ParticipantRole.valueOf("GUEST"));
    }

    @Test
    void testEnumToString() {
        // Act & Assert - verify toString returns the enum name
        assertEquals("INSTRUCTOR", ParticipantRole.INSTRUCTOR.toString());
        assertEquals("STUDENT", ParticipantRole.STUDENT.toString());
        assertEquals("GUEST", ParticipantRole.GUEST.toString());
    }
}

