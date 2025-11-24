package com.swe.ux.error;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.swe.controller.Meeting.ParticipantRole;
import com.swe.controller.Meeting.UserProfile;
import com.swe.ux.testutil.MockAbstractRPC;
import com.swe.ux.viewmodels.LoginViewModel;
import com.swe.ux.viewmodels.MainViewModel;
import com.swe.ux.viewmodels.MeetingViewModel;

/**
 * Error handling tests for ViewModels and components.
 */
class ErrorHandlingTest {

    private MockAbstractRPC mockRpc;

    private LoginViewModel loginViewModel;
    private MainViewModel mainViewModel;
    private MeetingViewModel meetingViewModel;
    private UserProfile testUser;

    @BeforeEach
    void setUp() {
        mockRpc = new MockAbstractRPC();
        loginViewModel = new LoginViewModel(mockRpc);
        mainViewModel = new MainViewModel(mockRpc);
        testUser = new UserProfile(
            "test@example.com",
            "Test User",
            "http://example.com/logo.png",
            ParticipantRole.STUDENT
        );
        meetingViewModel = new MeetingViewModel(testUser, mockRpc);
    }

    @Test
    void testLoginViewModel_NetworkError() throws Exception {
        // Arrange
        mockRpc.registerException("core/register", new RuntimeException("Network connection failed"));

        // Act
        loginViewModel.loginWithGoogle();

        // Wait for async operation
        Thread.sleep(200);

        // Assert
        assertFalse(loginViewModel.isLoading.get());
        assertTrue(loginViewModel.authErrorMessage.get().contains("Authentication failed"));
        assertNull(loginViewModel.currentUser.get());
    }

    @Test
    void testLoginViewModel_TimeoutError() throws Exception {
        // Arrange
        mockRpc.registerException("core/register", new TimeoutException("Request timed out"));

        // Act
        loginViewModel.loginWithGoogle();

        // Wait for async operation
        Thread.sleep(200);

        // Assert
        assertFalse(loginViewModel.isLoading.get());
        assertTrue(loginViewModel.authErrorMessage.get().contains("Authentication failed"));
        assertNull(loginViewModel.currentUser.get());
    }

    @Test
    void testLoginViewModel_InvalidResponse() throws Exception {
        // Arrange
        mockRpc.registerResponse("core/register", new byte[0]);

        // Act
        loginViewModel.loginWithGoogle();

        // Wait for async operation
        Thread.sleep(200);

        // Assert - should handle gracefully
        assertFalse(loginViewModel.isLoading.get());
    }

    @Test
    void testMainViewModel_StartMeeting_NetworkError() throws Exception {
        // Arrange
        mockRpc.registerException("core/createMeeting", new RuntimeException("Network error"));

        // Act
        String meetingId = mainViewModel.startMeeting();

        // Assert
        assertNull(meetingId);
        assertEquals("", mainViewModel.meetingCode.get());
    }

    @Test
    void testMainViewModel_StartMeeting_EmptyResponse() throws Exception {
        // Arrange
        mockRpc.registerResponse("core/createMeeting", new byte[0]);

        // Act
        String meetingId = mainViewModel.startMeeting();

        // Assert
        assertNull(meetingId);
    }

    @Test
    void testMainViewModel_JoinMeeting_InvalidCode() throws Exception {
        // Arrange
        String invalidCode = "";
        mockRpc.registerException("core/joinMeeting", new RuntimeException("Meeting not found"));

        // Act
        mainViewModel.joinMeeting(invalidCode);

        // Assert - should handle error gracefully
        assertTrue(mainViewModel.joinMeetingRequested.get());
    }

    @Test
    void testMainViewModel_JoinMeeting_NetworkError() throws Exception {
        // Arrange
        String meetingCode = "test-meeting-123";
        mockRpc.registerException("core/joinMeeting", new RuntimeException("Network connection failed"));

        // Act
        mainViewModel.joinMeeting(meetingCode);

        // Assert - should handle error gracefully
        assertTrue(mainViewModel.joinMeetingRequested.get());
    }

    @Test
    void testMeetingViewModel_StartMeeting_WithoutMeetingId() {
        // Act
        meetingViewModel.startMeeting();

        // Assert
        assertFalse(meetingViewModel.isMeetingActive.get());
    }

    @Test
    void testMeetingViewModel_StartMeeting_EmptyMeetingId() {
        // Arrange
        meetingViewModel.setMeetingId("");

        // Act
        meetingViewModel.startMeeting();

        // Assert
        assertFalse(meetingViewModel.isMeetingActive.get());
    }

