package com.swe.ux.integration;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

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
import com.swe.ux.views.LoginPage;

/**
 * Integration tests for LoginPage and LoginViewModel binding.
 */
class LoginPageViewModelIntegrationTest {

    private MockAbstractRPC mockRpc;

    private LoginViewModel viewModel;
    private LoginPage loginPage;

    @BeforeEach
    void setUp() throws Exception {
        mockRpc = new MockAbstractRPC();
        viewModel = new LoginViewModel(mockRpc);
        
        // Initialize UI on EDT
        CountDownLatch latch = new CountDownLatch(1);
        SwingUtilities.invokeAndWait(() -> {
            loginPage = new LoginPage(viewModel);
            latch.countDown();
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testLoginPage_InitialState() {
        SwingUtilities.invokeLater(() -> {
            assertNotNull(loginPage);
            assertFalse(viewModel.isLoading.get());
            assertEquals("", viewModel.authErrorMessage.get());
            assertNull(viewModel.currentUser.get());
        });
    }

    @Test
    void testLoginPage_ViewModelPropertyChanges() throws Exception {
        // Arrange
        UserProfile testUser = new UserProfile(
            "test@example.com",
            "Test User",
            "http://example.com/logo.png",
            ParticipantRole.STUDENT
        );
        byte[] serializedUser = DataSerializer.serialize(testUser);
        mockRpc.registerResponse("core/register", serializedUser);

        CountDownLatch latch = new CountDownLatch(1);

        // Act
        SwingUtilities.invokeLater(() -> {
            viewModel.loginWithGoogle();
            
            // Wait a bit for async operations
            new Thread(() -> {
                try {
                    Thread.sleep(200);
                    latch.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS));

        // Assert
        SwingUtilities.invokeLater(() -> {
            assertNotNull(viewModel.currentUser.get());
            assertEquals("test@example.com", viewModel.currentUser.get().getEmail());
        });
    }

    @Test
    void testLoginPage_ErrorState() throws Exception {
        // Arrange
        mockRpc.registerException("core/register", new RuntimeException("Network error"));

        CountDownLatch latch = new CountDownLatch(1);

        // Act
        SwingUtilities.invokeLater(() -> {
            viewModel.loginWithGoogle();
            
            new Thread(() -> {
                try {
                    Thread.sleep(200);
                    latch.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS));

        // Assert
        SwingUtilities.invokeLater(() -> {
            assertFalse(viewModel.isLoading.get());
            assertTrue(viewModel.authErrorMessage.get().contains("Authentication failed"));
            assertNull(viewModel.currentUser.get());
        });
    }

    @Test
    void testLoginPage_Reset() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        SwingUtilities.invokeLater(() -> {
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
            
            latch.countDown();
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }
}
