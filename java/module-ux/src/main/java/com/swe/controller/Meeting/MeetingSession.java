package com.swe.controller.Meeting;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a meeting created by an instructor.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeetingSession {
    /** Unique meeting ID. */
    @JsonProperty("meetingId")
    private final String meetingId;

    /** Email of the instructor who created the meeting. */
    @JsonProperty("createdBy")
    private final String createdBy;

    /** Time the meeting was created. */
    @JsonProperty("createdAt")
    private final long createdAt;

    /** Session mode: TEST or CLASS. */
    @JsonProperty("sessionMode")
    private final SessionMode sessionMode;

    /** Map of participants by email. */
    @JsonProperty("participants")
    private final Map<String, UserProfile> participants = new ConcurrentHashMap<>();

    /**
     * Creates a new meeting with a unique ID.
     *
     * @param createdByParam email of the instructor who created the meeting
     * @param mode the session mode
     */
    public MeetingSession(final String createdByParam, final SessionMode mode) {
        this.sessionMode = mode;
        this.meetingId = UUID.randomUUID().toString(); // generate unique ID
        this.createdBy = createdByParam;
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * Creates a meeting session from JSON deserialization.
     *
     * @param mtgId the meeting ID
     * @param creator the creator's email
     * @param creationTime the creation timestamp
     * @param mode the session mode
     * @param participantMap the participants map
     */
    @JsonCreator
    public MeetingSession(
            @JsonProperty("meetingId") final String mtgId,
            @JsonProperty("createdBy") final String creator,
            @JsonProperty("createdAt") final long creationTime,
            @JsonProperty("sessionMode") final SessionMode mode,
            @JsonProperty("participants") final Map<String, UserProfile> participantMap) {
        this.meetingId = mtgId;
        this.createdBy = creator;
        this.createdAt = creationTime;
        this.sessionMode = mode;
    }

    /**
     * Gets the meeting ID.
     *
     * @return the meeting ID
     */
    public String getMeetingId() {
        return this.meetingId;
    }

    /**
     * Gets the creator's email.
     *
     * @return the creator's email
     */
    public String getCreatedBy() {
        return this.createdBy;
    }

    /**
     * Gets the creation timestamp.
     *
     * @return the creation timestamp
     */
    public long getCreatedAt() {
        return this.createdAt;
    }

    /**
     * Gets the session mode.
     *
     * @return the session mode
     */
    public SessionMode getSessionMode() {
        return this.sessionMode;
    }

    /**
     * Gets a participant by email ID.
     *
     * @param emailId the email ID of the participant
     * @return the user profile of the participant
     */
    public UserProfile getParticipant(final String emailId) {
        return this.participants.get(emailId);
    }

    /**
     * Gets all participants.
     *
     * @return map of all participants
     */
    public Map<String, UserProfile> getParticipants() {
        return this.participants;
    }

    /**
     * Adds a participant to this session's in-memory list.
     *
     * @param p The participant to add.
     */
    public void addParticipant(final UserProfile p) {
        if (p != null && p.getEmail() != null) {
            this.participants.put(p.getEmail(), p);
        }
    }
}