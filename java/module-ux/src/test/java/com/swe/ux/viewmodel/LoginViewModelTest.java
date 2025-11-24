package com.swe.ux.viewmodel;

import java.beans.PropertyChangeListener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.swe.controller.Meeting.ParticipantRole;
import com.swe.controller.Meeting.UserProfile;
import com.swe.controller.serialize.DataSerializer;
import com.swe.ux.testutil.MockAbstractRPC;
import com.swe.ux.viewmodels.LoginViewModel;

/**
 * Unit tests for LoginViewModel.
 */
class LoginViewModelTest {

    private MockAbstractRPC mockRpc;

    private LoginViewModel viewModel;

    @BeforeEach
    void setUp() {
        mockRpc = new MockAbstractRPC();
        viewModel = new LoginViewModel(mockRpc);
    }

    @Test
    void testInitialState() {
        assertFalse(viewModel.isLoading.get());
        assertEquals("", viewModel.authErrorMessage.get());
        assertNull(viewModel.currentUser.get());
    }

    @Test
    void testLoginWithGoogle_Success() throws Exception {
        // Arrange
        UserProfile testUser = new UserProfile(
            "test@example.com",
            "Test User",
            "http://example.com/logo.png",
            ParticipantRole.STUDENT
        );
        byte[] serializedUser = DataSerializer.serialize(testUser);
        mockRpc.registerResponse("core/register", serializedUser);

        // Act
        viewModel.loginWithGoogle();

        // Assert
        assertTrue(viewModel.isLoading.get());
        assertEquals("", viewModel.authErrorMessage.get());
        assertNotNull(viewModel.currentUser.get());
        assertEquals("test@example.com", viewModel.currentUser.get().getEmail());
        assertEquals("Test User", viewModel.currentUser.get().getDisplayName());
    }

    @Test
    void testLoginWithGoogle_Failure() throws Exception {
        // Arrange
        mockRpc.registerException("core/register", new RuntimeException("Network error"));

        // Act
        viewModel.loginWithGoogle();

        // Wait a bit for async operations
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Assert
        assertFalse(viewModel.isLoading.get());
        assertTrue(viewModel.authErrorMessage.get().contains("Authentication failed"));
        assertNull(viewModel.currentUser.get());
    }

    @Test
    void testReset() {
        // Arrange
        viewModel.isLoading.set(true);
        viewModel.authErrorMessage.set("Some error");
        viewModel.currentUser.set(new UserProfile(
            "test@example.com",
            "Test",
            null,
            ParticipantRole.STUDENT
        ));

        // Act
        viewModel.reset();

        // Assert
        assertFalse(viewModel.isLoading.get());
        assertEquals("", viewModel.authErrorMessage.get());
        assertNull(viewModel.currentUser.get());
    }

    @Test
    void testPropertyChangeNotifications() {
        // Arrange
        boolean[] isLoadingChanged = {false};
        boolean[] errorChanged = {false};
        boolean[] userChanged = {false};

        PropertyChangeListener listener = evt -> {
            String propName = evt.getPropertyName();
            if ("isLoading".equals(propName)) {
                isLoadingChanged[0] = true;
            } else if ("authErrorMessage".equals(propName)) {
                errorChanged[0] = true;
            } else if ("currentUser".equals(propName)) {
                userChanged[0] = true;
            }
        };

        viewModel.isLoading.addListener(listener);
        viewModel.authErrorMessage.addListener(listener);
        viewModel.currentUser.addListener(listener);

        // Act
        viewModel.isLoading.set(true);
        viewModel.authErrorMessage.set("Test error");
        viewModel.currentUser.set(new UserProfile(
            "test@example.com",
            "Test",
            null,
            ParticipantRole.STUDENT
        ));

        // Assert
        assertTrue(isLoadingChanged[0]);
        assertTrue(errorChanged[0]);
        assertTrue(userChanged[0]);
    }
}

