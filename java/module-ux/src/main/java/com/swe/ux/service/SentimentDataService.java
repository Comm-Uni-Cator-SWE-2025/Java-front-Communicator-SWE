/**
 *  Contributed by Kishore.
 */

package com.swe.ux.service;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.controller.serialize.DataSerializer;
import com.swe.ux.model.analytics.SentimentPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Service for fetching and parsing sentiment data from the core module.
 */
public class SentimentDataService {
    /**
     * List of JSON sentiment strings.
     */
    private static final List<String> JSON_LIST = new ArrayList<>();
    /**
     * Current index for cycling through sentiment data.
     */
    private int currentIndex = 0;

    /**
     * Fetches the next sentiment data from the core module.
     *
     * @param rpc the RPC interface to communicate with core
     * @return the sentiment data string
     */
    public String fetchNextData(final AbstractRPC rpc) {
        String data = null;
        try {
            System.out.println("Fetching Sentiment Data from Core Module...");
            // final byte[] json = rpc.call("core/AiSentiment", new byte[0]).get();
            
            String sentiment = "true";
            final byte[] json = sentiment.getBytes(StandardCharsets.UTF_8);
            System.out.println("Received Sentiment Data: " + new String(json));
            if (json != null) {
                data = DataSerializer.deserialize(json, String.class);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
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
     * Parses JSON string to extract sentiment points.
     *
     * @param json the JSON string to parse
     * @return list of sentiment points
     */
    public List<SentimentPoint> parseJson(final String json) {
        final List<SentimentPoint> points = new ArrayList<>();
        
        final String[] parts = json.split("\\{");
        for (final String part : parts) {
            if (part.contains("sentiment")) {
                try {
                    final String timePart = part.split("\"time\":")[1].split(",")[0].trim();
                    final String time = timePart.replace("\"", "").replace("Z", "").trim();

                    final String sentimentPart = part.split("\"sentiment\":")[1].split("}")[0].trim();
                    final double sentiment = Double.parseDouble(sentimentPart.replace(",", ""));

                    points.add(new SentimentPoint(time, sentiment));
                } catch (Exception e) {
                    System.out.println("Error parsing: " + part);
                }
            }
        }
        
        return points;
    }
}
