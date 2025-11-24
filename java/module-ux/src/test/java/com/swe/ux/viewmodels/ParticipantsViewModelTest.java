package com.swe.ux.viewmodels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.swe.controller.Meeting.ParticipantRole;
import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.controller.Meeting.UserProfile;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ParticipantsViewModel}.
 */
class ParticipantsViewModelTest {

    private MeetingViewModel meetingViewModel;
    private ParticipantsViewModel participantsViewModel;

    @BeforeEach
    void setUp() {
        UserProfile instructor = createProfile("owner@example.com", "Owner", ParticipantRole.INSTRUCTOR);
        meetingViewModel = new MeetingViewModel(instructor, new NoopRpc());
        participantsViewModel = new ParticipantsViewModel(meetingViewModel);
    }

    @Test
    void updatesParticipantCountAndNamesWhenMeetingViewModelChanges() {
        UserProfile student1 = createProfile("a@example.com", "Alice", ParticipantRole.STUDENT);
        UserProfile student2 = createProfile("b@example.com", "Bob", ParticipantRole.STUDENT);

        meetingViewModel.participants.set(Arrays.asList(student1, student2));

        assertEquals(2, participantsViewModel.getParticipantCount());
        List<String> names = participantsViewModel.getParticipantNames();
        assertEquals(Arrays.asList("Alice", "Bob"), names);
        assertEquals(2, participantsViewModel.getParticipants().size());
    }

    @Test
    void handlesNullParticipantListGracefully() {
        meetingViewModel.participants.set(null);

        assertEquals(0, participantsViewModel.getParticipantCount());
        assertTrue(participantsViewModel.getParticipantNames().isEmpty());
    }

    private UserProfile createProfile(String email, String name, ParticipantRole role) {
        UserProfile profile = new UserProfile();
        profile.setEmail(email);
        profile.setDisplayName(name);
        profile.setRole(role);
        return profile;
    }

    private static final class NoopRpc implements AbstractRPC {
        @Override
        public void subscribe(String methodName, Function<byte[], byte[]> method) {
            // no-op
        }

        @Override
        public Thread connect(int portNumber) {
            return new Thread();
        }

        @Override
        public CompletableFuture<byte[]> call(String methodName, byte[] data) {
            return CompletableFuture.completedFuture(new byte[0]);
        }
    }
}