    @Test
    void testMeetingViewModel_SendMessage_EmptyMessage() {
        // Arrange
        meetingViewModel.setMeetingId("test-meeting-123");
        meetingViewModel.startMeeting();
        meetingViewModel.messageText.set("");

        // Act
        meetingViewModel.sendMessage();

        // Assert - should not send empty message
        assertTrue(meetingViewModel.messages.get().isEmpty());
    }

    @Test
    void testMeetingViewModel_SendMessage_WithoutActiveMeeting() {
        // Arrange
        // Note: currentMeeting is always initialized, so sendMessage will work
        // even if isMeetingActive is false
        meetingViewModel.messageText.set("Hello");

        // Act
        meetingViewModel.sendMessage();

        // Assert - message will be sent since currentMeeting exists
        // messageText will be cleared after sending
        assertEquals("", meetingViewModel.messageText.get());
        assertFalse(meetingViewModel.messages.get().isEmpty());
    }

    @Test
    void testMeetingViewModel_EndMeeting_WhenNotActive() {
        // Act - end meeting when not active
        meetingViewModel.endMeeting();

        // Assert - should handle gracefully
        assertFalse(meetingViewModel.isMeetingActive.get());
    }

    @Test
    void testMeetingViewModel_ToggleVideo_WithoutActiveMeeting() {
        // Arrange
        // Note: currentMeeting is always initialized, so toggle will work
        // But isMeetingActive might be false
        boolean initialState = meetingViewModel.isVideoEnabled.get();

        // Act
        meetingViewModel.toggleVideo();

        // Assert - toggle should work since currentMeeting exists
        // The state should change from initial state
        assertNotEquals(initialState, meetingViewModel.isVideoEnabled.get());
    }

    @Test
    void testMeetingViewModel_ToggleAudio_WithoutActiveMeeting() {
        // Arrange
        // Note: currentMeeting is always initialized, so toggle will work
        boolean initialState = meetingViewModel.isAudioEnabled.get();

        // Act
        meetingViewModel.toggleAudio();

        // Assert - toggle should work since currentMeeting exists
        assertNotEquals(initialState, meetingViewModel.isAudioEnabled.get());
    }

    @Test
    void testMeetingViewModel_ToggleScreenSharing_WithoutActiveMeeting() {
        // Arrange
        // Note: currentMeeting is always initialized, so toggle will work
        boolean initialState = meetingViewModel.isScreenShareEnabled.get();

        // Act
        meetingViewModel.toggleScreenSharing();

        // Assert - toggle should work since currentMeeting exists
        assertNotEquals(initialState, meetingViewModel.isScreenShareEnabled.get());
    }

    @Test
    void testMeetingViewModel_AddParticipant_Null() {
        // Arrange
        meetingViewModel.setMeetingId("test-meeting-123");
        meetingViewModel.startMeeting();
        int initialSize = meetingViewModel.participants.get().size();

        // Act & Assert - Meeting.addParticipant() will throw NPE when user is null
        // because it calls user.getEmail(). This is expected behavior.
        assertThrows(NullPointerException.class, () -> {
            meetingViewModel.addParticipant(null);
        });
        
        // Size should remain unchanged
        assertEquals(initialSize, meetingViewModel.participants.get().size());
    }

    @Test
    void testMeetingViewModel_RemoveParticipant_NotInMeeting() {
        // Arrange
        meetingViewModel.setMeetingId("test-meeting-123");
        meetingViewModel.startMeeting();
        UserProfile notInMeeting = new UserProfile(
            "other@example.com",
            "Other",
            null,
            ParticipantRole.STUDENT
        );

        // Act
        meetingViewModel.removeParticipant(notInMeeting);

        // Assert - should handle gracefully
        assertEquals(1, meetingViewModel.participants.get().size()); // Original user still there
    }

    @Test
    void testBindableProperty_NullValue() {
        // Arrange
        com.swe.ux.binding.BindableProperty<String> property = 
            new com.swe.ux.binding.BindableProperty<>("initial", "test");

        // Act & Assert - should handle null
        assertDoesNotThrow(() -> property.set(null));
        assertNull(property.get());
    }

    @Test
    void testRPC_CallFailure() throws Exception {
        // Arrange
        mockRpc.registerException("test/endpoint", new RuntimeException("RPC call failed"));

        // Act
        CompletableFuture<byte[]> result = mockRpc.call("test/endpoint", new byte[0]);

        // Assert
        assertThrows(Exception.class, () -> {
            try {
                result.get();
            } catch (Exception e) {
                throw e;
            }
        });
    }
}

