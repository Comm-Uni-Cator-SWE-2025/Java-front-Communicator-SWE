package com.swe.controller;

public class controller {
    public static void main(String[] args) {
        System.out.println("--- Controller Application Starting ---");

        // 1. Create a "dummy" implementation of the network module for testing.
        AbstractController dummyNetworkModule = new DummyNetworkController();

        // 2. Create the AuthService, giving it the dummy network module to use.
        AuthService authService = new AuthService();

        // 3. Simulate a user logging in and triggering the network call.
        ClientNode joiningUserAddress = new ClientNode("192.168.1.10", 54321);
        ClientNode mainServerAddress = new ClientNode("meet_server_ip", 8080);

        UserProfile user = authService.register("teacher@iitpkd.ac.in", "123456789", "Teacher", "https://example.com/logo.png");

        if (user != null) {
            System.out.println("User registered successfully: " + user);
            MeetingServices meetingServices = new MeetingServices();
            MeetingSession meeting = meetingServices.createMeeting(user);
            if (meeting != null) {
                System.out.println("Meeting created successfully with ID: " + meeting.getMeetingId());
                boolean joined = meetingServices.joinMeeting(user, meeting.getMeetingId(), mainServerAddress, joiningUserAddress, dummyNetworkModule);
                if (joined) {
                    System.out.println("User joined the meeting successfully.");
                } else {
                    System.out.println("Failed to join the meeting.");
                }
            } else {
                System.out.println("Failed to create meeting. User might not be an instructor.");
            }
        } else {
            System.out.println("User registration failed.");
        }
        System.out.println("--- Controller Application Finished ---");
    }
}

class DummyNetworkController implements AbstractController {
    @Override
    public void addUser(ClientNode deviceAddress, ClientNode mainServerAddress) {
        System.out.println("\n--- DUMMY NETWORK MODULE ---");
        System.out.println("SUCCESS: The addUser method was called correctly.");
        System.out.printf("Received Device Address: %s:%d\n", deviceAddress.hostName(), deviceAddress.port());
        System.out.printf("Received Main Server Address: %s:%d\n", mainServerAddress.hostName(), mainServerAddress.port());
        System.out.println("--------------------------\n");
    }
}