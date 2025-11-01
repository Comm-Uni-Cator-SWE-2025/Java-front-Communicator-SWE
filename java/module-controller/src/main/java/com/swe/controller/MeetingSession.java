package com.swe.controller;

import java.util.UUID;

/**
 * Represents a meeting created by an instructor.
 */
public class MeetingSession {
    /** Unique meeting ID. */
    private final String meetingId;

    /** Email of the instructor who created the meeting. */
    private final String createdBy;

    /** Time the meeting was created. */
    private final long createdAt;

    /**
     * Creates a new meeting with a unique ID.
     *
     * @param createdByParam email of the instructor who created the meeting
     */
    public MeetingSession(final String createdByParam) {
        this.meetingId = UUID.randomUUID().toString(); // generate unique ID
        this.createdBy = createdByParam;
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * Gets the unique meeting ID.
     *
     * @return meetingId
     */
    public String getMeetingId() {
        return meetingId;
    }

    /**
     * Gets the creator of the meeting.
     *
     * @return createdBy email
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * Gets the creation time of the meeting.
     *
     * @return createdAt timestamp
     */
    public long getCreatedAt() {
        return createdAt;
    }
}