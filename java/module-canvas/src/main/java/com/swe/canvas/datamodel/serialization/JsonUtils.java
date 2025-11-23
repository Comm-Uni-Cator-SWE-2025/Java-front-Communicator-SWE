package com.swe.canvas.datamodel.serialization;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.swe.canvas.datamodel.shape.Point;

/**
 * A utility class containing low-level static helper methods for manually
 * parsing and constructing JSON strings, without any external libraries.
 * These methods are used by the ManualJsonConverter.
 */
public final class JsonUtils {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private JsonUtils() {
    }

    // =========================================================================
    // Construction Helpers
    // =========================================================================

    /**
     * Converts a java.awt.Color to an ARGB hex string (e.g., "#FF000000").
     */
    public static String colorToHex(final Color color) {
        if (color == null) {
            return "#00000000";
        }
        // Color.getRGB() returns int in 0xAARRGGBB format
        return String.format("#%08X", color.getRGB());
    }

    /**
     * Helper to wrap a string value in quotes and escape internal quotes (if any).
     * @param value The value to wrap.
     * @return The quoted, escaped string.
     */
    public static String jsonEscape(final String value) {
        if (value == null) {
            return "null";
        }
        return "\"" + value.replace("\"", "\\\"") + "\"";
    }

    // =========================================================================
    // Parsing (Extraction) Helpers
    // =========================================================================

    /**
     * Converts an ARGB hex string to a java.awt.Color.
     */
    public static Color hexToColor(final String hexString) {
        if (hexString == null || hexString.length() != 9 || !hexString.startsWith("#")) {
            return Color.BLACK;
        }
        try {
            // Long.parseLong handles the parsing, using 'true' for alpha
            return new Color((int) Long.parseLong(hexString.substring(1), 16), true);
        } catch (NumberFormatException e) {
            return Color.BLACK;
        }
    }

    /**
     * Extracts a string value associated with a key from the content.
     */
    public static String extractString(final String content, final String key) {
        // Pattern: "key":"value" or "key":value (if value is 'null')
        final Pattern pattern = Pattern.compile(Pattern.quote("\"" + key + "\"") + ":\"(.*?)\"");
        final Matcher matcher = pattern.matcher(content);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * Extracts a double value associated with a key from the content.
     */
    public static double extractDouble(final String content, final String key) {
        // Pattern: "key":number (Handles numbers like 2.0, 15, or 0.5)
        final Pattern pattern = Pattern.compile(Pattern.quote("\"" + key + "\"") + ":([\\-0-9\\.]+)");
        final Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            try {
                // Group 1 contains the numeric string (e.g., "2.0")
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                System.err.println("Error parsing double for key " + key + ": " + matcher.group(1));
                return 0.0;
            }
        }
        return 0.0;
    }

    /**
     * Extracts a long value associated with a key from the content.
     */
    public static long extractLong(final String content, final String key) {
        // Pattern: "key":number
        final Pattern pattern = Pattern.compile(Pattern.quote("\"" + key + "\"") + ":(\\d+)");
        final Matcher matcher = pattern.matcher(content);
        return matcher.find() ? Long.parseLong(matcher.group(1)) : 0L;
    }

    /**
     * Extracts a boolean value associated with a key from the content.
     */
    public static boolean extractBoolean(final String content, final String key) {
        // Pattern: "key":true|false
        final Pattern pattern = Pattern.compile(Pattern.quote("\"" + key + "\"") + ":(true|false)");
        final Matcher matcher = pattern.matcher(content);
        return matcher.find() && Boolean.parseBoolean(matcher.group(1));
    }

    /**
     * Extracts the Points array content.
     */
    public static List<Point> extractPoints(final String content) {
        final List<Point> points = new ArrayList<>();
        // Find content between "Points":[ and ]
        final Pattern arrayPattern = Pattern.compile(Pattern.quote("\"Points\":") + "\\[(.*?)\\]");
        final Matcher arrayMatcher = arrayPattern.matcher(content);

        if (arrayMatcher.find()) {
            final String pointsArrayContent = arrayMatcher.group(1);
            // Pattern for individual points: {"X":123.0,"Y":456.0}
            final Pattern pointPattern = Pattern.compile("\\{\"X\":([\\-0-9\\.]+),\"Y\":([\\-0-9\\.]+)\\}");
            final Matcher pointMatcher = pointPattern.matcher(pointsArrayContent);

            while (pointMatcher.find()) {
                final double x = Double.parseDouble(pointMatcher.group(1));
                final double y = Double.parseDouble(pointMatcher.group(2));
                points.add(new Point(x, y));
            }
        }
        return points;
    }

    /**
     * Extracts a nested JSON object string from the content.
     * Searches for "key":{...} or "key":null.
     * This method is complex as it has to handle JSON nesting manually (curly brace counting).
     */
    public static String extractNestedJson(final String content, final String key) {
        final String searchKey = "\"" + key + "\":";
        int startIndex = content.indexOf(searchKey);

        if (startIndex == -1) {
            return null;
        }

        startIndex += searchKey.length();

        // Check for 'null' value
        if (content.regionMatches(startIndex, "null", 0, 4)) {
            return "null";
        }

        // Must be an object, find start '{'
        int objectStart = content.indexOf('{', startIndex);
        if (objectStart == -1) {
            return null;
        }

        int balance = 0;
        int objectEnd = -1;

        for (int i = objectStart; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') {
                balance++;
            } else if (c == '}') {
                balance--;
                if (balance == 0) {
                    objectEnd = i;
                    break;
                }
            }
        }

        if (objectEnd != -1) {
            // Return the object content including the outermost braces
            return content.substring(objectStart, objectEnd + 1);
        }

        return null;
    }
}
