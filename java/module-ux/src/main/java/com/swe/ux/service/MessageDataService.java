package com.swe.ux.service;

import java.util.ArrayList;
import java.util.List;

public class MessageDataService {
    private static final List<String> jsonList = new ArrayList<>();
    private int currentIndex = 0;

    static {
        jsonList.add("""
        ["Developer 1 will handle the backend deployment scripts and ensure the new API endpoints are tested.",
        "Developer 2 will update the UI for the feedback module and push the changes to the staging branch by tonight."]
        """);

        jsonList.add("""
        ["The QA Engineer will start regression testing and prepare the report by Thursday evening."]
        """);

        jsonList.add("""
        ["Team lead has approved the sprint plan for next week.",
        "Developer 3 is working on the database migration scripts.",
        "Designer will provide mockups for the new dashboard by Wednesday."]
        """);

        jsonList.add("""
        ["Code review scheduled for tomorrow at 2 PM.",
        "Please update your task status in the project management tool."]
        """);

        jsonList.add("""
        ["The staging environment will be down for maintenance between 10 PM and midnight tonight."]
        """);

        jsonList.add("""
        ["Security audit report is ready for review.",
        "Developer 4 will implement the authentication improvements.",
        "DevOps team is setting up the new monitoring dashboard.",
        "Client meeting scheduled for Friday at 3 PM."]
        """);
    }

    public String fetchNextData() {
        String response = jsonList.get(currentIndex);
        currentIndex = (currentIndex + 1) % jsonList.size();
        return response;
    }

    public List<String> parseJson(String json) {
        List<String> messages = new ArrayList<>();
        
        try {
            // Remove brackets and split by quotes
            String cleaned = json.trim();
            if (cleaned.startsWith("[")) {
                cleaned = cleaned.substring(1);
            }
            if (cleaned.endsWith("]")) {
                cleaned = cleaned.substring(0, cleaned.length() - 1);
            }
            
            // Split by "," pattern (quote-comma-quote)
            String[] parts = cleaned.split("\",\\s*\"");
            
            for (String part : parts) {
                String message = part.trim();
                // Remove leading/trailing quotes
                if (message.startsWith("\"")) {
                    message = message.substring(1);
                }
                if (message.endsWith("\"")) {
                    message = message.substring(0, message.length() - 1);
                }
                
                if (!message.isEmpty()) {
                    messages.add(message);
                }
            }
        } catch (Exception e) {
            System.out.println("Error parsing messages: " + e.getMessage());
        }
        
        return messages;
    }
}
