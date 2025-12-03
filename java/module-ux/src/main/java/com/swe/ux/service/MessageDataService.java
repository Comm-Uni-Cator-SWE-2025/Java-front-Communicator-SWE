/**
 *  Contributed by Kishore.
 */

package com.swe.ux.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.controller.serialize.DataSerializer;

/**
 * Service for fetching and parsing message data from the core module.
 */
public class MessageDataService {
    /**
     * List of JSON message strings.
     */
    private static final List<String> JSON_LIST = new ArrayList<>();
    /**
     * Current index for cycling through messages.
     */
    private int currentIndex = 0;

    /**
     * Fetches the next message data from the core module.
     *
     * @param rpc the RPC interface to communicate with core
     * @return the message data string
     */
    public String fetchNextData(final AbstractRPC rpc) {
        String data = null;
        try {
            if (rpc == null) {
                return "";
            }
            System.out.println("Fetching Message Data from Core Module...");
            final byte[] json = rpc.call("core/AiAction", new byte[0]).get();
            if (json != null && json.length > 0) {
                data = DataSerializer.deserialize(json, String.class);
                System.out.println("Received Message Data: " + data);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }

        if (data == null || data.isEmpty()) {
            return "";
        }

        JSON_LIST.add(data);
        final String response = JSON_LIST.get(currentIndex);
        currentIndex = (currentIndex + 1) % JSON_LIST.size();
        return response;
    }

    /**
     * Parses JSON string to extract list of messages.
     *
     * @param json the JSON string to parse
     * @return list of parsed messages
     */
    public List<String> parseJson(final String json) {
        final List<String> messages = new ArrayList<>();

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
            final String[] parts = cleaned.split("\",\\s*\"");

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
