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
     * @param hexString The hex string to convert.
     * @return The Color object.
     */
    public static Color hexToColor(final String hexString) {
        final int expectedLength = 9;
        if (hexString == null || hexString.length() != expectedLength || !hexString.startsWith("#")) {
            return Color.BLACK;
        }
        try {
            final int sixteen = 16;
            // Long.parseLong handles the parsing, using 'true' for alpha
            return new Color((int) Long.parseLong(hexString.substring(1), sixteen), true);
        } catch (final NumberFormatException e) {
            return Color.BLACK;
        }
    }

    /**
     * Extracts a string value associated with a key from the content.
     * @param content The JSON content.
     * @param key The key to extract.
     * @return The extracted string value.
     */
    public static String extractString(final String content, final String key) {
        // Pattern: "key":"value" or "key":value (if value is 'null')
        final Pattern pattern = Pattern.compile(Pattern.quote("\"" + key + "\"") + ":\"(.*?)\"");
        final Matcher matcher = pattern.matcher(content);
        final int one = 1;
        return matcher.find() ? matcher.group(one) : null;
    }

    /**
     * Extracts a double value associated with a key from the content.
     * @param content The JSON content.
     * @param key The key to extract.
     * @return The extracted double value.
     */
    public static double extractDouble(final String content, final String key) {
        // Pattern: "key":number (Handles numbers like 2.0, 15, or 0.5)
        final Pattern pattern = Pattern.compile(Pattern.quote("\"" + key + "\"") + ":([\\-0-9\\.]+)");
        final Matcher matcher = pattern.matcher(content);
        final double defaultValue = 0.0;
        
        if (matcher.find()) {
            try {
                final int one = 1;
                // Group 1 contains the numeric string (e.g., "2.0")
                return Double.parseDouble(matcher.group(one));
            } catch (final NumberFormatException e) {
                final int one = 1;
                System.err.println("Error parsing double for key " + key + ": " + matcher.group(one));
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Extracts a long value associated with a key from the content.
     * @param content The JSON content.
     * @param key The key to extract.
     * @return The extracted long value.
     */
    public static long extractLong(final String content, final String key) {
        // Pattern: "key":number
        final Pattern pattern = Pattern.compile(Pattern.quote("\"" + key + "\"") + ":(\\d+)");
        final Matcher matcher = pattern.matcher(content);
        final int one = 1;
        final long zeroL = 0L;
        return matcher.find() ? Long.parseLong(matcher.group(one)) : zeroL;
    }

    /**
     * Extracts a boolean value associated with a key from the content.
     * @param content The JSON content.
     * @param key The key to extract.
     * @return The extracted boolean value.
     */
    public static boolean extractBoolean(final String content, final String key) {
        // Pattern: "key":true|false
        final Pattern pattern = Pattern.compile(Pattern.quote("\"" + key + "\"") + ":(true|false)");
        final Matcher matcher = pattern.matcher(content);
        final int one = 1;
        return matcher.find() && Boolean.parseBoolean(matcher.group(one));
    }

    /**
     * Extracts the Points array content.
     * @param content The JSON content.
     * @return The list of extracted points.
     */
    public static List<Point> extractPoints(final String content) {
        final List<Point> points = new ArrayList<>();
        // Find content between "Points":[ and ]
        final Pattern arrayPattern = Pattern.compile(Pattern.quote("\"Points\":") + "\\[(.*?)\\]");
        final Matcher arrayMatcher = arrayPattern.matcher(content);

        if (arrayMatcher.find()) {
            final int one = 1;
            final int two = 2;
            final String pointsArrayContent = arrayMatcher.group(one);
            // Pattern for individual points: {"X":123.0,"Y":456.0}
            final Pattern pointPattern = Pattern.compile("\\{\"X\":([\\-0-9\\.]+),\"Y\":([\\-0-9\\.]+)\\}");
            final Matcher pointMatcher = pointPattern.matcher(pointsArrayContent);

            while (pointMatcher.find()) {
                final double x = Double.parseDouble(pointMatcher.group(one));
                final double y = Double.parseDouble(pointMatcher.group(two));
                points.add(new Point(x, y));
            }
        }
        return points;
    }

    /**
     * Extracts a nested JSON object string from the content.
     * Searches for "key":{...} or "key":null.
     * This method is complex as it has to handle JSON nesting manually (curly brace counting).
     * @param content The JSON content.
     * @param key The key to extract.
     * @return The extracted nested JSON string.
     */
    public static String extractNestedJson(final String content, final String key) {
        final String searchKey = "\"" + key + "\":";
        int startIndex = content.indexOf(searchKey);
        final int minusOne = -1;
        final int zero = 0;
        final int four = 4;
        final int one = 1;

        if (startIndex == minusOne) {
            return null;
        }

        startIndex += searchKey.length();

        // Check for 'null' value
        if (content.regionMatches(startIndex, "null", zero, four)) {
            return "null";
        }

        // Must be an object, find start '{'
        int objectStart = content.indexOf('{', startIndex);
        if (objectStart == minusOne) {
            return null;
        }

        int balance = zero;
        int objectEnd = minusOne;

        for (int i = objectStart; i < content.length(); i++) {
            final char c = content.charAt(i);
            if (c == '{') {
                balance++;
            } else if (c == '}') {
                balance--;
                if (balance == zero) {
                    objectEnd = i;
                    break;
                }
            }
        }

        if (objectEnd != minusOne) {
            // Return the object content including the outermost braces
            return content.substring(objectStart, objectEnd + one);
        }

        return null;
    }
}
