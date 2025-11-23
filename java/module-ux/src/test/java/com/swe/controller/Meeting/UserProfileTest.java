package com.swe.controller.Meeting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for UserProfile.
 */
class UserProfileTest {

    private UserProfile userProfile;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_DISPLAY_NAME = "Test User";
    private static final String TEST_LOGO_URL = "https://example.com/logo.png";
    private static final ParticipantRole TEST_ROLE = ParticipantRole.STUDENT;

    @BeforeEach
    void setUp() {
        userProfile = new UserProfile(TEST_EMAIL, TEST_DISPLAY_NAME, TEST_LOGO_URL, TEST_ROLE);
    }

    @Test
    void testDefaultConstructor() {
        // Act
        UserProfile profile = new UserProfile();

        // Assert
        assertNotNull(profile);
        assertNull(profile.getEmail());
        assertNull(profile.getDisplayName());
        assertNull(profile.getLogoUrl());
        assertNull(profile.getRole());
        assertEquals("", profile.getIp());
    }

    @Test
    void testParameterizedConstructor() {
        // Assert
        assertEquals(TEST_EMAIL, userProfile.getEmail());
        assertEquals(TEST_DISPLAY_NAME, userProfile.getDisplayName());
        assertEquals(TEST_LOGO_URL, userProfile.getLogoUrl());
        assertEquals(TEST_ROLE, userProfile.getRole());
        assertEquals(TEST_EMAIL, userProfile.getIp()); // ip is set to email in constructor
    }

    @Test
    void testGetters() {
        // Assert
        assertEquals(TEST_EMAIL, userProfile.getEmail());
        assertEquals(TEST_DISPLAY_NAME, userProfile.getDisplayName());
        assertEquals(TEST_LOGO_URL, userProfile.getLogoUrl());
        assertEquals(TEST_ROLE, userProfile.getRole());
        assertEquals(TEST_EMAIL, userProfile.getIp());
    }

    @Test
    void testSetters() {
        // Arrange
        String newEmail = "newemail@example.com";
        String newDisplayName = "New Display Name";
        String newLogoUrl = "https://example.com/newlogo.png";
        ParticipantRole newRole = ParticipantRole.INSTRUCTOR;
        String newIp = "192.168.1.1";

        // Act
        userProfile.setEmail(newEmail);
        userProfile.setDisplayName(newDisplayName);
        userProfile.setLogoUrl(newLogoUrl);
        userProfile.setRole(newRole);
        userProfile.setIp(newIp);

        // Assert
        assertEquals(newEmail, userProfile.getEmail());
        assertEquals(newDisplayName, userProfile.getDisplayName());
        assertEquals(newLogoUrl, userProfile.getLogoUrl());
        assertEquals(newRole, userProfile.getRole());
        assertEquals(newIp, userProfile.getIp());
    }

    @Test
    void testEquals_SameObject() {
        // Assert
        assertTrue(userProfile.equals(userProfile));
    }

    @Test
    void testEquals_EqualObjects() {
        // Arrange
        UserProfile profile2 = new UserProfile(TEST_EMAIL, TEST_DISPLAY_NAME, TEST_LOGO_URL, TEST_ROLE);

        // Assert
        assertTrue(userProfile.equals(profile2));
        assertEquals(userProfile.hashCode(), profile2.hashCode());
    }

    @Test
    void testEquals_DifferentObjects() {
        // Arrange
        UserProfile profile2 = new UserProfile("different@example.com", TEST_DISPLAY_NAME, TEST_LOGO_URL, TEST_ROLE);
        UserProfile profile3 = new UserProfile(TEST_EMAIL, "Different Name", TEST_LOGO_URL, TEST_ROLE);
        UserProfile profile4 = new UserProfile(TEST_EMAIL, TEST_DISPLAY_NAME, "different-url", TEST_ROLE);
        UserProfile profile5 = new UserProfile(TEST_EMAIL, TEST_DISPLAY_NAME, TEST_LOGO_URL, ParticipantRole.INSTRUCTOR);

        // Assert
        assertFalse(userProfile.equals(profile2));
        assertFalse(userProfile.equals(profile3));
        assertFalse(userProfile.equals(profile4));
        assertFalse(userProfile.equals(profile5));
    }

    @Test
    void testEquals_NullObject() {
        // Assert
        assertFalse(userProfile.equals(null));
    }

    @Test
    void testEquals_DifferentClass() {
        // Assert
        assertFalse(userProfile.equals("not a UserProfile"));
    }

    @Test
    void testHashCode_EqualObjects() {
        // Arrange
        UserProfile profile2 = new UserProfile(TEST_EMAIL, TEST_DISPLAY_NAME, TEST_LOGO_URL, TEST_ROLE);

        // Assert
        assertEquals(userProfile.hashCode(), profile2.hashCode());
    }

    @Test
    void testHashCode_DifferentObjects() {
        // Arrange
        UserProfile profile2 = new UserProfile("different@example.com", TEST_DISPLAY_NAME, TEST_LOGO_URL, TEST_ROLE);

        // Assert
        assertNotEquals(userProfile.hashCode(), profile2.hashCode());
    }

    @Test
    void testToString() {
        // Act
        String result = userProfile.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("UserProfile"));
        assertTrue(result.contains(TEST_EMAIL));
        assertTrue(result.contains(TEST_DISPLAY_NAME));
        assertTrue(result.contains(TEST_ROLE.toString()));
        assertTrue(result.contains(TEST_LOGO_URL));
    }

    @Test
    void testEquals_WithNullFields() {
        // Arrange
        UserProfile profile1 = new UserProfile();
        UserProfile profile2 = new UserProfile();

        // Assert
        assertTrue(profile1.equals(profile2));
        assertEquals(profile1.hashCode(), profile2.hashCode());
    }

    @Test
    void testEquals_OneNullField() {
        // Arrange
        UserProfile profile1 = new UserProfile(TEST_EMAIL, null, TEST_LOGO_URL, TEST_ROLE);
        UserProfile profile2 = new UserProfile(TEST_EMAIL, null, TEST_LOGO_URL, TEST_ROLE);
        UserProfile profile3 = new UserProfile(TEST_EMAIL, TEST_DISPLAY_NAME, TEST_LOGO_URL, TEST_ROLE);

        // Assert
        assertTrue(profile1.equals(profile2));
        assertFalse(profile1.equals(profile3));
    }
}

