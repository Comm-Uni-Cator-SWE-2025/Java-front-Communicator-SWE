package com.swe.controller.Auth;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;

/**
 * Helper class to handle Google OAuth login.
 */
public class AuthHelper {

    /** Google API URL to fetch user info. */
    private static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    /**
     * Constructor.
     */
    public AuthHelper() {}

    /**
     * Handles Google login using a Credential object.
     *
     * @param credential OAuth2 credential containing access token
     * @return GoogleUserInfo containing user information
     * @throws IOException if HTTP request or JSON parsing fails
     */
    public GoogleUserInfo handleGoogleLogin(final Credential credential) throws IOException {

        final HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory(credential);

        final GenericUrl url = new GenericUrl(GOOGLE_USERINFO_URL);

        final HttpRequest request = requestFactory.buildGetRequest(url);
        final HttpResponse response = request.execute();

        final ObjectMapper mapper = new ObjectMapper();
        @SuppressWarnings("unchecked")
        final Map<String, Object> userInfo = (Map<String, Object>) mapper.readValue(response.getContent(), Map.class);

        final String email = (String) userInfo.get("email");
        final String name = (String) userInfo.get("name");
        final String logoUrl = (String) userInfo.get("picture");

        return new GoogleUserInfo(email, name, logoUrl);
    }
    
    /**
     * Simple data class to hold Google user information.
     */
    public static class GoogleUserInfo {
        private final String email;
        private final String name;
        private final String logoUrl;
        
        public GoogleUserInfo(String email, String name, String logoUrl) {
            this.email = email;
            this.name = name;
            this.logoUrl = logoUrl;
        }
        
        public String getEmail() {
            return email;
        }
        
        public String getName() {
            return name;
        }
        
        public String getLogoUrl() {
            return logoUrl;
        }
    }
}
