package com.swe.ux.service;

import com.swe.ux.model.analytics.ScreenVideoTelemetryModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight data source for screen/video telemetry.
 * Mirrors the dummy pattern used by SentimentDataService/ShapeDataService.
 */
public class TelemetryDataService {
    private static final List<String> jsonList = new ArrayList<>();
    private int currentIndex = 0;

    static {
        jsonList.add("""
        {
          "startTime": 1732279200000,
          "endTime": 1732279260000,
          "withCamera": true,
          "withScreen": true,
          "fpsEvery3Seconds": [30.0, 29.2, 28.5, 30.8, 29.6, 31.0, 30.1]
        }
        """);

        jsonList.add("""
        {
          "startTime": 1732279260000,
          "endTime": 1732279320000,
          "withCamera": true,
          "withScreen": false,
          "fpsEvery3Seconds": [27.8, 28.9, 29.1, 30.5, 30.9, 29.7, 28.8]
        }
        """);

        jsonList.add("""
        {
          "startTime": 1732279320000,
          "endTime": 1732279380000,
          "withCamera": false,
          "withScreen": true,
          "fpsEvery3Seconds": [24.5, 25.0, 26.2, 27.1, 26.9, 27.5, 28.0]
        }
        """);

        jsonList.add("""
        {
          "startTime": 1732279380000,
          "endTime": 1732279440000,
          "withCamera": true,
          "withScreen": true,
          "fpsEvery3Seconds": [31.2, 30.8, 30.5, 31.0, 30.7, 31.5, 30.9]
        }
        """);
    }

    public String fetchNextData() {
        String response = jsonList.get(currentIndex);
        currentIndex = (currentIndex + 1) % jsonList.size();
        return response;
    }

    public ScreenVideoTelemetryModel parseJson(String json) {
        if (json == null || json.isEmpty()) {
            return new ScreenVideoTelemetryModel(0L, 0L, new ArrayList<>(), false, false);
        }

        long start = extractLong(json, "startTime", 0L);
        long end = extractLong(json, "endTime", start + 60000L);
        boolean withCamera = extractBoolean(json, "withCamera");
        boolean withScreen = extractBoolean(json, "withScreen");

        ArrayList<Double> fpsList = extractFpsList(json);
        return new ScreenVideoTelemetryModel(start, end, fpsList, withCamera, withScreen);
    }

    private long extractLong(String json, String key, long defaultValue) {
        String searchKey = "\"" + key + "\":";
        int idx = json.indexOf(searchKey);
        if (idx == -1) {
            return defaultValue;
        }
        idx += searchKey.length();
        int endIdx = json.indexOf(",", idx);
        if (endIdx == -1) {
            endIdx = json.indexOf("}", idx);
        }
        if (endIdx == -1) {
            return defaultValue;
        }
        try {
            return Long.parseLong(json.substring(idx, endIdx).trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean extractBoolean(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int idx = json.indexOf(searchKey);
        if (idx == -1) {
            return false;
        }
        idx += searchKey.length();
        int endIdx = json.indexOf(",", idx);
        if (endIdx == -1) {
            endIdx = json.indexOf("}", idx);
        }
        if (endIdx == -1) {
            endIdx = json.length();
        }
        return json.substring(idx, endIdx).toLowerCase().contains("true");
    }

    private ArrayList<Double> extractFpsList(String json) {
        ArrayList<Double> values = new ArrayList<>();
        String key = "\"fpsEvery3Seconds\":";
        int idx = json.indexOf(key);
        if (idx == -1) {
            return values;
        }

        int startBracket = json.indexOf("[", idx);
        int endBracket = json.indexOf("]", startBracket);
        if (startBracket == -1 || endBracket == -1 || endBracket <= startBracket) {
            return values;
        }

        String arrayContent = json.substring(startBracket + 1, endBracket);
        String[] parts = arrayContent.split(",");
        for (String part : parts) {
            try {
                values.add(Double.parseDouble(part.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return values;
    }
}
