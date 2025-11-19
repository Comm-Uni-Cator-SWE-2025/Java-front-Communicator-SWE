package com.swe.ux.service;

import com.swe.ux.model.analytics.SentimentPoint;

import java.util.ArrayList;
import java.util.List;

public class SentimentDataService {
    private static final List<String> jsonList = new ArrayList<>();
    private int currentIndex = 0;

    static {
        jsonList.add("""
        [
            {"time": "2025-11-07T10:00:00Z", "sentiment": 7.0},
            {"time": "2025-11-07T10:01:45Z", "sentiment": 3.0},
            {"time": "2025-11-07T10:03:20Z", "sentiment": -3.0},
            {"time": "2025-11-07T10:04:50Z", "sentiment": 2.0},
            {"time": "2025-11-07T10:06:10Z", "sentiment": 6.0},
            {"time": "2025-11-07T10:07:30Z", "sentiment": 4.0},
            {"time": "2025-11-07T10:08:55Z", "sentiment": 7.0},
            {"time": "2025-11-07T10:10:22Z", "sentiment": 8.0},
            {"time": "2025-11-07T10:12:40Z", "sentiment": -2.0},
            {"time": "2025-11-07T10:14:00Z", "sentiment": 3.0}
        ]
        """);

        jsonList.add("""
        [
            {"time": "2025-11-07T10:15:00Z", "sentiment": 5.0},
            {"time": "2025-11-07T10:16:32Z", "sentiment": 1.5},
            {"time": "2025-11-07T10:17:58Z", "sentiment": -4.0},
            {"time": "2025-11-07T10:19:20Z", "sentiment": 2.8},
            {"time": "2025-11-07T10:20:45Z", "sentiment": 6.2},
            {"time": "2025-11-07T10:22:10Z", "sentiment": -1.0},
            {"time": "2025-11-07T10:23:35Z", "sentiment": 4.7},
            {"time": "2025-11-07T10:25:00Z", "sentiment": 7.5},
            {"time": "2025-11-07T10:27:15Z", "sentiment": -2.3},
            {"time": "2025-11-07T10:29:40Z", "sentiment": 3.9}
        ]
        """);

        jsonList.add("""
        [
            {"time": "2025-11-07T10:30:10Z", "sentiment": 4.2},
            {"time": "2025-11-07T10:31:25Z", "sentiment": 2.7},
            {"time": "2025-11-07T10:32:40Z", "sentiment": -1.8},
            {"time": "2025-11-07T10:34:05Z", "sentiment": 3.3},
            {"time": "2025-11-07T10:35:30Z", "sentiment": 6.0},
            {"time": "2025-11-07T10:36:55Z", "sentiment": 1.1},
            {"time": "2025-11-07T10:38:20Z", "sentiment": 5.4},
            {"time": "2025-11-07T10:40:00Z", "sentiment": 7.1},
            {"time": "2025-11-07T10:42:10Z", "sentiment": -3.2},
            {"time": "2025-11-07T10:44:45Z", "sentiment": 2.9}
        ]
        """);

        jsonList.add("""
        [
            {"time": "2025-11-07T10:46:00Z", "sentiment": 3.4},
            {"time": "2025-11-07T10:47:22Z", "sentiment": -0.7},
            {"time": "2025-11-07T10:48:55Z", "sentiment": 4.9},
            {"time": "2025-11-07T10:50:33Z", "sentiment": 6.8},
            {"time": "2025-11-07T10:52:05Z", "sentiment": 2.1},
            {"time": "2025-11-07T10:53:40Z", "sentiment": -2.4},
            {"time": "2025-11-07T10:55:00Z", "sentiment": 5.6},
            {"time": "2025-11-07T10:56:20Z", "sentiment": 3.2},
            {"time": "2025-11-07T10:58:10Z", "sentiment": 7.7},
            {"time": "2025-11-07T10:59:50Z", "sentiment": -1.9}
        ]
        """);

        jsonList.add("""
        [
            {"time": "2025-11-07T11:00:05Z", "sentiment": 4.8},
            {"time": "2025-11-07T11:01:35Z", "sentiment": 1.2},
            {"time": "2025-11-07T11:03:00Z", "sentiment": -3.5},
            {"time": "2025-11-07T11:04:22Z", "sentiment": 6.3},
            {"time": "2025-11-07T11:06:10Z", "sentiment": 5.7},
            {"time": "2025-11-07T11:08:00Z", "sentiment": -0.4},
            {"time": "2025-11-07T11:10:15Z", "sentiment": 3.9},
            {"time": "2025-11-07T11:12:20Z", "sentiment": 6.6},
            {"time": "2025-11-07T11:13:55Z", "sentiment": -1.2},
            {"time": "2025-11-07T11:15:00Z", "sentiment": 2.4}
        ]
        """);

        jsonList.add("""
        [
            {"time": "2025-11-07T11:16:10Z", "sentiment": 5.1},
            {"time": "2025-11-07T11:17:40Z", "sentiment": -2.1},
            {"time": "2025-11-07T11:19:05Z", "sentiment": 4.4},
            {"time": "2025-11-07T11:20:30Z", "sentiment": 7.8},
            {"time": "2025-11-07T11:22:10Z", "sentiment": 1.9},
            {"time": "2025-11-07T11:23:35Z", "sentiment": -3.9},
            {"time": "2025-11-07T11:25:00Z", "sentiment": 6.1},
            {"time": "2025-11-07T11:26:45Z", "sentiment": 2.5},
            {"time": "2025-11-07T11:28:00Z", "sentiment": 8.0},
            {"time": "2025-11-07T11:29:50Z", "sentiment": -0.6}
        ]
        """);
    }

    public String fetchNextData() {
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
