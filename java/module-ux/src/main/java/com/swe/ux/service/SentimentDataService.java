/**
 *  Contributed by Kishore.
 */

package com.swe.ux.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.controller.serialize.DataSerializer;
import com.swe.ux.model.analytics.SentimentPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SentimentDataService {
    private static final List<String> jsonList = new ArrayList<>();
    private int currentIndex = 0;

    public String fetchNextData(AbstractRPC rpc) {
        String data = null;
        try {
            System.out.println("Fetching Sentiment Data from Core Module...");
            byte[] json = rpc.call("core/AiSentiment", new byte[0]).get();
            System.out.println("Received Sentiment Data: " + new String(json));
            if(json != null){
                data = DataSerializer.deserialize(json, String.class);
            }
        } catch (InterruptedException | ExecutionException | JsonProcessingException e) {
            e.printStackTrace();
        }

        if(data == null || data.isEmpty()) {
            return "";
        }
        
        jsonList.add(data);

        String response = jsonList.get(currentIndex);
        currentIndex = (currentIndex + 1) % jsonList.size();
        return response;
    }

    public List<SentimentPoint> parseJson(String json) {
        List<SentimentPoint> points = new ArrayList<>();
        
        String[] parts = json.split("\\{");
        for (String part : parts) {
            if (part.contains("sentiment")) {
                try {
                    String timePart = part.split("\"time\":")[1].split(",")[0].trim();
                    String time = timePart.replace("\"", "").replace("Z", "").trim();

                    String sentimentPart = part.split("\"sentiment\":")[1].split("}")[0].trim();
                    double sentiment = Double.parseDouble(sentimentPart.replace(",", ""));

                    points.add(new SentimentPoint(time, sentiment));
                } catch (Exception e) {
                    System.out.println("Error parsing: " + part);
                }
            }
        }
        
        return points;
    }
}
