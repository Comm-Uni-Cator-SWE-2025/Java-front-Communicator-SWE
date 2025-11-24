package com.swe.ux.service;

import java.util.ArrayList;
import java.util.List;

import com.swe.ux.model.analytics.ScreenVideoTelemetryModel;

/**
 * Lightweight data source for screen/video telemetry.
 * Mirrors the dummy pattern used by SentimentDataService/ShapeDataService.
 */
public class TelemetryDataService {
    /**
     * Number of milliseconds in one minute.
     */
    private static final long MILLISECONDS_IN_MINUTE = 60000L;

    /**
     * List of JSON telemetry strings.
     */
    private static final List<String> JSON_LIST = new ArrayList<>();
    /**
     * Current index for cycling through telemetry data.
     */
    private int currentIndex = 0;

    static {
        JSON_LIST.add("""
            {
              "startTime": 1732279200000,
              "endTime": 1732279260000,
              "withCamera": true,
              "withScreen": true,
              "fpsEvery3Seconds": [30.0, 29.2, 28.5, 30.8, 29.6, 31.0, 30.1]
            }
            """);

        JSON_LIST.add("""
            {
              "startTime": 1732279260000,
              "endTime": 1732279320000,
              "withCamera": true,
              "withScreen": false,
              "fpsEvery3Seconds": [27.8, 28.9, 29.1, 30.5, 30.9, 29.7, 28.8]
            }
            """);

        JSON_LIST.add("""
            {
              "startTime": 1732279320000,
              "endTime": 1732279380000,
              "withCamera": false,
              "withScreen": true,
              "fpsEvery3Seconds": [24.5, 25.0, 26.2, 27.1, 26.9, 27.5, 28.0]
            }
            """);

        JSON_LIST.add("""
            {
              "startTime": 1732279380000,
              "endTime": 1732279440000,
              "withCamera": true,
              "withScreen": true,
              "fpsEvery3Seconds": [31.2, 30.8, 30.5, 31.0, 30.7, 31.5, 30.9]
            }
            """);
    }

    /**
     * Fetches the next telemetry data from the static list.
     *
     * @return the telemetry data JSON string
     */
    public String fetchNextData() {
        final String response = JSON_LIST.get(currentIndex);
        currentIndex = (currentIndex + 1) % JSON_LIST.size();
        return response;
    }

    /**
     * Parses JSON string to extract telemetry model.
     *
     * @param json the JSON string to parse
     * @return ScreenVideoTelemetryModel with parsed data
     */
    public ScreenVideoTelemetryModel parseJson(final String json) {
        if (json == null || json.isEmpty()) {
            return new ScreenVideoTelemetryModel(0L, 0L, new ArrayList<>(), false, false);
        }

        final long start = extractLong(json, "startTime", 0L);
        final long end = extractLong(json, "endTime", start + MILLISECONDS_IN_MINUTE);
        final boolean withCamera = extractBoolean(json, "withCamera");
        final boolean withScreen = extractBoolean(json, "withScreen");

        final ArrayList<Double> fpsList = extractFpsList(json);
        return new ScreenVideoTelemetryModel(start, end, fpsList, withCamera, withScreen);
    }

    private long extractLong(final String json, final String key, final long defaultValue) {
        final String searchKey = "\"" + key + "\":";
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

    private boolean extractBoolean(final String json, final String key) {
        final String searchKey = "\"" + key + "\":";
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

    private ArrayList<Double> extractFpsList(final String json) {
        final ArrayList<Double> values = new ArrayList<>();
        final String key = "\"fpsEvery3Seconds\":";
        final int idx = json.indexOf(key);
        if (idx == -1) {
            return values;
        }

        final int startBracket = json.indexOf("[", idx);
        final int endBracket = json.indexOf("]", startBracket);
        if (startBracket == -1 || endBracket == -1 || endBracket <= startBracket) {
            return values;
        }

        final String arrayContent = json.substring(startBracket + 1, endBracket);
        final String[] parts = arrayContent.split(",");
        for (final String part : parts) {
            try {
                values.add(Double.parseDouble(part.trim()));
            } catch (NumberFormatException ignored) {
                // Ignore invalid numbers
            }
        }
        return values;
    }
}
