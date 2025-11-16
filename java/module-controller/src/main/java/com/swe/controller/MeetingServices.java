package com.swe.controller;

import java.util.HashMap;
import java.util.Map;

public class MeetingServices {

    /** Meetings mapped by meetingId. */
    private final Map<String, MeetingSession> meetings = new HashMap<>();

    /**
     * Creates a new meeting (only for instructors).
     *
     * @param userParam logged-in user
     * @return Meeting if created, null if user is not an instructor
     */
    public MeetingSession createMeeting(final UserProfile userParam) {
        if (!"instructor".equals(userParam.getRole())) {
            System.out.println("User is not instructor: " + userParam.getRole());
            return null;
        }
        final MeetingSession meeting = new MeetingSession(userParam.getEmail());
        meetings.put(meeting.getMeetingId(), meeting);
        return meeting;
    }

    /**
     * Joins a meeting using meeting ID.
     *
     * @param userParam      logged-in user
     * @param meetingIdParam meeting ID
     * @return true if joined, false otherwise
     */
    public boolean joinMeeting(final UserProfile userParam, final String meetingIdParam, ClientNode clientNode, ClientNode deviceNode, AbstractController networkController) {
//        this.clientNode = clientNode;
//        this.networkController = networkController;

        networkController.addUser(deviceNode, clientNode);

        return meetings.containsKey(meetingIdParam);
    }
}
