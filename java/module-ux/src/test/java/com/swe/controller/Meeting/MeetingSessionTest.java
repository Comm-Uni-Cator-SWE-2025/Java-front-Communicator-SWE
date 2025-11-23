package com.swe.controller.Meeting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for MeetingSession.
 */
class MeetingSessionTest {

    private static final String TEST_INSTRUCTOR_EMAIL = "instructor@example.com";
    private static final SessionMode TEST_SESSION_MODE = SessionMode.CLASS;
    private MeetingSession meetingSession;

    @BeforeEach
    void setUp() {
        meetingSession = new MeetingSession(TEST_INSTRUCTOR_EMAIL, TEST_SESSION_MODE);
    }

    @Test
    void testConstructor_GeneratesUniqueMeetingId() {
        // Act
        MeetingSession session1 = new MeetingSession(TEST_INSTRUCTOR_EMAIL, SessionMode.CLASS);
        MeetingSession session2 = new MeetingSession(TEST_INSTRUCTOR_EMAIL, SessionMode.TEST);

        // Assert
        assertNotNull(session1.getMeetingId());
        assertNotNull(session2.getMeetingId());
        assertTrue(session1.getMeetingId().length() > 0);
        assertTrue(session2.getMeetingId().length() > 0);
        // Verify they are different UUIDs
        assertTrue(!session1.getMeetingId().equals(session2.getMeetingId()));
    }

    @Test
    void testConstructor_SetsCreatedBy() {
        // Assert
        assertEquals(TEST_INSTRUCTOR_EMAIL, meetingSession.getCreatedBy());
    }

    @Test
    void testConstructor_SetsSessionMode() {
        // Assert
        assertEquals(TEST_SESSION_MODE, meetingSession.getSessionMode());
    }

    @Test
    void testConstructor_SetsCreatedAt() {
        // Arrange
        long beforeCreation = System.currentTimeMillis();

        // Act
        MeetingSession session = new MeetingSession(TEST_INSTRUCTOR_EMAIL, SessionMode.CLASS);
        long afterCreation = System.currentTimeMillis();

        // Assert
        assertTrue(session.getCreatedAt() >= beforeCreation);
        assertTrue(session.getCreatedAt() <= afterCreation);
    }

    @Test
    void testJsonCreatorConstructor() {
        // Arrange
        String meetingId = UUID.randomUUID().toString();
        String createdBy = "test@example.com";
        long createdAt = System.currentTimeMillis();
        SessionMode sessionMode = SessionMode.TEST;
        Map<String, UserProfile> participants = new java.util.HashMap<>();

        // Act
        MeetingSession session = new MeetingSession(meetingId, createdBy, createdAt, sessionMode, participants);

        // Assert
        assertEquals(meetingId, session.getMeetingId());
        assertEquals(createdBy, session.getCreatedBy());
        assertEquals(createdAt, session.getCreatedAt());
        assertEquals(sessionMode, session.getSessionMode());
        assertNotNull(session.getParticipants());
    }

    @Test
    void testGetters() {
        // Assert
        assertNotNull(meetingSession.getMeetingId());
        assertEquals(TEST_INSTRUCTOR_EMAIL, meetingSession.getCreatedBy());
        assertTrue(meetingSession.getCreatedAt() > 0);
        assertEquals(TEST_SESSION_MODE, meetingSession.getSessionMode());
        assertNotNull(meetingSession.getParticipants());
    }

    @Test
    void testGetParticipant_WhenParticipantExists() {
        // Arrange
        UserProfile participant = new UserProfile("student@example.com", "Student Name", 
                "https://example.com/logo.png", ParticipantRole.STUDENT);
        meetingSession.addParticipant(participant);

        // Act
        UserProfile retrieved = meetingSession.getParticipant("student@example.com");

        // Assert
        assertNotNull(retrieved);
        assertEquals(participant.getEmail(), retrieved.getEmail());
        assertEquals(participant.getDisplayName(), retrieved.getDisplayName());
    }

    @Test
    void testGetParticipant_WhenParticipantDoesNotExist() {
        // Act
        UserProfile retrieved = meetingSession.getParticipant("nonexistent@example.com");

        // Assert
        assertNull(retrieved);
    }

    @Test
    void testGetParticipants_ReturnsMap() {
        // Act
        Map<String, UserProfile> participants = meetingSession.getParticipants();

        // Assert
        assertNotNull(participants);
        assertTrue(participants.isEmpty());
    }

    @Test
    void testAddParticipant_ValidParticipant() {
        // Arrange
        UserProfile participant = new UserProfile("student@example.com", "Student Name", 
                "https://example.com/logo.png", ParticipantRole.STUDENT);

        // Act
        meetingSession.addParticipant(participant);

        // Assert
        assertEquals(1, meetingSession.getParticipants().size());
        assertNotNull(meetingSession.getParticipant("student@example.com"));
        assertEquals(participant, meetingSession.getParticipant("student@example.com"));
    }

    @Test
    void testAddParticipant_MultipleParticipants() {
        // Arrange
        UserProfile participant1 = new UserProfile("student1@example.com", "Student 1", 
                "https://example.com/logo1.png", ParticipantRole.STUDENT);
        UserProfile participant2 = new UserProfile("student2@example.com", "Student 2", 
                "https://example.com/logo2.png", ParticipantRole.STUDENT);
        UserProfile instructor = new UserProfile("instructor2@example.com", "Instructor 2", 
                "https://example.com/instructor.png", ParticipantRole.INSTRUCTOR);

        // Act
        meetingSession.addParticipant(participant1);
        meetingSession.addParticipant(participant2);
        meetingSession.addParticipant(instructor);

        // Assert
        assertEquals(3, meetingSession.getParticipants().size());
        assertNotNull(meetingSession.getParticipant("student1@example.com"));
        assertNotNull(meetingSession.getParticipant("student2@example.com"));
        assertNotNull(meetingSession.getParticipant("instructor2@example.com"));
    }

    @Test
    void testAddParticipant_NullParticipant() {
        // Act
        meetingSession.addParticipant(null);

        // Assert
        assertEquals(0, meetingSession.getParticipants().size());
    }

    @Test
    void testAddParticipant_ParticipantWithNullEmail() {
        // Arrange
        UserProfile participant = new UserProfile();
        participant.setDisplayName("Test Name");
        // email is null

        // Act
        meetingSession.addParticipant(participant);

        // Assert
        assertEquals(0, meetingSession.getParticipants().size());
    }

    @Test
    void testAddParticipant_OverwritesExistingParticipant() {
        // Arrange
        UserProfile participant1 = new UserProfile("student@example.com", "Student 1", 
                "https://example.com/logo1.png", ParticipantRole.STUDENT);
        UserProfile participant2 = new UserProfile("student@example.com", "Student 2 Updated", 
                "https://example.com/logo2.png", ParticipantRole.STUDENT);

        // Act
        meetingSession.addParticipant(participant1);
        meetingSession.addParticipant(participant2);

        // Assert
        assertEquals(1, meetingSession.getParticipants().size());
        assertEquals("Student 2 Updated", meetingSession.getParticipant("student@example.com").getDisplayName());
    }

    @Test
    void testConstructor_WithDifferentSessionModes() {
        // Act
        MeetingSession testSession = new MeetingSession(TEST_INSTRUCTOR_EMAIL, SessionMode.TEST);
        MeetingSession classSession = new MeetingSession(TEST_INSTRUCTOR_EMAIL, SessionMode.CLASS);

        // Assert
        assertEquals(SessionMode.TEST, testSession.getSessionMode());
        assertEquals(SessionMode.CLASS, classSession.getSessionMode());
    }
}

