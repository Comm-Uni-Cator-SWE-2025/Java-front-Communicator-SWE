/**
 *  Contributed by Ram Charan.
 */

package com.swe.ux.service;

import com.swe.ux.model.analytics.ShapeCount;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for fetching and parsing shape count data.
 */
public class ShapeDataService {
    /**
     * List of JSON shape data strings.
     */
    private static final List<String> JSON_LIST = new ArrayList<>();
    /**
     * Current index for cycling through shape data.
     */
    private int currentIndex = 0;

    static {
        JSON_LIST.add("""
            {
              "freeHand": 12,
              "straightLine": 5,
              "rectangle": 3,
              "ellipse": 2,
              "triangle": 4
            }
            """);

        JSON_LIST.add("""
            {
              "freeHand": 15,
              "straightLine": 8,
              "rectangle": 6,
              "ellipse": 4,
              "triangle": 7
            }
            """);

        JSON_LIST.add("""
            {
              "freeHand": 10,
              "straightLine": 12,
              "rectangle": 5,
              "ellipse": 3,
              "triangle": 6
            }
            """);

        JSON_LIST.add("""
            {
              "freeHand": 18,
              "straightLine": 6,
              "rectangle": 9,
              "ellipse": 5,
              "triangle": 8
            }
            """);

        JSON_LIST.add("""
            {
              "freeHand": 14,
              "straightLine": 10,
              "rectangle": 7,
              "ellipse": 6,
              "triangle": 5
            }
            """);

        JSON_LIST.add("""
            {
              "freeHand": 20,
              "straightLine": 9,
              "rectangle": 8,
              "ellipse": 7,
              "triangle": 10
            }
            """);
    }

    /**
     * Fetches the next shape data from the static list.
     *
     * @return the shape data JSON string
     */
    public String fetchNextData() {
        final String response = JSON_LIST.get(currentIndex);
        currentIndex = (currentIndex + 1) % JSON_LIST.size();
        return response;
    }

    /**
     * Parses JSON string to extract shape counts.
     *
     * @param json the JSON string to parse
     * @return ShapeCount object with parsed data
     */
    public ShapeCount parseJson(final String json) {
        try {
            final int freeHand = extractValue(json, "freeHand");
            final int straightLine = extractValue(json, "straightLine");
            final int rectangle = extractValue(json, "rectangle");
            final int ellipse = extractValue(json, "ellipse");
            final int triangle = extractValue(json, "triangle");

            return new ShapeCount(freeHand, straightLine, rectangle, ellipse, triangle);
        } catch (Exception e) {
            System.out.println("Error parsing shape data: " + e.getMessage());
            return new ShapeCount(0, 0, 0, 0, 0);
        }
    }

    private int extractValue(final String json, final String key) {
        final String searchKey = "\"" + key + "\":";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) {
            return 0;
        }
        
        startIndex += searchKey.length();
        int endIndex = json.indexOf(",", startIndex);
        if (endIndex == -1) {
            endIndex = json.indexOf("}", startIndex);
        }
        
        final String valueStr = json.substring(startIndex, endIndex).trim();
        return Integer.parseInt(valueStr);
    }
}
