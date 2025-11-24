// package com.swe.ux.integration;

// import java.util.concurrent.CountDownLatch;
// import java.util.concurrent.TimeUnit;

// import javax.swing.SwingUtilities;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertNull;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;

// import com.swe.controller.Meeting.MeetingSession;
// import com.swe.controller.Meeting.ParticipantRole;
// import com.swe.controller.Meeting.SessionMode;
// import com.swe.controller.Meeting.UserProfile;
// import com.swe.controller.serialize.DataSerializer;
// import com.swe.ux.testutil.MockAbstractRPC;
// import com.swe.ux.viewmodels.MainViewModel;
// import com.swe.ux.views.MainPage;

// /**
//  * Integration tests for MainPage and MainViewModel binding.
//  */
// class MainPageViewModelIntegrationTest {

//     private MockAbstractRPC mockRpc;

//     private MainViewModel viewModel;
//     private MainPage mainPage;
//     private UserProfile testUser;

//     @BeforeEach
//     void setUp() throws Exception {
//         mockRpc = new MockAbstractRPC();
//         viewModel = new MainViewModel(mockRpc);
//         testUser = new UserProfile(
//             "test@example.com",
//             "Test User",
//             ParticipantRole.STUDENT
//         );
        
//         // Initialize UI on EDT
//         CountDownLatch latch = new CountDownLatch(1);
//         SwingUtilities.invokeAndWait(() -> {
//             mainPage = new MainPage(viewModel);
//             latch.countDown();
//         });
//         assertTrue(latch.await(2, TimeUnit.SECONDS));
//     }

//     @Test
//     void testMainPage_InitialState() throws Exception {
//         CountDownLatch latch = new CountDownLatch(1);
        
//         SwingUtilities.invokeLater(() -> {
//             assertNotNull(mainPage);
//             assertNull(viewModel.currentUser.get());
//             assertFalse(viewModel.logoutRequested.get());
//             assertEquals("", viewModel.meetingCode.get());
//             latch.countDown();
//         });
        
//         assertTrue(latch.await(2, TimeUnit.SECONDS));
//     }

//     @Test
//     void testMainPage_SetCurrentUser() throws Exception {
//         CountDownLatch latch = new CountDownLatch(1);
        
//         SwingUtilities.invokeLater(() -> {
//             // Act
//             viewModel.setCurrentUser(testUser);

//             // Assert
//             assertNotNull(viewModel.currentUser.get());
//             assertEquals("test@example.com", viewModel.currentUser.get().getEmail());
//             latch.countDown();
//         });
        
//         assertTrue(latch.await(2, TimeUnit.SECONDS));
//     }

//     @Test
//     void testMainPage_StartMeeting() throws Exception {
//         // Arrange
//         MeetingSession session = new MeetingSession("test@example.com", SessionMode.CLASS);
//         byte[] serializedSession = DataSerializer.serialize(session);
//         mockRpc.registerResponse("core/createMeeting", serializedSession);

//         CountDownLatch latch = new CountDownLatch(1);

//         SwingUtilities.invokeLater(() -> {
//             // Act
//             String meetingId = viewModel.startMeeting();

//             // Assert
//             assertNotNull(meetingId);
//             assertEquals(session.getMeetingId(), meetingId);
//             assertEquals(session.getMeetingId(), viewModel.meetingCode.get());
//             // Note: startMeetingRequested is set by the view, not by startMeeting() itself
//             latch.countDown();
//         });
        
//         assertTrue(latch.await(2, TimeUnit.SECONDS));
//     }

//     @Test
//     void testMainPage_JoinMeeting() throws Exception {
//         // Arrange
//         String meetingCode = "test-meeting-123";
//         mockRpc.registerResponse("core/joinMeeting", new byte[0]);

//         CountDownLatch latch = new CountDownLatch(1);

//         SwingUtilities.invokeLater(() -> {
//             // Act
//             viewModel.meetingCode.set(meetingCode);
//             viewModel.joinMeeting(meetingCode);

//             // Assert
//             assertTrue(viewModel.joinMeetingRequested.get());
//             assertEquals(meetingCode, viewModel.meetingCode.get());
//             latch.countDown();
//         });
        
//         assertTrue(latch.await(2, TimeUnit.SECONDS));
//     }

//     @Test
//     void testMainPage_Logout() throws Exception {
//         CountDownLatch latch = new CountDownLatch(1);
        
//         SwingUtilities.invokeLater(() -> {
//             // Act
//             viewModel.logout();

//             // Assert
//             assertTrue(viewModel.logoutRequested.get());
//             latch.countDown();
//         });
        
//         assertTrue(latch.await(2, TimeUnit.SECONDS));
//     }

//     @Test
//     void testMainPage_ButtonStateChanges() throws Exception {
//         CountDownLatch latch = new CountDownLatch(1);
        
//         SwingUtilities.invokeLater(() -> {
//             // Test button enable/disable
//             mainPage.setStartControlsEnabled(false);
//             mainPage.setJoinControlsEnabled(false);
            
//             // Verify buttons exist and can be disabled
//             assertNotNull(mainPage);
            
//             // Re-enable
//             mainPage.setStartControlsEnabled(true);
//             mainPage.setJoinControlsEnabled(true);
            
//             latch.countDown();
//         });
        
//         assertTrue(latch.await(2, TimeUnit.SECONDS));
//     }
// }

