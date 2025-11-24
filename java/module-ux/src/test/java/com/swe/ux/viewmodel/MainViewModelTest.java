package com.swe.ux.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.swe.controller.Meeting.MeetingSession;
import com.swe.controller.Meeting.ParticipantRole;
import com.swe.controller.Meeting.SessionMode;
import com.swe.controller.Meeting.UserProfile;
import com.swe.controller.serialize.DataSerializer;
import com.swe.ux.testutil.MockAbstractRPC;
import com.swe.ux.viewmodels.MainViewModel;

/**
 * Unit tests for MainViewModel.
 */
class MainViewModelTest {

    private MockAbstractRPC mockRpc;

    private MainViewModel viewModel;
    private UserProfile testUser;

    @BeforeEach
    void setUp() {
        mockRpc = new MockAbstractRPC();
        viewModel = new MainViewModel(mockRpc);
        testUser = new UserProfile(
            "test@example.com",
            "Test User",
            "http://example.com/logo.png",
            ParticipantRole.STUDENT
        );
    }

    @Test
    void testInitialState() {
        assertNull(viewModel.currentUser.get());
        assertFalse(viewModel.logoutRequested.get());
        assertFalse(viewModel.startMeetingRequested.get());
        assertFalse(viewModel.joinMeetingRequested.get());
        assertEquals("", viewModel.meetingCode.get());
    }

    @Test
    void testSetCurrentUser() {
        // Act
        viewModel.setCurrentUser(testUser);

        // Assert
        assertNotNull(viewModel.currentUser.get());
        assertEquals("test@example.com", viewModel.currentUser.get().getEmail());
        assertEquals("Test User", viewModel.currentUser.get().getDisplayName());
    }

    @Test
    void testStartMeeting_Success() throws Exception {
        // Arrange
        MeetingSession session = new MeetingSession("test@example.com", SessionMode.CLASS);
        byte[] serializedSession = DataSerializer.serialize(session);
        mockRpc.registerResponse("core/createMeeting", serializedSession);

        // Act
        String meetingId = viewModel.startMeeting();

        // Assert
        assertNotNull(meetingId);
        assertEquals(session.getMeetingId(), meetingId);
        assertEquals(session.getMeetingId(), viewModel.meetingCode.get());
    }

    @Test
    void testStartMeeting_Failure() throws Exception {
        // Arrange
        mockRpc.registerException("core/createMeeting", new RuntimeException("Network error"));

        // Act
        String meetingId = viewModel.startMeeting();

        // Assert
        assertNull(meetingId);
    }

    @Test
    void testStartMeeting_EmptyResponse() throws Exception {
        // Arrange
        mockRpc.registerResponse("core/createMeeting", new byte[0]);

        // Act
        String meetingId = viewModel.startMeeting();

        // Assert
        assertNull(meetingId);
    }

    @Test
    void testJoinMeeting() throws Exception {
        // Arrange
        String meetingCode = "test-meeting-123";
        mockRpc.registerResponse("core/joinMeeting", new byte[0]);

        // Act
        viewModel.joinMeeting(meetingCode);

        // Assert
        assertTrue(viewModel.joinMeetingRequested.get());
    }

    @Test
    void testJoinMeeting_Failure() throws Exception {
        // Arrange
        String meetingCode = "test-meeting-123";
        mockRpc.registerException("core/joinMeeting", new RuntimeException("Meeting not found"));

        // Act
        viewModel.joinMeeting(meetingCode);

        // Assert
        assertTrue(viewModel.joinMeetingRequested.get());
    }

    @Test
    void testLogout() {
        // Act
        viewModel.logout();

        // Assert
        assertTrue(viewModel.logoutRequested.get());
    }

    @Test
    void testPropertyChangeNotifications() {
        // Arrange
        boolean[] userChanged = {false};
        boolean[] logoutChanged = {false};
        boolean[] startMeetingChanged = {false};
        boolean[] joinMeetingChanged = {false};
        boolean[] meetingCodeChanged = {false};

        viewModel.currentUser.addListener(evt -> userChanged[0] = true);
        viewModel.logoutRequested.addListener(evt -> logoutChanged[0] = true);
        viewModel.startMeetingRequested.addListener(evt -> startMeetingChanged[0] = true);
        viewModel.joinMeetingRequested.addListener(evt -> joinMeetingChanged[0] = true);
        viewModel.meetingCode.addListener(evt -> meetingCodeChanged[0] = true);

        // Act
        viewModel.setCurrentUser(testUser);
        viewModel.logout();
        viewModel.startMeetingRequested.set(true);
        viewModel.joinMeetingRequested.set(true);
        viewModel.meetingCode.set("test-code");

        // Assert
        assertTrue(userChanged[0]);
        assertTrue(logoutChanged[0]);
        assertTrue(startMeetingChanged[0]);
        assertTrue(joinMeetingChanged[0]);
        assertTrue(meetingCodeChanged[0]);
    }
}

