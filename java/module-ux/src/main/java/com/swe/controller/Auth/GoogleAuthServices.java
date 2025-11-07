package com.swe.controller.Auth;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * Service class to handle Google OAuth2 authentication.
 * Provides method to obtain OAuth2 credentials for the user.
 */
public class GoogleAuthServices {

    /** Port for the local server receiver used during OAuth flow. */
    private static final int LOCAL_RECEIVER_PORT = 8888;

    /** Directory path to store OAuth2 tokens. */
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /** JSON factory used by Google APIs. */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /** OAuth2 scopes required by the application. */
    private static final List<String> SCOPES = List.of(
            "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/userinfo.email"
    );

    /** Path to the client credentials JSON file. */
    private static final String CREDENTIALS_FILE_PATH = "/client_secret.json";

    /**
     * Returns a Google OAuth Credential object.
     * 
     * IMPORTANT: To allow external users (non-organization emails) to login,
     * the OAuth consent screen in Google Cloud Console must be set to "External" user type.
     * 
     * Steps to fix "Error 403: org_internal":
     * 1. Go to https://console.cloud.google.com/
     * 2. Select project: trusty-relic-467915-m0
     * 3. Navigate to: APIs & Services > OAuth consent screen
     * 4. Change User Type from "Internal" to "External"
     * 5. Fill required fields and save
     *
     * @return Credential object with access and refresh tokens
     * @throws IOException              if credentials file is missing or cannot be read
     * @throws GeneralSecurityException if transport setup fails
     */
    public Credential getCredentials() throws IOException, GeneralSecurityException {
        final InputStream in = GoogleAuthServices.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }

        final GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JSON_FACTORY,
                new InputStreamReader(in)
        );

        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                JSON_FACTORY,
                clientSecrets,
                SCOPES
        )
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        final LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(LOCAL_RECEIVER_PORT)
                .build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
    
    /**
     * Clears stored OAuth2 tokens to force account selection on next login.
     * This allows users to choose a different Google account.
     * 
     * @throws IOException if token directory cannot be deleted
     */
    public void clearStoredTokens() throws IOException {
        File tokensDir = new File(TOKENS_DIRECTORY_PATH);
        if (tokensDir.exists() && tokensDir.isDirectory()) {
            // Delete all files in the tokens directory
            File[] files = tokensDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file.toPath());
                    } else {
                        file.delete();
                    }
                }
            }
            // Delete the tokens directory itself
            tokensDir.delete();
        }
    }
    
    /**
     * Recursively deletes a directory and all its contents.
     * 
     * @param directory The directory path to delete
     * @throws IOException if deletion fails
     */
    private void deleteDirectory(Path directory) throws IOException {
        if (Files.exists(directory)) {
            Files.walk(directory)
                .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        // Log but don't throw - continue deleting other files
                        System.err.println("Failed to delete: " + path + " - " + e.getMessage());
                    }
                });
        }
    }
    
    /**
     * Returns a Google OAuth Credential object, forcing account selection.
     * This clears stored tokens before authentication to ensure the user can choose a different account.
     *
     * @param forceAccountSelection If true, clears stored tokens to force account selection
     * @return Credential object with access and refresh tokens
     * @throws IOException              if credentials file is missing or cannot be read
     * @throws GeneralSecurityException if transport setup fails
     */
    public Credential getCredentials(boolean forceAccountSelection) throws IOException, GeneralSecurityException {
        if (forceAccountSelection) {
            clearStoredTokens();
        }
        return getCredentials();
    }
}
