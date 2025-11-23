/**
 *  Contributed by Kishore.
 */

package com.swe.ux.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.controller.serialize.DataSerializer;

public class MessageDataService {
    private static final List<String> jsonList = new ArrayList<>();
    private int currentIndex = 0;

    public String fetchNextData(AbstractRPC rpc) {
        String data = null;
        try {
            System.out.println("Fetching Message Data from Core Module...");
            byte[] json = rpc.call("core/AiAction", new byte[0]).get();
            System.out.println("Received Message Data: " + new String(json));
            if (json != null) {
                data = DataSerializer.deserialize(json, String.class);
            }
        } catch (InterruptedException | ExecutionException | JsonProcessingException e) {
            e.printStackTrace();
        }

        if (data == null || data.isEmpty()) {
            return "";
        }

        jsonList.add(data);
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
