/**
 *  Contributed by Swadha.
 */

package com.swe.ux.viewmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.swe.ux.binding.BindableProperty;
import com.swe.controller.Meeting.UserProfile;

/**
 * ViewModel for managing participants in a meeting.
 * Tracks participant count and names from the MeetingViewModel.
 */
public class ParticipantsViewModel extends BaseViewModel {
    private final MeetingViewModel meetingViewModel;
    
    // Bindable properties
    public final BindableProperty<Integer> participantCount = new BindableProperty<>(0, "participantCount");
    public final BindableProperty<List<String>> participantNames = new BindableProperty<>(new ArrayList<>(), "participantNames");
    public final BindableProperty<List<UserProfile>> participants = new BindableProperty<>(new ArrayList<>(), "participants");
    
    /**
     * Creates a new ParticipantsViewModel.
     * @param meetingViewModel The MeetingViewModel to observe for participant changes
     */
    public ParticipantsViewModel(MeetingViewModel meetingViewModel) {
        this.meetingViewModel = meetingViewModel;
        setupBindings();
    }
    
    /**
     * Sets up bindings to observe participant changes from MeetingViewModel.
     */
    private void setupBindings() {
        // Listen to participant changes in MeetingViewModel
        meetingViewModel.participants.addListener(evt -> {
            List<UserProfile> currentParticipants = meetingViewModel.participants.get();
            updateParticipants(currentParticipants);
        });
        
        // Initial update
        updateParticipants(meetingViewModel.participants.get());
    }
    
    /**
     * Updates participant count and names based on the current participants list.
     * @param currentParticipants The current list of participants
     */
    private void updateParticipants(List<UserProfile> currentParticipants) {
        if (currentParticipants == null) {
            currentParticipants = new ArrayList<>();
        }
        
        // Update participants list
        participants.set(new ArrayList<>(currentParticipants));
        
        // Update participant count
        participantCount.set(currentParticipants.size());
        
        // Extract participant names
        List<String> names = currentParticipants.stream()
            .map(UserProfile::getDisplayName)
            .collect(Collectors.toList());
        participantNames.set(names);
    }
    
    /**
     * Gets the current number of participants.
     * @return The participant count
     */
    public int getParticipantCount() {
        return participantCount.get();
    }
    
    /**
     * Gets the list of participant names.
     * @return List of participant display names
     */
    public List<String> getParticipantNames() {
        return new ArrayList<>(participantNames.get());
    }
    
    /**
     * Gets the list of participant User objects.
     * @return List of participants
     */
    public List<UserProfile> getParticipants() {
        return new ArrayList<>(participants.get());
    }
}

