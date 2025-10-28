package com.swe.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

/**
 * Implements communication with the remote Cloud Storage module's API.
 */
public class CloudStorageAdapter {

    /** HTTP client for making requests to the cloud API. */
    private final HttpClient httpClient;
    
    /** JSON object mapper for serialization/deserialization. */
    private final ObjectMapper objectMapper;
    
    /** Base URL for the cloud API. */
    private final String cloudApiBaseUrl;
    
    /** HTTP status code for successful creation. */
    private static final int HTTP_CREATED = 201;
    
    /** HTTP status code for successful request. */
    private static final int HTTP_OK = 200;

    /**
     * Constructs a new CloudStorageAdapter with the specified base URL.
     *
     * @param cloudApiBaseUrlParam base URL for the cloud API
     */
    public CloudStorageAdapter(final String cloudApiBaseUrlParam) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.cloudApiBaseUrl = cloudApiBaseUrlParam;
    }

    /**
     * Asks the Cloud module to create a new meeting session.
     * Corresponds to: POST /api/sessions
     *
     * @param hostProfile The profile of the user starting the meeting.
     * @return An Optional containing the created MeetingSession, or empty on failure.
     */
    /**
     * Creates a new meeting session in the cloud storage.
     *
     * @param hostProfile The profile of the meeting host
     * @return Optional containing the created MeetingSession if successful, empty otherwise
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the operation is interrupted
     */
    public Optional<MeetingSession> createMeetingSession(final UserProfile hostProfile) 
            throws IOException, InterruptedException {
        // The Cloud API likely expects the host's info in the request body.
        final String requestBody = objectMapper.writeValueAsString(hostProfile);
        final String requestUrl = cloudApiBaseUrl + "/api/sessions";

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + getCloudServiceAuthToken())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        final HttpResponse<String> response = httpClient.send(
                request, 
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() == HTTP_CREATED) {
            final MeetingSession session = objectMapper.readValue(
                    response.body(), 
                    MeetingSession.class
            );
            return Optional.of(session);
        }
        return Optional.empty();
    }

    /**
     * Retrieves a meeting session by its ID.
     *
     * @param sessionIdParam The ID of the session to retrieve
     * @return Optional containing the MeetingSession if found, empty otherwise
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the operation is interrupted
     */
    public Optional<MeetingSession> getMeetingSession(final String sessionIdParam) 
            throws IOException, InterruptedException {
                
        final String requestUrl = cloudApiBaseUrl + "/api/sessions/" + sessionIdParam;
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .header("Authorization", "Bearer " + getCloudServiceAuthToken())
                .GET()
                .build();

        final HttpResponse<String> response = httpClient.send(
                request, 
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() == HTTP_OK) {
            final MeetingSession session = objectMapper.readValue(
                    response.body(), 
                    MeetingSession.class
            );
            return Optional.of(session);
        }
        return Optional.empty();
    }

    private String getCloudServiceAuthToken() {
        return "super-secret-service-to-service-token";
    }
}

