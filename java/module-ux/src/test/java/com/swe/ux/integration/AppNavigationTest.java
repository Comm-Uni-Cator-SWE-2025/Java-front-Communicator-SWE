package com.swe.ux.integration;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.swe.controller.Meeting.ParticipantRole;
import com.swe.controller.Meeting.UserProfile;
import com.swe.ux.App;
import com.swe.ux.testutil.MockAbstractRPC;

/**
 * Integration tests for App navigation between views.
 */
class AppNavigationTest {

    private MockAbstractRPC mockRpc;

    private App app;
    private UserProfile testUser;

    @BeforeEach
    void setUp() throws Exception {
        mockRpc = new MockAbstractRPC();
        testUser = new UserProfile(
            "test@example.com",
            "Test User",
            ParticipantRole.STUDENT
        );
        
        // Initialize app on EDT
        CountDownLatch latch = new CountDownLatch(1);
        SwingUtilities.invokeAndWait(() -> {
            app = App.getInstance(mockRpc);
            latch.countDown();
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testApp_InitialView() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        SwingUtilities.invokeLater(() -> {
            assertNotNull(app);
            // App should start with login view, currentUser should be null initially
            assertNull(app.getCurrentUser()); // Should be null initially
            latch.countDown();
        });
        
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testApp_ShowView() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        SwingUtilities.invokeLater(() -> {
            // Act
            app.showView(App.LOGIN_VIEW);
            app.showView(App.MAIN_VIEW);
            app.showView(App.MEETING_VIEW);

            // Assert - should not throw
            assertDoesNotThrow(() -> {
                app.showView(App.LOGIN_VIEW);
            });
            
            latch.countDown();
        });
        
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testApp_SetCurrentUser() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        SwingUtilities.invokeLater(() -> {
            // Act
            app.setCurrentUser(testUser);

            // Assert
            assertNotNull(app.getCurrentUser());
            assertEquals("test@example.com", app.getCurrentUser().getEmail());
            
            latch.countDown();
        });
        
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testApp_SetCurrentUser_Null() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        SwingUtilities.invokeLater(() -> {
            // Arrange
            app.setCurrentUser(testUser);
            assertNotNull(app.getCurrentUser());

            // Act
            app.setCurrentUser(null);

            // Assert
            assertNull(app.getCurrentUser());
            
            latch.countDown();
        });
        
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testApp_NavigateBack() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        SwingUtilities.invokeLater(() -> {
            // Arrange - navigate through views
            app.showView(App.LOGIN_VIEW);
            app.showView(App.MAIN_VIEW);
            app.showView(App.MEETING_VIEW);

            // Act
            app.navigateBack();

            // Assert - should navigate back
            assertDoesNotThrow(() -> app.navigateBack());
            
            latch.countDown();
        });
        
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testApp_NavigateBack_EmptyHistory() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        SwingUtilities.invokeLater(() -> {
            // Act - navigate back with minimal history
            app.navigateBack();

            // Assert - should not throw
            assertDoesNotThrow(() -> app.navigateBack());
            
            latch.countDown();
        });
        
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testApp_SingletonPattern() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        SwingUtilities.invokeLater(() -> {
            // Act
            App instance1 = App.getInstance(mockRpc);
            App instance2 = App.getInstance(mockRpc);

            // Assert
            assertSame(instance1, instance2);
            
            latch.countDown();
        });
        
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }
}

