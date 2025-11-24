/**
 *  Contributed by Swadha.
 */

package com.swe.ux.viewmodels;

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
    /** Meeting view model. */
    private final MeetingViewModel meetingViewModel;
    
    /** Participant count property. */
    private final BindableProperty<Integer> participantCount = new BindableProperty<>(0, "participantCount");
    /** Participant names property. */
    private final BindableProperty<List<String>> participantNames = 
        new BindableProperty<>(new ArrayList<>(), "participantNames");
    /** Participants property. */
    private final BindableProperty<List<UserProfile>> participants = 
        new BindableProperty<>(new ArrayList<>(), "participants");
    
    /**
     * Creates a new ParticipantsViewModel.
     * @param meetingViewModelParam The MeetingViewModel to observe for participant changes
     */
    public ParticipantsViewModel(final MeetingViewModel meetingViewModelParam) {
        this.meetingViewModel = meetingViewModelParam;
        setupBindings();
    }
    
    /**
     * Sets up bindings to observe participant changes from MeetingViewModel.
     */
    private void setupBindings() {
        // Listen to participant changes in MeetingViewModel
        meetingViewModel.getParticipants().addListener(evt -> {
            final List<UserProfile> currentParticipants = meetingViewModel.getParticipants().get();
            updateParticipants(currentParticipants);
        });
        
        // Initial update
        updateParticipants(meetingViewModel.getParticipants().get());
    }
    
    /**
     * Updates participant count and names based on the current participants list.
     * @param currentParticipants The current list of participants
     */
    private void updateParticipants(final List<UserProfile> currentParticipants) {
        final List<UserProfile> participantList;
        if (currentParticipants == null) {
            participantList = new ArrayList<>();
        } else {
            participantList = currentParticipants;
        }
        
        // Update participants list
        participants.set(new ArrayList<>(participantList));
        
        // Update participant count
        participantCount.set(participantList.size());
        
        // Extract participant names
        final List<String> names = participantList.stream()
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
     * Gets the participant count property.
     * @return The participant count property
     */
    public BindableProperty<Integer> getParticipantCountProperty() {
        return participantCount;
    }
    
    /**
     * Gets the list of participant names.
     * @return List of participant display names
     */
    public List<String> getParticipantNames() {
        return new ArrayList<>(participantNames.get());
    }

    /**
     * Gets the participant names property.
     * @return The participant names property
     */
    public BindableProperty<List<String>> getParticipantNamesProperty() {
        return participantNames;
    }
    
    /**
     * Gets the list of participant User objects.
     * @return List of participants
     */
    public List<UserProfile> getParticipants() {
        return new ArrayList<>(participants.get());
    }

    /**
     * Gets the participants property.
     * @return The participants property
     */
    public BindableProperty<List<UserProfile>> getParticipantsProperty() {
        return participants;
    }
}

